/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.forms;

import rife.template.Template;
import rife.tools.exceptions.BeanUtilsException;
import rife.validation.ConstrainedProperty;
import rife.validation.Validated;
import rife.validation.ValidationBuilder;

import java.util.Collection;
import java.util.Map;

/**
 * This interface defines the methods for bean-centric form generation.
 * <p>All the fields in a form can be generated at once by using the
 * <code>generateForm</code> methods, or individual fields can be generated
 * through the <code>generateField</code> method. It's also possible to remove
 * the form generation for all the fields or for one field in particular
 * through the <code>removeForm</code> and <code>removeField</code> methods.
 * <p>When generating a form of a {@link Validated} bean instance, and
 * associated {@link ValidationBuilder} will be used to also generate the
 * validation errors and markings.
 * <p>The form building works with a {@link Template} instance that it will
 * analyze for the presence of certain values. The names of these will be in
 * the following format: <code>form:prefix:name</code> where
 * <code>form:prefix:</code> identifies which type of field has to be generated
 * and <code>name</code> indicates the name of the property for which the form
 * field will be generated.
 * <p>The form builder will generally use the constraints on the bean property
 * to generate the form field with the appropriate metadata information.
 * Many times it's appropriate to add custom information to the form
 * fields. This can be done by providing text in between the form field value
 * begin and end tags. For example, for the HTML form builder you can provide
 * custom CSS style information:
 * <pre>
 * &lt;!--v form:input:firstname--&gt;style="border: 1px solid black;"&lt;!--/v--&gt;
 * </pre>
 * If the custom attributes are dynamic and contain value tags, then you should
 * use a dedicated block template tag that has an <code>attributes:</code>
 * middle part, for example:
 * <pre>
 * &lt;!--v form:input:firstname/--&gt;
 * &lt;!--b form:input:attributes:firstname--&gt;style="border: 1px solid {{v color/}};"&lt;!--/b--&gt;
 * </pre>
 * <p>By default, the generation of the form fields only includes the field
 * itself. You can however customize this by specifying a block tag that has
 * the same name as the value tag that will be filled in with the generated
 * form field. This bock tag supports the following value tags:
 * <code>form:label</code>, <code>form:name</code>, <code>form:value</code> and
 * <code>form:field</code>. If one of these is present, it will be replaced
 * respectively with: the label of the form field, the name of the property,
 * the current value of the property, and the generated form fields.
 * For example:
 * <pre>
 * &lt;!--b form:input:firstname--&gt;
 * &lt;!--v form:name/--&gt;-&lt;!--v form:value/--&gt; : &lt;!--v form:field/--&gt;
 * &lt;!--/b-->
 * </pre>
 * could be rendered like this with the HTML form builder:
 * <pre>
 * firstname-John : &lt;input type="text" name="firstname" /&gt;
 * </pre>
 * <p>
 * Additional template values are available for collection field, please
 * look at {@link #selectParameter} for more information.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see Validated
 * @see ValidationBuilder
 * @since 1.0
 */
public interface FormBuilder extends Cloneable {
    /**
     * Template values that start with <code>form:hidden:</code> will generate
     * a hidden form field for the property name that follows the prefix.
     *
     * @since 1.0
     */
    String PREFIX_FORM_HIDDEN = "form:hidden:";

    /**
     * Template values that start with <code>form:input:</code> will generate
     * a text input form field for the property name that follows the prefix.
     * The maximum length of the allowed text will be obtained from the
     * <code>maxLength</code> constraint.
     *
     * @since 1.0
     */
    String PREFIX_FORM_INPUT = "form:input:";

    /**
     * Template values that start with <code>form:secret:</code> will generate
     * a password input form field for the property name that follows the
     * prefix. The maximum length of the allowed text will be obtained from the
     * <code>maxLength</code> constraint.
     *
     * @since 1.0
     */
    String PREFIX_FORM_SECRET = "form:secret:";

