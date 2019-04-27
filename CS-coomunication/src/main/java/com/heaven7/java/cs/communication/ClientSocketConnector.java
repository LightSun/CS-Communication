package com.heaven7.java.cs.communication;

import okio.Okio;
import okio.Sink;
import okio.Source;

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
    public Sink getSink() throws IOException {
        return Okio.sink(mSocket);
    }
    @Override
    public Source getSource() throws IOException {
        return Okio.source(mSocket);
    }
}
