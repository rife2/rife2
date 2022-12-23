/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic.beans;

public class BinaryBean {
    private int id_ = -1;
    private byte[] theBytes_;

    public void setId(int id) {
        id_ = id;
    }

    public int getId() {
        return id_;
    }

    public byte[] getTheBytes() {
        return theBytes_;
    }

    public void setTheBytes(byte[] binary) {
        theBytes_ = binary;
    }
}