    /**
     * Template values that start with <code>form:textarea:</code> will
     * generate a multi-line text form field for the property name that follows
     * the prefix.
     *
     * @since 1.0
     */
    String PREFIX_FORM_TEXTAREA = "form:textarea:";

    /**
     * Template values that start with <code>form:radio:</code> will
     * generate a radio button group for the property name that follows the
     * prefix. The possible radio button values will be obtained from the
     * <code>inList</code> constraint,
     *
     * @since 1.0
     */
    String PREFIX_FORM_RADIO = "form:radio:";

    /**
     * Template values that start with <code>form:checkbox:</code> will
     * generate a checkbox group for the property name that follows the
     * prefix. The possible checkbox values will be obtained from the
     * <code>inList</code> constraint,
     *
     * @since 1.0
     */
    String PREFIX_FORM_CHECKBOX = "form:checkbox:";

    /**
     * Template values that start with <code>form:select:</code> will
     * generate a select list for the property name that follows the
     * prefix. The possible select list options will be obtained from the
     * <code>inList</code> constraint,
     *
     * @since 1.0
     */
    String PREFIX_FORM_SELECT = "form:select:";

    /**
     * Template values that start with <code>form:display:</code> will
     * not actually generate a form field but simply display the current value
     * as text.
     *
     * @since 1.0
     */
    String PREFIX_FORM_DISPLAY = "form:display:";

    /**
     * Template blocks that start with <code>form:label:</code> provide a
     * textual label for a specific value for a property name. The format after
     * the prefix is <code>propertyname:propertyvalue</code>.
     * <p>For example:
     * <pre>
     * &lt;!--b form:label:options:1--&gt;the first option&lt;!--/b--&gt;
     * &lt;!--b form:label:options:2--&gt;the second option&lt;!--/b--&gt;
     * </pre>
     *
     * @since 1.0
     */
    String PREFIX_FORM_LABEL = "form:label:";

    /**
     * A template block with an <code>ATTRIBUTES</code> middle part after the
     * standard form field prefix, allows for dynamic custom attributes
     * specifications for the form field generation.
     *
     * @since 1.0
     */
    String MIDDLE_ATTRIBUTES = "attributes:";

    /**
     * Template value tags with the <code>form:label</code> name will be
     * replaced by the label if they are present inside block tags that have
     * the same name as the template value that will contain the generated
     * form field. (More information in the {@link FormBuilder} interface
     * Javadocs)
     *
     * @since 1.0
     */
    String ID_FORM_LABEL = "form:label";

    /**
     * Template value tags with the <code>form:field</code> name will be
     * replaced by the actual generated form field if they are present inside
     * block tags that have the same name as the template value that will
     * contain the generated form field. (More information in the
     * {@link FormBuilder} interface Javadocs)
     *
     * @since 1.0
     */
    String ID_FORM_FIELD = "form:field";

    /**
     * Template value tags with the <code>form:name</code> name will be
     * replaced by the field name if they are present inside block tags that
     * have the same name as the template value that will contain the generated
     * form field. (More information in the {@link FormBuilder} interface
     * Javadocs)
     *
     * @since 1.0
     */
    String ID_FORM_NAME = "form:name";

    /**
     * Template value tags with the <code>form:value</code> name will be
     * replaced by the current field value if they are present inside block
     * tags that have the same name as the template value that will contain
     * the generated form field. (More information in the {@link FormBuilder}
     * interface Javadocs)
     *
     * @since 1.0
     */
    String ID_FORM_VALUE = "form:value";

    String SUFFIX_SELECTED = ":selected";
    String SUFFIX_CHECKED = ":checked";

