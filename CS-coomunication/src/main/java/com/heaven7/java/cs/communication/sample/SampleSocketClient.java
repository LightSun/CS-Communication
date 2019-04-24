package com.heaven7.java.cs.communication.sample;

import com.heaven7.java.base.util.DefaultPrinter;
import com.heaven7.java.cs.communication.*;
import com.heaven7.java.cs.communication.entity.BaseEntity;
import com.heaven7.java.cs.communication.util.JwtUtil;
import com.heaven7.java.message.protocol.Message;
import com.heaven7.java.message.protocol.MessageConfigManager;
import com.heaven7.java.pc.schedulers.Schedulers;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


public final class SampleSocketClient implements MessageHandler{

    private static final String TAG = "SampleSocketClient";
    private final ClientCommunicator mCommunicator;
    private final ClientMonitor mMonitor = new SampleClientMonitor();
    private BaseEntity mCoreEntity;

    public SampleSocketClient(HostDelegate host) {
        this.mCommunicator = new ClientCommunicator(new ClientSocketConnector(host), this, 500);
    }

    public void start(){
        try {
            boolean result = mCommunicator.start();
            mMonitor.onStart(result);
            if(result){
                BaseEntity entity = new BaseEntity();
                entity.setToken(JwtUtil.generateToken("sample_client", null));
                entity.setVersion(MessageConfigManager.getVersion());
                Message<Object> msg = Message.create(Message.LOGIN, "i want to login", entity);
                mCommunicator.sendMessage(msg);
                mMonitor.onSendLogin(msg);
            }
        } catch (IOException e) {
           throw new RuntimeException(e);
        }
    }

    public void dispose(){
        mCommunicator.dispose();
        mMonitor.onEnd();
    }

    /**
     * cancel client by send logout cmd.
     */
    public void cancel(){
        Message<Object> msg = Message.create(Message.LOGOUT, null, mCoreEntity);
        mCommunicator.sendMessage(msg);
    }

    @Override
    public void handleMessage(IMessageSender sender, Message<?> obj, float applyVersion) {
        mMonitor.onReceiveMessage(obj);

        int type = obj.getType();
        int responseState = obj.getState();
        switch (type){
            case Message.LOGIN:
                if(responseState == Message.SUCCESS){
                    mCoreEntity = (BaseEntity) obj.getEntity();
                    startTickPeriodically();
                }else {
                    DefaultPrinter.getDefault().warn(TAG, "handleMessage", "login failed!");
                }
                break;

            case Message.LOGOUT:
                if(responseState == Message.SUCCESS){
                    dispose();
                }else {
                    DefaultPrinter.getDefault().warn(TAG, "handleMessage", "logout failed!");
                }
                break;
        }
    }

    private void startTickPeriodically() {
        //tick every 30s
        Schedulers.io().newWorker().schedulePeriodically(new Runnable() {
            @Override
            public void run() {
                Message<Object> msg = Message.create(Message.TICK, null, mCoreEntity);
                mCommunicator.sendMessage(msg);
                mMonitor.onTick();
            }
        }, 5, 5, TimeUnit.SECONDS);
    }
}
