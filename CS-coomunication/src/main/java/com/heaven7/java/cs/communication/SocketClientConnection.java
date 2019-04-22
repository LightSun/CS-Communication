package com.heaven7.java.cs.communication;

import com.heaven7.java.base.util.Disposable;

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
    public OutputStream getOutputStream() throws IOException {
        return mSocket.getOutputStream();
    }
    @Override
    public InputStream getInputStream() throws IOException {
        return mSocket.getInputStream();
    }
    @Override
    public String getRemoteUniqueKey() {
        InetSocketAddress address = (InetSocketAddress) mSocket.getRemoteSocketAddress();
        return address.getHostString();
    }
    @Override
    public boolean isAlive() {
        return mSocket.isConnected() && !mSocket.isClosed();
    }

    @Override
    public void close() throws IOException{
        mSocket.getInputStream().close();
        mSocket.getOutputStream().close();
        mSocket.close();
    }
}
