/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud;

import rife.crud.exceptions.*;
import rife.engine.Context;
import rife.template.Template;
import rife.tools.BeanUtils;
import rife.tools.ClassUtils;
import rife.tools.Convert;
import rife.tools.exceptions.ConversionException;
import rife.tools.exceptions.BeanUtilsException;
import rife.validation.ConstrainedProperty;
import rife.validation.Validated;
import rife.validation.ValidationError;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Generates the form fields of an entity from the constraints of its
 * properties.
 * <p>The type of a property and its constraints decide which field is
 * generated: a property that is constrained to a list becomes a select, a
 * boolean becomes a checkbox, a long text becomes a textarea, a date whose
 * format is exactly what a native date control submits becomes that
 * control, and the lengths and the mandatory properties end up as the
 * corresponding attributes, so that the browser validates what it can
 * before anything is submitted.
 * <p>A field of a form that was submitted shows what was typed into it
 * instead of what the instance holds, so that a value that couldn't be
 * converted comes back as it was entered and can be corrected.
 * <p>All the markup comes from the {@code crud.fields} template, which
 * makes the generated fields as easy to restyle as the rest of the
 * administration.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.10
 */
class CrudFormBuilder {
    private final CrudEntity<?> entity_;
    private final Template fields_;
    private final boolean adding_;
    private final Context submitted_;
    private Map<String, String> conflicts_ = Map.of();

    CrudFormBuilder(CrudEntity<?> entity, Template fields, boolean adding) {
        this(entity, fields, adding, null);
    }

    CrudFormBuilder(CrudEntity<?> entity, Template fields, boolean adding, Context submitted) {
        entity_ = entity;
        fields_ = fields;
        adding_ = adding;
        submitted_ = submitted;
    }

    // shows, underneath every field this names, what is stored right now,
    // so that an edit which ran into somebody else's can be reconciled
    // knowingly
    void conflicts(Map<String, String> conflicts) {
        conflicts_ = conflicts;
    }

    String generateFields(Object instance) {
        var errors = errorsBySubject(instance);
        var result = new StringBuilder();
        var properties = adding_ ? entity_.getAddableProperties() : entity_.getEditableProperties();
        var sections = entity_.getOptions().getSections();
        var sectioned = new HashSet<String>();
        for (var section : sections.values()) {
            sectioned.addAll(section);
        }
        for (var property : properties) {
            if (!sectioned.contains(property)) {
                result.append(field(instance, property, errors.get(property)));
            }
        }
        for (var section : sections.entrySet()) {
            var fields = new StringBuilder();
            // a section lists its fields in the order that it wants them,
            // and the ones that this form leaves out are left out of it too
            for (var property : section.getValue()) {
                if (properties.contains(property)) {
                    fields.append(field(instance, property, errors.get(property)));
                }
            }
            if (fields.isEmpty()) {
                continue;
            }
            fields_.setValueEncoded("crud:sectionLabel", section.getKey());
            fields_.setValue("crud:sectionFields", fields.toString());
            result.append(fields_.getBlock("section"));
            fields_.removeValues(java.util.List.of("crud:sectionLabel", "crud:sectionFields"));
        }
        return result.toString();
    }

    String generateErrors(Object instance) {
        return generateErrors(instance, null);
    }

    // generates the summary of everything that is wrong with a submission;
    // a message that doesn't belong to a property is shown as well, so that
    // a database which refuses a submission still says so on a bean that
    // doesn't validate anything itself
    String generateErrors(Object instance, String message) {
        var errors = instance instanceof Validated validated ? validated.getValidationErrors() : null;
        if ((errors == null || errors.isEmpty()) && null == message) {
            return "";
        }

        fields_.blankValue("crud:errorItems");
        if (message != null) {
            fields_.setValueEncoded("crud:message", message);
            fields_.appendBlock("crud:errorItems", "errorItem");
        }
        if (errors != null) {
            for (var error : errors) {
                fields_.setValueEncoded("crud:message", message(error));
                fields_.appendBlock("crud:errorItems", "errorItem");
            }
        }
        return fields_.getBlock("errors");
    }

    // an error names its subject, which is the name of a property unless
    // the constraints gave it one of its own, so the subjects are translated
    // back before a field is looked up by them
    private Map<String, ValidationError> errorsBySubject(Object instance) {
        var result = new HashMap<String, ValidationError>();
        if (instance instanceof Validated validated) {
            for (var error : validated.getValidationErrors()) {
                result.putIfAbsent(entity_.propertyOfSubject(error.getSubject()), error);
            }
        }
        return result;
    }

