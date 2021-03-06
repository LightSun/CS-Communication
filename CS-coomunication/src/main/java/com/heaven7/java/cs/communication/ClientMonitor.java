package com.heaven7.java.cs.communication;

import com.heaven7.java.meshy.Message;

public interface ClientMonitor {

    void onStart(boolean result);
    void onEnd();
    void onSendMessageToRemote(Message<?> out);
    void onReceiveMessage(Message<?> inMsg);
    void onTick();
    void onReadException(Exception e);
}
