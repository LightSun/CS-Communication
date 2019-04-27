package com.heaven7.java.cs.communication.temp;

import com.heaven7.java.cs.communication.util.RSACoder;
import com.heaven7.java.message.protocol.Message;
import com.heaven7.java.message.protocol.MessageConfig;
import com.heaven7.java.message.protocol.MessageConfigManager;
import com.heaven7.java.message.protocol.OkMessage;
import com.heaven7.java.message.protocol.policy.DefaultRSASegmentationPolicy;
import com.heaven7.java.message.protocol.secure.MessageSecureFactory;
import com.heaven7.java.message.protocol.secure.RSAMessageSecure;
import com.heaven7.java.message.protocol.signature.MD5SaltSignature;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import okio.Timeout;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import static com.heaven7.java.cs.communication.CSConstant.TYPE_RSA_SINGLE;

public class Server2 {
    // initialize socket and input stream
    private Socket socket = null;
    private ServerSocket server = null;
    private BufferedSource in = null;
    private BufferedSink out = null;

    // constructor with port
    public Server2(int port) {
        initialize();
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
            Timeout timeout = in.timeout();
            while (!line.equals("Over")) {
                timeout.clearDeadline();
                timeout.deadline(5000, TimeUnit.MILLISECONDS);
                Message<Object> message = OkMessage.readMessage(in);
                System.out.println(message);
            }
            System.out.println("Closing connection");

            // close connection
            socket.close();
            in.close();
        } catch (IOException i) {
            System.out.println(i);
        }
    }

    public static void initialize(){
        String priKey;
        String pubKey;
        try {
            KeyPair keyPair = RSACoder.initKeys();
            priKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
            pubKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        MessageConfig config = MessageConfig.newConfig();
        config.version = 1.0f;
        config.signKey = "Google/heaven7";
        config.signature = new MD5SaltSignature();
        //rsa-512 means max 53 . 1024 means max 117
        config.segmentationPolicy = new DefaultRSASegmentationPolicy(53);
        try {
            config.secures.put(TYPE_RSA_SINGLE, MessageSecureFactory.createMessageSecure(
                    RSAMessageSecure.class.getName(), pubKey, priKey));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        MessageConfigManager.initialize(config);
    }

    public static void main(String args[]) {
        Server2 server = new Server2(5000);
    }
}
