package com.heaven7.java.cs.communication;

import com.heaven7.java.message.protocol.Message;

public interface ServerMonitor {

    void onStart();
    void onEnd();
    void onMessageReceived(Message<?> msg);
}
