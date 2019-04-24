package com.heaven7.java.cs.communication.entity;

import com.heaven7.java.message.protocol.anno.FieldMember;
import com.heaven7.java.message.protocol.anno.Inherit;

/**
 * @author heaven7
 */
public class BaseEntity {

    /**
     * for request this is temp token, for response. this is real token for next every request,
     */
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

    @Override
    public String toString() {
        return "BaseEntity{" +
                "token='" + token + '\'' +
                ", version=" + version +
                '}';
    }
}
