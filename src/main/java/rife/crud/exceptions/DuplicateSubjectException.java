/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud.exceptions;

import java.io.Serial;

public class DuplicateSubjectException extends CrudException {
    @Serial private static final long serialVersionUID = 5188104733962063721L;

    private final Class beanClass_;
    private final String subject_;
    private final String identifier_;
    private final String property_;

    public DuplicateSubjectException(Class beanClass, String subject, String identifier, String property) {
        super("The properties '" + identifier + "' and '" + property + "' of bean '" + beanClass.getName() +
              "' both report what is wrong with them as '" + subject + "', which leaves an addition unable to tell " +
              "the identifier that it provides itself apart from a value that somebody has to correct, give one of " +
              "them a subject of its own.");

        beanClass_ = beanClass;
        subject_ = subject;
        identifier_ = identifier;
        property_ = property;
    }

    public Class getBeanClass() {
        return beanClass_;
    }

    public String getSubject() {
        return subject_;
    }

    public String getIdentifier() {
        return identifier_;
    }

    public String getProperty() {
        return property_;
    }
}
