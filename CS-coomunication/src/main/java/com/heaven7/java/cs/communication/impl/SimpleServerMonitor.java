package com.heaven7.java.cs.communication.impl;

import com.heaven7.java.base.util.DefaultPrinter;
import com.heaven7.java.cs.communication.ServerMonitor;
import com.heaven7.java.meshy.Message;

import java.util.List;

/**
 * @author heaven7
 */
public class SimpleServerMonitor implements ServerMonitor {

    private static final String TAG = "SimpleServerMonitor";

    @Override
    public void onStart() {
        DefaultPrinter.getDefault().debug(TAG, "onStart", "server is started now!");
    }

    @Override
    public void onEnd() {
        DefaultPrinter.getDefault().debug(TAG, "onEnd", "server is stopped now!");
    }

    @Override
    public void onNewClient(String key) {
        DefaultPrinter.getDefault().debug(TAG, "onNewClient", "new client is arrived. remoteId = " + key);
    }

    @Override
    public void onMessageReceived(Message<?> msg) {
        DefaultPrinter.getDefault().debug(TAG, "onMessageReceived", "msg.type = " + MessageLog.getTypeString(msg.getType()));
    }

    @Override
    public void onSendBroadcast(Message<?> msg, List<String> clients) {
        DefaultPrinter.getDefault().debug(TAG, "onSendBroadcast", "msg.type = " + MessageLog.getTypeString(msg.getType()));
    }

    @Override
    public void onSendBroadcastFailed(Message<?> msg) {
        DefaultPrinter.getDefault().debug(TAG, "onSendBroadcastFailed", "msg.type = " + MessageLog.getTypeString(msg.getType()));
    }

    @Override
    public void onAddClientError(String remoteUniqueKey, Exception e) {
        DefaultPrinter.getDefault().debug(TAG, "onAddClientError", "remoteId = " + remoteUniqueKey);
    }

    @Override
    public void onNotLogin(String userId) {
        DefaultPrinter.getDefault().debug(TAG, "onNotLogin", "remoteId = " + userId);
    }

    @Override
    public void onVerifyTokenFailed(String expectToken, String realToken) {
        DefaultPrinter.getDefault().debug(TAG, "onVerifyTokenFailed", "expect token is "
                + expectToken + ", but is " + realToken);
    }

    @Override
    public void onLogout(String token) {
        DefaultPrinter.getDefault().debug(TAG, "onLogout", "token = " + token);
    }

    @Override
    public void onTick(String token) {
        DefaultPrinter.getDefault().debug(TAG, "onTick", "token = " + token);
    }

    @Override
    public void onValidateTempTokenFailed(String tmpToken) {
        DefaultPrinter.getDefault().debug(TAG, "onValidateTempTokenFailed", "tmpToken = " + tmpToken);
    }

    @Override
    public void onValidateTempTokenSuccess(String tmpToken, String newToken) {
        DefaultPrinter.getDefault().debug(TAG, "onValidateTempTokenSuccess", "tmpToken = "
                + tmpToken + " ,newToken = " + newToken);
    }
    @Override
    public void onStartReadMessage(String remoteId, String token) {
        DefaultPrinter.getDefault().debug(TAG, "onStartReadMessage", "remoteId = " + remoteId + ", token = " + token);
    }
    @Override
    public void onRemoveClient(String remoteId, String token) {
        DefaultPrinter.getDefault().debug(TAG, "onRemoveClient", "remoteId = " + remoteId + ", token = " + token);
    }

    @Override
    public void onReadException(String remoteId, String token, Exception e) {
      /*  DefaultPrinter.getDefault().warn(TAG, "onReadException", "remoteId = " + remoteId
                + ", token = " + token + " , exception is " + Throwables.getStackTraceAsString(e));*/
    }

    @Override
    public void onBlockingEnd(String remoteId, String token) {
        DefaultPrinter.getDefault().debug(TAG, "onBlockingEnd", "remoteId = " + remoteId + ", token = " + token);
    }
    @Override
    public void onBlockingStart(String remoteId) {
        DefaultPrinter.getDefault().debug(TAG, "onBlockingStart", "remoteId = " + remoteId);
    }
}
