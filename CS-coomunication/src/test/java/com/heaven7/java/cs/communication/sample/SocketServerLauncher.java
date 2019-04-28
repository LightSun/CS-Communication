package com.heaven7.java.cs.communication.sample;

/**
 * @author heaven7
 */
public class SocketServerLauncher {

    public static void main(String[] args) {
        String params = "isClient=false " +
                "rsaKey=MIIBVgIBADANBgkqhkiG9w0BAQEFAASCAUAwggE8AgEAAkEA3lTH001qR7XG+cxfsWQpTBW4gKZSnZaPkJ/yp+lb50FsBhR4Hh3LTLSB7wwSNo9/GsahyWpQv" +
                "b1XA8fE1L8UhQIDAQABAkBAH8e6g/uBT0ZiSbXX3gGjNiiOwmnVldU2a0t7bmzZV5V3AcHd99l3oY8IN9QjT7W8fY+Bf0VpE9OCW7EX2qUJAiEA9hpSaZcRfQ1Zw7z2IU" +
                "bCMY3sLCW8u6vy91FKwOvV95sCIQDnRbmKIS1p6RF2onVWR/jz/2MWOW6XsTVVL1fww/C2XwIhANFA2EoHtUsLCwQvq2fn7j6MWf+/ppY8Qj6f1FvtKuV5AiEA" +
                "jdCwnGYJzgcQIVovb3AE1cIGBavECBqvJIQ6fbX5FisCIQC7+brS02Zf6AvAu9IY4L33bGm48pXbof8EPSrGeCbrTg== " +
                "host=127.0.0.1 port=62381";
        Launcher.main(params.split(" "));
    }
}
