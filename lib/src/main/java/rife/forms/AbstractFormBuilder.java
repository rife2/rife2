/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.forms;

import rife.template.Template;
import rife.template.exceptions.TemplateException;
import rife.tools.*;
import rife.tools.exceptions.BeanUtilsException;
import rife.validation.*;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Logger;

abstract public class AbstractFormBuilder implements FormBuilder {
    protected abstract String getIdName();

    protected abstract String getIdAttributes();

    protected abstract String getIdValue();

    protected abstract String getIdMinlength();

    protected abstract String getIdMaxlength();

    protected abstract String getIdRequired();

    protected abstract String getIdChecked();

    protected abstract String getIdOptions();

    protected abstract String getIdSelected();

    protected abstract String getIdLabel();

    protected abstract String getIdDisabled();

    protected abstract String getIdFormOption();

    protected abstract String getValueSelected();

    protected abstract String getValueChecked();

    protected abstract Template getBuilderTemplateInstance();

    protected abstract String sanitizeAttributes(String value);

    protected abstract void generateFieldHidden(Template template, String templateFieldName, String name, ConstrainedProperty property, String[] values, Template builderTemplate, List<String> setValues, boolean replaceExistingValues);

    protected abstract void generateFieldInput(Template template, String templateFieldName, String name, ConstrainedProperty property, String[] values, Template builderTemplate, List<String> setValues, boolean replaceExistingValues);

    protected abstract void generateFieldSecret(Template template, String templateFieldName, String name, ConstrainedProperty property, String[] values, Template builderTemplate, List<String> setValues, boolean replaceExistingValues);

    protected abstract void generateFieldTextarea(Template template, String templateFieldName, String name, ConstrainedProperty property, String[] values, Template builderTemplate, List<String> setValues, boolean replaceExistingValues);

    protected abstract void generateFieldRadio(Template template, String templateFieldName, Class propertyType, String name, ConstrainedProperty property, String[] values, Template builderTemplate, List<String> setValues, boolean replaceExistingValues);

    protected abstract void generateFieldCheckbox(Template template, String templateFieldName, Class propertyType, String name, ConstrainedProperty property, String[] values, Template builderTemplate, List<String> setValues, boolean replaceExistingValues);

    public abstract ValidationBuilder getValidationBuilder();

    public Collection<String> generateForm(Template template, Class beanClass, Map<String, String[]> values, String prefix)
    throws BeanUtilsException {
        if (null == beanClass) {
            return Collections.emptyList();
        }

        // create an instance of the bean
        Object bean;
        try {
            bean = beanClass.getDeclaredConstructor().newInstance();
        } catch (Throwable e) {
            bean = null;
        }

        return generateForm(template, beanClass, bean, values, prefix);
    }

    public Collection<String> generateForm(Template template, Object bean, Map<String, String[]> values, String prefix)
    throws BeanUtilsException {
        if (null == bean) {
            return Collections.emptyList();
        }

        if (bean instanceof Class) throw new IllegalArgumentException("bean should be a bean instance, not a bean class.");

        return generateForm(template, bean.getClass(), bean, values, prefix);
    }

