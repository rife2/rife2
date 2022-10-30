/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

public class InitializedBeanImpl {
    private String string_ = "default";
    private StringBuffer stringbuffer_ = null;
    private int int_ = -1;
    private Integer integer_ = null;
    private char char_ = 'i';
    private Character character_ = 'k';

    public InitializedBeanImpl() {
    }

    public void setString(String string) {
        string_ = string;
    }

    public String getString() {
        return string_;
    }

    public void setStringbuffer(StringBuffer stringbuffer) {
        stringbuffer_ = stringbuffer;
    }

    public StringBuffer getStringbuffer() {
        return stringbuffer_;
    }

    public void setInt(int integer) {
        int_ = integer;
    }

    public int getInt() {
        return int_;
    }

    public void setInteger(Integer integer) {
        integer_ = integer;
    }

    public Integer getInteger() {
        return integer_;
    }

    public void setChar(char character) {
        char_ = character;
    }

    public char getChar() {
        return char_;
    }

    public void setCharacter(Character character) {
        character_ = character;
    }

    public Character getCharacter() {
        return character_;
    }
}

