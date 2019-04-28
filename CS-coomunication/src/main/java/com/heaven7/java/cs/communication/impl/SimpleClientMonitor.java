package com.heaven7.java.cs.communication.impl;

import com.heaven7.java.base.util.DefaultPrinter;
import com.heaven7.java.cs.communication.ClientMonitor;
import com.heaven7.java.message.protocol.Message;

import static com.heaven7.java.cs.communication.impl.MessageLog.getStateString;
import static com.heaven7.java.cs.communication.impl.MessageLog.getTypeString;

public class SimpleClientMonitor implements ClientMonitor {

    private static final String TAG = "SampleClientMonitor";

    @Override
    public void onStart(boolean result) {
        DefaultPrinter.getDefault().debug(TAG, "onStart", "client start success = " + result);
    }

    @Override
    public void onEnd() {
        DefaultPrinter.getDefault().debug(TAG, "onEnd", "client end ." );
    }

    @Override
    public void onSendMessageToRemote(Message<?> out) {
        DefaultPrinter.getDefault().debug(TAG, "onSendMessageToRemote", "client  send message."  + getTypeString(out.getType()));
    }

    @Override
    public void onReceiveMessage(Message<?> inMsg) {
        String type = getTypeString(inMsg.getType());
        String state = getStateString(inMsg.getState());
        DefaultPrinter.getDefault().info(TAG, "onReceiveMessage", "client received message. type = "
                + type +" ,state = "+ state + " ,detail msg = " + inMsg.getMsg() + " ,entity = " + inMsg.getEntity());
    }

    @Override
    public void onTick() {
        DefaultPrinter.getDefault().debug(TAG, "onTick", "time is " + System.currentTimeMillis());
    }

    @Override
    public void onReadException(Exception e) {
        DefaultPrinter.getDefault().warn(TAG, "onReadException", e);
    }
}