    protected Collection<String> generateForm(final Template template, Class beanClass, Object bean, final Map<String, String[]> values, final String prefix)
    throws BeanUtilsException {
        final var set_values = new ArrayList<String>();

        if (null == template) {
            return set_values;
        }

        if (null == bean) {
            // generate the form fields using the provided value map to set the
            // default values for the fields
            BeanUtils.processProperties(beanClass, null, null, null, (name, descriptor) -> {
                String[] property_values = null;
                if (values != null) {
                    if (null == prefix) {
                        property_values = values.get(name);
                    } else {
                        property_values = values.get(prefix + name);
                    }
                }

                generateFormField(template, null, descriptor.getPropertyType(), name, property_values, prefix, set_values);

                return true;
            });
        } else {
            // check if the bean is constrained
            final var constrained = ConstrainedUtils.makeConstrainedInstance(bean);

            Validated validated = null;

            final Set<ValidationError> previous_errors;
            if (null == values) {
                // check if the bean is validated
                if (bean instanceof Validated) {
                    validated = (Validated) bean;

                    // check if validation errors are already present in the bean,
                    // and generate the formatted errors
                    set_values.addAll(getValidationBuilder().generateValidationErrors(template, validated.getValidationErrors(), validated.getValidatedSubjects(), prefix));
                    set_values.addAll(getValidationBuilder().generateErrorMarkings(template, validated.getValidationErrors(), validated.getValidatedSubjects(), prefix));

                    // store the validation errors and revalidate
                    previous_errors = validated.getValidationErrors();

                    // revalidate the bean to check which fields have to be displayed
                    validated.resetValidation();
                    validated.validate();
                } else {
                    previous_errors = null;
                }
            } else {
                previous_errors = null;
            }

            // generate the form fields using the bean's property values to set
            // the default values for the fields
            BeanUtils.processPropertyValues(bean, null, null, null, (name, descriptor, value) -> {
                String[] property_values = null;
                ConstrainedProperty constrained_property = null;

                // get the corresponding constrained property, if it exists
                if (constrained != null) {
                    constrained_property = constrained.getConstrainedProperty(name);
                }

                // check if the bean is validated and if the validation error for the
                // property contains an erroneous value, in that case this value will
                // be used instead, since it contains the exact cause of the error.
                if (previous_errors != null &&
                    previous_errors.size() > 0) {
                    for (var error : previous_errors) {
                        if (error.getErroneousValue() != null &&
                            error.getSubject().equals(name)) {
                            property_values = ArrayUtils.createStringArray(error.getErroneousValue(), constrained_property);
                            break;
                        }
                    }
                }

                if (values != null) {
                    if (null == prefix) {
                        property_values = values.get(name);
                    } else {
                        property_values = values.get(prefix + name);
                    }
                } else {
                    // if no erroneous value could be obtained, create a string
                    // representation of the property value
                    if (null == property_values) {
                        if (null == value) {
                            property_values = null;
                        } else {
                            property_values = ArrayUtils.createStringArray(value, constrained_property);
                        }
                    }
                }

                // generate the form field
                generateFormField(template, constrained, descriptor.getPropertyType(), name, property_values, prefix, set_values);
            });

            if (validated != null) {
                // restore the previous validation errors
                validated.replaceValidationErrors(previous_errors);
            }
        }

        template.addGeneratedValues(set_values);

        return set_values;
    }

    protected void generateFormField(Template template, Constrained constrained, Class propertyType, String propertyName, String[] propertyValues, String prefix, ArrayList<String> setValues) {
        ConstrainedProperty constrained_property = null;

        if (constrained != null) {
            constrained_property = constrained.getConstrainedProperty(propertyName);
        }

        if (null == constrained_property) {
            generateField(template, null, propertyType, propertyName, propertyValues, prefix, setValues, false);
        } else {
            generateField(template, null, propertyType, constrained_property, propertyValues, prefix, setValues, false);
        }
    }

    public Collection<String> generateField(Template template, ConstrainedProperty property, String[] values, String prefix) {
        return generateField(template, null, null, property, values, prefix, null, false);
    }

    public Collection<String> generateField(Template template, Class propertyType, ConstrainedProperty property, String[] values, String prefix) {
        return generateField(template, null, propertyType, property, values, prefix, null, false);
    }

    public Collection<String> replaceField(Template template, String templateFieldName, ConstrainedProperty property, String[] values, String prefix) {
        return generateField(template, templateFieldName, null, property, values, prefix, null, true);
    }

    public Collection<String> replaceField(Template template, String templateFieldName, Class propertyType, ConstrainedProperty property, String[] values, String prefix) {
        return generateField(template, templateFieldName, propertyType, property, values, prefix, null, true);
    }

    protected Collection<String> generateField(Template template, String templateFieldName, Class propertyType, ConstrainedProperty property, String[] values, String prefix, ArrayList<String> setValues, boolean replaceExistingValues) {
        if (null == setValues) {
            setValues = new ArrayList<>();
        }

        if (null == template ||
            null == property) {
            return setValues;
        }

        String name;
        if (null == prefix) {
            name = property.getPropertyName();
        } else {
            name = prefix + property.getPropertyName();
        }

        if (null == templateFieldName) {
            templateFieldName = name;
        }

        generateField(template, templateFieldName, propertyType, name, property, values, setValues, replaceExistingValues);

        return setValues;
    }

    public Collection<String> generateField(Template template, String name, String[] values, String prefix) {
        var set_values = generateField(template, null, null, name, values, prefix, null, false);
        if (template != null) {
            template.addGeneratedValues(set_values);
        }
        return set_values;
    }

    public Collection<String> generateField(Template template, Class propertyType, String name, String[] values, String prefix) {
        var set_values = generateField(template, null, propertyType, name, values, prefix, null, false);
        if (template != null) {
            template.addGeneratedValues(set_values);
        }
        return set_values;
    }

    public Collection<String> replaceField(Template template, String templateFieldName, String name, String[] values, String prefix) {
        var set_values = generateField(template, templateFieldName, null, name, values, prefix, null, true);
        if (template != null) {
            template.addGeneratedValues(set_values);
        }
        return set_values;
    }

    public Collection<String> replaceField(Template template, String templateFieldName, Class propertyType, String name, String[] values, String prefix) {
        var set_values = generateField(template, templateFieldName, propertyType, name, values, prefix, null, true);
        if (template != null) {
            template.addGeneratedValues(set_values);
        }
        return set_values;
    }

