/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations.instrument;

abstract class TypesOpcode {
    static final byte SET = 1;
    static final byte GET = 2;
    static final byte IINC = 3;
    static final byte POP = 4;
    static final byte POP2 = 5;
    static final byte PUSH = 6;
    static final byte AALOAD = 7;
    static final byte DUP = 8;
    static final byte DUPX1 = 9;
    static final byte DUPX2 = 10;
    static final byte DUP2 = 11;
    static final byte DUP2_X1 = 12;
    static final byte DUP2_X2 = 13;
    static final byte SWAP = 14;
    static final byte PAUSE = 15;
    static final byte LABEL = 16;

    static String toString(byte opcode) {
        return switch (opcode) {
            case SET -> "SET";
            case GET -> "GET";
            case IINC -> "IINC";
            case POP -> "POP";
            case POP2 -> "POP2";
            case PUSH -> "PUSH";
            case AALOAD -> "AALOAD";
            case DUP -> "DUP";
            case DUPX1 -> "DUPX1";
            case DUPX2 -> "DUPX2";
            case DUP2 -> "DUP2";
            case DUP2_X1 -> "DUP2_X1";
            case DUP2_X2 -> "DUP2_X2";
            case SWAP -> "SWAP";
            case PAUSE -> "PAUSE";
            case LABEL -> "LABEL";
            default -> null;
        };

    }
}

