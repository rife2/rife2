/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

/**
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see ConstrainedBean
 * @since 1.0
 */
public interface TextualIdentifierGenerator<T> {
    void setBean(T bean);

    String generateIdentifier();
}