    protected Collection<String> generateField(Template template, String templateFieldName, Class propertyType, String name, String[] values, String prefix, ArrayList<String> setValues, boolean replaceExistingValues) {
        if (null == setValues) {
            setValues = new ArrayList<>();
        }

        if (null == template ||
            null == name ||
            0 == name.length()) {
            return setValues;
        }

        if (prefix != null) {
            name = prefix + name;
        }

        if (null == templateFieldName) {
            templateFieldName = name;
        }

        generateField(template, templateFieldName, propertyType, name, null, values, setValues, replaceExistingValues);

        return setValues;
    }

    protected void generateField(Template template, String templateFieldName, Class propertyType, String name, ConstrainedProperty property, String[] values, ArrayList<String> setValues, boolean replaceExistingValues) {
        var builder_template = getBuilderTemplateInstance();

        generateFieldHidden(template, templateFieldName, name, property, values, builder_template, setValues, replaceExistingValues);
        generateFieldInput(template, templateFieldName, name, property, values, builder_template, setValues, replaceExistingValues);
        generateFieldSecret(template, templateFieldName, name, property, values, builder_template, setValues, replaceExistingValues);
        generateFieldTextarea(template, templateFieldName, name, property, values, builder_template, setValues, replaceExistingValues);
        generateFieldRadio(template, templateFieldName, propertyType, name, property, values, builder_template, setValues, replaceExistingValues);
        generateFieldCheckbox(template, templateFieldName, propertyType, name, property, values, builder_template, setValues, replaceExistingValues);
        generateFieldSelect(template, templateFieldName, propertyType, name, property, values, builder_template, setValues, replaceExistingValues);
        generateFieldDisplay(template, templateFieldName, propertyType, name, property, values, builder_template, setValues, replaceExistingValues);
    }

    protected void generateFieldText(String prefix, boolean setValue, boolean valueAsAttribute, boolean limitLength, boolean disableField, boolean requireField, Template template, String templateFieldName, String name, ConstrainedProperty property, String[] values, Template builderTemplate, List<String> setValues, boolean replaceExistingValues) {
        StringBuilder field_buffer;
        String field;
        String field_attributes;

        field_buffer = new StringBuilder(prefix.length() + templateFieldName.length());
        field_buffer.append(prefix);
        field_buffer.append(templateFieldName);
        field = field_buffer.toString();

        if (template.hasValueId(field) &&
            (replaceExistingValues || (!template.isValueSet(field) && !template.isValueGenerated(field)))) {
            field_buffer = new StringBuilder(prefix.length() + MIDDLE_ATTRIBUTES.length() + templateFieldName.length());
            field_buffer.append(prefix);
            field_buffer.append(MIDDLE_ATTRIBUTES);
            field_buffer.append(templateFieldName);
            field_attributes = field_buffer.toString();

            // set the field name
            builderTemplate.setValue(getIdName(), template.getEncoder().encode(name));

            // determine the value by using the provided value or apply the
            // default value if it has been set in the ConstrainedProperty
            String value = null;
            if (values != null &&
                values.length > 0 &&
                values[0] != null) {
                value = template.getEncoder().encode(values[0]);
            } else if (property != null &&
                property.hasDefaultValue()) {
                value = template.getEncoder().encode(property.getDefaultValue().toString());
            }

            // set the attributes that the user provided through a block value
            if (template.hasBlock(field_attributes)) {
                // try to set the name if a placeholder is available for it
                if (template.hasValueId(ID_FORM_NAME)) {
                    template.setValue(ID_FORM_NAME, template.getEncoder().encode(name));
                }

                // try to set the value if a placeholder is available for it
                if (template.hasValueId(ID_FORM_VALUE) &&
                    value != null) {
                    template.setValue(ID_FORM_VALUE, value);
                }

                builderTemplate.setValue(getIdAttributes(), sanitizeAttributes(template.getBlock(field_attributes)));

                // remove the values that were set
                if (template.hasValueId(ID_FORM_NAME)) {
                    template.removeValue(ID_FORM_NAME);
                }
                if (template.hasValueId(ID_FORM_VALUE)) {
                    template.removeValue(ID_FORM_VALUE);
                }
            }
            // set the attributes that the user provided through a default value
            else if (template.hasDefaultValue(field)) {
                builderTemplate.setValue(getIdAttributes(), sanitizeAttributes(template.getDefaultValue(field)));
            }

            // set the provided value or apply the default value if it has been
            // set in the ConstrainedProperty
            if (setValue &&
                value != null) {
                builderTemplate.setValue(getIdValue(), value);
                if (valueAsAttribute) {
                    builderTemplate.appendBlock(getIdAttributes(), getIdValue());
                }
            }

            // handle the length limit if it has been set in the
            // ConstrainedProperty
            if (limitLength &&
                property != null) {
                int min_length = 0;
                if (property.isNotEmpty()) {
                    min_length = 1;
                }
                if (property.getMinLength() > min_length) {
                    min_length = property.getMinLength();
                }
                if (min_length > 0) {
                    builderTemplate.setValue(getIdMinlength(), min_length);
                    builderTemplate.appendBlock(getIdAttributes(), getIdMinlength());
                }
                if (property.getMaxLength() >= 0) {
                    builderTemplate.setValue(getIdMaxlength(), property.getMaxLength());
                    builderTemplate.appendBlock(getIdAttributes(), getIdMaxlength());
                }
            }

            // set the field to required if the ConstrainedProperty is notNull
            if (requireField &&
                property != null &&
                property.isNotNull()) {
                builderTemplate.appendBlock(getIdAttributes(), getIdRequired());
            }

            // set the field to disabled if the ConstrainedProperty is not
            // editable
            if (disableField &&
                property != null &&
                !property.isEditable()) {
                builderTemplate.appendBlock(getIdAttributes(), getIdDisabled());
            }

            // replace the form field tag in the provided template by the
            // newly constructed functional form field
            var builder_template_id = prefix;
            if (builder_template_id.equals(PREFIX_FORM_INPUT) && property != null) {
                if (property.isEmail()) {
                    builder_template_id = PREFIX_FORM_EMAIL;
                } else if (property.isUrl()) {
                    builder_template_id = PREFIX_FORM_URL;
                }
            }
            template.setValue(field, builderTemplate.getBlock(builder_template_id));

            // register the form fields tag in the list of value ids that
            // have been set
            setValues.add(field);

            // clear out template
            builderTemplate.removeValue(getIdName());
            builderTemplate.removeValue(getIdValue());
            builderTemplate.removeValue(getIdMinlength());
            builderTemplate.removeValue(getIdMaxlength());
            builderTemplate.removeValue(getIdAttributes());
        }
    }

