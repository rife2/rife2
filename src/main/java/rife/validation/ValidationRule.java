/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

public interface ValidationRule extends Cloneable {
    boolean validate();

    String getSubject();

    ValidationError getError();

    Object getBean();

    <T extends ValidationRule> T setBean(Object bean);

    Object clone()
    throws CloneNotSupportedException;
}
