package com.heaven7.java.cs.communication.sample;

import com.heaven7.java.base.util.DefaultPrinter;
import com.heaven7.java.cs.communication.ClientMonitor;
import com.heaven7.java.message.protocol.Message;

public class SampleClientMonitor implements ClientMonitor {

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
    public void onSendLogin(Message<?> out) {
        DefaultPrinter.getDefault().debug(TAG, "onSendLogin", "client sended login message." );
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

    private static String getStateString(int state) {
        switch (state){
            case Message.SUCCESS:
                return "SUCCESS";
            case Message.FAILED:
                return "FAILED";
        }
        return null;
    }

    private static String getTypeString(int type) {
        switch (type){
            case Message.LOGIN:
                return "LOGIN";

            case Message.LOGOUT:
                return "LOGOUT";

            case Message.TICK:
                return "TICK";

            case Message.COMMON:
                return "COMMON";
        }
        return null;
    }
}