    protected void generateFieldCollection(String prefix, boolean singleValue, Template template, String templateFieldName, Class propertyType, String name, ConstrainedProperty property, String[] values, Template builderTemplate, List<String> setValues, boolean replaceExistingValues) {
        StringBuilder field_buffer;
        String field;
        String field_attributes;

        // handle input form fields
        field_buffer = new StringBuilder(prefix.length() + templateFieldName.length());
        field_buffer.append(prefix);
        field_buffer.append(templateFieldName);
        field = field_buffer.toString();

        if (template.hasValueId(field) &&
            (replaceExistingValues || (!template.isValueSet(field) && !template.isValueGenerated(field)))) {
            if (replaceExistingValues) {
                template.blankValue(field);
            }

            field_buffer = new StringBuilder(prefix.length() + MIDDLE_ATTRIBUTES.length() + templateFieldName.length());
            field_buffer.append(prefix);
            field_buffer.append(MIDDLE_ATTRIBUTES);
            field_buffer.append(templateFieldName);
            field_attributes = field_buffer.toString();

            // set the field name
            builderTemplate.setValue(getIdName(), template.getEncoder().encode(name));

            // set up the active values
            ArrayList<String> active_values = null;
            if (values != null &&
                values.length > 0) {
                if (singleValue) {
                    if (values[0] != null) {
                        active_values = new ArrayList<>();
                        active_values.add(values[0]);
                    }
                } else {
                    active_values = new ArrayList<>();
                    for (var value : values) {
                        if (null == value) {
                            continue;
                        }
                        active_values.add(value);
                    }
                }
            }
            if (null == active_values &&
                property != null &&
                property.hasDefaultValue()) {
                active_values = new ArrayList<>();
                active_values.add(property.getDefaultValue().toString());
            }

            String[] list_values = null;

            // obtain the list of possible values for properties that are constrained to a list
            if (property != null &&
                property.isInList()) {
                list_values = property.getInList();
            }
            // obtain the list of possible values for enum properties
            else if (propertyType != null) {
                if (propertyType.isEnum()) {
                    list_values = ClassUtils.getEnumClassValues(propertyType);
                } else if (propertyType.isArray() &&
                    propertyType.getComponentType().isEnum()) {
                    list_values = ClassUtils.getEnumClassValues(propertyType.getComponentType());
                }
            }

            // if no list has been defined for the constrained property, just
            // set one tag without any additional attributes
            if (null == list_values) {
                // set the attributes that the user provided through a default value
                if (template.hasBlock(field_attributes)) {
                    // try to set the name if a placeholder is available for it
                    if (template.hasValueId(ID_FORM_NAME)) {
                        template.setValue(ID_FORM_NAME, template.getEncoder().encode(name));
                    }

                    // try to set the value if a placeholder is available for it
                    if (template.hasValueId(ID_FORM_VALUE)) {
                        if (values != null &&
                            values.length > 0 &&
                            values[0] != null) {
                            template.setValue(ID_FORM_VALUE, template.getEncoder().encode(values[0]));
                        } else if (property != null &&
                            property.hasDefaultValue()) {
                            template.setValue(ID_FORM_VALUE, template.getEncoder().encode(property.getDefaultValue().toString()));
                        }
                    }

                    builderTemplate.setValue(getIdAttributes(), sanitizeAttributes(template.getBlock(field_attributes)));

                    // remove the values that were set
                    if (template.hasValueId(ID_FORM_NAME)) {
                        template.removeValue(ID_FORM_NAME);
                    }
                    if (template.hasValueId(ID_FORM_VALUE)) {
                        template.removeValue(ID_FORM_VALUE);
                    }
                } else if (template.hasDefaultValue(field)) {
                    builderTemplate.setValue(getIdAttributes(), sanitizeAttributes(template.getDefaultValue(field)));
                } else {
                    builderTemplate.setValue(getIdAttributes(), "");
                }

                // check the field if the first value is active
                if (active_values != null &&
                    StringUtils.convertToBoolean(active_values.get(0))) {
                    builderTemplate.appendBlock(getIdAttributes(), getIdChecked());
                }

                // set the field to required if the ConstrainedProperty is notNull
                if (property != null &&
                    property.isNotNull()) {
                    builderTemplate.appendBlock(getIdAttributes(), getIdRequired());
                }

                // set the field to disabled if the ConstrainedProperty is not
                // editable
                if (property != null &&
                    !property.isEditable()) {
                    builderTemplate.appendBlock(getIdAttributes(), getIdDisabled());
                }

                template.setValue(field, builderTemplate.getBlock(prefix));
            } else {
                for (var value : list_values) {
                    if (null == value) {
                        continue;
                    }

                    // try to set the name if a placeholder is available for it
                    if (template.hasValueId(ID_FORM_NAME)) {
                        template.setValue(ID_FORM_NAME, template.getEncoder().encode(name));
                    }

                    // try to set the value if a placeholder is available for it
                    if (template.hasValueId(ID_FORM_VALUE)) {
                        template.setValue(ID_FORM_VALUE, template.getEncoder().encode(value));
                    }

                    // set the attributes that the user provided through a default value
                    if (template.hasBlock(field_attributes)) {
                        builderTemplate.setValue(getIdAttributes(), sanitizeAttributes(template.getBlock(field_attributes)));
                    } else if (template.hasDefaultValue(field)) {
                        builderTemplate.setValue(getIdAttributes(), sanitizeAttributes(template.getDefaultValue(field)));
                    } else {
                        builderTemplate.setValue(getIdAttributes(), "");
                    }

                    // set the value of the field entry
                    builderTemplate.setValue(getIdValue(), template.getEncoder().encode(value));
                    builderTemplate.appendBlock(getIdAttributes(), getIdValue());

                    // set the field entry that corresponds to the active value
                    if (active_values != null &&
                        active_values.contains(value)) {
                        builderTemplate.appendBlock(getIdAttributes(), getIdChecked());
                    }

                    // set the field to required if the ConstrainedProperty is notNull
                    if (property != null &&
                        property.isNotNull()) {
                        builderTemplate.appendBlock(getIdAttributes(), getIdRequired());
                    }

                    // set the field to disabled if the ConstrainedProperty is not
                    // editable
                    if (property != null &&
                        !property.isEditable()) {
                        builderTemplate.appendBlock(getIdAttributes(), getIdDisabled());
                    }

                    // check if this field entry has a custom layout
                    if (template.hasBlock(field)) {
                        // try to set the field if a placeholder is available for it
                        if (template.hasValueId(ID_FORM_FIELD)) {
                            template.setValue(ID_FORM_FIELD, builderTemplate.getBlock(prefix));
                        }

                        // try to set the label if a placeholder is available for it
                        if (template.hasValueId(ID_FORM_LABEL)) {
                            template.setValue(ID_FORM_LABEL, generateLabel(template, templateFieldName, value));
                        }

                        // append a new field entry button
                        template.appendBlock(field, field);

                        // clear the template
                        if (template.hasValueId(ID_FORM_FIELD)) {
                            template.removeValue(ID_FORM_FIELD);
                        }
                        if (template.hasValueId(ID_FORM_LABEL)) {
                            template.removeValue(ID_FORM_LABEL);
                        }
                    }
                    // there's no custom layout, put the label to the right
                    // of the field entry
                    else {
                        // append a new field entry
                        template.appendValue(field, builderTemplate.getBlock(prefix));

                        // append the custom label if it's available
                        template.appendValue(field, generateLabel(template, templateFieldName, value));
                    }

                    // remove the values that were set
                    if (template.hasValueId(ID_FORM_NAME)) {
                        template.removeValue(ID_FORM_NAME);
                    }
                    if (template.hasValueId(ID_FORM_VALUE)) {
                        template.removeValue(ID_FORM_VALUE);
                    }
                }
            }

            // register the form fields tag in the list of value ids that
            // have been set
            setValues.add(field);

            // clear out template
            builderTemplate.removeValue(getIdName());
            builderTemplate.removeValue(getIdValue());
            builderTemplate.removeValue(getIdAttributes());
        }
    }

