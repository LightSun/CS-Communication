package com.heaven7.java.cs.communication;

import com.heaven7.java.base.util.DefaultPrinter;
import com.heaven7.java.base.util.Disposable;
import com.heaven7.java.base.util.ThreadProxy;
import com.heaven7.java.cs.communication.entity.BaseEntity;
import com.heaven7.java.message.protocol.Message;
import com.heaven7.java.message.protocol.MessageConfigManager;
import com.heaven7.java.message.protocol.OkMessage;
import com.heaven7.java.pc.schedulers.Schedulers;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.heaven7.java.cs.communication.CSConstant.INVALID_TOKEN;
import static com.heaven7.java.cs.communication.CSConstant.SUCCESS;
import static com.heaven7.java.cs.communication.CSConstant.YOU_SHOULD_LOGIN_FIRST;

/** @author heaven7 */
public final class ServerCommunicator implements Disposable {

    public interface Callback {
        void handleMessage(IMessageSender sender, Message<?> msg, float version);
    }

    public interface InternalCallback {
        boolean validateTempToken(String token);
        String generateToken(String tmpToken, String userKey);
        void onTickTimeTimeout(ClientInfo info);

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
        OutputStream getOutputStream() throws IOException;
        InputStream getInputStream() throws IOException;

        String getRemoteUniqueKey();

        boolean isAlive();
        void close() throws IOException;
    }

    private static final String TAG = "ServerCommunicator";
    private static final CSThreadFactory sFACTORY = new CSThreadFactory(TAG);
    private final ThreadProxy mProxy;
    private final AtomicInteger mConnectCount = new AtomicInteger(0);
    private final Reporter0 mReporter = new Reporter0();
    private Looper mLooper;

    private final Connector mConnector;
    private final Callback mCallback;
    private final long mMaxTickTimeSpace;
    private InternalCallback mInternalCallback;

    private final Map<String, ClientInfo> mClientInfoMap = new ConcurrentHashMap<>();

    public ServerCommunicator(long tickTimeSpace, Connector connector, Callback callback) {
        this.mCallback = callback;
        this.mProxy = ThreadProxy.create(sFACTORY);
        this.mConnector = connector;
        this.mMaxTickTimeSpace = tickTimeSpace;
    }

    /**
     * start connect
     * @throws IOException if connect occurs.
     */
    public void start() throws IOException {
        if(mLooper == null){
            mLooper = new Looper();
            mLooper.start();
        }
        mConnector.connect(mProxy, mReporter);
    }

    /**
     * send broadcast message to clients
     * @param msg the message
     * @return true if send success. false otherwise
     */
    public boolean sendBroadcast(Message<Object> msg){
        if(mLooper != null){
            mLooper.sendBroadcast(msg);
            return true;
        }
        return false;
    }

