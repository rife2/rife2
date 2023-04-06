/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools.exceptions;

/**
 * This is a marker interface to make it possible to throw runtime exceptions
 * that have to be treated as expected and 'positive'. The purpose is for
 * code that receives them to act correspondingly. For instance, if this is
 * used inside a transaction without forcing a rollback, the transaction
 * handler should commit when the exception is caught and afterwards rethrow
 * the exception.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public interface ControlFlowRuntimeException {
}