    protected void generateFieldSelect(Template template, String templateFieldName, Class propertyType, String name, ConstrainedProperty property, String[] values, Template builderTemplate, ArrayList<String> setValues, boolean replaceExistingValues) {
        StringBuilder field_buffer;
        String field;

        // handle input form fields
        field_buffer = new StringBuilder(PREFIX_FORM_SELECT.length() + templateFieldName.length());
        field_buffer.append(PREFIX_FORM_SELECT);
        field_buffer.append(templateFieldName);
        field = field_buffer.toString();

        if (template.hasValueId(field) &&
            (replaceExistingValues || (!template.isValueSet(field) && !template.isValueGenerated(field)))) {
            // set the field name
            builderTemplate.setValue(getIdName(), template.getEncoder().encode(name));

            // set up the active values
            ArrayList<String> active_values = null;
            if (values != null &&
                values.length > 0) {
                active_values = new ArrayList<>();
                for (var value : values) {
                    if (null == value) {
                        continue;
                    }
                    active_values.add(value);
                }
            }
            if (null == active_values &&
                property != null &&
                property.hasDefaultValue()) {
                active_values = new ArrayList<>();
                active_values.add(property.getDefaultValue().toString());
            }

            String[] list_values = null;

            // set up a select field options for constrained properties that
            // are constrained to a list
            if (property != null &&
                property.isInList()) {
                list_values = property.getInList();
            }
            // set up a select field options for enum properties
            else if (propertyType != null) {
                if (propertyType.isEnum()) {
                    list_values = ClassUtils.getEnumClassValues(propertyType);
                } else if (propertyType.isArray() &&
                    propertyType.getComponentType().isEnum()) {
                    list_values = ClassUtils.getEnumClassValues(propertyType.getComponentType());
                }
            }

            if (list_values != null) {
                var list = Arrays.asList(list_values);

                String default_value = null;
                if (property != null &&
                    property.hasDefaultValue()) {
                    default_value = Convert.toString(property.getDefaultValue());
                    if (list.contains(default_value)) {
                        default_value = null;
                    }
                }

                // set up the select options
                var i = 0;
                while (i < list.size()) {
                    String value;
                    if (default_value != null) {
                        value = default_value;
                        default_value = null;
                    } else {
                        value = list.get(i);
                        i++;
                    }

                    if (null == value) {
                        continue;
                    }

                    // set the value of the field entry
                    builderTemplate.setValue(getIdValue(), template.getEncoder().encode(value));

                    // set the field entry that corresponds to the active value
                    if (active_values != null &&
                        active_values.contains(value)) {
                        builderTemplate.setBlock(getIdAttributes(), getIdSelected());
                    } else {
                        builderTemplate.setValue(getIdAttributes(), "");
                    }

                    builderTemplate.setValue(getIdLabel(), generateLabel(template, templateFieldName, value));

                    // append a new option
                    builderTemplate.appendBlock(getIdOptions(), getIdFormOption());

                    // clear the option values
                    builderTemplate.removeValue(getIdAttributes());
                    builderTemplate.removeValue(getIdValue());
                    builderTemplate.removeValue(getIdLabel());
                    if (template.hasValueId(ID_FORM_NAME)) {
                        template.removeValue(ID_FORM_NAME);
                    }
                    if (template.hasValueId(ID_FORM_VALUE)) {
                        template.removeValue(ID_FORM_VALUE);
                    }
                }

                // set the attributes that the user provided through a default value
                if (template.hasDefaultValue(field)) {
                    builderTemplate.setValue(getIdAttributes(), sanitizeAttributes(template.getDefaultValue(field)));
                } else {
                    builderTemplate.setValue(getIdAttributes(), "");
                }

                // set the field to required if the ConstrainedProperty is notNull
                if (property != null &&
                    property.isNotNull()) {
                    builderTemplate.appendBlock(getIdAttributes(), getIdRequired());
                }

                // set the field to disabled if the ConstrainedProperty is not
                // editable
                if (property != null &&
                    !property.isEditable()) {
                    builderTemplate.appendBlock(getIdAttributes(), getIdDisabled());
                }
            }

            // set the select field
            template.setValue(field, builderTemplate.getBlock(PREFIX_FORM_SELECT));

            // register the form fields tag in the list of value ids that
            // have been set
            setValues.add(field);

            // clear out template
            builderTemplate.removeValue(getIdName());
            builderTemplate.removeValue(getIdOptions());
            builderTemplate.removeValue(getIdAttributes());
        }
    }

