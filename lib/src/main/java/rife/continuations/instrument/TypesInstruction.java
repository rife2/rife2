/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations.instrument;

class TypesInstruction {
    private final byte opcode_;
    private final int argument_;
    private final String type_;

    TypesInstruction(byte opcode) {
        opcode_ = opcode;
        argument_ = -1;
        type_ = null;
    }

    TypesInstruction(byte opcode, String type) {
        opcode_ = opcode;
        argument_ = -1;
        type_ = type;
    }

    TypesInstruction(byte opcode, int argument) {
        opcode_ = opcode;
        argument_ = argument;
        type_ = null;
    }

    TypesInstruction(byte opcode, int argument, String type) {
        opcode_ = opcode;
        argument_ = argument;
        type_ = type;
    }

    byte getOpcode() {
        return opcode_;
    }

    int getArgument() {
        return argument_;
    }

    String getType() {
        return type_;
    }

    public String toString() {
        var result = new StringBuilder(TypesOpcode.toString(opcode_));
        if (argument_ != -1) {
            result.append(", ");
            result.append(argument_);
        }
        if (type_ != null) {
            result.append(", ");
            result.append(type_);
        }

        return result.toString();
    }
}
