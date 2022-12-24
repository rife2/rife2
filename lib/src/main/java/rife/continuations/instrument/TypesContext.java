/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations.instrument;

import rife.asm.Type;
import rife.tools.ExceptionUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

class TypesContext implements Cloneable {
    public static final String CAT1_BOOLEAN = "1Z";
    public static final String CAT1_CHAR = "1C";
    public static final String CAT1_FLOAT = "1F";
    public static final String CAT1_BYTE = "1B";
    public static final String CAT1_SHORT = "1S";
    public static final String CAT1_INT = "1I";
    public static final String CAT1_ADDRESS = "1A";
    public static final String CAT2_DOUBLE = "2D";
    public static final String CAT2_LONG = "2J";
    public static final String ARRAY_BOOLEAN = "[Z";
    public static final String ARRAY_CHAR = "[C";
    public static final String ARRAY_FLOAT = "[F";
    public static final String ARRAY_BYTE = "[B";
    public static final String ARRAY_SHORT = "[S";
    public static final String ARRAY_INT = "[I";
    public static final String ARRAY_DOUBLE = "[D";
    public static final String ARRAY_LONG = "[J";
    public static final String TYPE_NULL = "NULL";

    private Map<Integer, String> vars_;
    private Stack<String> stack_;

    private int sort_ = TypesNode.REGULAR;

    private String debugIndent_ = null;

    TypesContext() {
        vars_ = new HashMap<>();
        stack_ = new Stack<>();
    }

    TypesContext(Map<Integer, String> vars, Stack<String> stack) {
        vars_ = vars;
        stack_ = stack;
    }

    Map<Integer, String> getVars() {
        return vars_;
    }

    Stack<String> getStack() {
        return stack_;
    }

    boolean hasVar(int var) {
        return vars_.containsKey(var);
    }

    String getVar(int var) {
        return vars_.get(var);
    }

    void setVar(int var, String type) {
        vars_.put(var, type);
    }

    int getVarType(int var) {
        var type = getVar(var);
        if (CAT1_INT == type) {
            return Type.INT;
        } else if (CAT1_FLOAT == type) {
            return Type.FLOAT;
        } else if (CAT2_LONG == type) {
            return Type.LONG;
        } else if (CAT2_DOUBLE == type) {
            return Type.DOUBLE;
        } else {
            return Type.OBJECT;
        }
    }

    String peek() {
        return stack_.peek();
    }

    String pop() {
        String result = null;
        if (stack_.size() > 0) {
            result = stack_.pop();
        }
        printStack();
        return result;
    }

    void push(String type) {
        stack_.push(type);
        printStack();
    }

    Stack<String> getStackClone() {
        return (Stack<String>) stack_.clone();
    }

    void cloneVars() {
        vars_ = new HashMap<Integer, String>(vars_);
    }

    void setSort(int type) {
        sort_ = type;
    }

    int getSort() {
        return sort_;
    }

    void printStack() {
        if (ContinuationDebug.LOGGER.isLoggable(Level.FINEST)) {
            if (0 == stack_.size()) {
                ContinuationDebug.LOGGER.finest(debugIndent_ + "  | empty");
            } else {
                for (var i = 0; i < stack_.size(); i++) {
                    ContinuationDebug.LOGGER.finest(debugIndent_ + "  | " + i + " : " + stack_.get(i));
                }
            }
        }
    }

    void setDebugIndent(String debugIndent) {
        debugIndent_ = debugIndent;
    }

    TypesContext clone(TypesNode node) {
        var new_context = new TypesContext(new HashMap<Integer, String>(vars_), (Stack<String>) stack_.clone());
        new_context.setSort(node.getSort());
        return new_context;
    }

    public TypesContext clone() {
        TypesContext new_context = null;
        try {
            new_context = (TypesContext) super.clone();
        } catch (CloneNotSupportedException e) {
            // this should never happen
            Logger.getLogger("rife.continuations").severe(ExceptionUtils.getExceptionStackTrace(e));
        }

        new_context.vars_ = new HashMap<Integer, String>(vars_);
        new_context.stack_ = (Stack<String>) stack_.clone();

        return new_context;
    }
}
