/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

public class Context {
    private final Request request_;
    private final Response response_;

    public Context(Request request, Response response) {
        this.request_ = request;
        this.response_ = response;
    }

    public Request request() {
        return request_;
    }

    public Response response() {
        return response_;
    }

    public void print(Object o) {
        response_.print(o);
    }
}
