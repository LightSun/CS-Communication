package com.heaven7.java.cs.communication;

import com.heaven7.java.base.util.ThreadProxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author heaven7
 */
public class ServerSocketConnector implements ServerCommunicator.Connector {

    private final int port;
    private ServerSocket server;

    public ServerSocketConnector(int port) {
        this.port = port;
    }

    @Override
    public void connect(ThreadProxy proxy, ServerCommunicator.Reporter reporter) throws IOException {
        server = new ServerSocket(port);
        proxy.start(new Runner(reporter));
    }

    @Override
    public void disconnect() throws IOException {
        if(server != null){
            server.close();
            server = null;
        }
    }

    private class Runner implements Runnable{

        final ServerCommunicator.Reporter reporter;

        public Runner(ServerCommunicator.Reporter reporter) {
            this.reporter = reporter;
        }
        @Override
        public void run() {
            try{
                while (server != null){
                    Socket socket = server.accept();
                    //handle client too many ?
                    reporter.reportNewConnection(new SocketClientConnection(socket));
                }
            }catch (IOException e){
                reporter.reportError(e);
            }
        }
    }
}