    /**
     * send broadcast message to the target clients
     * @param msg the message
     * @param clients the client id list
     * @return true if send success. false otherwise
     */
    public boolean sendBroadcast(Message<Object> msg, List<String> clients){
        if(mLooper != null){
            mLooper.sendBroadcast(msg, clients);
            return true;
        }
        return false;
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
        if(mLooper != null){
            mLooper.dispose();
            mLooper = null;
        }
        try {
            mConnector.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mProxy.dispose();
        mConnectCount.getAndSet(0);
        //TODO server disconnect by exception. handle it ?
        mClientInfoMap.clear();
    }

    private class Reporter0 implements ServerCommunicator.Reporter {
        @Override
        public void reportError(IOException e) {
            e.printStackTrace();
        }

        @Override
        public void reportNewConnection(ClientConnection cc) {
            mClientInfoMap.put(cc.getRemoteUniqueKey(), new ClientInfo());
            mConnectCount.incrementAndGet();
            try {
                mLooper.addClientConnection(cc);
            } catch (IOException e) {
                mClientInfoMap.remove(cc.getRemoteUniqueKey());
                mConnectCount.decrementAndGet();
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

    private class Looper implements Disposable, Runnable {
        private final ThreadProxy mProxy;
        private final CopyOnWriteArrayList<ClientConnectionWrapper> connections =
                new CopyOnWriteArrayList<>();
        private final AtomicBoolean mClosed = new AtomicBoolean(false);

        public Looper() {
            this.mProxy = ThreadProxy.create(sFACTORY);
        }

        public void start() {
            mProxy.start(this);
        }

        @Override
        public void run() {
            List<ClientConnectionWrapper> list = new ArrayList<>();
            while (!mClosed.get()) {
                list.addAll(connections);

                for (ClientConnectionWrapper conn : list) {
                    String uniqueKey = conn.getRemoteUniqueKey();
                    ClientInfo clientInfo = mClientInfoMap.get(uniqueKey);

                    boolean removeClient = false;
                    if (conn.isAlive()) {
                        Message<?> msg = conn.readMessage();
                        if (msg != null) {
                            if (verifyMessage(conn, clientInfo, msg)) continue;
                            removeClient = handleMessage(conn, uniqueKey, clientInfo, msg);
                        } else {
                            if ((System.currentTimeMillis() - clientInfo.lastTickTime)
                                    >= mMaxTickTimeSpace) {
                                removeClient = true;
                                mInternalCallback.onTickTimeTimeout(clientInfo);
                            }
                        }
                    } else {
                        removeClient = true;
                    }
                    if (removeClient) {
                        connections.remove(conn);
                        mClientInfoMap.remove(uniqueKey);
                        conn.close();
                        mConnectCount.decrementAndGet();
                    }
                }
                list.clear();
            }
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
                        // gen token and response.
                        entity.setToken(clientInfo.token);
                        entity.setVersion(MessageConfigManager.getVersion());
                        outMessage =
                                Message.create(msg.getType(), Message.SUCCESS, SUCCESS, entity);

                    } else {
                        outMessage =
                                Message.create(msg.getType(), Message.FAILED, INVALID_TOKEN, null);
                        removeClient = true;
                    }
                    break;

                case Message.TICK:
                    {
                        clientInfo.lastTickTime = System.currentTimeMillis();
                        outMessage = Message.create(msg.getType(), Message.SUCCESS, SUCCESS, null);
                    }
                    break;

                case Message.LOGOUT:
                    outMessage = Message.create(msg.getType(), Message.SUCCESS, SUCCESS, null);
                    removeClient = true;
                    break;

                default:
                    {
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
                                        mCallback.handleMessage(conn, msg, clientInfo.version);
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
                } else if (!clientInfo.token.equals(entity.getToken())) {
                    outMessage = Message.create(msg.getType(), Message.FAILED, INVALID_TOKEN, null);
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
                mProxy.dispose();
                connections.clear();
            }
        }
        public void addClientConnection(ClientConnection cc) throws IOException {
            if(cc.isAlive()){
                connections.add(new ClientConnectionWrapper(cc));
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
        final ClientConnection connection;
        final BufferedSource source;
        final BufferedSink sink;
        boolean permit;

        public ClientConnectionWrapper(ClientConnection connection) throws IOException {
            this.connection = connection;
            this.source = Okio.buffer(Okio.source(connection.getInputStream()));
            this.sink = Okio.buffer(Okio.sink(connection.getOutputStream()));
        }

        public boolean isAlive() {
            return connection.isAlive();
        }

        public String getRemoteUniqueKey() {
            return connection.getRemoteUniqueKey();
        }

        public Message<?> readMessage() {
            return OkMessage.readMessage(source);
        }

        @Override
        public boolean sendMessage(Message<Object> msg, float version) {
            OkMessage.writeMessage(sink, msg, CSConstant.TYPE_RSA_SINGLE, version);
            return true;
        }

        @Override
        public boolean sendMessage(Message<Object> msg) {
            OkMessage.writeMessage(
                    sink, msg, CSConstant.TYPE_RSA_SINGLE, MessageConfigManager.getVersion());
            return true;
        }

        public void close() {
            try {
                connection.close();
            } catch (IOException e) {
                // ignore
            }
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
