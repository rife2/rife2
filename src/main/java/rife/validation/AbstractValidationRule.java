/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

import java.util.logging.Logger;

import rife.tools.ExceptionUtils;

public abstract class AbstractValidationRule implements ValidationRule {
    private Object bean_ = null;
    private String cachedSubject_ = null;

    protected AbstractValidationRule() {
    }

    public abstract boolean validate();

    public abstract ValidationError getError();

    public String getSubject() {
        if (null == cachedSubject_) {
            cachedSubject_ = getError().getSubject();
        }

        return cachedSubject_;
    }

    public <T extends ValidationRule> T setBean(Object bean) {
        bean_ = bean;

        return (T) this;
    }

    public Object getBean() {
        return bean_;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // this should never happen
            Logger.getLogger("rife.validation").severe(ExceptionUtils.getExceptionStackTrace(e));
            return null;
        }
    }
}
