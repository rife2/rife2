/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

import rife.tools.ExceptionUtils;

import java.util.logging.Logger;

/**
 * Instances of this class detail subjects that were found invalid during
 * validation.
 * <p>Each <code>ValidationError</code> is tied to a specific subject and
 * provides more information through an explicative textual identifier.
 * <p>A collection of commonly used identifiers and implementations are
 * provided as static member variables and static inner classes.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see Validated
 * @since 1.0
 */
public abstract class ValidationError implements Cloneable {
    public static final String IDENTIFIER_MANDATORY = "mandatory";
    public static final String IDENTIFIER_UNIQUENESS = "uniqueness";
    public static final String IDENTIFIER_WRONG_LENGTH = "wrongLength";
    public static final String IDENTIFIER_WRONG_FORMAT = "wrongFormat";
    public static final String IDENTIFIER_NOT_NUMERIC = "notNumeric";
    public static final String IDENTIFIER_UNEXPECTED = "unexpected";
    public static final String IDENTIFIER_INCOMPLETE = "incomplete";
    public static final String IDENTIFIER_INVALID = "invalid";
    public static final String IDENTIFIER_DIFFERENT = "different";

    private final String identifier_;
    private final String subject_;
    private Object erroneousValue_ = null;
    private boolean overridable_ = false;

    /**
     * Creates a new <code>ValidationError</code> instance for the specified
     * identifier and subject.
     * <p>The error will not be automatic overridable.
     *
     * @param identifier a non-<code>null</code> <code>String</code> with the
     *                   textual error identifier
     * @param subject    a non-<code>null</code> <code>String</code> with the
     *                   name of the erroneous subject
     * @since 1.0
     */
    public ValidationError(String identifier, String subject) {
        if (null == identifier) throw new IllegalArgumentException("identifier can't be null");
        if (null == subject) throw new IllegalArgumentException("subject can't be null");

        identifier_ = identifier;
        subject_ = subject;
    }

    /**
     * Creates a new <code>ValidationError</code> instance for the specified
     * identifier and subject.
     *
     * @param identifier  a non-<code>null</code> <code>String</code> with the
     *                    textual error identifier
     * @param subject     a non-<code>null</code> <code>String</code> with the
     *                    name of the erroneous subject
     * @param overridable <code>true</code> to make any other error for the same
     *                    subject override this error, <code>false</code> if this error should
     *                    always be shown
     * @since 1.0
     */
    public ValidationError(String identifier, String subject, boolean overridable) {
        this(identifier, subject);

        overridable_ = overridable;
    }

    /**
     * Returns the textual identifier that categorizes this validation error.
     *
     * @since 1.0
     */
    public final String getIdentifier() {
        return identifier_;
    }

    /**
     * Returns the erroneous subject name of this validation error.
     *
     * @since 1.0
     */
    public final String getSubject() {
        return subject_;
    }

    /**
     * Returns whether this error is overridable for the same subject.
     *
     * @since 1.0
     */
    public final boolean isOverridable() {
        return overridable_;
    }

    /**
     * Stores the erroneous value that caused the validation error.
     * This is optional and should only be done when the erroneous value
     * gives more information from the context in which the validation
     * error occurred.
     *
     * @since 1.0
     */
    public void setErroneousValue(Object erroneousValue) {
        erroneousValue_ = erroneousValue;
    }

    /**
     * Chainable setter to make validation error construction easier
     *
     * @see #setErroneousValue
     * @since 1.0
     */
    public ValidationError erroneousValue(Object erroneousValue) {
        setErroneousValue(erroneousValue);

        return this;
    }

    /**
     * Returns the erroneous value that caused the validation error, if it's present.
     *
     * @since 1.0
     */
    public Object getErroneousValue() {
        return erroneousValue_;
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

    public int hashCode() {
        return identifier_.hashCode() * subject_.hashCode();
    }

    public boolean equals(Object object) {
        if (null == object) {
            return false;
        }

        if (object instanceof ValidationError other_error) {
            return other_error.identifier_.equals(identifier_) &&
                other_error.subject_.equals(subject_);
        }

        return false;
    }

    public static class MANDATORY extends ValidationError {
        public MANDATORY(String subject) {
            super(IDENTIFIER_MANDATORY, subject, true);
        }
    }

    public static class UNIQUENESS extends ValidationError {
        public UNIQUENESS(String subject) {
            super(IDENTIFIER_UNIQUENESS, subject);
        }
    }

    public static class WRONGLENGTH extends ValidationError {
        public WRONGLENGTH(String subject) {
            super(IDENTIFIER_WRONG_LENGTH, subject);
        }
    }

    public static class WRONGFORMAT extends ValidationError {
        public WRONGFORMAT(String subject) {
            super(IDENTIFIER_WRONG_FORMAT, subject);
        }
    }

    public static class NOTNUMERIC extends ValidationError {
        public NOTNUMERIC(String subject) {
            super(IDENTIFIER_NOT_NUMERIC, subject);
        }
    }

    public static class UNEXPECTED extends ValidationError {
        public UNEXPECTED(String subject) {
            super(IDENTIFIER_UNEXPECTED, subject);
        }
    }

    public static class INCOMPLETE extends ValidationError {
        public INCOMPLETE(String subject) {
            super(IDENTIFIER_INCOMPLETE, subject);
        }
    }

    public static class INVALID extends ValidationError {
        public INVALID(String subject) {
            super(IDENTIFIER_INVALID, subject);
        }
    }

    public static class NOTSAMEAS extends ValidationError {
        public NOTSAMEAS(String subject) {
            super(IDENTIFIER_DIFFERENT, subject);
        }
    }
}
