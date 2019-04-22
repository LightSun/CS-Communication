package com.heaven7.java.cs.communication.entity;

import com.heaven7.java.message.protocol.anno.FieldMember;
import com.heaven7.java.message.protocol.anno.Inherit;

/**
 * @author heaven7
 */
public class LoginEntity {

    @FieldMember
    @Inherit
    private String token;

    @FieldMember
    @Inherit
    private float version;

    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }

    public float getVersion() {
        return version;
    }
    public void setVersion(float version) {
        this.version = version;
    }
}