    /**
     * Generates all the form fields for a bean class.
     * <p>If content has already been filled in for any of the template values
     * that are normally replaced with generated form fields, no generation
     * will happen for those and the existing content will simply remain
     * present.
     *
     * @param template  the template instance that will be used for the form
     *                  generation
     * @param beanClass the bean class whose properties will be analyzed for
     *                  the form generation
     * @param values    a map of name-value pairs that indicate the currently
     *                  active context for the form that will be generated, these are typically
     *                  the values that have been submitted previously through a web page. The
     *                  values will be used to pre-populate the form fields with content.
     * @param prefix    the prefix of the bean property names that should be used
     *                  while generating the form, this is handy to use when the several forms
     *                  are present that use the same bean class or that has overlapping
     *                  property names
     * @return the collection of template value names that have been generated
     * @throws BeanUtilsException when errors occurred during the form
     *                            generation
     * @since 1.0
     */
    Collection<String> generateForm(Template template, Class beanClass, Map<String, String[]> values, String prefix)
    throws BeanUtilsException;

    /**
     * Generates all the form fields for a bean instance.
     * <p>If content has already been filled in for any of the template values
     * that are normally replaced with generated form fields, no generation
     * will happen for those and the existing content will simply remain
     * present.
     *
     * @param template the template instance that will be used for the form
     *                 generation
     * @param bean     the bean instance whose properties and validation errors
     *                 will be analyzed for the form generation
     * @param values   a map of name-value pairs that indicate the currently
     *                 active context for the form that will be generated, these are typically
     *                 the values that have been submitted previously through a web page. The
     *                 values will be used to pre-populate the form fields with content.
     * @param prefix   the prefix of the bean property names that should be used
     *                 while generating the form, this is handy to use when the several forms
     *                 are present that use the same bean class or that has overlapping
     *                 property names
     * @return the collection of template value names that have been generated
     * @throws BeanUtilsException when errors occurred during the form
     *                            generation
     * @since 1.0
     */
    Collection<String> generateForm(Template template, Object bean, Map<String, String[]> values, String prefix)
    throws BeanUtilsException;

    /**
     * Generates a form field for one particular property.
     * <p>If content has already been filled in for any of the template values
     * that are normally replaced with generated form fields, no generation
     * will happen for those and the existing content will simply remain
     * present.
     *
     * @param template the template instance that will be used for the form
     *                 generation
     * @param property the constrained property that the form field will be
     *                 generated for
     * @param values   the current values of the property, these are typically
     *                 the values that have been submitted previously through a web page. The
     *                 values will be used to pre-populate the form field with content.
     * @param prefix   the prefix of the bean property names that should be used
     *                 while generating the form, this is handy to use when the several forms
     *                 are present that use the same bean class or that has overlapping
     *                 property names
     * @return the collection of template value names that have been generated
     * @since 1.0
     */
    Collection<String> generateField(Template template, ConstrainedProperty property, String[] values, String prefix);

    /**
     * Generates a form field for one particular property.
     * <p>If content has already been filled in for any of the template values
     * that are normally replaced with generated form fields, no generation
     * will happen for those and the existing content will simply remain
     * present.
     *
     * @param template     the template instance that will be used for the form
     *                     generation
     * @param propertyType the type of the property
     * @param property     the constrained property that the form field will be
     *                     generated for
     * @param values       the current values of the property, these are typically
     *                     the values that have been submitted previously through a web page. The
     *                     values will be used to pre-populate the form field with content.
     * @param prefix       the prefix of the bean property names that should be used
     *                     while generating the form, this is handy to use when the several forms
     *                     are present that use the same bean class or that has overlapping
     *                     property names
     * @return the collection of template value names that have been generated
     * @since 1.0
     */
    Collection<String> generateField(Template template, Class propertyType, ConstrainedProperty property, String[] values, String prefix);

