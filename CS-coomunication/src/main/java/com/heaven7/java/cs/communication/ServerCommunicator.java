package com.heaven7.java.cs.communication;

import com.heaven7.java.base.util.Disposable;
import com.heaven7.java.base.util.ThreadProxy;
import com.heaven7.java.cs.communication.entity.LoginEntity;
import com.heaven7.java.cs.communication.entity.TokenEntity;
import com.heaven7.java.message.protocol.Message;
import com.heaven7.java.message.protocol.MessageConfigManager;
import com.heaven7.java.message.protocol.MessageProtocol;
import com.heaven7.java.message.protocol.OkMessage;
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

import static com.heaven7.java.cs.communication.CSConstant.MSG_INVALID_TMP_TOKEN;

/** @author heaven7 */
public final class ServerCommunicator implements Disposable {

    public interface Callback{
        void handleMessage(IMessageSender sender, Message<?> msg, float version);
        boolean validateTempToken(String token);
    }

    public interface InternalCallback{
        boolean validateTempToken(String token);
        String generateToken(String tmpToken, String userKey);
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
    }

    private static final CSThreadFactory sFACTORY = new CSThreadFactory("ServerCommunicator");
    private final ThreadProxy mProxy;
    private final Connector mConnector;
    private final Callback mCallback;
    private final long mTickTimeSpace;
    private InternalCallback mInternalCallback;

    private final AtomicInteger mConnectCount = new AtomicInteger(0);
    private final Reporter0 mReporter = new Reporter0();

    private final Vector<ClientConnection> mPendingClients = new Vector<>();
    private final Map<String, ClientInfo> mClientInfoMap = new ConcurrentHashMap<>();

    public ServerCommunicator(long tickTimeSpace, Connector connector, Callback callback) {
        this.mCallback = callback;
        this.mProxy = ThreadProxy.create(sFACTORY);
        this.mConnector = connector;
        this.mTickTimeSpace = tickTimeSpace;
    }

    public void start() throws IOException {
        mConnector.connect(mProxy, mReporter);
    }

    @Override
    public void dispose() {
        try {
            mConnector.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mProxy.dispose();
        mConnectCount.getAndSet(0);
    }

    private class Reporter0 implements ServerCommunicator.Reporter {
        @Override
        public void reportError(IOException e) {
            e.printStackTrace();
        }

        @Override
        public void reportNewConnection(ClientConnection cc) {
            mConnectCount.incrementAndGet();
            mPendingClients.add(cc);
        }
    }

    static class ClientInfo {
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

        public void start(){
            mProxy.start(this);
        }

        @Override
        public void run() {
            List<ClientConnectionWrapper> list = new ArrayList<>();
            while (!mClosed.get()) {
                list.addAll(connections);
                for (ClientConnectionWrapper conn : list) {
                    String uniqueKey = conn.getRemoteUniqueKey();
                    boolean removeClient = false;
                    if(conn.isAlive()){
                        Message<?> msg = conn.readMessage();
                        if(msg != null){
                            ClientInfo clientInfo = mClientInfoMap.get(uniqueKey);
                            switch (msg.getType()){
                                case Message.LOGIN:
                                    LoginEntity entity = (LoginEntity) msg.getEntity();
                                    if(mCallback.validateTempToken(entity.getToken())){
                                        clientInfo.version = entity.getVersion();
                                        clientInfo.token = mInternalCallback.generateToken(entity.getToken(), uniqueKey);
                                        conn.permit = true;
                                        //gen token and response.
                                        entity.setToken(clientInfo.token);
                                        entity.setVersion(MessageConfigManager.getVersion());
                                        conn.sendMessage(Message.create(Message.LOGIN, CSConstant.SUCCESS, entity), clientInfo.version);
                                    }else{
                                        conn.sendMessage(Message.create(CSConstant.MSG_LOGIN_FAILED, MSG_INVALID_TMP_TOKEN, null));
                                        removeClient = true;
                                    }
                                    break;

                                case Message.TICK:
                                    {
                                        clientInfo.lastTickTime = System.currentTimeMillis();
                                    }
                                    break;

                                default:
                                    {
                                        mCallback.handleMessage(conn, msg, clientInfo.version);
                                    }
                            }
                        }
                    }else {
                        removeClient = true;
                    }
                    if(removeClient){
                        connections.remove(conn);
                        mClientInfoMap.remove(uniqueKey);
                    }
                }
                list.clear();
            }
        }
        @Override
        public void dispose() {
            if(mClosed.compareAndSet(false, true)){
                mProxy.dispose();
            }
        }
    }
    private static class ClientConnectionWrapper implements IMessageSender{
        final ClientConnection connection;
        final BufferedSource source;
        final BufferedSink sink;
        boolean permit;
        public ClientConnectionWrapper(ClientConnection connection) throws IOException{
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
            OkMessage.writeMessage(sink, msg, CSConstant.TYPE_RSA_SINGLE, MessageConfigManager.getVersion());
            return true;
        }
    }
}
