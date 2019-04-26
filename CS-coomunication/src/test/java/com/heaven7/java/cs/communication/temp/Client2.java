package com.heaven7.java.cs.communication.temp;

import com.heaven7.java.cs.communication.CSConstant;
import com.heaven7.java.cs.communication.entity.BaseEntity;
import com.heaven7.java.message.protocol.Message;
import com.heaven7.java.message.protocol.OkMessage;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client2 {
    // initialize socket and input output streams
    private Socket socket = null;
    private BufferedSource input = null;
    private BufferedSink out = null;

    // constructor to put ip address and port
    public Client2(String address, int port) {
        // establish a connection
        Server2.initialize();
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
            BaseEntity entity = new BaseEntity();
            entity.setToken("Hello Google");
            entity.setVersion(1.0f);
            OkMessage.writeMessage(out, Message.create(Message.LOGIN, "heaven7", entity), CSConstant.TYPE_RSA_SINGLE);
            try {
                out.writeByte('\n');
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
        Client2 client = new Client2("127.0.0.1", 5000);
    }
}
