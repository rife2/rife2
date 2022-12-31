/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.forms;

import rife.template.Template;
import rife.template.TemplateFactory;
import rife.validation.ConstrainedProperty;
import rife.validation.ValidationBuilder;
import rife.validation.ValidationBuilderHtml;

import java.util.List;

public class FormBuilderHtml extends AbstractFormBuilder {
    public static final String VALUE_SELECTED = " selected=\"selected\"";
    public static final String VALUE_CHECKED = " checked=\"checked\"";

    private static final String ID_NAME = "NAME";
    private static final String ID_ATTRIBUTES = "ATTRIBUTES";
    private static final String ID_VALUE = "VALUE";
    private static final String ID_MAXLENGTH = "MAXLENGTH";
    private static final String ID_CHECKED = "CHECKED";
    private static final String ID_OPTIONS = "OPTIONS";
    private static final String ID_SELECTED = "SELECTED";
    private static final String ID_LABEL = "LABEL";
    private static final String ID_DISABLED = "DISABLED";
    private static final String ID_FORM_OPTION = "FORM:OPTION:";

    private ValidationBuilder mValidationBuilder = new ValidationBuilderHtml();

    public ValidationBuilder getValidationBuilder() {
        return mValidationBuilder;
    }

    protected Template getBuilderTemplateInstance() {
        return TemplateFactory.HTML.get("formbuilder.html.fields");
    }

    protected String sanitizeAttributes(String value) {
        if (null == value) {
            return value;
        }
        return " " + value.trim();
    }

    protected void generateFieldHidden(Template template, String templateFieldName, String name, ConstrainedProperty property, String[] values, Template builderTemplate, List<String> setValues, boolean replaceExistingValues) {
        generateFieldText(PREFIX_FORM_HIDDEN, true, true, false, false, template, templateFieldName, name, property, values, builderTemplate, setValues, replaceExistingValues);
    }

    protected void generateFieldInput(Template template, String templateFieldName, String name, ConstrainedProperty property, String[] values, Template builderTemplate, List<String> setValues, boolean replaceExistingValues) {
        generateFieldText(PREFIX_FORM_INPUT, true, true, true, true, template, templateFieldName, name, property, values, builderTemplate, setValues, replaceExistingValues);
    }

    protected void generateFieldSecret(Template template, String templateFieldName, String name, ConstrainedProperty property, String[] values, Template builderTemplate, List<String> setValues, boolean replaceExistingValues) {
        generateFieldText(PREFIX_FORM_SECRET, false, true, true, true, template, templateFieldName, name, property, values, builderTemplate, setValues, replaceExistingValues);
    }

    protected void generateFieldTextarea(Template template, String templateFieldName, String name, ConstrainedProperty property, String[] values, Template builderTemplate, List<String> setValues, boolean replaceExistingValues) {
        generateFieldText(PREFIX_FORM_TEXTAREA, true, false, false, true, template, templateFieldName, name, property, values, builderTemplate, setValues, replaceExistingValues);
    }

    protected void generateFieldRadio(Template template, String templateFieldName, Class propertyType, String name, ConstrainedProperty property, String[] values, Template builderTemplate, List<String> setValues, boolean replaceExistingValues) {
        generateFieldCollection(PREFIX_FORM_RADIO, true, template, templateFieldName, propertyType, name, property, values, builderTemplate, setValues, replaceExistingValues);
    }

    protected void generateFieldCheckbox(Template template, String templateFieldName, Class propertyType, String name, ConstrainedProperty property, String[] values, Template builderTemplate, List<String> setValues, boolean replaceExistingValues) {
        generateFieldCollection(PREFIX_FORM_CHECKBOX, false, template, templateFieldName, propertyType, name, property, values, builderTemplate, setValues, replaceExistingValues);
    }

    protected String getIdName() {
        return ID_NAME;
    }

    protected String getIdAttributes() {
        return ID_ATTRIBUTES;
    }

    protected String getIdValue() {
        return ID_VALUE;
    }

    protected String getIdMaxlength() {
        return ID_MAXLENGTH;
    }

    protected String getIdChecked() {
        return ID_CHECKED;
    }

    protected String getIdOptions() {
        return ID_OPTIONS;
    }

    protected String getIdSelected() {
        return ID_SELECTED;
    }

    protected String getIdLabel() {
        return ID_LABEL;
    }

    protected String getIdDisabled() {
        return ID_DISABLED;
    }

    protected String getIdFormOption() {
        return ID_FORM_OPTION;
    }

    protected String getValueSelected() {
        return VALUE_SELECTED;
    }

    protected String getValueChecked() {
        return VALUE_CHECKED;
    }

    public Object clone() {
        FormBuilderHtml other = (FormBuilderHtml) super.clone();

        other.mValidationBuilder = (ValidationBuilder) this.mValidationBuilder.clone();

        return other;
    }
}
