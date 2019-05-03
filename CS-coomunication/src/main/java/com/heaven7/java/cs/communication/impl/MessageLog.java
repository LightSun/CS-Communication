package com.heaven7.java.cs.communication.impl;

import com.heaven7.java.meshy.Message;

/**
 * @author heaven7
 */
public final class MessageLog {

    public static String getStateString(int state) {
        switch (state){
            case Message.SUCCESS:
                return "SUCCESS";
            case Message.FAILED:
                return "FAILED";
        }
        return null;
    }

    public static String getTypeString(int type) {
        switch (type){
            case Message.LOGIN:
                return "LOGIN";

            case Message.LOGOUT:
                return "LOGOUT";

            case Message.TICK:
                return "TICK";

            case Message.COMMON:
                return "COMMON";
        }
        return null;
    }
}
