package com.heaven7.java.cs.communication;

import com.heaven7.java.base.util.DefaultPrinter;
import com.heaven7.java.base.util.Disposable;
import com.heaven7.java.base.util.ThreadProxy;
import com.heaven7.java.cs.communication.entity.BaseEntity;
import com.heaven7.java.meshy.Meshy;
import com.heaven7.java.meshy.Message;
import com.heaven7.java.pc.schedulers.Schedulers;
import okio.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.heaven7.java.cs.communication.CSConstant.*;

/**
 * the server communicator which can use blocking or non-blocking mode. by calling {@linkplain #setBlockingMode(boolean)}.
 * that means for socket you should use blocking mode.
 * @author heaven7
 */
public class ServerCommunicator implements Disposable {

    public interface Callback {
        /**
         * validate the temp token if is valid return true.
         * @param token the token
         * @return true if is permit
         */
        boolean validateTempToken(String token);

        /**
         * generate token for target user
         * @param tmpToken the temp token
         * @param userKey the user key
         * @return the generate token. can't be null
         */
        String generateToken(String tmpToken, String userKey);

        /**
         * called on the client tick time is time out.
         * @param token the token of the client.
         */
        void onTickTimeTimeout(String token);

        /**
         * accept token if the token is accept from target permit ids.
         * @param token the token of client
         * @param ids the ids from {@linkplain ServerCommunicator#sendBroadcast(Message, List)}
         * @return true if accept.
         */
        boolean acceptToken(String token, List<String> ids);
    }

    public interface Connector {
        void connect(ThreadProxy proxy, Reporter reporter) throws IOException;
        void disconnect() throws IOException;
    }

    public interface Reporter {
        void reportError(IOException e);
        void reportNewConnection(ClientConnection cc);
    }

    public interface ClientConnection {
        Sink getSink() throws IOException;
        Source getSource() throws IOException;

        String getRemoteUniqueKey();
        boolean isAlive();
        void close() throws IOException;
        boolean isReadyToRead();
    }

    private static final String TAG = "ServerCommunicator";
    private static final CSThreadFactory sFACTORY = new CSThreadFactory(TAG);

    private final Map<String, ClientInfo> mClientInfoMap = new ConcurrentHashMap<>();

    private final AtomicInteger mConnectCount = new AtomicInteger(0);
    private final Reporter0 mReporter = new Reporter0();
    private final ThreadProxy mProxy;
    private final Looper mLooper = new Looper();

    private final long mMaxTickTimeSpace;
    private final Meshy mMeshy;
    private final Connector mConnector;
    private final MessageHandler nHandler;
    private final Callback mInternalCallback;

    private ServerMonitor mMonitor;
    private boolean mBlockingMode = true;

    /**
     * create server communicator
     * @param mMeshy the meshy
     * @param tickTimeSpace the max tick time space in mills
     * @param mInternalCallback the internal callback of server
     * @param connector the connector
     * @param handler the message handler
     */
    public ServerCommunicator(Meshy mMeshy,long tickTimeSpace, Callback mInternalCallback, Connector connector, MessageHandler handler) {
        this.mMeshy = mMeshy;
        this.nHandler = handler;
        this.mProxy = ThreadProxy.create(sFACTORY);
        this.mInternalCallback = mInternalCallback;
        this.mConnector = connector;
        this.mMaxTickTimeSpace = tickTimeSpace;
    }

    /**
     * indicate the work mode is blocking or not.
     * @return true if it is blocking mode
     */
    public boolean isBlockingMode() {
        return mBlockingMode;
    }

    /**
     * set is blocking mode or not
     * @param blockingMode true if is blocking mode
     */
    public void setBlockingMode(boolean blockingMode) {
        this.mBlockingMode = blockingMode;
    }

    public ServerMonitor getServerMonitor() {
        return mMonitor;
    }
    public void setServerMonitor(ServerMonitor mMonitor) {
        this.mMonitor = mMonitor;
    }

    /**
     * start connect
     * @throws IOException if connect occurs.
     */
    public void start() throws IOException {
        mLooper.start();
        mConnector.connect(mProxy, mReporter);
        mMonitor.onStart();
    }

