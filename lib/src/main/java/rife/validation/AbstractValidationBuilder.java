/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

import java.util.*;
import java.util.logging.Logger;

import rife.validation.exceptions.MissingMarkingBlockException;
import rife.validation.exceptions.ValidationBuilderException;
import rife.template.InternalValue;
import rife.template.Template;
import rife.template.exceptions.TemplateException;
import rife.tools.ExceptionUtils;

public abstract class AbstractValidationBuilder implements ValidationBuilder {
    public void setFallbackErrorArea(Template template, String message) {
        if (null == template) {
            return;
        }

        if (null == message) {
            message = "";
        }

        if (template.hasValueId(ID_ERRORS) &&
            template.hasBlock(ID_ERRORS_FALLBACK)) {
            if (template.hasValueId(ID_ERRORMESSAGE) &&
                template.hasBlock(ID_ERRORMESSAGE_WILDCARD)) {
                template.setValue(ID_ERRORMESSAGE, message);
                template.setBlock(ID_ERRORS, ID_ERRORMESSAGE_WILDCARD);
                template.setBlock(ID_ERRORS_WILDCARD, ID_ERRORS_FALLBACK);

                template.removeValue(ID_ERRORMESSAGE);
                template.removeValue(ID_ERRORS);
            } else {
                template.setValue(ID_ERRORS, message);
                template.setBlock(ID_ERRORS_WILDCARD, ID_ERRORS_FALLBACK);

                template.removeValue(ID_ERRORS);
            }
        } else if (template.hasValueId(ID_ERRORS) &&
            template.hasBlock(ID_ERRORS_WILDCARD)) {
            if (template.hasValueId(ID_ERRORMESSAGE) &&
                template.hasBlock(ID_ERRORMESSAGE_WILDCARD)) {
                template.setValue(ID_ERRORMESSAGE, message);
                template.setBlock(ID_ERRORS, ID_ERRORMESSAGE_WILDCARD);
                template.setBlock(ID_ERRORS_WILDCARD, ID_ERRORS_WILDCARD);

                template.removeValue(ID_ERRORMESSAGE);
                template.removeValue(ID_ERRORS);
            } else {
                template.setValue(ID_ERRORS, message);
                template.setBlock(ID_ERRORS_WILDCARD, ID_ERRORS_WILDCARD);

                template.removeValue(ID_ERRORS);
            }
        } else {
            template.setValue(ID_ERRORS_WILDCARD, message);
        }
    }