    protected void generateFieldDisplay(Template template, String templateFieldName, Class propertyType, String name, ConstrainedProperty property, String[] values, Template builderTemplate, ArrayList<String> setValues, boolean replaceExistingValues) {
        StringBuilder field_buffer;
        String field;
        String field_attributes;

        field_buffer = new StringBuilder(PREFIX_FORM_DISPLAY.length() + templateFieldName.length());
        field_buffer.append(PREFIX_FORM_DISPLAY);
        field_buffer.append(templateFieldName);
        field = field_buffer.toString();

        if (template.hasValueId(field) &&
            (replaceExistingValues || (!template.isValueSet(field) && !template.isValueGenerated(field)))) {
            if (replaceExistingValues) {
                template.blankValue(field);
            }

            field_buffer = new StringBuilder(PREFIX_FORM_DISPLAY.length() + MIDDLE_ATTRIBUTES.length() + templateFieldName.length());
            field_buffer.append(PREFIX_FORM_DISPLAY);
            field_buffer.append(MIDDLE_ATTRIBUTES);
            field_buffer.append(templateFieldName);
            field_attributes = field_buffer.toString();

            var counter = 0;
            do {
                // determine the value by using the provided value or apply the
                // default value if it has been set in the ConstrainedProperty
                String value = null;
                if (values != null &&
                    values.length > counter &&
                    values[counter] != null) {
                    value = template.getEncoder().encode(values[counter]);
                } else if (property != null &&
                    property.hasDefaultValue()) {
                    value = template.getEncoder().encode(property.getDefaultValue().toString());
                }

                // set the attributes that the user provided through a block value
                if (template.hasBlock(field_attributes)) {
                    // try to set the name if a placeholder is available for it
                    if (template.hasValueId(ID_FORM_NAME)) {
                        template.setValue(ID_FORM_NAME, template.getEncoder().encode(name));
                    }

                    // try to set the value if a placeholder is available for it
                    if (template.hasValueId(ID_FORM_VALUE) &&
                        value != null) {
                        template.setValue(ID_FORM_VALUE, value);
                    }

                    builderTemplate.setValue(getIdAttributes(), sanitizeAttributes(template.getBlock(field_attributes)));

                    // remove the values that were set
                    if (template.hasValueId(ID_FORM_NAME)) {
                        template.removeValue(ID_FORM_NAME);
                    }
                    if (template.hasValueId(ID_FORM_VALUE)) {
                        template.removeValue(ID_FORM_VALUE);
                    }
                }
                // set the attributes that the user provided through a default value
                else if (template.hasDefaultValue(field)) {
                    builderTemplate.setValue(getIdAttributes(), sanitizeAttributes(template.getDefaultValue(field)));
                }

                // set the provided value or apply the default value if it has been
                // set in the ConstrainedProperty
                if (value != null) {
                    // if the field is a constrained property that is constrained
                    // to a list, display the label instead of the value
                    if ((property != null && property.isInList() ||
                        propertyType != null && (propertyType.isEnum() || propertyType.isArray() && propertyType.getComponentType().isEnum()))) {
                        builderTemplate.setValue(getIdValue(), generateLabel(template, templateFieldName, value));
                    } else {
                        builderTemplate.setValue(getIdValue(), value);
                    }
                }

                // replace the form field tag in the provided template by the
                // newly constructed functional form field
                var field_content = builderTemplate.getBlock(PREFIX_FORM_DISPLAY);
                if (0 == counter) {
                    template.setValue(field, field_content);
                } else {
                    template.appendValue(field, field_content);
                }
            }
            while (values != null && values.length > 1 && ++counter < values.length);

            // register the form fields tag in the list of value ids that
            // have been set
            setValues.add(field);

            // clear out template
            builderTemplate.removeValue(getIdValue());
            builderTemplate.removeValue(getIdAttributes());
        }
    }