    private String message(ValidationError error) {
        var subject = label(entity_.propertyOfSubject(error.getSubject()));
        return switch (error.getIdentifier()) {
            case ValidationError.IDENTIFIER_MANDATORY -> subject + " is required.";
            case ValidationError.IDENTIFIER_UNIQUENESS -> subject + " is already taken.";
            case ValidationError.IDENTIFIER_WRONG_LENGTH -> subject + " has the wrong length.";
            case ValidationError.IDENTIFIER_WRONG_FORMAT -> subject + " has the wrong format.";
            case ValidationError.IDENTIFIER_INVALID -> subject + " isn't valid.";
            case ValidationError.IDENTIFIER_NOT_NUMERIC -> subject + " has to be a number.";
            default -> subject + " isn't accepted.";
        };
    }

    private String field(Object instance, String property, ValidationError error) {
        var constraint = entity_.getConstraint(property);
        var type = propertyType(property);
        // some types are read back from their own literal instead of through
        // the format of a property, so those are shown and submitted that way
        // while everything else uses what the format writes
        var literal = !BeanUtils.parsesWithFormat(type);
        var value = value(instance, property, !literal);

        fields_.setValueEncoded("crud:property", property);
        fields_.setValueEncoded("crud:label", label(property));

        if (constraint != null && constraint.isNotNull()) {
            fields_.setBlock("crud:required", "required");
        } else {
            fields_.blankValue("crud:required");
        }

        // the help text explains what a property is for, underneath the
        // field that enters it, since the constraints only say what a value
        // has to be
        var help = entity_.getOptions().help(property);
        if (help != null) {
            fields_.setValueEncoded("crud:helpText", help);
            fields_.setBlock("crud:help", "help");
        } else {
            fields_.blankValue("crud:help");
        }

        if (constraint != null && constraint.isInList()) {
            fields_.setValue("crud:control", select(constraint, type, value, constraint.getInList()));
        } else if (type != null && type.isEnum()) {
            // an enum property can only hold its own constants, so they're
            // offered instead of being typed out
            fields_.setValue("crud:control", select(constraint, type, value, ClassUtils.getEnumClassValues(type)));
        } else if (boolean.class == type || (Boolean.class == type && constraint != null && constraint.isNotNull())) {
            fields_.setValue("crud:control", checkbox(value));
        } else if (Boolean.class == type) {
            // a boxed boolean that may be null has three states to tell
            // apart, and an unticked checkbox doesn't say which of the two
            // remaining ones is meant
            fields_.setValue("crud:control", choice(constraint, type, value));
        } else if (isLongText(constraint, type)) {
            fields_.setValue("crud:control", textarea(constraint, value));
        } else {
            fields_.setValue("crud:control", input(constraint, type, value));
        }

        if (error != null) {
            fields_.setBlock("crud:invalid", "invalid");
            fields_.setValueEncoded("crud:message", message(error));
            fields_.setBlock("crud:error", "error");
        } else {
            fields_.blankValue("crud:invalid");
            fields_.blankValue("crud:error");
        }

        if (conflicts_.containsKey(property)) {
            var stored = conflicts_.get(property);
            if (stored.isEmpty()) {
                fields_.setBlock("crud:conflict", "conflict:empty");
            } else {
                fields_.setValueEncoded("crud:conflictValue", stored);
                fields_.setBlock("crud:conflict", "conflict");
            }
        } else {
            fields_.blankValue("crud:conflict");
        }

        var result = fields_.getBlock("field");
        fields_.removeValues(java.util.List.of("crud:property", "crud:label", "crud:control",
            "crud:required", "crud:invalid", "crud:error", "crud:message", "crud:help", "crud:helpText",
            "crud:conflict", "crud:conflictValue"));
        return result;
    }

    private String attributes(ConstrainedProperty constraint, Class<?> type) {
        var result = new StringBuilder();
        // a field of one character can only submit one, whatever the
        // constraints say about how long its text is allowed to be, and text
        // that holds more is refused instead of being shortened
        Integer maxlength = null;
        if (char.class == type || Character.class == type) {
            maxlength = 1;
        } else if (constraint != null && constraint.hasMaxLength()) {
            maxlength = constraint.getMaxLength();
        }
        if (maxlength != null) {
            fields_.setValue("crud:maxlength", maxlength);
            result.append(fields_.getBlock("attribute:maxlength"));
        }
        if (constraint != null && constraint.isNotNull()) {
            result.append(fields_.getBlock("attribute:required"));
        }
        return result.toString();
    }

