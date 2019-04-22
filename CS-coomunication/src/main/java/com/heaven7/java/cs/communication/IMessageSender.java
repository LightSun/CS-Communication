package com.heaven7.java.cs.communication;


import com.heaven7.java.message.protocol.Message;

/**
 * @author heaven7
 */
public interface IMessageSender  {

    boolean sendMessage(Message<Object> msg, float version);

    boolean sendMessage(Message<Object> msg);
}
