package com.heaven7.java.cs.communication.impl;

import com.heaven7.java.cs.communication.ServerMonitor;
import com.heaven7.java.message.protocol.Message;

import java.util.List;

/**
 * @author heaven7
 */
public class ServerMonitorAdapter implements ServerMonitor {

    @Override
    public void onStart() {
    }

    @Override
    public void onEnd() {

    }

    @Override
    public void onNewClient(String key) {

    }

    @Override
    public void onMessageReceived(Message<?> msg) {

    }

    @Override
    public void onSendBroadcast(Message<?> msg, List<String> clients) {

    }

    @Override
    public void onSendBroadcastFailed(Message<?> msg) {

    }

    @Override
    public void onAddClientError(String remoteUniqueKey, Exception e) {

    }

    @Override
    public void onNotLogin(String userId) {

    }

    @Override
    public void onVerifyTokenFailed(String expectToken, String realToken) {

    }

    @Override
    public void onLogout(String token) {

    }

    @Override
    public void onTick(String token) {

    }

    @Override
    public void onValidateTempTokenFailed(String tmpToken) {

    }

    @Override
    public void onValidateTempTokenSuccess(String tmpToken, String newToken) {

    }

    @Override
    public void onStartReadMessage(String remoteId, String token) {

    }

    @Override
    public void onRemoveClient(String remoteId, String token) {

    }

    @Override
    public void onReadException(String remoteId, String token, Exception e) {

    }
}
