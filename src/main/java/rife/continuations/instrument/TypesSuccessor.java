/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations.instrument;

import rife.asm.Label;

class TypesSuccessor {
    private Label label_ = null;
    private TypesSuccessor nextSuccessor_ = null;

    void setNextSuccessor(TypesSuccessor nextSuccessor) {
        nextSuccessor_ = nextSuccessor;
    }

    TypesSuccessor getNextSuccessor() {
        return nextSuccessor_;
    }

    void setLabel(Label successor) {
        label_ = successor;
    }

    Label getLabel() {
        return label_;
    }
} 