    /**
     * send broadcast message to clients
     * @param msg the message
     * @return true if send success. false otherwise
     */
    public boolean sendBroadcast(Message<Object> msg){
        if(mLooper.isOpen()){
            mLooper.sendBroadcast(msg);
            mMonitor.onSendBroadcast(msg, null);
            return true;
        }else {
            mMonitor.onSendBroadcastFailed(msg);
            return false;
        }
    }

    /**
     * send broadcast message to the target clients
     * @param msg the message
     * @param clients the client id list
     * @return true if send success. false otherwise
     */
    public boolean sendBroadcast(Message<Object> msg, List<String> clients){
        if(mLooper.isOpen()){
            mLooper.sendBroadcast(msg, clients);
            mMonitor.onSendBroadcast(msg, clients);
            return true;
        }else {
            mMonitor.onSendBroadcastFailed(msg);
            return false;
        }
    }

    /**
     * get the connected count of clients
     * @return the connected count
     */
    public int getConnectedCount(){
        return mConnectCount.get();
    }

    @Override
    public void dispose() {
        mLooper.dispose();
        try {
            mConnector.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mProxy.dispose();
        mConnectCount.getAndSet(0);
        //TODO server disconnect by exception. handle it ?
        mClientInfoMap.clear();
        mMonitor.onEnd();
    }

    private class Reporter0 implements ServerCommunicator.Reporter {
        @Override
        public void reportError(IOException e) {
            e.printStackTrace();
        }

        @Override
        public void reportNewConnection(ClientConnection cc) {
            String remoteId = cc.getRemoteUniqueKey();
            mClientInfoMap.put(remoteId, new ClientInfo());
            mConnectCount.incrementAndGet();
            try {
                mLooper.addClientConnection(cc);
                mMonitor.onNewClient(remoteId);
            } catch (IOException e) {
                mClientInfoMap.remove(remoteId);
                mConnectCount.decrementAndGet();
                mMonitor.onAddClientError(remoteId, e);
                e.printStackTrace();
            }
        }
    }

    public static class ClientInfo {
        String token;
        float version;
        /** last tick time */
        long lastTickTime;
    }

    private class Looper implements Disposable,Runnable {
        private final CopyOnWriteArrayList<ClientConnectionWrapper> connections =
                new CopyOnWriteArrayList<>();
        private final AtomicBoolean mClosed = new AtomicBoolean(true);
        private final ThreadProxy mProxy_looper = ThreadProxy.create(sFACTORY) ;

        public Looper() {
        }
        public boolean isOpen(){
            return !mClosed.get();
        }

        public void start() {
            if(mClosed.compareAndSet(true, false)){
                if(!mBlockingMode){
                    mProxy_looper.start(this);
                }
            }
        }

        @Override
        public void run() {
            List<ClientConnectionWrapper> list = new ArrayList<>();
            //Scheduler.Worker worker = Schedulers.io().newWorker();
            while (!mClosed.get()) {
                list.addAll(connections);
                //handle every connection in different threads. (because socket is blocking)
                for (final ClientConnectionWrapper conn : list) {
                    handleClientConnection(conn);
                }
                list.clear();
            }
        }
        /**
         *  handle client connection. if return true means the client connection will be closed.
         * @param conn the connection wrapper
         * @return true if the connection will be closed.
         */
        boolean handleClientConnection(ClientConnectionWrapper conn){
            if(!conn.isReadyToRead()){
                return false;
            }
            String uniqueKey = conn.getRemoteUniqueKey();
            ClientInfo clientInfo = mClientInfoMap.get(uniqueKey);
            boolean removeClient = false;
            if (conn.isAlive()) {
                mMonitor.onStartReadMessage(uniqueKey, clientInfo.token);
                Message<?> msg = null;
                try{
                    msg = conn.readMessage();
                }catch (Exception e){
                    if(!mClosed.get()){
                        mMonitor.onReadException(uniqueKey, clientInfo.token,  e);
                        if(conn.increaseReadErrorCount() >= 3){
                            removeClient = true;
                        }
                    }
                }
                if (msg != null) {
                    if (verifyMessage(conn, clientInfo, msg))
                        return false;
                    removeClient = handleMessage(conn, uniqueKey, clientInfo, msg);
                } else {
                    if (!removeClient && (System.currentTimeMillis() - clientInfo.lastTickTime)
                            >= mMaxTickTimeSpace) {
                        removeClient = true;
                        mInternalCallback.onTickTimeTimeout(clientInfo.token);
                    }
                }
            } else {
                removeClient = true;
            }
            if (removeClient) {
                mMonitor.onRemoveClient(uniqueKey, clientInfo.token);
                connections.remove(conn);
                mClientInfoMap.remove(uniqueKey);
                //give a chance that client can do some work of clean.
                Schedulers.io().newWorker().scheduleDelay(new Runnable() {
                    @Override
                    public void run() {
                        mConnectCount.decrementAndGet();
                        conn.close();
                    }
                }, 30, TimeUnit.SECONDS);
                return true;
            }
            return false;
        }

        // handle message and return true if need remove client.
        boolean handleMessage(
                ClientConnectionWrapper conn,
                String uniqueKey,
                ClientInfo clientInfo,
                Message<?> msg) {
            boolean removeClient = false;
            BaseEntity entity = (BaseEntity) msg.getEntity();

            Message<Object> outMessage = null;
            // handle message
            switch (msg.getType()) {
                case Message.LOGIN:
                    if (mInternalCallback.validateTempToken(entity.getToken())) {
                        clientInfo.version = entity.getVersion();
                        clientInfo.token =
                                mInternalCallback.generateToken(entity.getToken(), uniqueKey);
                        clientInfo.lastTickTime = System.currentTimeMillis();
                        conn.permit = true;
                        //monitor
                        mMonitor.onValidateTempTokenSuccess(entity.getToken(), clientInfo.token);
                        // gen token and response.
                        entity.setToken(clientInfo.token);
                        entity.setVersion(mMeshy.getVersion());
                        outMessage =
                                Message.create(msg.getType(), Message.SUCCESS, SUCCESS, entity);
                    } else {
                        outMessage =
                                Message.create(msg.getType(), Message.FAILED, INVALID_TOKEN, null);
                        removeClient = true;
                        mMonitor.onValidateTempTokenFailed(entity.getToken());
                    }
                    break;

                case Message.TICK:
                    {
                        clientInfo.lastTickTime = System.currentTimeMillis();
                        outMessage = Message.create(msg.getType(), Message.SUCCESS, SUCCESS, null);
                        mMonitor.onTick(entity.getToken());
                    }
                    break;

                case Message.LOGOUT:
                    mMonitor.onLogout(entity.getToken());
                    outMessage = Message.create(msg.getType(), Message.SUCCESS, SUCCESS, null);
                    removeClient = true;
                    break;

                default:
                    {
                        mMonitor.onMessageReceived(msg);
                        clientInfo.lastTickTime = System.currentTimeMillis();
                    }
            }
            if (outMessage != null) {
                Schedulers.io()
                        .newWorker()
                        .schedule(new SendRunner(conn, outMessage, clientInfo.version));
            } else {
                // message not handled
                Schedulers.io()
                        .newWorker()
                        .schedule(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        nHandler.handleMessage(conn, msg, clientInfo.version);
                                    }
                                });
            }
            return removeClient;
        }