    protected String generateLabel(Template template, String templateFieldName, String value)
    throws TemplateException {
        StringBuilder label_id_buffer;
        String label_id;
        StringBuilder label_valueid_buffer;
        String label_valueid;

        // create the label id
        label_id_buffer = new StringBuilder(templateFieldName.length() + 1 + value.length());
        label_id_buffer.append(templateFieldName);
        label_id_buffer.append(":");
        label_id_buffer.append(value);
        label_id = label_id_buffer.toString();

        // create the label value id
        label_valueid_buffer = new StringBuilder(PREFIX_FORM_LABEL.length() + label_id.length());
        label_valueid_buffer.append(PREFIX_FORM_LABEL);
        label_valueid_buffer.append(label_id);
        label_valueid = label_valueid_buffer.toString();

        String label = null;

        // set a custom label if it's available from the resource bundle
        if (template.hasResourceBundles()) {
            for (var bundle : template.getResourceBundles()) {
                // obtain the configuration value
                try {
                    label = bundle.getString(label_id);
                    if (label != null) {
                        label = template.getEncoder().encode(label);
                    }
                    break;
                } catch (MissingResourceException e) {
                    // whenever a resource can't be found, just use the next one in the collection
                }
            }
        }

        // set a custom label if it's available from the template
        if (null == label &&
            template.hasBlock(label_valueid)) {
            label = template.getBlock(label_valueid);
        }

        // otherwise use the value
        if (null == label) {
            label = value;
        }

        return label;
    }

