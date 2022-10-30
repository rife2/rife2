/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

/**
 * This interface has to be implemented by all classes that provide a context
 * in which <code>Validated</code> bean instances can be validated.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see ValidationError
 * @since 1.0
 */
public interface ValidationContext {
    /**
     * Validates a <code>Validated</code> in this context.
     * <p>This method is not supposed to reset the validation errors or to
     * start the validation from scratch, but it's intended to add additional
     * errors to an existing collection.
     *
     * @param validated the <code>Validated</code> instance that will be validated
     * @since 1.0
     */
    void validate(Validated validated);
}