    /**
     * Generates a form field for one particular property name.
     * <p>If content has already been filled in for any of the template values
     * that are normally replaced with generated form fields, no generation
     * will happen for those and the existing content will simply remain
     * present.
     *
     * @param template the template instance that will be used for the form
     *                 generation
     * @param name     the name of the property that the form field will be
     *                 generated for
     * @param values   the current values of the property, these are typically
     *                 the values that have been submitted previously through a web page. The
     *                 values will be used to pre-populate the form field with content.
     * @param prefix   the prefix of the bean property names that should be used
     *                 while generating the form, this is handy to use when the several forms
     *                 are present that use the same bean class or that has overlapping
     *                 property names
     * @return the collection of template value names that have been generated
     * @since 1.0
     */
    Collection<String> generateField(Template template, String name, String[] values, String prefix);

    /**
     * Generates a form field for one particular property name.
     * <p>If content has already been filled in for any of the template values
     * that are normally replaced with generated form fields, no generation
     * will happen for those and the existing content will simply remain
     * present.
     *
     * @param template     the template instance that will be used for the form
     *                     generation
     * @param propertyType the type of the property
     * @param name         the name of the property that the form field will be
     *                     generated for
     * @param values       the current values of the property, these are typically
     *                     the values that have been submitted previously through a web page. The
     *                     values will be used to pre-populate the form field with content.
     * @param prefix       the prefix of the bean property names that should be used
     *                     while generating the form, this is handy to use when the several forms
     *                     are present that use the same bean class or that has overlapping
     *                     property names
     * @return the collection of template value names that have been generated
     * @since 1.0
     */
    Collection<String> generateField(Template template, Class propertyType, String name, String[] values, String prefix);

    /**
     * Generates a form field for one particular property and always replace
     * the content of the template values that match the auto-generation
     * name format.
     * <p>This method also adds a <code>templateFieldName</code> parameter.
     * It allows you to use another property name for the template values
     * than the one of the actual bean property. You will typically use this
     * when you dynamically generate a form and iterate over the generation
     * of certain fields. So, for example, when you have properties named
     * <code>answer1</code> and <code>answer2</code>, you can use this
     * template snippet:
     * <pre>
     * &lt;!--v answers/--&gt;
     * &lt;!--b answer--&gt;
     * &lt;!--v form:input:answer/--&gt;
     * &lt;!--/b--&gt;
     * </pre>
     * By using the <code>replaceField(template, "answer", property, null, null)</code>
     * method call for each answer property and appending the
     * <code>answer</code> block to the <code>answers</code> value, you can
     * benefit from the automatic form field generation but still dynamically
     * aggregate the results into one area.
     *
     * @param template          the template instance that will be used for the form
     *                          generation
     * @param templateFieldName the name of the form field that will be used to
     *                          look for supported value tags in the property template
     * @param property          the constrained property that the form field will be
     *                          generated for
     * @param values            the current values of the property, these are typically
     *                          the values that have been submitted previously through a web page. The
     *                          values will be used to pre-populate the form field with content.
     * @param prefix            the prefix of the bean property names that should be used
     *                          while generating the form, this is handy to use when the several forms
     *                          are present that use the same bean class or that has overlapping
     *                          property names
     * @return the collection of template value names that have been generated
     * @since 1.0
     */
    Collection<String> replaceField(Template template, String templateFieldName, ConstrainedProperty property, String[] values, String prefix);

    /**
     * Generates a form field for one particular property and always replace
     * the content of the template values that match the auto-generation
     * name format.
     * <p>The documentation of the
     * {@link #replaceField(Template, String, ConstrainedProperty, String[], String) previous replaceField method}
     * contains more information about the functionality of this method.
     *
     * @param template          the template instance that will be used for the form
     *                          generation
     * @param templateFieldName the name of the form field that will be used to
     *                          look for supported value tags in the property template
     * @param propertyType      the type of the property
     * @param property          the constrained property that the form field will be
     *                          generated for
     * @param values            the current values of the property, these are typically
     *                          the values that have been submitted previously through a web page. The
     *                          values will be used to pre-populate the form field with content.
     * @param prefix            the prefix of the bean property names that should be used
     *                          while generating the form, this is handy to use when the several forms
     *                          are present that use the same bean class or that has overlapping
     *                          property names
     * @return the collection of template value names that have been generated
     * @since 1.0
     */
    Collection<String> replaceField(Template template, String templateFieldName, Class propertyType, ConstrainedProperty property, String[] values, String prefix);

