/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations;

/**
 * Interface that needs to be implemented by classes that should support
 * continuations functionalities and become resumable.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public interface ContinuableObject extends Cloneable {
    /**
     * When continuations are resumed, they are by default cloned to ensure
     * that their state is properly isolated. Implementing this method
     * allows for full customization of the cloning behavior.
     *
     * @return the cloned instance of this continuable object
     * @throws CloneNotSupportedException
     * @see ContinuationConfigRuntime#cloneContinuations
     */
    Object clone()
    throws CloneNotSupportedException;
}
