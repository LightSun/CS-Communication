package com.heaven7.java.cs.communication.sample;

import com.heaven7.java.cs.communication.*;
import com.heaven7.java.cs.communication.impl.SimpleServerMonitor;
import com.heaven7.java.message.protocol.Message;

import java.io.IOException;

public final class SampleSocketServer implements MessageHandler {

    private final ServerCommunicator mCommunicator;

    public SampleSocketServer(int port) {
        this.mCommunicator = new ServerCommunicator(3600*1000, new SimpleServerCallback(),
                new ServerSocketConnector(port), this);
        mCommunicator.setServerMonitor(new SimpleServerMonitor());
    }

    public void start(){
        try {
            mCommunicator.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void dispose(){
        mCommunicator.dispose();
    }

    @Override
    public void handleMessage(IMessageSender sender, Message<?> obj, float applyVersion) {
        //sample handle message.
        String inMsg = obj.getEntity() != null ? obj.getEntity().toString() : "null";
        Message<Object> outMsg = Message.create(obj.getType(), Message.SUCCESS, "message received. " + inMsg, null);
        sender.sendMessage(outMsg, applyVersion);
    }
}