    public void removeForm(Template template, Class beanClass, String prefix)
    throws BeanUtilsException {
        if (null == template ||
            null == beanClass) {
            return;
        }

        // create an instance of the bean
        Object bean;
        try {
            bean = beanClass.getDeclaredConstructor().newInstance();
        } catch (Throwable e) {
            bean = null;
        }

        // generate the form fields
        var property_names = BeanUtils.getPropertyNames(beanClass, null, null, null);
        for (var property_name : property_names) {
            removeField(template, property_name, prefix);
        }

        Validated validated;
        if (bean instanceof Validated) {
            validated = (Validated) bean;

            getValidationBuilder().removeValidationErrors(template, validated.getValidatedSubjects(), prefix);
            getValidationBuilder().removeErrorMarkings(template, validated.getValidatedSubjects(), prefix);
        }
    }

    public void removeField(Template template, String name, String prefix) {
        if (null == template ||
            null == name ||
            0 == name.length()) {
            return;
        }

        if (prefix != null) {
            name = prefix + name;
        }

        removeField(template, name);
    }

    public void removeField(Template template, String templateFieldName) {
        if (null == template ||
            null == templateFieldName ||
            0 == templateFieldName.length()) {
            return;
        }

        String value_id;
        for (var value_prefix : VALUE_PREFIXES) {
            value_id = value_prefix + templateFieldName;
            if (template.isValueSet(value_id) || template.isValueGenerated(value_id)) {
                template.removeValue(value_id);
            }
        }
    }

    public Collection<String> selectParameter(Template template, String name, String[] values) {
        var set_values = new ArrayList<String>();

        if (null == template ||
            null == name ||
            0 == name.length() ||
            null == values ||
            0 == values.length) {
            return set_values;
        }

        StringBuilder value_id_buffer;
        String value_id;
        for (var value : values) {
            if (null == value) {
                continue;
            }

            value_id_buffer = new StringBuilder(name);
            value_id_buffer.append(":");
            value_id_buffer.append(value);

            value_id = value_id_buffer + SUFFIX_SELECTED;
            if (template.hasValueId(value_id)) {
                template.setValue(value_id, getValueSelected());
                set_values.add(value_id);
            }

            value_id = value_id_buffer + SUFFIX_CHECKED;
            if (template.hasValueId(value_id)) {
                template.setValue(value_id, getValueChecked());
                set_values.add(value_id);
            }

            value_id = name + SUFFIX_CHECKED;
            if (template.hasValueId(value_id)) {
                if (StringUtils.convertToBoolean(value)) {
                    template.setValue(value_id, getValueChecked());
                    set_values.add(value_id);
                }
            }
        }

        template.addGeneratedValues(set_values);

        return set_values;
    }

    public void unselectParameter(Template template, String name, String[] values) {
        if (null == template ||
            null == name ||
            0 == name.length()) {
            return;
        }

        String value_id;

        value_id = name + SUFFIX_CHECKED;
        if (template.hasValueId(value_id)) {
            template.removeValue(value_id);
        }

        if (null == values ||
            0 == values.length) {
            return;
        }

        StringBuilder value_id_buffer;
        for (var value : values) {
            if (null == value) {
                continue;
            }

            value_id_buffer = new StringBuilder(name);
            value_id_buffer.append(":");
            value_id_buffer.append(value);

            value_id = value_id_buffer + SUFFIX_SELECTED;
            if (template.hasValueId(value_id)) {
                template.removeValue(value_id);
            }

            value_id = value_id_buffer + SUFFIX_CHECKED;
            if (template.hasValueId(value_id)) {
                template.removeValue(value_id);
            }
        }
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // this should never happen
            Logger.getLogger("rife.forms").severe(ExceptionUtils.getExceptionStackTrace(e));
            return null;
        }
    }
}