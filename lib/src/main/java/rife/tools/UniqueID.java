/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import java.util.Base64;
import java.util.UUID;

public class UniqueID {
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

    private byte[] id_ = null;
    private String idString_ = null;

    UniqueID(byte[] id) {
        setID(id);
    }

    public byte[] getID() {
        return id_;
    }

    void setID(byte[] id) {
        id_ = id;
        idString_ = null;
    }

    public String toString() {
        if (null == idString_) {
            idString_ = ENCODER.encodeToString(id_);
        }

        return idString_;
    }
}