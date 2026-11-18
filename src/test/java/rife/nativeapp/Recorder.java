/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.nativeapp;

import rife.engine.SiteResponse;
import rife.engine.SiteResponseListener;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

// what a request tells, in the order it's told
class Recorder implements SiteResponseListener {
    final List<String> calls = new ArrayList<>();
    final ByteArrayOutputStream content = new ByteArrayOutputStream();
    int status;
    String headers;
    Throwable failure;

    public synchronized void committed(SiteResponse response) {
        calls.add("committed");
        status = response.getStatus();
        headers = NativeApp.headerLines(response);
    }

    public synchronized void content(byte[] bytes, int offset, int length) {
        if (calls.isEmpty() || !calls.get(calls.size() - 1).equals("content")) {
            calls.add("content");
        }
        content.write(bytes, offset, length);
    }

    public synchronized void completed() {
        calls.add("completed");
    }

    public synchronized void failed(Throwable exception) {
        calls.add("failed");
        failure = exception;
    }

    public synchronized void cancelled() {
        calls.add("cancelled");
    }

    synchronized String text() {
        return content.toString(StandardCharsets.UTF_8);
    }
}
