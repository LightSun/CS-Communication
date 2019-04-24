package com.heaven7.java.cs.communication;

import com.heaven7.java.message.protocol.MessageConfig;
import com.heaven7.java.message.protocol.MessageConfigManager;
import com.heaven7.java.message.protocol.policy.DefaultRSASegmentationPolicy;
import com.heaven7.java.message.protocol.secure.MessageSecureFactory;
import com.heaven7.java.message.protocol.secure.SingleRSAMessageSecure;
import com.heaven7.java.message.protocol.signature.MD5SaltSignature;

/**
 * the simple class to init message configuration.
 * @author heaven7
 */
public final class SimpleInitializer {

    public static void initialize(String rsaKey, boolean priKey){
        MessageConfig config = MessageConfig.newConfig();
        config.version = 1.0f;
        config.signKey = "Google/heaven7";
        config.signature = new MD5SaltSignature();
        //rsa-512 means max 53 . 1024 means max 117
        config.segmentationPolicy = new DefaultRSASegmentationPolicy(53);
        try {
            config.secures.put(CSConstant.TYPE_RSA_SINGLE, MessageSecureFactory.createMessageSecure(
                    SingleRSAMessageSecure.class.getName(), rsaKey, priKey +""));
        } catch (Exception e) {
           throw new RuntimeException(e);
        }
        MessageConfigManager.initialize(config);
    }
}
