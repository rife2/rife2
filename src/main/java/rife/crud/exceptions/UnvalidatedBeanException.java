/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud.exceptions;

import java.io.Serial;

public class UnvalidatedBeanException extends CrudException {
    @Serial private static final long serialVersionUID = 6822090446651840142L;

    private final Class beanClass_;

    public UnvalidatedBeanException(Class beanClass) {
        super("The bean '" + beanClass.getName() + "' doesn't validate anything, while an administration reports what " +
              "it refuses through the validation of the instance that it was given. A value that a submission can't be " +
              "read into would be left out with nothing able to say so, and the operation would report that it " +
              "succeeded. Let the bean extend 'rife.validation.MetaData', or give it a metadata class of its own and " +
              "make sure that the merging of it is active.");

        beanClass_ = beanClass;
    }

    public Class getBeanClass() {
        return beanClass_;
    }
}
