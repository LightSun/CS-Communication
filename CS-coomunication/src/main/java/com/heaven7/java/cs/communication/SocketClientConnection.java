package com.heaven7.java.cs.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
}
