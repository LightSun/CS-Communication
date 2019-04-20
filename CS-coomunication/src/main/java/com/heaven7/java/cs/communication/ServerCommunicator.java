package com.heaven7.java.cs.communication;

import com.heaven7.java.base.util.Disposable;
import com.heaven7.java.base.util.ThreadProxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author heaven7
 */
public class ServerCommunicator implements Disposable{

    public interface Connector{
        void connect(ThreadProxy proxy, Reporter reporter) throws IOException;
         void disconnect() throws IOException;
    }
    public interface Reporter{
        void reportError(IOException e);
        void reportNewConnection(ClientConnection cc);
    }

    public interface ClientConnection{
        OutputStream getOutputStream() throws IOException;
        InputStream getInputStream() throws IOException;
    }

    private static CSThreadFactory sFACTORY = new CSThreadFactory("ServerCommunicator");
    private final ThreadProxy mProxy;
    private final Connector mConnector;
    private final AtomicInteger mConnectCount = new AtomicInteger(0);

    public ServerCommunicator(Connector connector) {
        this.mProxy = ThreadProxy.create(sFACTORY);
        this.mConnector = connector;
    }

    public void start() throws IOException{
        mConnector.connect(mProxy, new Reporter0());
    }

    @Override
    public void dispose() {
        mProxy.dispose();
        mConnectCount.getAndSet(0);
    }

    private class Reporter0 implements ServerCommunicator.Reporter{
        @Override
        public void reportError(IOException e) {
             e.printStackTrace();
        }
        @Override
        public void reportNewConnection(ClientConnection cc) {
            mConnectCount.incrementAndGet();
        }
    }
}
