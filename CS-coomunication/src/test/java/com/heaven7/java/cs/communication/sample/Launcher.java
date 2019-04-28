package com.heaven7.java.cs.communication.sample;

import com.heaven7.java.cs.communication.SimpleHost;
import com.heaven7.java.pc.schedulers.Schedulers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class Launcher {

    public static final String KEY_IS_CLIENT = "isClient";
    public static final String KEY_RSA_KEY   = "rsaKey";
    public static final String KEY_HOST      = "host";
    public static final String KEY_PORT      = "port";

    public static void main(String[] args) {
        //params: isClient=true,rsaKey=xxx,host=xxx,port=xxx,
        Map<String, String> params = new HashMap<>();
        for (String arg : args){
            if(!arg.contains("=")){
                continue;
            }
            int index = arg.indexOf("=");
            String key = arg.substring(0, index);
            String value = arg.substring(index + 1);
            params.put(key, value);
        }
        boolean isClient = Boolean.valueOf(params.get(KEY_IS_CLIENT));
        int port = Integer.valueOf(params.get(KEY_PORT));
        SampleInitializer.initialize(params.get(KEY_RSA_KEY), !isClient);

        if(isClient){
            final SampleSocketClient client = new SampleSocketClient(new SimpleHost(params.get(KEY_HOST), port));
            client.start();
            Schedulers.io().newWorker().scheduleDelay(new Runnable() {
                @Override
                public void run() {
                    client.logout();
                }
            }, 30, TimeUnit.SECONDS);
        }else {
            SampleSocketServer server = new SampleSocketServer(port);
            server.start();
        }
        Thread.yield();
    }
}
