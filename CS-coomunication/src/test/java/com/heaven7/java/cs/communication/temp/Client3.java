package com.heaven7.java.cs.communication.temp;

import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class Client3 {
    // initialize socket and input output streams
    private Socket socket = null;
    private BufferedSource input = null;
    private BufferedSink out = null;

    // constructor to put ip address and port
    public Client3(String address, int port) {
        // establish a connection
        try {
            socket = new Socket(address, port);
            System.out.println("Connected");

            // takes input from terminal
            input = Okio.buffer(Okio.source(System.in));

            // sends output to the socket
            out = Okio.buffer(Okio.sink(socket.getOutputStream()));
        } catch (UnknownHostException u) {
            System.out.println(u);
        } catch (IOException i) {
            System.out.println(i);
        }

        // string to read message from input
        String line = "";

        // keep reading until "Over" is input
        while (!line.equals("Over")) {
            try {
                String msg = "sdfdsfjdsfssssssssssssssss";
                out.writeInt(43);
                out.writeInt(msg.length());
                out.write(msg.getBytes(StandardCharsets.UTF_8));
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("client write message success,");
            synchronized (this){
                try {
                    this.wait();
                } catch (InterruptedException e) {
                }
            }
        }

        // close the connection
        try {
            input.close();
            out.close();
            socket.close();
        } catch (IOException i) {
            System.out.println(i);
        }
    }

    public static void main(String args[]) {
        Client3 client = new Client3("127.0.0.1", 5000);
    }
}