    private String select(ConstrainedProperty constraint, Class<?> type, String value, String[] options) {
        fields_.blankValue("crud:options");
        var mandatory = constraint != null && constraint.isNotNull();
        var empty = false;
        if (!mandatory) {
            fields_.appendBlock("crud:options", "option:empty");
            empty = true;
        }
        for (var option : options) {
            // a null in the list stands for no choice at all, and a
            // property that has to hold something isn't offered that choice
            // since it can't keep it
            if (null == option) {
                if (!empty && !mandatory) {
                    fields_.appendBlock("crud:options", "option:empty");
                    empty = true;
                }
                continue;
            }
            // the label is what the format writes and the value is what
            // comes back, which is the same thing except for the types that
            // are read from their own literal
            var written = writtenOption(constraint, type, option);
            var submitted = BeanUtils.parsesWithFormat(type) ? written : option;
            fields_.setValueEncoded("crud:optionValue", submitted);
            fields_.setValueEncoded("crud:optionLabel", written);
            fields_.appendBlock("crud:options", submitted.equals(value) ? "option:selected" : "option");
        }
        return fields_.getBlock("select");
    }

    // generates the control of a boolean that can also hold nothing
    private String choice(ConstrainedProperty constraint, Class<?> type, String value) {
        fields_.blankValue("crud:options");
        fields_.appendBlock("crud:options", "option:empty");
        for (var option : new String[]{"true", "false"}) {
            // a boolean submits its own literal, while the format of a
            // property decides how it reads
            var label = writtenOption(constraint, type, option);
            if (label.equals(option)) {
                label = "true".equals(option) ? "Yes" : "No";
            }
            fields_.setValueEncoded("crud:optionValue", option);
            fields_.setValueEncoded("crud:optionLabel", label);
            fields_.appendBlock("crud:options", option.equals(value) ? "option:selected" : "option");
        }
        return fields_.getBlock("select");
    }

    // writes one of the values that a property is constrained to the way
    // its format writes it, since that's what the option displays; the types
    // that are read from their own literal submit something else, because a
    // format that writes them produces text that can't be read back as the
    // value it was written from
    private String writtenOption(ConstrainedProperty constraint, Class<?> type, String option) {
        if (null == constraint || !constraint.isFormatted()) {
            return option;
        }
        try {
            // the option is read the way a submission reads that same text,
            // so that the property and its option agree for a type that only
            // its own format reads
            return BeanUtils.formatPropertyValue(BeanUtils.parseInputValue(option, type, constraint), constraint);
        } catch (ConversionException e) {
            // a value that the property can't hold is left as it was listed
            return option;
        }
    }

    private String checkbox(String value) {
        var checked = "true".equalsIgnoreCase(value) || "1".equals(value);
        if (checked) {
            fields_.setBlock("crud:checked", "checked");
        } else {
            fields_.blankValue("crud:checked");
        }
        return fields_.getBlock("checkbox");
    }

    private String textarea(ConstrainedProperty constraint, String value) {
        fields_.setValue("crud:attributes", attributes(constraint, null));
        fields_.setValueEncoded("crud:value", value);
        return fields_.getBlock("textarea");
    }

    private String input(ConstrainedProperty constraint, Class<?> type, String value) {
        var attributes = attributes(constraint, type);
        var input_type = inputType(constraint, type);
        // a number input only accepts whole numbers unless it's told which
        // steps it takes
        if ("number".equals(input_type) && isFractional(type)) {
            attributes += fields_.getBlock("attribute:step");
        }
        fields_.setValue("crud:attributes", attributes);
        fields_.setValueEncoded("crud:value", value);
        fields_.setValue("crud:type", input_type);
        return fields_.getBlock("input");
    }

