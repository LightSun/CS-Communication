package com.heaven7.java.cs.communication;

import com.heaven7.java.base.util.DefaultPrinter;
import com.heaven7.java.cs.communication.util.JwtUtil;

import java.util.List;

/**
 * @author heaven7
 */
public class SimpleServerCallback implements ServerCommunicator.Callback {

    private static final String TAG = "SimpleServerCallback";

    @Override
    public boolean validateTempToken(String token) {
        try{
            return JwtUtil.parseJWT(token).get("id") != null;
        }catch (Exception e){
            return false;
        }
    }

    @Override
    public String generateToken(String tmpToken, String userKey) {
        return JwtUtil.generateToken(userKey, TAG);
    }

    @Override
    public void onTickTimeTimeout(String token) {
        DefaultPrinter.getDefault().info(TAG, "onTickTimeTimeout", "user is " + JwtUtil.parseJWT(token).get("id"));
    }

    @Override
    public boolean acceptToken(String token, List<String> ids) {
        String id = JwtUtil.parseJWT(token).get("id").toString();
        return ids.contains(id);
    }
}
