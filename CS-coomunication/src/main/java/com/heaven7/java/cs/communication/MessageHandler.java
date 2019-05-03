package com.heaven7.java.cs.communication;

import com.heaven7.java.meshy.Message;

/**
 * @author heaven7
 */
public interface MessageHandler {

    /**
     * handle the message
     * @param sender the message sender used to reply message
     * @param obj the message to handle
     * @param applyVersion the apply version
     */
    void handleMessage(IMessageSender sender, Message<?> obj, float applyVersion);
}