    private String inputType(ConstrainedProperty constraint, Class<?> type) {
        if (constraint != null && constraint.isEmail()) {
            return "email";
        }
        if (constraint != null && constraint.isUrl()) {
            return "url";
        }
        var date_type = nativeDateType(constraint, type);
        if (date_type != null) {
            return date_type;
        }
        // a formatted number is shown the way its format writes it, which a
        // number field refuses to hold, since the groupings and the symbols
        // of it aren't a number to a browser
        if (constraint != null && constraint.isFormatted()) {
            return "text";
        }
        if (Number.class.isAssignableFrom(wrap(type)) && !CharSequence.class.isAssignableFrom(type)) {
            return "number";
        }
        return "text";
    }

    // a native date control shows and submits its own format, whatever the
    // page put into it, so one is only offered when the property's format is
    // exactly the format of the control; the default date format differs
    // from every one of them by its separator, so a property without a
    // format of its own keeps its text field
    private static String nativeDateType(ConstrainedProperty constraint, Class<?> type) {
        if (null == constraint || !constraint.isFormatted() || !isDateType(type)) {
            return null;
        }
        if (!(constraint.getFormat() instanceof SimpleDateFormat format)) {
            return null;
        }
        return switch (format.toPattern()) {
            case "yyyy-MM-dd" -> "date";
            case "HH:mm" -> "time";
            case "yyyy-MM-dd'T'HH:mm" -> "datetime-local";
            default -> null;
        };
    }

    private static boolean isDateType(Class<?> type) {
        return type != null &&
               (Date.class.isAssignableFrom(type) ||
                LocalDate.class == type || LocalTime.class == type ||
                LocalDateTime.class == type || Instant.class == type);
    }

    // checks whether a property still carries what a new instance gives it,
    // since a primitive has that value instead of being null
    private boolean isUnset(Object instance, String property, Object value) {
        if (null == value) {
            return true;
        }
        if (!propertyType(property).isPrimitive()) {
            return false;
        }
        try {
            var pristine = entity_.getBeanClass().getDeclaredConstructor().newInstance();
            return value.equals(BeanUtils.getPropertyValue(pristine, property));
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isFractional(Class<?> type) {
        return float.class == type || Float.class == type ||
               double.class == type || Double.class == type ||
               java.math.BigDecimal.class.isAssignableFrom(type);
    }

    private boolean isLongText(ConstrainedProperty constraint, Class<?> type) {
        if (!CharSequence.class.isAssignableFrom(type)) {
            return false;
        }
        return constraint != null && constraint.hasMaxLength() && constraint.getMaxLength() > 255;
    }

    private static Class<?> wrap(Class<?> type) {
        if (int.class == type) return Integer.class;
        if (long.class == type) return Long.class;
        if (short.class == type) return Short.class;
        if (byte.class == type) return Byte.class;
        if (double.class == type) return Double.class;
        if (float.class == type) return Float.class;
        return type;
    }

    private Class<?> propertyType(String property) {
        try {
            var type = BeanUtils.getPropertyType(entity_.getBeanClass(), property);
            return type == null ? String.class : type;
        } catch (BeanUtilsException e) {
            throw new PropertyAccessException(entity_.getBeanClass(), property, e);
        }
    }


    // retrieves what a field shows for a property: a value is shown the way
    // its format writes it, except where the field has to submit exactly
    // what it was given, since a format that groups or decorates a value
    // writes something that isn't the value anymore
    private String value(Object instance, String property, boolean formatted) {
        var constraint = entity_.getConstraint(property);
        // a value that couldn't be converted never reached the instance, so
        // the form shows what was typed instead of what it still holds
        if (submitted_ != null) {
            var value = submitted_.parameter(property);
            if (value != null) {
                return value;
            }
        }
        try {
            var value = BeanUtils.getPropertyValue(instance, property);
            // a stored value was stored that way on purpose, only a new
            // instance falls back to the default of its constraints
            // only a form that hasn't been submitted yet falls back to it,
            // since an unchecked checkbox sends nothing and would otherwise
            // get the default back after every other field's mistake
            if (adding_ && submitted_ == null &&
                constraint != null && constraint.hasDefaultValue() && isUnset(instance, property, value)) {
                value = constraint.getDefaultValue();
            }
            if (value == null) {
                return "";
            }
            return BeanUtils.formatPropertyValue(value, formatted ? constraint : null);
        } catch (BeanUtilsException e) {
            // a value that can't be retrieved has to be reported instead of
            // being shown as empty, since an empty edit field would replace
            // what is actually stored
            throw new PropertyAccessException(entity_.getBeanClass(), property, e);
        }
    }

    String label(String property) {
        return entity_.getPropertyLabel(property);
    }
}
