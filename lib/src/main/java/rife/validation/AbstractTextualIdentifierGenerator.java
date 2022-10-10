/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

/**
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see TextualIdentifierGenerator
 * @since 1.0
 */
public abstract class AbstractTextualIdentifierGenerator<T> implements TextualIdentifierGenerator<T> {
    protected T bean_ = null;

    public void setBean(T bean) {
        bean_ = bean;
    }

    public T getBean() {
        return bean_;
    }

    public abstract String generateIdentifier();
}

