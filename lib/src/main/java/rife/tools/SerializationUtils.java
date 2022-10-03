/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import java.io.*;

import rife.tools.exceptions.DeserializationErrorException;
import rife.tools.exceptions.SerializationErrorException;
import rife.tools.exceptions.SerializationUtilsErrorException;

import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public abstract class SerializationUtils {
    public static <TargetType extends Serializable> TargetType deserializeFromString(String value)
    throws SerializationUtilsErrorException {
        if (null == value) {
            return null;
        }

        byte[] value_bytes_decoded = Base64.getDecoder().decode(value);
        if (null == value_bytes_decoded) {
            throw new DeserializationErrorException(null);
        }

        ByteArrayInputStream bytes_is = new ByteArrayInputStream(value_bytes_decoded);
        GZIPInputStream gzip_is = null;
        ObjectInputStream object_is = null;
        try {
            gzip_is = new GZIPInputStream(bytes_is);
            object_is = new ObjectInputStream(gzip_is);
            return (TargetType) object_is.readObject();
        } catch (IOException e) {
            throw new DeserializationErrorException(e);
        } catch (ClassNotFoundException e) {
            throw new DeserializationErrorException(e);
        }
    }

    public static String serializeToString(Serializable value)
    throws SerializationUtilsErrorException {
        if (null == value) throw new IllegalArgumentException("value can't be null.");

        ByteArrayOutputStream byte_os = new ByteArrayOutputStream();
        GZIPOutputStream gzip_os = null;
        ObjectOutputStream object_os = null;
        try {
            gzip_os = new GZIPOutputStream(byte_os);
            object_os = new ObjectOutputStream(gzip_os);
            object_os.writeObject(value);
            object_os.flush();
            gzip_os.flush();
            gzip_os.finish();
        } catch (IOException e) {
            throw new SerializationErrorException(value, e);
        }

        byte[] value_bytes_decoded = byte_os.toByteArray();

        return Base64.getEncoder().encodeToString(value_bytes_decoded);
    }
}