        // return true if verify message failed.
        boolean verifyMessage(ClientConnectionWrapper conn, ClientInfo clientInfo, Message<?> msg) {
            BaseEntity entity = (BaseEntity) msg.getEntity();
            Message<Object> outMessage = null;
            // non-login need validate token
            if (msg.getType() != Message.LOGIN) {
                if (!conn.permit) {
                    outMessage =
                            Message.create(
                                    msg.getType(), Message.FAILED, YOU_SHOULD_LOGIN_FIRST, null);
                    mMonitor.onNotLogin(conn.getRemoteUniqueKey());
                } else if (!clientInfo.token.equals(entity.getToken())) {
                    outMessage = Message.create(msg.getType(), Message.FAILED, INVALID_TOKEN, null);
                    mMonitor.onVerifyTokenFailed(clientInfo.token, entity.getToken());
                }
            }
            if (outMessage != null) {
                Schedulers.io()
                        .newWorker()
                        .schedule(new SendRunner(conn, outMessage, clientInfo.version));
            }
            return outMessage != null;
        }
        @Override
        public void dispose() {
            if (mClosed.compareAndSet(false, true)) {
                for (ClientConnectionWrapper  conn: connections){
                    conn.close();
                }
                connections.clear();
                mProxy_looper.dispose();
            }
        }