    /**
     * Generates a form field for one particular property name and always
     * replace the content of the template values that match the
     * auto-generation name format.
     * <p>The documentation of the
     * {@link #replaceField(Template, String, ConstrainedProperty, String[], String) previous replaceField method}
     * contains more information about the functionality of this method.
     *
     * @param template          the template instance that will be used for the form
     *                          generation
     * @param templateFieldName the name of the form field that will be used to
     *                          look for supported value tags in the property template
     * @param name              the name of the property that the form field will be
     *                          generated for
     * @param values            the current values of the property, these are typically
     *                          the values that have been submitted previously through a web page. The
     *                          values will be used to pre-populate the form field with content.
     * @param prefix            the prefix of the bean property names that should be used
     *                          while generating the form, this is handy to use when the several forms
     *                          are present that use the same bean class or that has overlapping
     *                          property names
     * @return the collection of template value names that have been generated
     * @see #replaceField(Template, String, ConstrainedProperty, String[], String)
     * @since 1.0
     */
    Collection<String> replaceField(Template template, String templateFieldName, String name, String[] values, String prefix);

    /**
     * Generates a form field for one particular property name and always
     * replace the content of the template values that match the
     * auto-generation name format.
     * <p>The documentation of the
     * {@link #replaceField(Template, String, ConstrainedProperty, String[], String) previous replaceField method}
     * contains more information about the functionality of this method.
     *
     * @param template          the template instance that will be used for the form
     *                          generation
     * @param templateFieldName the name of the form field that will be used to
     *                          look for supported value tags in the property template
     * @param propertyType      the type of the property
     * @param name              the name of the property that the form field will be
     *                          generated for
     * @param values            the current values of the property, these are typically
     *                          the values that have been submitted previously through a web page. The
     *                          values will be used to pre-populate the form field with content.
     * @param prefix            the prefix of the bean property names that should be used
     *                          while generating the form, this is handy to use when the several forms
     *                          are present that use the same bean class or that has overlapping
     *                          property names
     * @return the collection of template value names that have been generated
     * @see #replaceField(Template, String, ConstrainedProperty, String[], String)
     * @since 1.0
     */
    Collection<String> replaceField(Template template, String templateFieldName, Class propertyType, String name, String[] values, String prefix);

    /**
     * Removes the content of all the template values that would otherwise be
     * filled in with generated form fields through the {@link #generateForm(Template, Class, Map, String)}
     * method.
     *
     * @param template  the template instance whose values will be cleared
     * @param beanClass the bean class whose properties will be analyzed for
     *                  clearing the form field values
     * @param prefix    the prefix of the bean property names that should be used
     *                  while clearing the form, this is handy to use when the several forms
     *                  are present that use the same bean class or that has overlapping
     *                  property names
     * @throws BeanUtilsException when errors occurred during the clearing of
     *                            the form
     * @see #generateForm
     * @since 1.0
     */
    void removeForm(Template template, Class beanClass, String prefix)
    throws BeanUtilsException;

    /**
     * Removes the content of the template value that would otherwise be
     * filled in with a generated form field through a {@link #generateField}
     * method.
     *
     * @param template the template instance whose values will be cleared
     * @param name     the name of the property for which the template value will
     *                 be cleared
     * @param prefix   the prefix of the bean property names that should be used
     *                 while clearing the form, this is handy to use when the several forms
     *                 are present that use the same bean class or that has overlapping
     *                 property names
     * @see #generateField
     * @since 1.0
     */
    void removeField(Template template, String name, String prefix);

