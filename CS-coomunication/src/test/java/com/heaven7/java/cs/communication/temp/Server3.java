package com.heaven7.java.cs.communication.temp;

import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Server3 {
    // initialize socket and input stream
    private Socket socket = null;
    private ServerSocket server = null;
    private BufferedSource in = null;
    private BufferedSink out = null;

    // constructor with port
    public Server3(int port) {
        //regist

        // starts server and waits for a connection
        try {
            server = new ServerSocket(port);
            System.out.println("Server started");

            System.out.println("Waiting for a client ...");

            socket = server.accept();
            System.out.println("Client accepted");

            // takes input from the client socket
            //in = Okio.buffer(Okio.source(socket.getInputStream()));
            out = Okio.buffer(Okio.sink(socket));
            in = Okio.buffer(Okio.source(socket));

            String line = "";

            // reads message from client until "Over" is sent
           /* while (!line.equals("Over")) {
                BufferedSource peek = in.peek();
                peek.readInt();
                int len = peek.readInt();
                if(!peek.request(len)){
                    System.out.println("message len = " + len + " not all reached.");
                    continue;
                }
                in.skip(8);
                byte[] bytes = in.readByteArray(len);
                System.out.println("message reached:  " + new String(bytes, StandardCharsets.UTF_8));
            }*/
            while (!line.equals("Over")) {
                in.skip(4);
                int len = in.readInt();
                if(!in.request(len)){
                    System.out.println("data not reached.");
                    break;
                }
                byte[] bytes = in.readByteArray(len);
                System.out.println("message reached:  " + new String(bytes, StandardCharsets.UTF_8));
            }

            //below is ok.
            /*while (!line.equals("Over")) {
                in.skip(4);
                int len = in.readInt();
                byte[] bytes = in.readByteArray(len);
                System.out.println("message reached:  " + new String(bytes, StandardCharsets.UTF_8));
            }*/
            System.out.println("Closing connection");

            // close connection
            socket.close();
            in.close();
        } catch (IOException i) {
            System.out.println(i);
        }
    }

    public static void main(String args[]) {
        Server3 server = new Server3(5000);
    }
}
