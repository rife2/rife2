/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

public final class IntegerUtils {
    private IntegerUtils() {
        // no-op
    }

    public static byte[] intToBytes(int integer) {
        var bytes = new byte[4];

        bytes[0] = (byte) (integer & 0x000000ff);
        bytes[1] = (byte) ((integer & 0x0000ff00) >> 8);
        bytes[2] = (byte) ((integer & 0x00ff0000) >> 16);
        bytes[3] = (byte) ((integer & 0xff000000) >> 24);

        return bytes;
    }

    public static int bytesToInt(byte[] bytes) {
        var q3 = bytes[3] << 24;
        var q2 = bytes[2] << 16;
        var q1 = bytes[1] << 8;
        int q0 = bytes[0];
        if (q2 < 0) q2 += 16777216;
        if (q1 < 0) q1 += 65536;
        if (q0 < 0) q0 += 256;

        return q3 | q2 | q1 | q0;
    }
}

