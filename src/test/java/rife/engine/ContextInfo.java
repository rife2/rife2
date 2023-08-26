/*
 * Copyright 2023 Erik C. Thauvin (https://erik.thauvin.net/)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

public class ContextInfo {
    private String remoteAddr = null;
    private int serverPort = -1;
    private String pathInfo = null;

    public String remoteAddr() {
        return remoteAddr;
    }

    public void remoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    public void context(Context c) {
        remoteAddr = c.remoteAddr();
        serverPort = c.serverPort();
        pathInfo = c.pathInfo();
    }

    public int serverPort() {
        return serverPort;
    }

    public void serverPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String pathInfo() {
        return pathInfo;
    }

    public void pathInfo(String pathInfo) {
        this.pathInfo = pathInfo;
    }
}
