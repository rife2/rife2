/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations;

public class ContinuableSupport {
    public final void pause() {
        // this should not be triggered, since bytecode rewriting will replace this
        // method call with the appropriate logic
        throw new UnsupportedOperationException();
    }

    public final void stepBack() {
        // this should not be triggered, since bytecode rewriting will replace this
        // method call with the appropriate logic
        throw new UnsupportedOperationException();
    }

    public final Object call(Class target) {
        // this should not be triggered, since bytecode rewriting will replace this
        // method call with the appropriate logic
        throw new UnsupportedOperationException();
    }

    public final void answer() {
        // this should not be triggered, since bytecode rewriting will replace this
        // method call with the appropriate logic
        throw new UnsupportedOperationException();
    }

    public final void answer(Object answer) {
        // this should not be triggered, since bytecode rewriting will replace this
        // method call with the appropriate logic
        throw new UnsupportedOperationException();
    }
}