    public Collection<String> generateValidationErrors(Template template, Collection<ValidationError> errors, Collection<String> onlySubjectsToClear, String prefix) {
        if (null == template ||
            null == errors ||
            0 == errors.size()) {
            return Collections.emptyList();
        }

        var set_values = new ArrayList<String>();

        // adapt for subject prefixes
        if (prefix != null &&
            onlySubjectsToClear != null) {
            var prefixed_subjects = new ArrayList<String>();
            for (var property : onlySubjectsToClear) {
                prefixed_subjects.add(prefix + property);
            }
            onlySubjectsToClear = prefixed_subjects;
        }

        // check if validation errors are already present in the bean,
        // and generate the formatted errors
        var has_wildcard_errors_block = template.hasBlock(ID_ERRORS_WILDCARD);
        var has_fallback_errors_block = template.hasBlock(ID_ERRORS_FALLBACK);

        var has_fallback_errors_value = template.hasValueId(ID_ERRORS_WILDCARD);
        InternalValue fallback_errors_construction = null;
        if (has_fallback_errors_value) {
            template.removeValue(ID_ERRORS_WILDCARD);
            fallback_errors_construction = template.createInternalValue();
        }

        // Reorder the filtered error values so that they're ordered according
        // to the number of properties they declare. Those with the same
        // number of properties will be ordered according to their order
        // of declaration.
        // Also, all the content from the filtered values will be cleared to
        // ensure that errors from previous submissions will not prevail.
        var filtered_error_values = template.getFilteredValues(TAG_ERRORS);
        ArrayList<List<String[]>> sorted_error_values = null;
        if (filtered_error_values.size() > 0) {
            sorted_error_values = new ArrayList<List<String[]>>();

            List<String[]> error_values;
            for (var filtered_value : filtered_error_values) {
                // only clear the filtered value if one of the subjects is
                // validated
                if (null == onlySubjectsToClear) {
                    template.removeValue(filtered_value[0]);
                } else {
                    for (var i = 1; i < filtered_value.length; i++) {
                        if (onlySubjectsToClear.contains(filtered_value[i])) {
                            template.removeValue(filtered_value[0]);
                            break;
                        }
                    }
                }

                if (filtered_value.length - 1 < sorted_error_values.size()) {
                    error_values = sorted_error_values.get(filtered_value.length - 1);
                } else {
                    error_values = null;
                    while (!(filtered_value.length - 1 < sorted_error_values.size())) {
                        sorted_error_values.add(null);
                    }
                }
                if (null == error_values) {
                    error_values = new ArrayList<String[]>();
                    sorted_error_values.set(filtered_value.length - 1, error_values);
                }
                error_values.add(filtered_value);
            }
        }

        // Reorder the filtered error blocks so that they're ordered according
        // to the number of properties they declare. Those with the same
        // number of properties will be ordered according to their order
        // of declaration.
        var filtered_error_blocks = template.getFilteredBlocks(TAG_ERRORS);
        ArrayList<List<String[]>> sorted_error_blocks = null;
        if (filtered_error_blocks.size() > 0) {
            sorted_error_blocks = new ArrayList<List<String[]>>();

            List<String[]> error_blocks;
            for (var filtered_block : filtered_error_blocks) {
                if (filtered_block.length - 1 < sorted_error_blocks.size()) {
                    error_blocks = sorted_error_blocks.get(filtered_block.length - 1);
                } else {
                    error_blocks = null;
                    while (!(filtered_block.length - 1 < sorted_error_blocks.size())) {
                        sorted_error_blocks.add(null);
                    }
                }
                if (null == error_blocks) {
                    error_blocks = new ArrayList<String[]>();
                    sorted_error_blocks.set(filtered_block.length - 1, error_blocks);
                }
                error_blocks.add(filtered_block);
            }
        }

        // Re-arrange the filtered blocks to be able to easily select the
        // block id that corresponds to an error value.
        // The block have been sorted so that the most specific one
        // (less properties) is considered before a more general one
        // (more properties). When they have the same specificity they
        // are processed according to their order of declaration.
        // A block id with 'property1' will thus gain precedence
        // over a block id with 'property1,property2', but when only the
        // latter is defined it will be used even if either property
        // has an error individually.
        LinkedHashMap<String, ArrayList<String>> block_properties_mapping = null;
        if (sorted_error_blocks != null) {
            block_properties_mapping = new LinkedHashMap<String, ArrayList<String>>();

            ArrayList<String> block_properties;

            for (var categorized_error_blocks : sorted_error_blocks) {
                if (null == categorized_error_blocks) {
                    continue;
                }

                for (var filtered_block : categorized_error_blocks) {
                    block_properties = new ArrayList<>();
                    block_properties_mapping.put(filtered_block[0], block_properties);

                    block_properties.addAll(Arrays.asList(filtered_block).subList(1, filtered_block.length));
                }
            }
        }

        var invalid_subjects = collectSubjects(errors, prefix);

        // Go over the error values according to their order of importance.
        // Values with a broader scope (more properties) get precedence of
        // those with a narrower scope (less properties) and inside the
        // same scope level they are handled according to their order
        // of declaration.
        // For each value that is used to report errors in, an internal
        // construction variable is created. It is there that the error
        // messages will be appended.
        // The formatting block that corresponds best to the used value
        // is also determined and stored for later retrieval during
        // the template construction.
        HashMap<String, InternalValue> values_construction = null;
        HashMap<String, String> property_value_mapping = null;
        HashMap<String, String> values_block_mapping = null;
        if (filtered_error_values.size() > 0) {
            values_construction = new HashMap<>();
            property_value_mapping = new HashMap<>();
            values_block_mapping = new HashMap<>();

            List<String[]> error_values;

            for (var i = sorted_error_values.size() - 1; i >= 0; i--) {
                error_values = sorted_error_values.get(i);
                if (null == error_values) {
                    continue;
                }

                for (var filtered_value : error_values) {
                    // check if the filtered value contains only subjects
                    // that are invalid
                    var use_value = true;
                    for (var j = 1; j < filtered_value.length; j++) {
                        if (!invalid_subjects.contains(filtered_value[j])) {
                            use_value = false;
                            break;
                        }
                    }

                    // If the value can be used, register its id for
                    // all the declared subjects. If the subject is already
                    // bound to another value, it is simply skipped.
                    if (use_value) {
                        var tied_to_properties = false;
                        for (var j = 1; j < filtered_value.length; j++) {
                            if (!property_value_mapping.containsKey(filtered_value[j])) {
                                property_value_mapping.put(filtered_value[j], filtered_value[0]);
                                tied_to_properties = true;
                            }
                        }

                        if (tied_to_properties) {
                            // prepare an internal value to construct the value
                            values_construction.put(filtered_value[0], template.createInternalValue());

                            // go over all the error blocks until one is found that supports the
                            // same properties as the value
                            ArrayList<String> error_block_properties;
                            if (block_properties_mapping != null) {
                                for (var error_block : block_properties_mapping.entrySet()) {
                                    var matching_block = true;

                                    error_block_properties = error_block.getValue();
                                    for (var j = 1; j < filtered_value.length; j++) {
                                        if (!error_block_properties.contains(filtered_value[j])) {
                                            matching_block = false;
                                            break;
                                        }
                                    }

                                    if (matching_block) {
                                        values_block_mapping.put(filtered_value[0], error_block.getKey());
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Process the sorted error message blocks so that for each
        // property the first occuring block will be used.
        var filtered_error_messages = template.getFilteredBlocks(TAG_ERRORMESSAGE);
        HashMap<String, String> property_error_messages_mapping = null;
        if (filtered_error_messages.size() > 0) {
            property_error_messages_mapping = new HashMap<String, String>();

            for (var filtered_errormessage : filtered_error_messages) {
                for (var i = 1; i < filtered_errormessage.length; i++) {
                    if (property_error_messages_mapping.containsKey(filtered_errormessage[i])) {
                        continue;
                    }

                    property_error_messages_mapping.put(filtered_errormessage[i], filtered_errormessage[0]);
                }
            }
        }

        // Go over all the validation errors and format them according to
        // their subject.
        String property_value;
        InternalValue value_construction;
        String subject;
        for (var error : errors) {
            subject = error.getSubject();
            if (prefix != null) {
                subject = prefix + subject;
            }

            // get the value where the error has to be displayed
            if (property_value_mapping != null) {
                property_value = property_value_mapping.get(subject);
            } else {
                property_value = null;
            }

            if (property_value != null) {
                value_construction = values_construction.get(property_value);

                // generate the error message
                generateErrorMessage(template, error, prefix, value_construction, property_error_messages_mapping);
            } else if (has_fallback_errors_value) {
                // generate the error message
                generateErrorMessage(template, error, prefix, fallback_errors_construction, property_error_messages_mapping);
            }
        }

        // Now that all the values have been constructed, go over
        // the corresponding blocks to format the content.
        if (values_construction != null) {
            for (var property_value_id : values_construction.entrySet()) {
                if (values_block_mapping != null &&
                    values_block_mapping.containsKey(property_value_id.getKey())) {
                    template.setValue(ID_ERRORS, property_value_id.getValue());
                    template.setBlock(property_value_id.getKey(), values_block_mapping.get(property_value_id.getKey()));
                    template.removeValue(ID_ERRORS);
                } else if (has_wildcard_errors_block) {
                    template.setValue(ID_ERRORS, property_value_id.getValue());
                    template.setBlock(property_value_id.getKey(), ID_ERRORS_WILDCARD);
                    template.removeValue(ID_ERRORS);
                } else {
                    template.setValue(property_value_id.getKey(), property_value_id.getValue());
                }

                set_values.add(property_value_id.getKey());
            }
        }

        // Set the global errors value content.
        if (fallback_errors_construction != null &&
            !fallback_errors_construction.isEmpty()) {
            if (has_fallback_errors_block) {
                template.setValue(ID_ERRORS, fallback_errors_construction);
                template.setBlock(ID_ERRORS_WILDCARD, ID_ERRORS_FALLBACK);
                template.removeValue(ID_ERRORS);
            } else if (has_wildcard_errors_block) {
                template.setValue(ID_ERRORS, fallback_errors_construction);
                template.setBlock(ID_ERRORS_WILDCARD, ID_ERRORS_WILDCARD);
                template.removeValue(ID_ERRORS);
            } else {
                template.setValue(ID_ERRORS_WILDCARD, fallback_errors_construction);
            }

            set_values.add(ID_ERRORS_WILDCARD);
        }

        return set_values;
    }

    private Collection<String> collectSubjects(Collection<ValidationError> errors, String prefix) {
        // Collect the invalid subjects in a seperate collection which
        // only contains their name.
        var invalid_subjects = new ArrayList<String>();
        String subject;
        for (var error : errors) {
            subject = error.getSubject();
            if (prefix != null) {
                subject = prefix + subject;
            }
            if (!invalid_subjects.contains(error.getSubject())) {
                invalid_subjects.add(subject);
            }
        }

        return invalid_subjects;
    }

    private String generateErrorBlockId(ValidationError error, String prefix) {
        var result = new StringBuilder(error.getIdentifier());
        result.append(":");
        if (prefix != null) {
            result.append(prefix);
        }
        result.append(error.getSubject());
        return result.toString();
    }

    private String generateFallbackSubjectBlockId(ValidationError error, String prefix) {
        var result = new StringBuilder(PREFIX_ERROR);
        if (prefix != null) {
            result.append(prefix);
        }
        result.append(error.getSubject());
        return result.toString();
    }

    private String generateFallbackIdentifierBlockId(ValidationError error) {
        return error.getIdentifier() + ":*";
    }

    private void generateErrorMessage(Template template, ValidationError error, String prefix, InternalValue valueConstruction, HashMap<String, String> error_messagesMapping)
    throws TemplateException {
        // try to obtain the id of a block that formats each error message
        // for the provided error
        String errormessage_block_id = null;
        var subject = error.getSubject();
        if (prefix != null) {
            subject = prefix + subject;
        }

        if (error_messagesMapping != null) {
            errormessage_block_id = error_messagesMapping.get(subject);
        }

        if (null == errormessage_block_id &&
            template.hasBlock(ID_ERRORMESSAGE_WILDCARD)) {
            errormessage_block_id = ID_ERRORMESSAGE_WILDCARD;
        }

        // if a custom error message formatting block was found, set the
        // actual error message and append the formatted result to the
        // error construction
        if (errormessage_block_id != null) {
            if (template.hasValueId(ID_ERRORMESSAGE)) {
                var errorblock_id = generateErrorBlockId(error, prefix);
                // support the IDENTIFIER:subject block id
                if (template.hasBlock(errorblock_id)) {
                    template.setBlock(ID_ERRORMESSAGE, errorblock_id);
                } else {
                    var fallback_subject_errorblock_id = generateFallbackSubjectBlockId(error, prefix);
                    // support the ERROR:subject block id
                    if (template.hasBlock(fallback_subject_errorblock_id)) {
                        template.setBlock(ID_ERRORMESSAGE, fallback_subject_errorblock_id);
                    } else {
                        // support the IDENTIFIER:* block id
                        var fallback_identifier_errorblock_id = generateFallbackIdentifierBlockId(error);
                        if (template.hasBlock(fallback_identifier_errorblock_id)) {
                            template.setBlock(ID_ERRORMESSAGE, fallback_identifier_errorblock_id);
                        } else {
                            // support the ERROR:* block id
                            if (template.hasBlock(ID_ERROR_WILDCARD)) {
                                template.setBlock(ID_ERRORMESSAGE, ID_ERROR_WILDCARD);
                            } else {
                                // just output an IDENTIFIER:subject string
                                template.setValue(ID_ERRORMESSAGE, errorblock_id);
                            }
                        }
                    }
                }

                valueConstruction.appendBlock(errormessage_block_id);
                template.removeValue(ID_ERRORMESSAGE);
            } else {
                valueConstruction.appendBlock(errormessage_block_id);
            }
        }
        // append a generic error message to the error construction
        else {
            var error_value_id = generateErrorBlockId(error, prefix);
            if (template.hasBlock(error_value_id)) {
                valueConstruction.appendBlock(error_value_id);
            } else {
                var errorblock_id = generateErrorBlockId(error, prefix);
                // support the IDENTIFIER:subject block id
                if (template.hasBlock(errorblock_id)) {
                    valueConstruction.appendBlock(errorblock_id);
                } else {
                    var fallback_subject_errorblock_id = generateFallbackSubjectBlockId(error, prefix);
                    // support the ERROR:subject block id
                    if (template.hasBlock(fallback_subject_errorblock_id)) {
                        valueConstruction.appendBlock(fallback_subject_errorblock_id);
                    } else {
                        // support the IDENTIFIER:* block id
                        var fallback_identifier_errorblock_id = generateFallbackIdentifierBlockId(error);
                        if (template.hasBlock(fallback_identifier_errorblock_id)) {
                            valueConstruction.appendBlock(fallback_identifier_errorblock_id);
                        } else {
                            // support the ERROR:* block id
                            if (template.hasBlock(ID_ERROR_WILDCARD)) {
                                valueConstruction.appendBlock(ID_ERROR_WILDCARD);
                            } else {
                                // just output an IDENTIFIER:subject string
                                valueConstruction.appendValue(formatLine(errorblock_id));
                            }
                        }
                    }
                }
            }
        }
    }

    protected abstract String formatLine(String content);

    public Collection<String> generateErrorMarkings(Template template, Collection<ValidationError> errors, Collection<String> onlySubjectsToClear, String prefix)
    throws ValidationBuilderException {
        if (null == template ||
            null == errors ||
            0 == errors.size()) {
            return Collections.emptyList();
        }

        var set_values = new ArrayList<String>();

        // adapt for subject prefixes
        if (prefix != null &&
            onlySubjectsToClear != null) {
            var prefixed_subjects = new ArrayList<String>();
            for (var property : onlySubjectsToClear) {
                prefixed_subjects.add(prefix + property);
            }
            onlySubjectsToClear = prefixed_subjects;
        }

        // Reorder the filtered mark so that they're ordered according
        // to the number of subjects they declare for each mark extension.
        // Those with the same number of subjects will be ordered according
        // to their order of declaration.
        // Also, all the content from the filtered marks will be cleared to
        // ensure that errors from previous submissions will not prevail.
        var filtered_marks = template.getFilteredValues(TAG_MARK);
        HashMap<String, ArrayList<List<String[]>>> sorted_marks_map = null;
        if (filtered_marks.size() > 0) {
            sorted_marks_map = new HashMap<String, ArrayList<List<String[]>>>();

            ArrayList<List<String[]>> sorted_marks;
            List<String[]> marks;
            for (var filtered_mark : filtered_marks) {
                // only clear the filtered mark if one of the subjects is
                // validated
                if (null == onlySubjectsToClear) {
                    template.removeValue(filtered_mark[0]);
                } else {
                    for (var i = 1; i < filtered_mark.length; i++) {
                        if (onlySubjectsToClear.contains(filtered_mark[i])) {
                            template.removeValue(filtered_mark[0]);
                            break;
                        }
                    }
                }

                // get the sorted marks for the specific mark extension
                sorted_marks = sorted_marks_map.get(filtered_mark[1]);
                // or create a new collection if it doesn't exist yet
                if (null == sorted_marks) {
                    sorted_marks = new ArrayList<List<String[]>>();
                    sorted_marks_map.put(filtered_mark[1], sorted_marks);
                }

                // setup the collection of marks with the same number of
                // properties and create a new one if that's needed
                var number_of_properties = (filtered_mark.length - 2) / 2;
                if (number_of_properties < sorted_marks.size()) {
                    marks = sorted_marks.get(number_of_properties);
                } else {
                    marks = null;
                    while (!(number_of_properties < sorted_marks.size())) {
                        sorted_marks.add(null);
                    }
                }
                if (null == marks) {
                    marks = new ArrayList<String[]>();
                    sorted_marks.set(number_of_properties, marks);
                }

                // add the filtered mark to the collection of all marks with
                // the same number of properties
                marks.add(filtered_mark);
            }
        }

        var invalid_subjects = collectSubjects(errors, prefix);

        // Go over the mark extensions according to their order of importance.
        // Marks with a broader scope (more properties) get precedence of
        // those with a narrower scope (less properties) and inside the
        // same scope level they are handled according to their order
        // of declaration.
        if (sorted_marks_map != null) {
            for (var sorted_marks_entry : sorted_marks_map.entrySet()) {
                String mark_block_id;
                var mark_extension = sorted_marks_entry.getKey();
                if (null == mark_extension) {
                    mark_block_id = PREFIX_MARK_ERROR;
                } else {
                    mark_block_id = PREFIX_MARK_ERROR + ":" + mark_extension;
                }

                var property_mark_mapping = new HashMap<String, String>();

                var sorted_marks = sorted_marks_entry.getValue();
                List<String[]> marks;
                for (var i = sorted_marks.size() - 1; i >= 0; i--) {
                    marks = sorted_marks.get(i);
                    if (null == marks) {
                        continue;
                    }

                    for (var filtered_mark : marks) {
                        // check if the filtered mark contains only subjects
                        // that are invalid
                        var use_mark = true;
                        for (var j = 2; j < filtered_mark.length; j += 2) {
                            if (!invalid_subjects.contains(filtered_mark[j])) {
                                use_mark = false;
                                break;
                            }
                        }

                        // If the mark can be used, register its id for
                        // all the declared subjects. If the subject is already
                        // bound to another value, it is simply skipped.
                        if (use_mark) {
                            var tied_to_properties = false;
                            for (var j = 2; j < filtered_mark.length; j += 2) {
                                if (!property_mark_mapping.containsKey(filtered_mark[j])) {
                                    property_mark_mapping.put(filtered_mark[j], filtered_mark[0]);
                                    tied_to_properties = true;
                                }
                            }

                            if (tied_to_properties) {
                                if (!template.hasBlock(mark_block_id)) {
                                    throw new MissingMarkingBlockException(mark_block_id);
                                }

                                template.setBlock(filtered_mark[0], mark_block_id);
                                set_values.add(filtered_mark[0]);
                            }
                        }
                    }
                }
            }
        }

        return set_values;
    }

    public void removeValidationErrors(Template template, Collection<String> subjects, String prefix) {
        if (null == template) {
            return;
        }

        if (null == subjects ||
            0 == subjects.size()) {
            return;
        }

        if (template.hasValueId(ID_ERRORS_WILDCARD)) {
            template.removeValue(ID_ERRORS_WILDCARD);
        }

        if (prefix != null) {
            var prefixed_subjects = new ArrayList<String>();
            for (var property : subjects) {
                prefixed_subjects.add(prefix + property);
            }
            subjects = prefixed_subjects;
        }

        var filtered_error_values = template.getFilteredValues(TAG_ERRORS);
        if (filtered_error_values.size() > 0) {
            for (var filtered_value : filtered_error_values) {
                for (var i = 1; i < filtered_value.length; i++) {
                    if (subjects.contains(filtered_value[i])) {
                        template.removeValue(filtered_value[0]);
                        break;
                    }
                }
            }
        }
    }

    public void removeErrorMarkings(Template template, Collection<String> subjects, String prefix) {
        if (null == template) {
            return;
        }

        if (null == subjects ||
            0 == subjects.size()) {
            return;
        }

        if (prefix != null) {
            var prefixed_subjects = new ArrayList<String>();
            for (var property : subjects) {
                prefixed_subjects.add(prefix + property);
            }
            subjects = prefixed_subjects;
        }

        var filtered_marks = template.getFilteredValues(TAG_MARK);
        if (filtered_marks.size() > 0) {
            for (var filtered_mark : filtered_marks) {
                for (var i = 2; i < filtered_mark.length; i += 2) {
                    if (subjects.contains(filtered_mark[i])) {
                        template.removeValue(filtered_mark[0]);
                        break;
                    }
                }
            }
        }
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
