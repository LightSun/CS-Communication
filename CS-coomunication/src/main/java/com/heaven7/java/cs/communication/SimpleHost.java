package com.heaven7.java.cs.communication;

public class SimpleHost implements HostDelegate {
    private String name;
    private int port;

    public SimpleHost(String name, int port) {
        this.name = name;
        this.port = port;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String getHostName() {
        return name;
    }

    @Override
    public int getHostPort() {
        return port;
    }
}