    /**
     * Removes the content of the template value that would otherwise be
     * filled in with a generated form field through a {@link #replaceField}
     * method.
     *
     * @param template          the template instance whose values will be cleared
     * @param templateFieldName the that is use in the template values to
     *                          identify the property that has to be cleared
     * @see #replaceField(Template, String, ConstrainedProperty, String[], String)
     * @since 1.0
     */
    void removeField(Template template, String templateFieldName);

    /**
     * Generates the required attributes so that an existing form field
     * indicates its checked or selected status.
     * <p>This method will check the template for certain value tags and set
     * them to the correct attributes according to the name and the provided
     * values in this method. This is dependent on the template type and
     * currently only makes sense for {@code html} templates.
     * <p>For example for select boxes, consider the name '{@code colors}',
     * the values '{@code blue}' and '{@code red}', and the
     * following HTML template excerpt:
     * <pre>
     * &lt;select name="colors"&gt;
     *   &lt;option value="blue"{{v colors:blue:selected}}{{/v}}&gt;Blue&lt;/option&gt;
     *   &lt;option value="orange"{{v colors:orange:selected}}{{/v}}&gt;Orange&lt;/option&gt;
     *   &lt;option value="red"{{v colors:red:selected}}{{/v}}&gt;Red&lt;/option&gt;
     *   &lt;option value="green"{{v colors:green:selected'}}{{/v}}&gt;Green&lt;/option&gt;
     * &lt;/select&gt;</pre>
     * <p>the result will then be:
     * <pre>
     * &lt;select name="colors"&gt;
     *   &lt;option value="blue" selected="selected"&gt;Blue&lt;/option&gt;
     *   &lt;option value="orange"&gt;Orange&lt;/option&gt;
     *   &lt;option value="red" selected="selected"&gt;Red&lt;/option&gt;
     *   &lt;option value="green"&gt;Green&lt;/option&gt;
     * &lt;/select&gt;</pre>
     * <p>For example for radio buttons, consider the name '{@code size}',
     * the value '{@code large}' and the following HTML template excerpt:
     * <pre>
     * &lt;input type="radio" name="size" value="large"{{v size:large:checked}}{{/v}} /&gt;
     * &lt;input type="radio" name="size" value="small"{{v size:small:checked}}{{/v}} /&gt;
     * </pre>
     * <p>the result will then be:
     * <pre>
     * &lt;input type="radio" name="size" value="large" checked="checked" /&gt;
     * &lt;input type="radio" name="size" value="small" /&gt;
     * </pre>
     * <p>For example for checkboxes, consider the name '{@code active}',
     * the value '{@code true}' and the following HTML template excerpt:
     * <pre>
     * &lt;input type="checkbox" name="active"{{v active:checked}}{{/v}} /&gt;
     * &lt;input type="checkbox" name="senditnow"{{v senditnow:checked}}{{/v}} /&gt;
     * </pre>
     * <p>the result will then be:
     * <pre>
     * &lt;input type="checkbox" name="active" checked="checked" /&gt;
     * &lt;input type="checkbox" name="senditnow" /&gt;
     * </pre>
     *
     * @param template the template instance that will be used for the
     *                 generation
     * @param name     the name of the parameter
     * @param values   the values of the parameter
     * @return the collection of template value names that have been generated
     */
    Collection<String> selectParameter(Template template, String name, String[] values);

    /**
     * Removes the generated attributes that indicate that an existing form
     * field is checked or selected
     *
     * @param template the template instance that will be used for the clearing
     * @param name     the name of the parameter
     * @param values   the values of the parameter
     */
    void unselectParameter(Template template, String name, String[] values);

    /**
     * Returns the <code>ValidationBuilder</code> that is used by this
     * <code>FormBuilder</code>.
     *
     * @return this <code>FormBuilder</code>'s <code>ValidationBuilder</code>
     * @since 1.0
     */
    ValidationBuilder getValidationBuilder();

    Object clone();
}
