package com.heaven7.java.cs.communication;

import com.heaven7.java.meshy.Message;

import java.util.List;

public interface ServerMonitor {

    void onStart();
    void onEnd();
    void onNewClient(String remoteId);
    void onMessageReceived(Message<?> msg);

    void onSendBroadcast(Message<?> msg, List<String> clients);
    void onSendBroadcastFailed(Message<?> msg);

    void onAddClientError(String remoteId, Exception e);
    void onNotLogin(String userId);
    void onVerifyTokenFailed(String expectToken, String realToken);

    void onLogout(String token);
    void onTick(String token);
    void onValidateTempTokenFailed(String tmpToken);
    void onValidateTempTokenSuccess(String tmpToken, String newToken);

    void onStartReadMessage(String remoteId, String token);
    void onRemoveClient(String remoteId, String token);

    void onReadException(String remoteId, String token, Exception e);

    void onBlockingEnd(String remoteId, String token);
    void onBlockingStart(String remoteId);
}