        public void addClientConnection(ClientConnection cc) throws IOException {
            if(cc.isAlive()){
                final ClientConnectionWrapper wrapper = new ClientConnectionWrapper(mMeshy, cc);
                connections.add(wrapper);

                //for blocking mode . we need add task to pool. or else the extra thread will loop the 'wrapper'.
                if(mBlockingMode){
                    final String remoteId = wrapper.getRemoteUniqueKey();
                    mMonitor.onBlockingStart(remoteId);

                    Schedulers.io().newWorker().schedule(new Runnable() {
                        @Override
                        public void run() {
                            boolean end = true;
                            if (!mClosed.get() && !handleClientConnection(wrapper)){
                                end = false;
                            }
                            if(end){
                                ClientInfo info = mClientInfoMap.get(remoteId);
                                mMonitor.onBlockingEnd(remoteId, info != null ? info.token: null);
                            }else {
                                Schedulers.io().newWorker().schedule(this);
                            }
                            //below can't run ok
                          /*  while (!mClosed.get()){
                                if(handleClientConnection(wrapper)){
                                    break;
                                }
                            }
                            System.out.println("connection >>> end...");*/
                        }
                    });
                }
            }else {
                DefaultPrinter.getDefault().error(TAG, "addClientConnection",
                        "client connection is not alive. key = " + cc.getRemoteUniqueKey());
            }
        }

        public void sendBroadcast(Message<Object> msg) {
            final List<ClientConnectionWrapper> clients = new ArrayList<>(connections);
            final HashMap<String, ClientInfo> map = new HashMap<>(mClientInfoMap);
            Schedulers.io().newWorker().schedule(new Runnable() {
                @Override
                public void run() {
                    for (ClientConnectionWrapper conn : clients){
                        ClientInfo info = map.get(conn.getRemoteUniqueKey());
                        if(info != null){
                            conn.sendMessage(msg, info.version);
                        }
                    }
                }
            });
        }

        public void sendBroadcast(Message<Object> msg, final List<String> ids) {
            final List<ClientConnectionWrapper> clients = new ArrayList<>(connections);
            final HashMap<String, ClientInfo> map = new HashMap<>(mClientInfoMap);
            Schedulers.io().newWorker().schedule(new Runnable() {
                @Override
                public void run() {
                    for (ClientConnectionWrapper conn : clients){
                        ClientInfo info = map.get(conn.getRemoteUniqueKey());
                        if(info != null && mInternalCallback.acceptToken(info.token, ids)){
                            conn.sendMessage(msg, info.version);
                        }
                    }
                }
            });
        }
    }

    private static class ClientConnectionWrapper implements IMessageSender {
        final Meshy meshy;
        final ClientConnection connection;
        final BufferedSource source;
        final BufferedSink sink;
        int errorCount;
        boolean permit;

        public ClientConnectionWrapper(Meshy meshy, ClientConnection connection) throws IOException {
            this.meshy = meshy;
            this.connection = connection;
            this.source = Okio.buffer(connection.getSource());
            this.sink = Okio.buffer(connection.getSink());
        }

        public boolean isAlive() {
            return connection.isAlive();
        }

        public String getRemoteUniqueKey() {
            return connection.getRemoteUniqueKey();
        }

        public Message<?> readMessage() {
           /* Timeout timeout = source.timeout();
            timeout.clearDeadline();
            timeout.deadline(5000, TimeUnit.MILLISECONDS);*/
            return meshy.getReader().readMessage(source);
        }

        @Override
        public boolean sendMessage(Message<Object> msg, float version) {
            meshy.getWriter().writeMessage(sink, msg, CSConstant.TYPE_RSA_SINGLE, version);
            return true;
        }

        @Override
        public boolean sendMessage(Message<Object> msg) {
            meshy.getWriter().writeMessage(
                    sink, msg, CSConstant.TYPE_RSA_SINGLE, meshy.getVersion());
            return true;
        }

        public void close() {
            try {
                connection.close();
            } catch (IOException e) {
                // ignore
            }
        }
        public int increaseReadErrorCount() {
            return ++errorCount;
        }
        public boolean isReadyToRead() {
            return connection.isReadyToRead();
        }
    }

    private static class SendRunner implements Runnable {
        final IMessageSender sender;
        final Message<Object> message;
        final float version;

        public SendRunner(IMessageSender sender, Message<Object> message, float version) {
            this.sender = sender;
            this.message = message;
            this.version = version;
        }

        @Override
        public void run() {
            sender.sendMessage(message, version);
        }
    }
}
