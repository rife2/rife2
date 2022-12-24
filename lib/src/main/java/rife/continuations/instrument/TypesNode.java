/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations.instrument;

import rife.asm.Label;

import java.util.ArrayList;
import java.util.Collection;

class TypesNode {
    static final int REGULAR = 0;
    static final int EXCEPTION = 1;

    private final ArrayList<TypesInstruction> instructions_ = new ArrayList<>();

    private TypesNode followingNode_ = null;
    private boolean isSuccessor_ = false;
    private TypesSuccessor successors_ = null;

    private int level_ = 0;
    private TypesContext context_ = null;
    private boolean processed_ = false;
    private TypesNode nextToProcess_ = null;
    private TypesNode predecessor_ = null;

    private int sort_ = REGULAR;

    void addInstruction(TypesInstruction instruction) {
        instructions_.add(instruction);
    }

    Collection<TypesInstruction> getInstructions() {
        return instructions_;
    }

    void setSort(int sort) {
        sort_ = sort;
    }

    int getSort() {
        return sort_;
    }

    void setFollowingNode(TypesNode followingNode) {
        followingNode_ = followingNode;
    }

    TypesNode getFollowingNode() {
        return followingNode_;
    }

    void addSuccessor(Label label) {
        TypesSuccessor successor = new TypesSuccessor();

        successor.setLabel(label);
        successor.setNextSuccessor(getSuccessors());

        setSuccessors(successor);
    }

    void setSuccessors(TypesSuccessor successors) {
        successors_ = successors;
    }

    TypesSuccessor getSuccessors() {
        return successors_;
    }

    void setNextToProcess(TypesNode nextNode) {
        nextToProcess_ = nextNode;
    }

    TypesNode getNextToProcess() {
        return nextToProcess_;
    }

    void setPredecessor(boolean isSuccessor, TypesNode predecessor) {
        isSuccessor_ = isSuccessor;
        predecessor_ = predecessor;

        if (isSuccessor_) {
            level_ = predecessor_.getLevel() + 1;
        } else {
            level_ = predecessor_.getLevel();
        }
    }

    TypesNode getPredecessor() {
        return predecessor_;
    }

    boolean getIsSuccessor() {
        return isSuccessor_;
    }

    void setProcessed(boolean processed) {
        processed_ = processed;
    }

    boolean isProcessed() {
        return processed_;
    }

    void setContext(TypesContext previousContext) {
        context_ = previousContext;
    }

    TypesContext getContext() {
        return context_;
    }

    int getLevel() {
        return level_;
    }
}

