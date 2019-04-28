package com.heaven7.java.cs.communication;

import okio.Okio;
import okio.Sink;
import okio.Source;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author heaven7
 */
public class SocketClientConnection implements ServerCommunicator.ClientConnection {

    private final Socket mSocket;

    public SocketClientConnection(Socket mSocket) {
        this.mSocket = mSocket;
    }

    @Override
    public Sink getSink() throws IOException {
        return Okio.sink(mSocket);
    }

    @Override
    public Source getSource() throws IOException {
        return Okio.source(mSocket);
    }

    @Override
    public String getRemoteUniqueKey() {
        InetSocketAddress address = (InetSocketAddress) mSocket.getRemoteSocketAddress();
        return address.getHostString();
    }
    @Override
    public boolean isAlive() {
        return mSocket.isConnected() && !mSocket.isClosed()
                && !mSocket.isInputShutdown() && !mSocket.isOutputShutdown();
    }

    @Override
    public void close() throws IOException{
        mSocket.getInputStream().close();
        mSocket.getOutputStream().close();
        mSocket.close();
    }
}
