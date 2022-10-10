/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

import java.util.Collection;

import rife.validation.exceptions.ValidationBuilderException;
import rife.template.Template;

public interface ValidationBuilder extends Cloneable {
    String PREFIX_ERROR = "error:";
    String PREFIX_ERRORMESSAGE = "errormessage:";
    String PREFIX_ERRORS = "errors:";
    String PREFIX_MARK = "mark:";
    String PREFIX_MARK_ERROR = "mark:error";

    String ID_ERROR_WILDCARD = "error:*";
    String ID_ERRORMESSAGE = "errormessage";
    String ID_ERRORMESSAGE_WILDCARD = "errormessage:*";
    String ID_ERRORS = "errors";
    String ID_ERRORS_FALLBACK = "errors:";
    String ID_ERRORS_WILDCARD = "errors:*";

    String TAG_ERRORS = "(?=(?<=^" + PREFIX_ERRORS + ")|\\G(?<!^))\\s*(\\w+)\\s*,?(?=[\\w,]+$|$)";
    String TAG_ERRORMESSAGE = "(?=(?<=^" + PREFIX_ERRORMESSAGE + ")|\\G(?<!^))\\s*(\\w+)\\s*,?(?=[\\w,]+$|$)";
    String TAG_MARK = "(?:^" + PREFIX_MARK + "(?:(\\w+):)?|\\G(?<!^))\\s*(\\w+)\\s*,?(?=[\\w,]+$|$)";

    void setFallbackErrorArea(Template template, String message);

    Collection<String> generateValidationErrors(Template template, Collection<ValidationError> errors, Collection<String> onlySubjectsToClear, String prefix);

    Collection<String> generateErrorMarkings(Template template, Collection<ValidationError> errors, Collection<String> onlySubjectsToClear, String prefix)
    throws ValidationBuilderException;

    void removeValidationErrors(Template template, Collection<String> subjects, String prefix);

    void removeErrorMarkings(Template template, Collection<String> subjects, String prefix);

    Object clone();
}
