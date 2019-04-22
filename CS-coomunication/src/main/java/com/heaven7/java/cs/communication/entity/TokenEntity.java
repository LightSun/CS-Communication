package com.heaven7.java.cs.communication.entity;

import com.heaven7.java.message.protocol.anno.FieldMembers;
import com.heaven7.java.message.protocol.anno.Inherit;

/**
 * @author heaven7
 */
@FieldMembers
public class TokenEntity {

    @Inherit
    private int type;
    @Inherit
    private String key;

    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
}
