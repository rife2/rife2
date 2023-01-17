/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations;

/**
 * Interface that should be implemented by classes that support cloneable
 * continuations.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public interface CloneableContinuable extends Cloneable {
    /**
     * When continuations are resumed, they can be cloned to ensure
     * that their state is properly isolated. Implementing this method
     * allows for full customization of the cloning behavior.
     *
     * @return the cloned instance of this continuable object
     * @throws CloneNotSupportedException if the object can't be cloned
     * @see ContinuationConfigRuntime#cloneContinuations
     */
    Object clone()
    throws CloneNotSupportedException;
}
