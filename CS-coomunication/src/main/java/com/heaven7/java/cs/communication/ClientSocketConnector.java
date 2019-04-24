package com.heaven7.java.cs.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @author heaven7
 */
public class ClientSocketConnector implements ClientCommunicator.Connector {

    private final HostDelegate mHost;
    private Socket mSocket;

    public ClientSocketConnector(HostDelegate mHost) {
        this.mHost = mHost;
    }

    @Override
    public void connect() throws IOException {
        mSocket = new Socket(mHost.getHostName(), mHost.getHostPort());
    }

    @Override
    public void disconnect() throws IOException {
        if(mSocket != null){
            mSocket.getOutputStream().close();
            mSocket.getInputStream().close();
            mSocket.close();
            mSocket = null;
        }
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return mSocket.getOutputStream();
    }
    @Override
    public InputStream getInputStream()throws IOException {
        return mSocket.getInputStream();
    }
}
