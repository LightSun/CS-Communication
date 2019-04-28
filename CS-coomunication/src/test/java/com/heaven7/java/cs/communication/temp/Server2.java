package com.heaven7.java.cs.communication.temp;

import com.heaven7.java.message.protocol.Message;
import com.heaven7.java.message.protocol.MessageConfig;
import com.heaven7.java.message.protocol.MessageConfigManager;
import com.heaven7.java.message.protocol.OkMessage;
import com.heaven7.java.message.protocol.policy.DefaultRSASegmentationPolicy;
import com.heaven7.java.message.protocol.secure.MessageSecureFactory;
import com.heaven7.java.message.protocol.secure.SingleRSAMessageSecure;
import com.heaven7.java.message.protocol.signature.MD5SaltSignature;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import okio.Timeout;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
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
        initialize(PRI_KEY, true);
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
               /* timeout.clearDeadline();
                timeout.deadline(5000, TimeUnit.MILLISECONDS);*/
                if(OkMessage.isReadyToRead(in)){
                    Message<Object> message = OkMessage.readMessageWithoutMagic(in);
                    System.out.println(message);
                }else {
                    System.out.println("data not prepared.");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                }
            }
            System.out.println("Closing connection");

            // close connection
            socket.close();
            in.close();
        } catch (IOException i) {
            System.out.println(i);
        }
    }

    public static void initialize(String key, boolean priKey){
        MessageConfig config = MessageConfig.newConfig();
        config.version = 1.0f;
        config.signKey = "Google/heaven7";
        config.signature = new MD5SaltSignature();
        //rsa-512 means max 53 . 1024 means max 117
        config.segmentationPolicy = new DefaultRSASegmentationPolicy(53);
        try {
            config.secures.put(TYPE_RSA_SINGLE, MessageSecureFactory.createMessageSecure(
                    SingleRSAMessageSecure.class.getName(), key, priKey +""));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        MessageConfigManager.initialize(config);
    }

    public static void main(String args[]) {
        Server2 server = new Server2(5000);
    }

    public static final String PRI_KEY =
            "MIIBVQIBADANBgkqhkiG9w0BAQEFAASCAT8wggE7AgEAAkEAq1onoTCwjVkHN72MwjxUfV" +
                    "J4P2L7bDynSoKrPfdZE8bG5cRSLjRyZy/6iSn08LvjmfRT8Zh5mUMtSbZ5WsQoawIDAQABAkBo3PyBtdVmO0SihRreR6" +
                    "OBKnYyfHXXcGsEu2DmxDe8VJgfPtlZ7Aya+OzxYg+g6M3Ay5NNiT/eTp6GnckyJYdBAiEA7pIZxo2y0yQmuADTgBgYm2CAX" +
                    "5VFmx3a3jmC3UDUCUsCIQC33uQIWc52eY5EkRa0JVn4UbMYA9PJ8DFXFCgyw6sJYQIhAKW8l+2JWTc3wTJJWKV/l/CxfN1qXT3" +
                    "i4r9JYvGEbQm7AiAUyK0eBIjB+5uJRJ08X7x7xUBpRoV6Hhx1q2Gf215KAQIhAOD8ogFfCgJ1JZA5S/RUKyl8YSvDpT+wkHs5w1Pq0fnO";
    public static final String PUB_KEY = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAKtaJ6EwsI1ZBze9jMI8VH1SeD9i+2w8p0qCqz33WRPGxuXEUi40cmcv+okp9PC745n0U/GYeZlDLUm2eVrEKGsCAwEAAQ==";
}
