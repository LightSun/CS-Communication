package com.heaven7.java.cs.communication;


import com.heaven7.java.message.protocol.Message;

/**
 * @author heaven7
 */
public interface IMessageSender  {

    /**
     * send a message to remote
     * @param msg the message
     * @param version the version to send
     * @return true if send success.
     */
    boolean sendMessage(Message<Object> msg, float version);

    boolean sendMessage(Message<Object> msg);
}
