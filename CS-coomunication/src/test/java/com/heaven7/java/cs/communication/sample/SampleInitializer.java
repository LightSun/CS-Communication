package com.heaven7.java.cs.communication.sample;

import com.heaven7.java.cs.communication.CSConstant;
import com.heaven7.java.meshy.Meshy;
import com.heaven7.java.meshy.MeshyBuilder;
import com.heaven7.java.meshy.secure.SingleRSAMessageSecure;
import com.heaven7.java.meshy.util.RSAUtils;

import java.security.Key;

/**
 * the simple class to init message configuration.
 * @author heaven7
 */
public final class SampleInitializer {

    public static Meshy initialize(String rsaKey, boolean priKey){
        Key key = priKey ? RSAUtils.getPrivateKey(rsaKey) : RSAUtils.getPublicKey(rsaKey);
        return new MeshyBuilder()
                .setVersion(1.0f)
                .setSignatureKey("Hello Google")
                .registerMessageSecure(CSConstant.TYPE_RSA_SINGLE, new SingleRSAMessageSecure(key.getEncoded(), priKey))
                .build();
    }
}
