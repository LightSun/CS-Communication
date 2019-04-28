package com.heaven7.java.cs.communication.sample;

/**
 * @author heaven7
 */
public class SocketClientLauncher {

    public static void main(String[] args) {
        String params = "isClient=true rsaKey=MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAN5Ux9NNake1xvnMX7FkKUwVuICmUp2Wj5Cf8qfpW+dBbAYUeB4dy0y0ge8MEjaPfxrGoclqUL29VwPHxNS/FIUCAwEAAQ== " +
                "host=127.0.0.1 port=62381";
        Launcher.main(params.split(" "));
    }
}
