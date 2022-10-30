/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.forms;

import org.junit.jupiter.api.Test;
import rife.template.Template;
import rife.template.TemplateFactory;
import rife.tools.ExceptionUtils;
import rife.tools.StringUtils;
import rife.tools.exceptions.BeanUtilsException;
import rife.validation.ConstrainedBeanImpl;
import rife.validation.ConstrainedProperty;
import rife.validation.RegularBeanImpl;
import rife.validation.ValidationError;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class TestFormBuilderHtml {
    public enum RadioInListEnum {a1, a3, a2}

    public enum CheckboxInListEnum {
        v1(1), v3(3), v2(2);

        private int mNumber;

        CheckboxInListEnum(int number) {
            mNumber = number;
        }

        public String toString() {
            return String.valueOf(mNumber);
        }
    }

    public enum SelectInListEnum {black, red, green, blue}

    public enum SelectInListEnum2 {black, red, blue}

    public enum SelectInListEnum3 {
        v1(1), v3(3), v5(5), v9(9);

        private int mNumber;

        SelectInListEnum3(int number) {
            mNumber = number;
        }

        public String toString() {
            return String.valueOf(mNumber);
        }
    }

    @Test
    public void testInstantiate() {
        FormBuilderHtml builder = new FormBuilderHtml();
        assertNotNull(builder);
    }

    @Test
    public void testClone() {
        FormBuilderHtml builder1 = new FormBuilderHtml();
        FormBuilderHtml builder2 = (FormBuilderHtml) builder1.clone();
        assertNotNull(builder2);
        assertNotSame(builder1, builder2);
    }

    @Test
    public void testGetValidationBuilder() {
        FormBuilderHtml builder = new FormBuilderHtml();
        assertNotNull(builder.getValidationBuilder());
    }

    @Test
    public void testGenerateFieldInvalidArguments() {
        FormBuilderHtml builder = new FormBuilderHtml();
        assertEquals(0, builder.generateField(null, (ConstrainedProperty) null, null, null).size());
        assertEquals(0, builder.generateField(null, (String) null, null, null).size());

        Template template = TemplateFactory.HTML.get("formbuilder_fields");
        String raw_content = template.getContent();
        assertNotNull(template);
        assertEquals(0, builder.generateField(template, (ConstrainedProperty) null, null, null).size());
        assertEquals(raw_content, template.getContent());
        assertEquals(0, builder.generateField(template, (String) null, null, null).size());
        assertEquals(raw_content, template.getContent());
    }

    @Test
    public void testRemoveFieldInvalidArguments() {
        FormBuilderHtml builder = new FormBuilderHtml();
        builder.removeField(null, null, null);

        Template template = TemplateFactory.HTML.get("formbuilder_fields");
        String raw_content = template.getContent();
        assertNotNull(template);
        builder.removeField(template, null, null);
        assertEquals(raw_content, template.getContent());
        builder.removeField(template, "", null);
        assertEquals(raw_content, template.getContent());
    }

    @Test
    public void testGenerateFieldHiddenWithDefault() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        set_values = builder.generateField(template, "hidden", null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:hidden:hidden", set_values.iterator().next());
        assertEquals("<input type=\"hidden\" name=\"hidden\" id=\"thehiddenone\" />", StringUtils.splitToArray(template.getContent(), "\n")[52]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("hidden"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:hidden:hidden", set_values.iterator().next());
        assertEquals("<input type=\"hidden\" name=\"hidden\" id=\"thehiddenone\" />", StringUtils.splitToArray(template.getContent(), "\n")[52]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("hidden"), new String[]{null, "één"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:hidden:hidden", set_values.iterator().next());
        assertEquals("<input type=\"hidden\" name=\"hidden\" id=\"thehiddenone\" />", StringUtils.splitToArray(template.getContent(), "\n")[52]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("hidden"), new String[]{"één"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:hidden:hidden", set_values.iterator().next());
        assertEquals("<input type=\"hidden\" name=\"hidden\" id=\"thehiddenone\" value=\"&eacute;&eacute;n\" />", StringUtils.splitToArray(template.getContent(), "\n")[52]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("hidden").defaultValue("non&e").maxLength(20), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:hidden:hidden", set_values.iterator().next());
        assertEquals("<input type=\"hidden\" name=\"hidden\" id=\"thehiddenone\" value=\"non&amp;e\" />", StringUtils.splitToArray(template.getContent(), "\n")[52]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("hidden").defaultValue("non&e").maxLength(20), new String[]{"hé", "you"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:hidden:hidden", set_values.iterator().next());
        assertEquals("<input type=\"hidden\" name=\"hidden\" id=\"thehiddenone\" value=\"h&eacute;\" />", StringUtils.splitToArray(template.getContent(), "\n")[52]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        template.setValue("form:hidden:hidden", "already set");
        set_values = builder.generateField(template, new ConstrainedProperty("hidden"), null, null);
        assertEquals(0, set_values.size());
        assertEquals("already set", StringUtils.splitToArray(template.getContent(), "\n")[52]);
        template.removeValue("form:hidden:hidden");
    }

    @Test
    public void testGenerateFieldHiddenWithoutDefault() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        set_values = builder.generateField(template, "anotherhidden", null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:hidden:anotherhidden", set_values.iterator().next());
        assertEquals("<input type=\"hidden\" name=\"anotherhidden\" />", StringUtils.splitToArray(template.getContent(), "\n")[53]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("anotherhidden"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:hidden:anotherhidden", set_values.iterator().next());
        assertEquals("<input type=\"hidden\" name=\"anotherhidden\" />", StringUtils.splitToArray(template.getContent(), "\n")[53]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("anotherhidden"), new String[]{"één"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:hidden:anotherhidden", set_values.iterator().next());
        assertEquals("<input type=\"hidden\" name=\"anotherhidden\" value=\"&eacute;&eacute;n\" />", StringUtils.splitToArray(template.getContent(), "\n")[53]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("anotherhidden").defaultValue("non&e").maxLength(20), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:hidden:anotherhidden", set_values.iterator().next());
        assertEquals("<input type=\"hidden\" name=\"anotherhidden\" value=\"non&amp;e\" />", StringUtils.splitToArray(template.getContent(), "\n")[53]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();
    }

    @Test
    public void testGenerateFieldHiddenPrefix() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        set_values = builder.generateField(template, "hidden", null, "another");
        assertEquals(1, set_values.size());
        assertEquals("form:hidden:anotherhidden", set_values.iterator().next());
        assertEquals("<input type=\"hidden\" name=\"anotherhidden\" />", StringUtils.splitToArray(template.getContent(), "\n")[53]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("hidden"), null, "another");
        assertEquals(1, set_values.size());
        assertEquals("form:hidden:anotherhidden", set_values.iterator().next());
        assertEquals("<input type=\"hidden\" name=\"anotherhidden\" />", StringUtils.splitToArray(template.getContent(), "\n")[53]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        template.setValue("form:hidden:anotherhidden", "already set");
        set_values = builder.generateField(template, new ConstrainedProperty("hidden"), null, "another");
        assertEquals(0, set_values.size());
        assertEquals("already set", StringUtils.splitToArray(template.getContent(), "\n")[53]);
        template.removeValue("form:hidden:anotherhidden");
    }

    @Test
    public void testGenerateFieldHiddenTemplateNameWithDefault() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;

        Template template = TemplateFactory.HTML.get("formbuilder_fields_templatename");

        set_values = builder.replaceField(template, "templatenamehidden", "hidden", null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:hidden:templatenamehidden", set_values.iterator().next());
        assertEquals("<input type=\"hidden\" name=\"hidden\" id=\"thehiddenone\" />", StringUtils.splitToArray(template.getContent(), "\n")[52]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenamehidden", new ConstrainedProperty("hidden"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:hidden:templatenamehidden", set_values.iterator().next());
        assertEquals("<input type=\"hidden\" name=\"hidden\" id=\"thehiddenone\" />", StringUtils.splitToArray(template.getContent(), "\n")[52]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenamehidden", new ConstrainedProperty("hidden"), new String[]{null, "één"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:hidden:templatenamehidden", set_values.iterator().next());
        assertEquals("<input type=\"hidden\" name=\"hidden\" id=\"thehiddenone\" />", StringUtils.splitToArray(template.getContent(), "\n")[52]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenamehidden", new ConstrainedProperty("hidden"), new String[]{"één"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:hidden:templatenamehidden", set_values.iterator().next());
        assertEquals("<input type=\"hidden\" name=\"hidden\" id=\"thehiddenone\" value=\"&eacute;&eacute;n\" />", StringUtils.splitToArray(template.getContent(), "\n")[52]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenamehidden", new ConstrainedProperty("hidden").defaultValue("non&e").maxLength(20), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:hidden:templatenamehidden", set_values.iterator().next());
        assertEquals("<input type=\"hidden\" name=\"hidden\" id=\"thehiddenone\" value=\"non&amp;e\" />", StringUtils.splitToArray(template.getContent(), "\n")[52]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenamehidden", new ConstrainedProperty("hidden").defaultValue("non&e").maxLength(20), new String[]{"hé", "you"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:hidden:templatenamehidden", set_values.iterator().next());
        assertEquals("<input type=\"hidden\" name=\"hidden\" id=\"thehiddenone\" value=\"h&eacute;\" />", StringUtils.splitToArray(template.getContent(), "\n")[52]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        template.setValue("form:hidden:templatenamehidden", "already set");
        set_values = builder.replaceField(template, "templatenamehidden", new ConstrainedProperty("hidden"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:hidden:templatenamehidden", set_values.iterator().next());
        assertEquals("<input type=\"hidden\" name=\"hidden\" id=\"thehiddenone\" />", StringUtils.splitToArray(template.getContent(), "\n")[52]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();
    }

    @Test
    public void testRemoveFieldHidden() {
        FormBuilderHtml builder = new FormBuilderHtml();

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        String raw_content = template.getContent();

        builder.generateField(template, new ConstrainedProperty("hidden").defaultValue("non&e").maxLength(20), new String[]{"hé", "you"}, null);
        assertNotEquals(raw_content, template.getContent());
        builder.removeField(template, "hidden", null);
        assertEquals(raw_content, template.getContent());
    }

    @Test
    public void testRemoveFieldHiddenPrefix() {
        FormBuilderHtml builder = new FormBuilderHtml();

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        String raw_content = template.getContent();

        builder.generateField(template, new ConstrainedProperty("hidden").defaultValue("non&e").maxLength(20), new String[]{"hé", "you"}, "another");
        assertNotEquals(raw_content, template.getContent());
        builder.removeField(template, "hidden", "another");
        assertEquals(raw_content, template.getContent());
    }

    @Test
    public void testRemoveFieldHiddenTemplateName() {
        FormBuilderHtml builder = new FormBuilderHtml();

        Template template = TemplateFactory.HTML.get("formbuilder_fields_templatename");

        String raw_content = template.getContent();

        builder.replaceField(template, "templatenamehidden", new ConstrainedProperty("hidden").defaultValue("non&e").maxLength(20), new String[]{"hé", "you"}, null);
        assertNotEquals(raw_content, template.getContent());
        builder.removeField(template, "templatenamehidden");
        assertEquals(raw_content, template.getContent());
    }

    @Test
    public void testGenerateFieldInputWithDefault() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;
        Iterator<String> set_values_it;

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        set_values = builder.generateField(template, "login", null, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:input:login", set_values_it.next());
        assertEquals("form:display:login", set_values_it.next());
        assertEquals("<input type=\"text\" name=\"login\" size=\"10\" />", StringUtils.splitToArray(template.getContent(), "\n")[0]);
        assertEquals("<div class=\"thedisplayedone\"></div>", StringUtils.splitToArray(template.getContent(), "\n")[54]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("login"), null, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:input:login", set_values_it.next());
        assertEquals("form:display:login", set_values_it.next());
        assertEquals("<input type=\"text\" name=\"login\" size=\"10\" />", StringUtils.splitToArray(template.getContent(), "\n")[0]);
        assertEquals("<div class=\"thedisplayedone\"></div>", StringUtils.splitToArray(template.getContent(), "\n")[54]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("login"), new String[]{null, "één"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:input:login", set_values_it.next());
        assertEquals("form:display:login", set_values_it.next());
        assertEquals("<input type=\"text\" name=\"login\" size=\"10\" />", StringUtils.splitToArray(template.getContent(), "\n")[0]);
        assertEquals("<div class=\"thedisplayedone\"></div><div class=\"thedisplayedone\">&eacute;&eacute;n</div>", StringUtils.splitToArray(template.getContent(), "\n")[54]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("login"), new String[]{"één"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:input:login", set_values_it.next());
        assertEquals("form:display:login", set_values_it.next());
        assertEquals("<input type=\"text\" name=\"login\" size=\"10\" value=\"&eacute;&eacute;n\" />", StringUtils.splitToArray(template.getContent(), "\n")[0]);
        assertEquals("<div class=\"thedisplayedone\">&eacute;&eacute;n</div>", StringUtils.splitToArray(template.getContent(), "\n")[54]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("login").defaultValue("non&e").maxLength(20), null, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:input:login", set_values_it.next());
        assertEquals("form:display:login", set_values_it.next());
        assertEquals("<input type=\"text\" name=\"login\" size=\"10\" value=\"non&amp;e\" maxlength=\"20\" />", StringUtils.splitToArray(template.getContent(), "\n")[0]);
        assertEquals("<div class=\"thedisplayedone\">non&amp;e</div>", StringUtils.splitToArray(template.getContent(), "\n")[54]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("login").defaultValue("non&e").maxLength(20), new String[]{"hé", "you"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:input:login", set_values_it.next());
        assertEquals("form:display:login", set_values_it.next());
        assertEquals("<input type=\"text\" name=\"login\" size=\"10\" value=\"h&eacute;\" maxlength=\"20\" />", StringUtils.splitToArray(template.getContent(), "\n")[0]);
        assertEquals("<div class=\"thedisplayedone\">h&eacute;</div><div class=\"thedisplayedone\">you</div>", StringUtils.splitToArray(template.getContent(), "\n")[54]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        template.setValue("form:input:login", "already set");
        template.setValue("form:display:login", "already set too");
        set_values = builder.generateField(template, new ConstrainedProperty("login"), null, null);
        assertEquals(0, set_values.size());
        assertEquals("already set", StringUtils.splitToArray(template.getContent(), "\n")[0]);
        assertEquals("already set too", StringUtils.splitToArray(template.getContent(), "\n")[54]);
        template.removeValue("form:input:login");
        template.removeValue("form:display:login");
    }

    @Test
    public void testGenerateFieldInputWithoutDefault() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;
        Iterator<String> set_values_it;

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        set_values = builder.generateField(template, "anotherlogin", null, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:input:anotherlogin", set_values_it.next());
        assertEquals("form:display:anotherlogin", set_values_it.next());
        assertEquals("<input type=\"text\" name=\"anotherlogin\" />", StringUtils.splitToArray(template.getContent(), "\n")[1]);
        assertEquals("<div></div>", StringUtils.splitToArray(template.getContent(), "\n")[55]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("anotherlogin"), null, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:input:anotherlogin", set_values_it.next());
        assertEquals("form:display:anotherlogin", set_values_it.next());
        assertEquals("<input type=\"text\" name=\"anotherlogin\" />", StringUtils.splitToArray(template.getContent(), "\n")[1]);
        assertEquals("<div></div>", StringUtils.splitToArray(template.getContent(), "\n")[55]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("anotherlogin"), new String[]{"één"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:input:anotherlogin", set_values_it.next());
        assertEquals("form:display:anotherlogin", set_values_it.next());
        assertEquals("form:input:anotherlogin", set_values.iterator().next());
        assertEquals("<input type=\"text\" name=\"anotherlogin\" value=\"&eacute;&eacute;n\" />", StringUtils.splitToArray(template.getContent(), "\n")[1]);
        assertEquals("<div>&eacute;&eacute;n</div>", StringUtils.splitToArray(template.getContent(), "\n")[55]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("anotherlogin").defaultValue("non&e").maxLength(20), null, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:input:anotherlogin", set_values_it.next());
        assertEquals("form:display:anotherlogin", set_values_it.next());
        assertEquals("form:input:anotherlogin", set_values.iterator().next());
        assertEquals("<input type=\"text\" name=\"anotherlogin\" value=\"non&amp;e\" maxlength=\"20\" />", StringUtils.splitToArray(template.getContent(), "\n")[1]);
        assertEquals("<div>non&amp;e</div>", StringUtils.splitToArray(template.getContent(), "\n")[55]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();
    }

    @Test
    public void testGenerateFieldInputPrefix() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;
        Iterator<String> set_values_it;

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        set_values = builder.generateField(template, "login", null, "another");
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:input:anotherlogin", set_values_it.next());
        assertEquals("form:display:anotherlogin", set_values_it.next());
        assertEquals("<input type=\"text\" name=\"anotherlogin\" />", StringUtils.splitToArray(template.getContent(), "\n")[1]);
        assertEquals("<div></div>", StringUtils.splitToArray(template.getContent(), "\n")[55]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("login"), null, "another");
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:input:anotherlogin", set_values_it.next());
        assertEquals("form:display:anotherlogin", set_values_it.next());
        assertEquals("<input type=\"text\" name=\"anotherlogin\" />", StringUtils.splitToArray(template.getContent(), "\n")[1]);
        assertEquals("<div></div>", StringUtils.splitToArray(template.getContent(), "\n")[55]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        template.setValue("form:input:anotherlogin", "already set");
        template.setValue("form:display:anotherlogin", "already set too");
        set_values = builder.generateField(template, new ConstrainedProperty("login"), null, "another");
        assertEquals(0, set_values.size());
        assertEquals("already set", StringUtils.splitToArray(template.getContent(), "\n")[1]);
        assertEquals("already set too", StringUtils.splitToArray(template.getContent(), "\n")[55]);
        template.removeValue("form:input:anotherlogin");
        template.removeValue("form:display:anotherlogin");
    }

    @Test
    public void testGenerateFieldInputWithDefaultTemplateName() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;
        Iterator<String> set_values_it;

        Template template = TemplateFactory.HTML.get("formbuilder_fields_templatename");

        set_values = builder.replaceField(template, "templatenamelogin", "login", null, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:input:templatenamelogin", set_values_it.next());
        assertEquals("form:display:templatenamelogin", set_values_it.next());
        assertEquals("<input type=\"text\" name=\"login\" size=\"10\" />", StringUtils.splitToArray(template.getContent(), "\n")[0]);
        assertEquals("<div class=\"thedisplayedone\"></div>", StringUtils.splitToArray(template.getContent(), "\n")[54]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenamelogin", new ConstrainedProperty("login"), null, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:input:templatenamelogin", set_values_it.next());
        assertEquals("form:display:templatenamelogin", set_values_it.next());
        assertEquals("<input type=\"text\" name=\"login\" size=\"10\" />", StringUtils.splitToArray(template.getContent(), "\n")[0]);
        assertEquals("<div class=\"thedisplayedone\"></div>", StringUtils.splitToArray(template.getContent(), "\n")[54]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenamelogin", new ConstrainedProperty("login"), new String[]{null, "één"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:input:templatenamelogin", set_values_it.next());
        assertEquals("form:display:templatenamelogin", set_values_it.next());
        assertEquals("<input type=\"text\" name=\"login\" size=\"10\" />", StringUtils.splitToArray(template.getContent(), "\n")[0]);
        assertEquals("<div class=\"thedisplayedone\"></div><div class=\"thedisplayedone\">&eacute;&eacute;n</div>", StringUtils.splitToArray(template.getContent(), "\n")[54]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenamelogin", new ConstrainedProperty("login"), new String[]{"één"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:input:templatenamelogin", set_values_it.next());
        assertEquals("form:display:templatenamelogin", set_values_it.next());
        assertEquals("<input type=\"text\" name=\"login\" size=\"10\" value=\"&eacute;&eacute;n\" />", StringUtils.splitToArray(template.getContent(), "\n")[0]);
        assertEquals("<div class=\"thedisplayedone\">&eacute;&eacute;n</div>", StringUtils.splitToArray(template.getContent(), "\n")[54]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenamelogin", new ConstrainedProperty("login").defaultValue("non&e").maxLength(20), null, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:input:templatenamelogin", set_values_it.next());
        assertEquals("form:display:templatenamelogin", set_values_it.next());
        assertEquals("<input type=\"text\" name=\"login\" size=\"10\" value=\"non&amp;e\" maxlength=\"20\" />", StringUtils.splitToArray(template.getContent(), "\n")[0]);
        assertEquals("<div class=\"thedisplayedone\">non&amp;e</div>", StringUtils.splitToArray(template.getContent(), "\n")[54]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenamelogin", new ConstrainedProperty("login").defaultValue("non&e").maxLength(20), new String[]{"hé", "you"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:input:templatenamelogin", set_values_it.next());
        assertEquals("form:display:templatenamelogin", set_values_it.next());
        assertEquals("<input type=\"text\" name=\"login\" size=\"10\" value=\"h&eacute;\" maxlength=\"20\" />", StringUtils.splitToArray(template.getContent(), "\n")[0]);
        assertEquals("<div class=\"thedisplayedone\">h&eacute;</div><div class=\"thedisplayedone\">you</div>", StringUtils.splitToArray(template.getContent(), "\n")[54]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        template.setValue("form:input:templatenamelogin", "already set");
        template.setValue("form:display:templatenamelogin", "already set too");
        set_values = builder.replaceField(template, "templatenamelogin", new ConstrainedProperty("login"), null, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:input:templatenamelogin", set_values_it.next());
        assertEquals("form:display:templatenamelogin", set_values_it.next());
        assertEquals("<input type=\"text\" name=\"login\" size=\"10\" />", StringUtils.splitToArray(template.getContent(), "\n")[0]);
        assertEquals("<div class=\"thedisplayedone\"></div>", StringUtils.splitToArray(template.getContent(), "\n")[54]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();
    }

    @Test
    public void testRemoveFieldInput() {
        FormBuilderHtml builder = new FormBuilderHtml();

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        String raw_content = template.getContent();

        builder.generateField(template, new ConstrainedProperty("login").defaultValue("non&e").maxLength(20), new String[]{"hé", "you"}, null);
        assertNotEquals(raw_content, template.getContent());
        builder.removeField(template, "login", null);
        assertEquals(raw_content, template.getContent());
    }

    @Test
    public void testRemoveFieldInputPrefix() {
        FormBuilderHtml builder = new FormBuilderHtml();

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        String raw_content = template.getContent();

        builder.generateField(template, new ConstrainedProperty("login").defaultValue("non&e").maxLength(20), new String[]{"hé", "you"}, "another");
        assertNotEquals(raw_content, template.getContent());
        builder.removeField(template, "login", "another");
        assertEquals(raw_content, template.getContent());
    }

    @Test
    public void testRemoveFieldInputTemplateName() {
        FormBuilderHtml builder = new FormBuilderHtml();

        Template template = TemplateFactory.HTML.get("formbuilder_fields_templatename");

        String raw_content = template.getContent();

        builder.replaceField(template, "templatenamelogin", new ConstrainedProperty("login").defaultValue("non&e").maxLength(20), new String[]{"hé", "you"}, null);
        assertNotEquals(raw_content, template.getContent());
        builder.removeField(template, "templatenamelogin");
        assertEquals(raw_content, template.getContent());
    }

    @Test
    public void testGenerateFieldSecretWithDefault() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        set_values = builder.generateField(template, "password", null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:secret:password", set_values.iterator().next());
        assertEquals("<input type=\"password\" name=\"password\" size=\"10\" />", StringUtils.splitToArray(template.getContent(), "\n")[2]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("password"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:secret:password", set_values.iterator().next());
        assertEquals("<input type=\"password\" name=\"password\" size=\"10\" />", StringUtils.splitToArray(template.getContent(), "\n")[2]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("password"), new String[]{null, "één"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:secret:password", set_values.iterator().next());
        assertEquals("<input type=\"password\" name=\"password\" size=\"10\" />", StringUtils.splitToArray(template.getContent(), "\n")[2]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("password"), new String[]{"één"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:secret:password", set_values.iterator().next());
        assertEquals("<input type=\"password\" name=\"password\" size=\"10\" />", StringUtils.splitToArray(template.getContent(), "\n")[2]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("password").defaultValue("non&e").maxLength(20), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:secret:password", set_values.iterator().next());
        assertEquals("<input type=\"password\" name=\"password\" size=\"10\" maxlength=\"20\" />", StringUtils.splitToArray(template.getContent(), "\n")[2]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("password").defaultValue("non&e").maxLength(20), new String[]{"hé", "you"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:secret:password", set_values.iterator().next());
        assertEquals("<input type=\"password\" name=\"password\" size=\"10\" maxlength=\"20\" />", StringUtils.splitToArray(template.getContent(), "\n")[2]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        template.setValue("form:secret:password", "already set");
        set_values = builder.generateField(template, new ConstrainedProperty("password"), null, null);
        assertEquals(0, set_values.size());
        assertEquals("already set", StringUtils.splitToArray(template.getContent(), "\n")[2]);
        template.removeValue("form:secret:password");
    }

    @Test
    public void testGenerateFieldSecretWithoutDefault() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        set_values = builder.generateField(template, "anotherpassword", null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:secret:anotherpassword", set_values.iterator().next());
        assertEquals("<input type=\"password\" name=\"anotherpassword\" />", StringUtils.splitToArray(template.getContent(), "\n")[3]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("anotherpassword"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:secret:anotherpassword", set_values.iterator().next());
        assertEquals("<input type=\"password\" name=\"anotherpassword\" />", StringUtils.splitToArray(template.getContent(), "\n")[3]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("anotherpassword"), new String[]{"één"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:secret:anotherpassword", set_values.iterator().next());
        assertEquals("<input type=\"password\" name=\"anotherpassword\" />", StringUtils.splitToArray(template.getContent(), "\n")[3]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("anotherpassword").defaultValue("non&e").maxLength(20), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:secret:anotherpassword", set_values.iterator().next());
        assertEquals("<input type=\"password\" name=\"anotherpassword\" maxlength=\"20\" />", StringUtils.splitToArray(template.getContent(), "\n")[3]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();
    }

    @Test
    public void testGenerateFieldSecretPrefix() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        set_values = builder.generateField(template, "password", null, "another");
        assertEquals(1, set_values.size());
        assertEquals("form:secret:anotherpassword", set_values.iterator().next());
        assertEquals("<input type=\"password\" name=\"anotherpassword\" />", StringUtils.splitToArray(template.getContent(), "\n")[3]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("password"), null, "another");
        assertEquals(1, set_values.size());
        assertEquals("form:secret:anotherpassword", set_values.iterator().next());
        assertEquals("<input type=\"password\" name=\"anotherpassword\" />", StringUtils.splitToArray(template.getContent(), "\n")[3]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        template.setValue("form:secret:anotherpassword", "already set");
        set_values = builder.generateField(template, new ConstrainedProperty("password"), null, "another");
        assertEquals(0, set_values.size());
        assertEquals("already set", StringUtils.splitToArray(template.getContent(), "\n")[3]);
        template.removeValue("form:secret:anotherpassword");
    }

    @Test
    public void testGenerateFieldSecretTemplateNameWithDefault() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;

        Template template = TemplateFactory.HTML.get("formbuilder_fields_templatename");

        set_values = builder.replaceField(template, "templatenamepassword", "password", null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:secret:templatenamepassword", set_values.iterator().next());
        assertEquals("<input type=\"password\" name=\"password\" size=\"10\" />", StringUtils.splitToArray(template.getContent(), "\n")[2]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenamepassword", new ConstrainedProperty("password"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:secret:templatenamepassword", set_values.iterator().next());
        assertEquals("<input type=\"password\" name=\"password\" size=\"10\" />", StringUtils.splitToArray(template.getContent(), "\n")[2]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenamepassword", new ConstrainedProperty("password"), new String[]{null, "één"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:secret:templatenamepassword", set_values.iterator().next());
        assertEquals("<input type=\"password\" name=\"password\" size=\"10\" />", StringUtils.splitToArray(template.getContent(), "\n")[2]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenamepassword", new ConstrainedProperty("password"), new String[]{"één"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:secret:templatenamepassword", set_values.iterator().next());
        assertEquals("<input type=\"password\" name=\"password\" size=\"10\" />", StringUtils.splitToArray(template.getContent(), "\n")[2]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenamepassword", new ConstrainedProperty("password").defaultValue("non&e").maxLength(20), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:secret:templatenamepassword", set_values.iterator().next());
        assertEquals("<input type=\"password\" name=\"password\" size=\"10\" maxlength=\"20\" />", StringUtils.splitToArray(template.getContent(), "\n")[2]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenamepassword", new ConstrainedProperty("password").defaultValue("non&e").maxLength(20), new String[]{"hé", "you"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:secret:templatenamepassword", set_values.iterator().next());
        assertEquals("<input type=\"password\" name=\"password\" size=\"10\" maxlength=\"20\" />", StringUtils.splitToArray(template.getContent(), "\n")[2]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        template.setValue("form:secret:templatenamepassword", "already set");
        set_values = builder.replaceField(template, "templatenamepassword", new ConstrainedProperty("password"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:secret:templatenamepassword", set_values.iterator().next());
        assertEquals("<input type=\"password\" name=\"password\" size=\"10\" />", StringUtils.splitToArray(template.getContent(), "\n")[2]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();
    }

    @Test
    public void testRemoveFieldSecret() {
        FormBuilderHtml builder = new FormBuilderHtml();

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        String raw_content = template.getContent();

        builder.generateField(template, new ConstrainedProperty("password").defaultValue("non&e").maxLength(20), new String[]{"hé", "you"}, null);
        assertNotEquals(raw_content, template.getContent());
        builder.removeField(template, "password", null);
        assertEquals(raw_content, template.getContent());
    }

    @Test
    public void testRemoveFieldSecretPrefix() {
        FormBuilderHtml builder = new FormBuilderHtml();

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        String raw_content = template.getContent();

        builder.generateField(template, new ConstrainedProperty("password").defaultValue("non&e").maxLength(20), new String[]{"hé", "you"}, "another");
        assertNotEquals(raw_content, template.getContent());
        builder.removeField(template, "password", "another");
        assertEquals(raw_content, template.getContent());
    }

    @Test
    public void testRemoveFieldSecretTemplateName() {
        FormBuilderHtml builder = new FormBuilderHtml();

        Template template = TemplateFactory.HTML.get("formbuilder_fields_templatename");

        String raw_content = template.getContent();

        builder.replaceField(template, "templatenamepassword", new ConstrainedProperty("password").defaultValue("non&e").maxLength(20), new String[]{"hé", "you"}, null);
        assertNotEquals(raw_content, template.getContent());
        builder.removeField(template, "templatenamepassword");
        assertEquals(raw_content, template.getContent());
    }

    @Test
    public void testGenerateFieldTextareaWithDefault() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        set_values = builder.generateField(template, "comment", null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:textarea:comment", set_values.iterator().next());
        assertEquals("<textarea name=\"comment\" cols=\"10\" rows=\"5\" id=\"comment{{v form:value/}}\"></textarea>", StringUtils.splitToArray(template.getContent(), "\n")[4]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("comment"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:textarea:comment", set_values.iterator().next());
        assertEquals("<textarea name=\"comment\" cols=\"10\" rows=\"5\" id=\"comment{{v form:value/}}\"></textarea>", StringUtils.splitToArray(template.getContent(), "\n")[4]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("comment"), new String[]{null, "één"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:textarea:comment", set_values.iterator().next());
        assertEquals("<textarea name=\"comment\" cols=\"10\" rows=\"5\" id=\"comment{{v form:value/}}\"></textarea>", StringUtils.splitToArray(template.getContent(), "\n")[4]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("comment"), new String[]{"één"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:textarea:comment", set_values.iterator().next());
        assertEquals("<textarea name=\"comment\" cols=\"10\" rows=\"5\" id=\"comment&eacute;&eacute;n\">&eacute;&eacute;n</textarea>", StringUtils.splitToArray(template.getContent(), "\n")[4]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("comment").defaultValue("non&e").maxLength(20), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:textarea:comment", set_values.iterator().next());
        assertEquals("<textarea name=\"comment\" cols=\"10\" rows=\"5\" id=\"commentnon&amp;e\">non&amp;e</textarea>", StringUtils.splitToArray(template.getContent(), "\n")[4]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("comment").defaultValue("non&e").maxLength(20), new String[]{"hé", "you"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:textarea:comment", set_values.iterator().next());
        assertEquals("<textarea name=\"comment\" cols=\"10\" rows=\"5\" id=\"commenth&eacute;\">h&eacute;</textarea>", StringUtils.splitToArray(template.getContent(), "\n")[4]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        template.setValue("form:textarea:comment", "already set");
        set_values = builder.generateField(template, new ConstrainedProperty("comment"), null, null);
        assertEquals(0, set_values.size());
        assertEquals("already set", StringUtils.splitToArray(template.getContent(), "\n")[4]);
        template.removeValue("form:textarea:comment");
    }

    @Test
    public void testGenerateFieldTextareaWithoutDefault() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        set_values = builder.generateField(template, "anothercomment", null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:textarea:anothercomment", set_values.iterator().next());
        assertEquals("<textarea name=\"anothercomment\"></textarea>", StringUtils.splitToArray(template.getContent(), "\n")[5]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("anothercomment"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:textarea:anothercomment", set_values.iterator().next());
        assertEquals("<textarea name=\"anothercomment\"></textarea>", StringUtils.splitToArray(template.getContent(), "\n")[5]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("anothercomment"), new String[]{"één"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:textarea:anothercomment", set_values.iterator().next());
        assertEquals("<textarea name=\"anothercomment\">&eacute;&eacute;n</textarea>", StringUtils.splitToArray(template.getContent(), "\n")[5]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("anothercomment").defaultValue("non&e").maxLength(20), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:textarea:anothercomment", set_values.iterator().next());
        assertEquals("<textarea name=\"anothercomment\">non&amp;e</textarea>", StringUtils.splitToArray(template.getContent(), "\n")[5]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();
    }

    @Test
    public void testGenerateFieldTextareaPrefix() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        set_values = builder.generateField(template, "comment", null, "another");
        assertEquals(1, set_values.size());
        assertEquals("form:textarea:anothercomment", set_values.iterator().next());
        assertEquals("<textarea name=\"anothercomment\"></textarea>", StringUtils.splitToArray(template.getContent(), "\n")[5]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("comment"), null, "another");
        assertEquals(1, set_values.size());
        assertEquals("form:textarea:anothercomment", set_values.iterator().next());
        assertEquals("<textarea name=\"anothercomment\"></textarea>", StringUtils.splitToArray(template.getContent(), "\n")[5]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        template.setValue("form:textarea:anothercomment", "already set");
        set_values = builder.generateField(template, new ConstrainedProperty("comment"), null, "another");
        assertEquals(0, set_values.size());
        assertEquals("already set", StringUtils.splitToArray(template.getContent(), "\n")[5]);
        template.removeValue("form:textarea:anothercomment");
    }

    @Test
    public void testGenerateFieldTextareaTemplateNameWithDefault() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;

        Template template = TemplateFactory.HTML.get("formbuilder_fields_templatename");

        set_values = builder.replaceField(template, "templatenamecomment", "comment", null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:textarea:templatenamecomment", set_values.iterator().next());
        assertEquals("<textarea name=\"comment\" cols=\"10\" rows=\"5\" id=\"comment{{v form:value/}}\"></textarea>", StringUtils.splitToArray(template.getContent(), "\n")[4]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenamecomment", new ConstrainedProperty("comment"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:textarea:templatenamecomment", set_values.iterator().next());
        assertEquals("<textarea name=\"comment\" cols=\"10\" rows=\"5\" id=\"comment{{v form:value/}}\"></textarea>", StringUtils.splitToArray(template.getContent(), "\n")[4]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenamecomment", new ConstrainedProperty("comment"), new String[]{null, "één"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:textarea:templatenamecomment", set_values.iterator().next());
        assertEquals("<textarea name=\"comment\" cols=\"10\" rows=\"5\" id=\"comment{{v form:value/}}\"></textarea>", StringUtils.splitToArray(template.getContent(), "\n")[4]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenamecomment", new ConstrainedProperty("comment"), new String[]{"één"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:textarea:templatenamecomment", set_values.iterator().next());
        assertEquals("<textarea name=\"comment\" cols=\"10\" rows=\"5\" id=\"comment&eacute;&eacute;n\">&eacute;&eacute;n</textarea>", StringUtils.splitToArray(template.getContent(), "\n")[4]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenamecomment", new ConstrainedProperty("comment").defaultValue("non&e").maxLength(20), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:textarea:templatenamecomment", set_values.iterator().next());
        assertEquals("<textarea name=\"comment\" cols=\"10\" rows=\"5\" id=\"commentnon&amp;e\">non&amp;e</textarea>", StringUtils.splitToArray(template.getContent(), "\n")[4]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenamecomment", new ConstrainedProperty("comment").defaultValue("non&e").maxLength(20), new String[]{"hé", "you"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:textarea:templatenamecomment", set_values.iterator().next());
        assertEquals("<textarea name=\"comment\" cols=\"10\" rows=\"5\" id=\"commenth&eacute;\">h&eacute;</textarea>", StringUtils.splitToArray(template.getContent(), "\n")[4]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        template.setValue("form:textarea:templatenamecomment", "already set");
        set_values = builder.replaceField(template, "templatenamecomment", new ConstrainedProperty("comment"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:textarea:templatenamecomment", set_values.iterator().next());
        assertEquals("<textarea name=\"comment\" cols=\"10\" rows=\"5\" id=\"comment{{v form:value/}}\"></textarea>", StringUtils.splitToArray(template.getContent(), "\n")[4]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();
    }

    @Test
    public void testRemoveFieldTextarea() {
        FormBuilderHtml builder = new FormBuilderHtml();

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        String raw_content = template.getContent();

        builder.generateField(template, new ConstrainedProperty("comment").defaultValue("non&e").maxLength(20), new String[]{"hé", "you"}, null);
        assertNotEquals(raw_content, template.getContent());
        builder.removeField(template, "comment", null);
        assertEquals(raw_content, template.getContent());
    }

    @Test
    public void testRemoveFieldTextareaPrefix() {
        FormBuilderHtml builder = new FormBuilderHtml();

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        String raw_content = template.getContent();

        builder.generateField(template, new ConstrainedProperty("comment").defaultValue("non&e").maxLength(20), new String[]{"hé", "you"}, "another");
        assertNotEquals(raw_content, template.getContent());
        builder.removeField(template, "comment", "another");
        assertEquals(raw_content, template.getContent());
    }

    @Test
    public void testRemoveFieldTextareaTemplateName() {
        FormBuilderHtml builder = new FormBuilderHtml();

        Template template = TemplateFactory.HTML.get("formbuilder_fields_templatename");

        String raw_content = template.getContent();

        builder.replaceField(template, "templatenamecomment", new ConstrainedProperty("comment").defaultValue("non&e").maxLength(20), new String[]{"hé", "you"}, null);
        assertNotEquals(raw_content, template.getContent());
        builder.removeField(template, "templatenamecomment", null);
        assertEquals(raw_content, template.getContent());
    }

    @Test
    public void testGenerateFieldRadioWithDefault() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;
        Iterator<String> set_values_it;

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        set_values = builder.generateField(template, "question", null, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:radio:question", set_values_it.next());
        assertEquals("form:display:question", set_values_it.next());
        assertEquals("<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"question{{v form:value/}}\" />", StringUtils.splitToArray(template.getContent(), "\n")[6]);
        assertEquals("<div></div>", StringUtils.splitToArray(template.getContent(), "\n")[56]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("question"), null, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:radio:question", set_values_it.next());
        assertEquals("form:display:question", set_values_it.next());
        assertEquals("<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"question{{v form:value/}}\" />", StringUtils.splitToArray(template.getContent(), "\n")[6]);
        assertEquals("<div></div>", StringUtils.splitToArray(template.getContent(), "\n")[56]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("question").inList(null, "a1", null, "a3", "a2"), null, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:radio:question", set_values_it.next());
        assertEquals("form:display:question", set_values_it.next());
        assertEquals("<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona1\" value=\"a1\" />answer1<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona3\" value=\"a3\" />a3<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona2\" value=\"a2\" />answer2", StringUtils.splitToArray(template.getContent(), "\n")[6]);
        assertEquals("<div></div>", StringUtils.splitToArray(template.getContent(), "\n")[56]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, SelectInListEnum.class, new ConstrainedProperty("question").inList(null, "a1", null, "a3", "a2"), null, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:radio:question", set_values_it.next());
        assertEquals("form:display:question", set_values_it.next());
        assertEquals("<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona1\" value=\"a1\" />answer1<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona3\" value=\"a3\" />a3<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona2\" value=\"a2\" />answer2", StringUtils.splitToArray(template.getContent(), "\n")[6]);
        assertEquals("<div></div>", StringUtils.splitToArray(template.getContent(), "\n")[56]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, RadioInListEnum.class, "question", null, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:radio:question", set_values_it.next());
        assertEquals("form:display:question", set_values_it.next());
        assertEquals("<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona1\" value=\"a1\" />answer1<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona3\" value=\"a3\" />a3<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona2\" value=\"a2\" />answer2", StringUtils.splitToArray(template.getContent(), "\n")[6]);
        assertEquals("<div></div>", StringUtils.splitToArray(template.getContent(), "\n")[56]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("question").inList(null, "a1", null, "a3", "a2").defaultValue("a2"), new String[]{null, "a1"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:radio:question", set_values_it.next());
        assertEquals("form:display:question", set_values_it.next());
        assertEquals("<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona1\" value=\"a1\" />answer1<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona3\" value=\"a3\" />a3<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona2\" value=\"a2\" checked=\"checked\" />answer2", StringUtils.splitToArray(template.getContent(), "\n")[6]);
        assertEquals("<div>answer2</div><div>answer1</div>", StringUtils.splitToArray(template.getContent(), "\n")[56]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, RadioInListEnum.class, new ConstrainedProperty("question").defaultValue("a2"), new String[]{null, "a1"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:radio:question", set_values_it.next());
        assertEquals("form:display:question", set_values_it.next());
        assertEquals("<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona1\" value=\"a1\" />answer1<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona3\" value=\"a3\" />a3<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona2\" value=\"a2\" checked=\"checked\" />answer2", StringUtils.splitToArray(template.getContent(), "\n")[6]);
        assertEquals("<div>answer2</div><div>answer1</div>", StringUtils.splitToArray(template.getContent(), "\n")[56]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("question").inList(null, "a1", null, "a3", "a2"), new String[]{"a1", "a2"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:radio:question", set_values_it.next());
        assertEquals("form:display:question", set_values_it.next());
        assertEquals("<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona1\" value=\"a1\" checked=\"checked\" />answer1<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona3\" value=\"a3\" />a3<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona2\" value=\"a2\" />answer2", StringUtils.splitToArray(template.getContent(), "\n")[6]);
        assertEquals("<div>answer1</div><div>answer2</div>", StringUtils.splitToArray(template.getContent(), "\n")[56]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, RadioInListEnum.class, "question", new String[]{"a1", "a2"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:radio:question", set_values_it.next());
        assertEquals("form:display:question", set_values_it.next());
        assertEquals("<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona1\" value=\"a1\" checked=\"checked\" />answer1<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona3\" value=\"a3\" />a3<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona2\" value=\"a2\" />answer2", StringUtils.splitToArray(template.getContent(), "\n")[6]);
        assertEquals("<div>answer1</div><div>answer2</div>", StringUtils.splitToArray(template.getContent(), "\n")[56]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("question").inList(null, "a1", null, "a3", "a2").defaultValue("a3"), new String[]{"a1", "a2"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:radio:question", set_values_it.next());
        assertEquals("form:display:question", set_values_it.next());
        assertEquals("<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona1\" value=\"a1\" checked=\"checked\" />answer1<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona3\" value=\"a3\" />a3<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona2\" value=\"a2\" />answer2", StringUtils.splitToArray(template.getContent(), "\n")[6]);
        assertEquals("<div>answer1</div><div>answer2</div>", StringUtils.splitToArray(template.getContent(), "\n")[56]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, RadioInListEnum.class, new ConstrainedProperty("question").defaultValue("a3"), new String[]{"a1", "a2"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:radio:question", set_values_it.next());
        assertEquals("form:display:question", set_values_it.next());
        assertEquals("<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona1\" value=\"a1\" checked=\"checked\" />answer1<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona3\" value=\"a3\" />a3<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona2\" value=\"a2\" />answer2", StringUtils.splitToArray(template.getContent(), "\n")[6]);
        assertEquals("<div>answer1</div><div>answer2</div>", StringUtils.splitToArray(template.getContent(), "\n")[56]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("question").inList(null, "a1", null, "a3", "a2"), new String[]{"a4"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:radio:question", set_values_it.next());
        assertEquals("form:display:question", set_values_it.next());
        assertEquals("<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona1\" value=\"a1\" />answer1<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona3\" value=\"a3\" />a3<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona2\" value=\"a2\" />answer2", StringUtils.splitToArray(template.getContent(), "\n")[6]);
        assertEquals("<div>a4</div>", StringUtils.splitToArray(template.getContent(), "\n")[56]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, RadioInListEnum.class, "question", new String[]{"a4"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:radio:question", set_values_it.next());
        assertEquals("form:display:question", set_values_it.next());
        assertEquals("<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona1\" value=\"a1\" />answer1<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona3\" value=\"a3\" />a3<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona2\" value=\"a2\" />answer2", StringUtils.splitToArray(template.getContent(), "\n")[6]);
        assertEquals("<div>a4</div>", StringUtils.splitToArray(template.getContent(), "\n")[56]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        template.setValue("form:radio:question", "already set");
        template.setValue("form:display:question", "already set too");
        set_values = builder.generateField(template, new ConstrainedProperty("question"), null, null);
        assertEquals(0, set_values.size());
        assertEquals("already set", StringUtils.splitToArray(template.getContent(), "\n")[6]);
        assertEquals("already set too", StringUtils.splitToArray(template.getContent(), "\n")[56]);
        template.removeValue("form:radio:question");
        template.removeValue("form:display:question");
    }

    @Test
    public void testGenerateFieldRadioWithoutDefault() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        set_values = builder.generateField(template, "anotherquestion", null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:anotherquestion", set_values.iterator().next());
        assertEquals("<input type=\"radio\" name=\"anotherquestion\" />", StringUtils.splitToArray(template.getContent(), "\n")[10]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("anotherquestion"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:anotherquestion", set_values.iterator().next());
        assertEquals("<input type=\"radio\" name=\"anotherquestion\" />", StringUtils.splitToArray(template.getContent(), "\n")[10]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("anotherquestion").inList(null, "a1", null, "a3", "a2"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:anotherquestion", set_values.iterator().next());
        assertEquals("<input type=\"radio\" name=\"anotherquestion\" value=\"a1\" />another answer 1<input type=\"radio\" name=\"anotherquestion\" value=\"a3\" />a3<input type=\"radio\" name=\"anotherquestion\" value=\"a2\" />another answer 2", StringUtils.splitToArray(template.getContent(), "\n")[10]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, RadioInListEnum.class, "anotherquestion", null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:anotherquestion", set_values.iterator().next());
        assertEquals("<input type=\"radio\" name=\"anotherquestion\" value=\"a1\" />another answer 1<input type=\"radio\" name=\"anotherquestion\" value=\"a3\" />a3<input type=\"radio\" name=\"anotherquestion\" value=\"a2\" />another answer 2", StringUtils.splitToArray(template.getContent(), "\n")[10]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("anotherquestion").inList(null, "a1", null, "a3", "a2").defaultValue("a3"), new String[]{"a1", "a2"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:anotherquestion", set_values.iterator().next());
        assertEquals("<input type=\"radio\" name=\"anotherquestion\" value=\"a1\" checked=\"checked\" />another answer 1<input type=\"radio\" name=\"anotherquestion\" value=\"a3\" />a3<input type=\"radio\" name=\"anotherquestion\" value=\"a2\" />another answer 2", StringUtils.splitToArray(template.getContent(), "\n")[10]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, RadioInListEnum.class, new ConstrainedProperty("anotherquestion").defaultValue("a3"), new String[]{"a1", "a2"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:anotherquestion", set_values.iterator().next());
        assertEquals("<input type=\"radio\" name=\"anotherquestion\" value=\"a1\" checked=\"checked\" />another answer 1<input type=\"radio\" name=\"anotherquestion\" value=\"a3\" />a3<input type=\"radio\" name=\"anotherquestion\" value=\"a2\" />another answer 2", StringUtils.splitToArray(template.getContent(), "\n")[10]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();
    }

    @Test
    public void testGenerateFieldRadioPrefix() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        set_values = builder.generateField(template, "question", null, "another");
        assertEquals(1, set_values.size());
        assertEquals("form:radio:anotherquestion", set_values.iterator().next());
        assertEquals("<input type=\"radio\" name=\"anotherquestion\" />", StringUtils.splitToArray(template.getContent(), "\n")[10]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("question"), null, "another");
        assertEquals(1, set_values.size());
        assertEquals("form:radio:anotherquestion", set_values.iterator().next());
        assertEquals("<input type=\"radio\" name=\"anotherquestion\" />", StringUtils.splitToArray(template.getContent(), "\n")[10]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("question").inList(null, "a1", null, "a3", "a2"), null, "another");
        assertEquals(1, set_values.size());
        assertEquals("form:radio:anotherquestion", set_values.iterator().next());
        assertEquals("<input type=\"radio\" name=\"anotherquestion\" value=\"a1\" />another answer 1<input type=\"radio\" name=\"anotherquestion\" value=\"a3\" />a3<input type=\"radio\" name=\"anotherquestion\" value=\"a2\" />another answer 2", StringUtils.splitToArray(template.getContent(), "\n")[10]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, RadioInListEnum.class, "question", null, "another");
        assertEquals(1, set_values.size());
        assertEquals("form:radio:anotherquestion", set_values.iterator().next());
        assertEquals("<input type=\"radio\" name=\"anotherquestion\" value=\"a1\" />another answer 1<input type=\"radio\" name=\"anotherquestion\" value=\"a3\" />a3<input type=\"radio\" name=\"anotherquestion\" value=\"a2\" />another answer 2", StringUtils.splitToArray(template.getContent(), "\n")[10]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("question").inList(null, "a1", null, "a3", "a2").defaultValue("a3"), new String[]{"a1", "a2"}, "another");
        assertEquals(1, set_values.size());
        assertEquals("form:radio:anotherquestion", set_values.iterator().next());
        assertEquals("<input type=\"radio\" name=\"anotherquestion\" value=\"a1\" checked=\"checked\" />another answer 1<input type=\"radio\" name=\"anotherquestion\" value=\"a3\" />a3<input type=\"radio\" name=\"anotherquestion\" value=\"a2\" />another answer 2", StringUtils.splitToArray(template.getContent(), "\n")[10]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, RadioInListEnum.class, new ConstrainedProperty("question").defaultValue("a3"), new String[]{"a1", "a2"}, "another");
        assertEquals(1, set_values.size());
        assertEquals("form:radio:anotherquestion", set_values.iterator().next());
        assertEquals("<input type=\"radio\" name=\"anotherquestion\" value=\"a1\" checked=\"checked\" />another answer 1<input type=\"radio\" name=\"anotherquestion\" value=\"a3\" />a3<input type=\"radio\" name=\"anotherquestion\" value=\"a2\" />another answer 2", StringUtils.splitToArray(template.getContent(), "\n")[10]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        template.setValue("form:radio:anotherquestion", "already set");
        set_values = builder.generateField(template, new ConstrainedProperty("question"), null, "another");
        assertEquals(0, set_values.size());
        assertEquals("already set", StringUtils.splitToArray(template.getContent(), "\n")[10]);
        template.removeValue("form:radio:anotherquestion");
    }

    @Test
    public void testGenerateFieldRadioDynamic() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;
        Iterator<String> set_values_it;

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        template.addResourceBundle(new ListResourceBundle() {
            public Object[][] getContents() {
                return new Object[][]{
                    {"question:a1", "dynamic first"},
                    {"question:a2", "dynamic second"},
                    {"question:a3", "dynamic third"}
                };
            }
        });

        set_values = builder.generateField(template, new ConstrainedProperty("question").inList("a1", "a3", "a2").defaultValue("a2"), null, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:radio:question", set_values_it.next());
        assertEquals("form:display:question", set_values_it.next());
        assertEquals("<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona1\" value=\"a1\" />dynamic first<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona3\" value=\"a3\" />dynamic third<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona2\" value=\"a2\" checked=\"checked\" />dynamic second", StringUtils.splitToArray(template.getContent(), "\n")[6]);
        assertEquals("<div>dynamic second</div>", StringUtils.splitToArray(template.getContent(), "\n")[56]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, RadioInListEnum.class, new ConstrainedProperty("question").defaultValue("a2"), null, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:radio:question", set_values_it.next());
        assertEquals("form:display:question", set_values_it.next());
        assertEquals("<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona1\" value=\"a1\" />dynamic first<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona3\" value=\"a3\" />dynamic third<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona2\" value=\"a2\" checked=\"checked\" />dynamic second", StringUtils.splitToArray(template.getContent(), "\n")[6]);
        assertEquals("<div>dynamic second</div>", StringUtils.splitToArray(template.getContent(), "\n")[56]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("question").inList("a1", "a3", "a2").defaultValue("a2"), new String[]{"a1", "a2"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:radio:question", set_values_it.next());
        assertEquals("form:display:question", set_values_it.next());
        assertEquals("<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona1\" value=\"a1\" checked=\"checked\" />dynamic first<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona3\" value=\"a3\" />dynamic third<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona2\" value=\"a2\" />dynamic second", StringUtils.splitToArray(template.getContent(), "\n")[6]);
        assertEquals("<div>dynamic first</div><div>dynamic second</div>", StringUtils.splitToArray(template.getContent(), "\n")[56]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, RadioInListEnum.class, new ConstrainedProperty("question").defaultValue("a2"), new String[]{"a1", "a2"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:radio:question", set_values_it.next());
        assertEquals("form:display:question", set_values_it.next());
        assertEquals("<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona1\" value=\"a1\" checked=\"checked\" />dynamic first<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona3\" value=\"a3\" />dynamic third<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona2\" value=\"a2\" />dynamic second", StringUtils.splitToArray(template.getContent(), "\n")[6]);
        assertEquals("<div>dynamic first</div><div>dynamic second</div>", StringUtils.splitToArray(template.getContent(), "\n")[56]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        template.clear();

        template.addResourceBundle(new ListResourceBundle() {
            public Object[][] getContents() {
                return new Object[][]{
                    {"question:a1", "dynamic first"},
                };
            }
        });

        set_values = builder.generateField(template, new ConstrainedProperty("question").inList("a1", "a3", "a2").defaultValue("a2"), new String[]{"a1", "a2"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:radio:question", set_values_it.next());
        assertEquals("form:display:question", set_values_it.next());
        assertEquals("<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona1\" value=\"a1\" checked=\"checked\" />dynamic first<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona3\" value=\"a3\" />a3<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona2\" value=\"a2\" />answer2", StringUtils.splitToArray(template.getContent(), "\n")[6]);
        assertEquals("<div>dynamic first</div><div>answer2</div>", StringUtils.splitToArray(template.getContent(), "\n")[56]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, RadioInListEnum.class, new ConstrainedProperty("question").defaultValue("a2"), new String[]{"a1", "a2"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:radio:question", set_values_it.next());
        assertEquals("form:display:question", set_values_it.next());
        assertEquals("<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona1\" value=\"a1\" checked=\"checked\" />dynamic first<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona3\" value=\"a3\" />a3<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona2\" value=\"a2\" />answer2", StringUtils.splitToArray(template.getContent(), "\n")[6]);
        assertEquals("<div>dynamic first</div><div>answer2</div>", StringUtils.splitToArray(template.getContent(), "\n")[56]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("question").inList("a1", "a3", "a2").defaultValue("a2"), new String[]{"a2"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:radio:question", set_values_it.next());
        assertEquals("form:display:question", set_values_it.next());
        assertEquals("<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona1\" value=\"a1\" />dynamic first<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona3\" value=\"a3\" />a3<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona2\" value=\"a2\" checked=\"checked\" />answer2", StringUtils.splitToArray(template.getContent(), "\n")[6]);
        assertEquals("<div>answer2</div>", StringUtils.splitToArray(template.getContent(), "\n")[56]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, RadioInListEnum.class, new ConstrainedProperty("question").defaultValue("a2"), new String[]{"a2"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:radio:question", set_values_it.next());
        assertEquals("form:display:question", set_values_it.next());
        assertEquals("<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona1\" value=\"a1\" />dynamic first<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona3\" value=\"a3\" />a3<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona2\" value=\"a2\" checked=\"checked\" />answer2", StringUtils.splitToArray(template.getContent(), "\n")[6]);
        assertEquals("<div>answer2</div>", StringUtils.splitToArray(template.getContent(), "\n")[56]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();
    }

    @Test
    public void testGenerateFieldRadioTemplateNameWithDefault() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;
        Iterator<String> set_values_it;

        Template template = TemplateFactory.HTML.get("formbuilder_fields_templatename");

        set_values = builder.replaceField(template, "templatenamequestion", "question", null, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:radio:templatenamequestion", set_values_it.next());
        assertEquals("form:display:templatenamequestion", set_values_it.next());
        assertEquals("<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"question{{v form:value/}}\" />", StringUtils.splitToArray(template.getContent(), "\n")[6]);
        assertEquals("<div></div>", StringUtils.splitToArray(template.getContent(), "\n")[56]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenamequestion", new ConstrainedProperty("question"), null, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:radio:templatenamequestion", set_values_it.next());
        assertEquals("form:display:templatenamequestion", set_values_it.next());
        assertEquals("<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"question{{v form:value/}}\" />", StringUtils.splitToArray(template.getContent(), "\n")[6]);
        assertEquals("<div></div>", StringUtils.splitToArray(template.getContent(), "\n")[56]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenamequestion", new ConstrainedProperty("question").inList(null, "a1", null, "a3", "a2"), null, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:radio:templatenamequestion", set_values_it.next());
        assertEquals("form:display:templatenamequestion", set_values_it.next());
        assertEquals("<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona1\" value=\"a1\" />answer1<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona3\" value=\"a3\" />a3<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona2\" value=\"a2\" />answer2", StringUtils.splitToArray(template.getContent(), "\n")[6]);
        assertEquals("<div></div>", StringUtils.splitToArray(template.getContent(), "\n")[56]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenamequestion", RadioInListEnum.class, "question", null, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:radio:templatenamequestion", set_values_it.next());
        assertEquals("form:display:templatenamequestion", set_values_it.next());
        assertEquals("<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona1\" value=\"a1\" />answer1<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona3\" value=\"a3\" />a3<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona2\" value=\"a2\" />answer2", StringUtils.splitToArray(template.getContent(), "\n")[6]);
        assertEquals("<div></div>", StringUtils.splitToArray(template.getContent(), "\n")[56]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenamequestion", new ConstrainedProperty("question").inList(null, "a1", null, "a3", "a2").defaultValue("a2"), new String[]{null, "a1"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:radio:templatenamequestion", set_values_it.next());
        assertEquals("form:display:templatenamequestion", set_values_it.next());
        assertEquals("<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona1\" value=\"a1\" />answer1<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona3\" value=\"a3\" />a3<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona2\" value=\"a2\" checked=\"checked\" />answer2", StringUtils.splitToArray(template.getContent(), "\n")[6]);
        assertEquals("<div>answer2</div><div>answer1</div>", StringUtils.splitToArray(template.getContent(), "\n")[56]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenamequestion", RadioInListEnum.class, new ConstrainedProperty("question").defaultValue("a2"), new String[]{null, "a1"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:radio:templatenamequestion", set_values_it.next());
        assertEquals("form:display:templatenamequestion", set_values_it.next());
        assertEquals("<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona1\" value=\"a1\" />answer1<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona3\" value=\"a3\" />a3<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona2\" value=\"a2\" checked=\"checked\" />answer2", StringUtils.splitToArray(template.getContent(), "\n")[6]);
        assertEquals("<div>answer2</div><div>answer1</div>", StringUtils.splitToArray(template.getContent(), "\n")[56]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenamequestion", new ConstrainedProperty("question").inList(null, "a1", null, "a3", "a2"), new String[]{"a1", "a2"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:radio:templatenamequestion", set_values_it.next());
        assertEquals("form:display:templatenamequestion", set_values_it.next());
        assertEquals("<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona1\" value=\"a1\" checked=\"checked\" />answer1<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona3\" value=\"a3\" />a3<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona2\" value=\"a2\" />answer2", StringUtils.splitToArray(template.getContent(), "\n")[6]);
        assertEquals("<div>answer1</div><div>answer2</div>", StringUtils.splitToArray(template.getContent(), "\n")[56]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenamequestion", RadioInListEnum.class, "question", new String[]{"a1", "a2"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:radio:templatenamequestion", set_values_it.next());
        assertEquals("form:display:templatenamequestion", set_values_it.next());
        assertEquals("<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona1\" value=\"a1\" checked=\"checked\" />answer1<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona3\" value=\"a3\" />a3<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona2\" value=\"a2\" />answer2", StringUtils.splitToArray(template.getContent(), "\n")[6]);
        assertEquals("<div>answer1</div><div>answer2</div>", StringUtils.splitToArray(template.getContent(), "\n")[56]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenamequestion", new ConstrainedProperty("question").inList(null, "a1", null, "a3", "a2").defaultValue("a3"), new String[]{"a1", "a2"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:radio:templatenamequestion", set_values_it.next());
        assertEquals("form:display:templatenamequestion", set_values_it.next());
        assertEquals("<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona1\" value=\"a1\" checked=\"checked\" />answer1<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona3\" value=\"a3\" />a3<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona2\" value=\"a2\" />answer2", StringUtils.splitToArray(template.getContent(), "\n")[6]);
        assertEquals("<div>answer1</div><div>answer2</div>", StringUtils.splitToArray(template.getContent(), "\n")[56]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenamequestion", RadioInListEnum.class, new ConstrainedProperty("question").defaultValue("a3"), new String[]{"a1", "a2"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:radio:templatenamequestion", set_values_it.next());
        assertEquals("form:display:templatenamequestion", set_values_it.next());
        assertEquals("<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona1\" value=\"a1\" checked=\"checked\" />answer1<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona3\" value=\"a3\" />a3<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona2\" value=\"a2\" />answer2", StringUtils.splitToArray(template.getContent(), "\n")[6]);
        assertEquals("<div>answer1</div><div>answer2</div>", StringUtils.splitToArray(template.getContent(), "\n")[56]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenamequestion", new ConstrainedProperty("question").inList(null, "a1", null, "a3", "a2"), new String[]{"a4"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:radio:templatenamequestion", set_values_it.next());
        assertEquals("form:display:templatenamequestion", set_values_it.next());
        assertEquals("<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona1\" value=\"a1\" />answer1<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona3\" value=\"a3\" />a3<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona2\" value=\"a2\" />answer2", StringUtils.splitToArray(template.getContent(), "\n")[6]);
        assertEquals("<div>a4</div>", StringUtils.splitToArray(template.getContent(), "\n")[56]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenamequestion", RadioInListEnum.class, "question", new String[]{"a4"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:radio:templatenamequestion", set_values_it.next());
        assertEquals("form:display:templatenamequestion", set_values_it.next());
        assertEquals("<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona1\" value=\"a1\" />answer1<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona3\" value=\"a3\" />a3<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"questiona2\" value=\"a2\" />answer2", StringUtils.splitToArray(template.getContent(), "\n")[6]);
        assertEquals("<div>a4</div>", StringUtils.splitToArray(template.getContent(), "\n")[56]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        template.setValue("form:radio:templatenamequestion", "already set");
        template.setValue("form:display:templatenamequestion", "already set too");
        set_values = builder.replaceField(template, "templatenamequestion", "question", null, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:radio:templatenamequestion", set_values_it.next());
        assertEquals("form:display:templatenamequestion", set_values_it.next());
        assertEquals("<input type=\"radio\" name=\"question\" alt=\"sometext\" id=\"question{{v form:value/}}\" />", StringUtils.splitToArray(template.getContent(), "\n")[6]);
        assertEquals("<div></div>", StringUtils.splitToArray(template.getContent(), "\n")[56]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();
    }

    @Test
    public void testGenerateFieldRadioCustomWithDefault() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        set_values = builder.generateField(template, "customquestion", null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:customquestion", set_values.iterator().next());
        assertEquals("<input type=\"radio\" name=\"customquestion\" alt=\"customtext\" />", StringUtils.splitToArray(template.getContent(), "\n")[14]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("customquestion"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:customquestion", set_values.iterator().next());
        assertEquals("<input type=\"radio\" name=\"customquestion\" alt=\"customtext\" />", StringUtils.splitToArray(template.getContent(), "\n")[14]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("customquestion").inList(null, "a1", null, "a3", "a2"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:customquestion", set_values.iterator().next());
        assertEquals("custom answer 1 : <input type=\"radio\" name=\"customquestion\" alt=\"customtext\" value=\"a1\" />a3 : <input type=\"radio\" name=\"customquestion\" alt=\"customtext\" value=\"a3\" />custom answer 2 : <input type=\"radio\" name=\"customquestion\" alt=\"customtext\" value=\"a2\" />", StringUtils.splitToArray(template.getContent(), "\n")[14]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("customquestion").inList(null, "a1", null, "a3", "a2"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:customquestion", set_values.iterator().next());
        assertEquals("custom answer 1 : <input type=\"radio\" name=\"customquestion\" alt=\"customtext\" value=\"a1\" />a3 : <input type=\"radio\" name=\"customquestion\" alt=\"customtext\" value=\"a3\" />custom answer 2 : <input type=\"radio\" name=\"customquestion\" alt=\"customtext\" value=\"a2\" />", StringUtils.splitToArray(template.getContent(), "\n")[14]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, RadioInListEnum.class, new ConstrainedProperty("customquestion").defaultValue("a2"), new String[]{null, "a1"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:customquestion", set_values.iterator().next());
        assertEquals("custom answer 1 : <input type=\"radio\" name=\"customquestion\" alt=\"customtext\" value=\"a1\" />a3 : <input type=\"radio\" name=\"customquestion\" alt=\"customtext\" value=\"a3\" />custom answer 2 : <input type=\"radio\" name=\"customquestion\" alt=\"customtext\" value=\"a2\" checked=\"checked\" />", StringUtils.splitToArray(template.getContent(), "\n")[14]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("customquestion").inList(null, "a1", null, "a3", "a2"), new String[]{"a1", "a2"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:customquestion", set_values.iterator().next());
        assertEquals("custom answer 1 : <input type=\"radio\" name=\"customquestion\" alt=\"customtext\" value=\"a1\" checked=\"checked\" />a3 : <input type=\"radio\" name=\"customquestion\" alt=\"customtext\" value=\"a3\" />custom answer 2 : <input type=\"radio\" name=\"customquestion\" alt=\"customtext\" value=\"a2\" />", StringUtils.splitToArray(template.getContent(), "\n")[14]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, RadioInListEnum.class, "customquestion", new String[]{"a1", "a2"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:customquestion", set_values.iterator().next());
        assertEquals("custom answer 1 : <input type=\"radio\" name=\"customquestion\" alt=\"customtext\" value=\"a1\" checked=\"checked\" />a3 : <input type=\"radio\" name=\"customquestion\" alt=\"customtext\" value=\"a3\" />custom answer 2 : <input type=\"radio\" name=\"customquestion\" alt=\"customtext\" value=\"a2\" />", StringUtils.splitToArray(template.getContent(), "\n")[14]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("customquestion").inList(null, "a1", null, "a3", "a2").defaultValue("a3"), new String[]{"a1", "a2"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:customquestion", set_values.iterator().next());
        assertEquals("custom answer 1 : <input type=\"radio\" name=\"customquestion\" alt=\"customtext\" value=\"a1\" checked=\"checked\" />a3 : <input type=\"radio\" name=\"customquestion\" alt=\"customtext\" value=\"a3\" />custom answer 2 : <input type=\"radio\" name=\"customquestion\" alt=\"customtext\" value=\"a2\" />", StringUtils.splitToArray(template.getContent(), "\n")[14]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, RadioInListEnum.class, new ConstrainedProperty("customquestion").defaultValue("a3"), new String[]{"a1", "a2"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:customquestion", set_values.iterator().next());
        assertEquals("custom answer 1 : <input type=\"radio\" name=\"customquestion\" alt=\"customtext\" value=\"a1\" checked=\"checked\" />a3 : <input type=\"radio\" name=\"customquestion\" alt=\"customtext\" value=\"a3\" />custom answer 2 : <input type=\"radio\" name=\"customquestion\" alt=\"customtext\" value=\"a2\" />", StringUtils.splitToArray(template.getContent(), "\n")[14]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("customquestion").inList(null, "a1", null, "a3", "a2"), new String[]{"a4"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:customquestion", set_values.iterator().next());
        assertEquals("custom answer 1 : <input type=\"radio\" name=\"customquestion\" alt=\"customtext\" value=\"a1\" />a3 : <input type=\"radio\" name=\"customquestion\" alt=\"customtext\" value=\"a3\" />custom answer 2 : <input type=\"radio\" name=\"customquestion\" alt=\"customtext\" value=\"a2\" />", StringUtils.splitToArray(template.getContent(), "\n")[14]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, RadioInListEnum.class, "customquestion", new String[]{"a4"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:customquestion", set_values.iterator().next());
        assertEquals("custom answer 1 : <input type=\"radio\" name=\"customquestion\" alt=\"customtext\" value=\"a1\" />a3 : <input type=\"radio\" name=\"customquestion\" alt=\"customtext\" value=\"a3\" />custom answer 2 : <input type=\"radio\" name=\"customquestion\" alt=\"customtext\" value=\"a2\" />", StringUtils.splitToArray(template.getContent(), "\n")[14]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        template.setValue("form:radio:customquestion", "already set");
        set_values = builder.generateField(template, new ConstrainedProperty("customquestion"), null, null);
        assertEquals(0, set_values.size());
        assertEquals("already set", StringUtils.splitToArray(template.getContent(), "\n")[14]);
        template.removeValue("form:radio:customquestion");
    }

    @Test
    public void testGenerateFieldRadioCustomWithoutDefault() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        set_values = builder.generateField(template, "anothercustomquestion", null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:anothercustomquestion", set_values.iterator().next());
        assertEquals("<input type=\"radio\" name=\"anothercustomquestion\" />", StringUtils.splitToArray(template.getContent(), "\n")[19]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("anothercustomquestion"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:anothercustomquestion", set_values.iterator().next());
        assertEquals("<input type=\"radio\" name=\"anothercustomquestion\" />", StringUtils.splitToArray(template.getContent(), "\n")[19]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("anothercustomquestion").inList(null, "a1", null, "a3", "a2"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:anothercustomquestion", set_values.iterator().next());
        assertEquals("anothercustomquestion-a1:anothercustom answer 1 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a1\" />anothercustomquestion-a3:a3 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a3\" />anothercustomquestion-a2:anothercustom answer 2 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a2\" />", StringUtils.splitToArray(template.getContent(), "\n")[19]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, RadioInListEnum.class, "anothercustomquestion", null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:anothercustomquestion", set_values.iterator().next());
        assertEquals("anothercustomquestion-a1:anothercustom answer 1 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a1\" />anothercustomquestion-a3:a3 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a3\" />anothercustomquestion-a2:anothercustom answer 2 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a2\" />", StringUtils.splitToArray(template.getContent(), "\n")[19]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("anothercustomquestion").inList(null, "a1", null, "a3", "a2").defaultValue("a2"), new String[]{null, "a1"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:anothercustomquestion", set_values.iterator().next());
        assertEquals("anothercustomquestion-a1:anothercustom answer 1 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a1\" />anothercustomquestion-a3:a3 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a3\" />anothercustomquestion-a2:anothercustom answer 2 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a2\" checked=\"checked\" />", StringUtils.splitToArray(template.getContent(), "\n")[19]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, RadioInListEnum.class, new ConstrainedProperty("anothercustomquestion").defaultValue("a2"), new String[]{null, "a1"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:anothercustomquestion", set_values.iterator().next());
        assertEquals("anothercustomquestion-a1:anothercustom answer 1 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a1\" />anothercustomquestion-a3:a3 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a3\" />anothercustomquestion-a2:anothercustom answer 2 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a2\" checked=\"checked\" />", StringUtils.splitToArray(template.getContent(), "\n")[19]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("anothercustomquestion").inList(null, "a1", null, "a3", "a2"), new String[]{"a1", "a2"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:anothercustomquestion", set_values.iterator().next());
        assertEquals("anothercustomquestion-a1:anothercustom answer 1 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a1\" checked=\"checked\" />anothercustomquestion-a3:a3 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a3\" />anothercustomquestion-a2:anothercustom answer 2 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a2\" />", StringUtils.splitToArray(template.getContent(), "\n")[19]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, RadioInListEnum.class, "anothercustomquestion", new String[]{"a1", "a2"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:anothercustomquestion", set_values.iterator().next());
        assertEquals("anothercustomquestion-a1:anothercustom answer 1 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a1\" checked=\"checked\" />anothercustomquestion-a3:a3 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a3\" />anothercustomquestion-a2:anothercustom answer 2 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a2\" />", StringUtils.splitToArray(template.getContent(), "\n")[19]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("anothercustomquestion").inList(null, "a1", null, "a3", "a2").defaultValue("a3"), new String[]{"a1", "a2"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:anothercustomquestion", set_values.iterator().next());
        assertEquals("anothercustomquestion-a1:anothercustom answer 1 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a1\" checked=\"checked\" />anothercustomquestion-a3:a3 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a3\" />anothercustomquestion-a2:anothercustom answer 2 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a2\" />", StringUtils.splitToArray(template.getContent(), "\n")[19]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, RadioInListEnum.class, new ConstrainedProperty("anothercustomquestion").defaultValue("a3"), new String[]{"a1", "a2"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:anothercustomquestion", set_values.iterator().next());
        assertEquals("anothercustomquestion-a1:anothercustom answer 1 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a1\" checked=\"checked\" />anothercustomquestion-a3:a3 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a3\" />anothercustomquestion-a2:anothercustom answer 2 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a2\" />", StringUtils.splitToArray(template.getContent(), "\n")[19]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("anothercustomquestion").inList(null, "a1", null, "a3", "a2"), new String[]{"a4"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:anothercustomquestion", set_values.iterator().next());
        assertEquals("anothercustomquestion-a1:anothercustom answer 1 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a1\" />anothercustomquestion-a3:a3 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a3\" />anothercustomquestion-a2:anothercustom answer 2 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a2\" />", StringUtils.splitToArray(template.getContent(), "\n")[19]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, RadioInListEnum.class, "anothercustomquestion", new String[]{"a4"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:anothercustomquestion", set_values.iterator().next());
        assertEquals("anothercustomquestion-a1:anothercustom answer 1 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a1\" />anothercustomquestion-a3:a3 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a3\" />anothercustomquestion-a2:anothercustom answer 2 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a2\" />", StringUtils.splitToArray(template.getContent(), "\n")[19]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        template.setValue("form:radio:anothercustomquestion", "already set");
        set_values = builder.generateField(template, new ConstrainedProperty("anothercustomquestion"), null, null);
        assertEquals(0, set_values.size());
        assertEquals("already set", StringUtils.splitToArray(template.getContent(), "\n")[19]);
        template.removeValue("form:radio:anothercustomquestion");
    }

    @Test
    public void testGenerateFieldRadioCustomPrefix() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        set_values = builder.generateField(template, new ConstrainedProperty("customquestion").inList(null, "a1", null, "a3", "a2"), null, "another");
        assertEquals(1, set_values.size());
        assertEquals("form:radio:anothercustomquestion", set_values.iterator().next());
        assertEquals("anothercustomquestion-a1:anothercustom answer 1 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a1\" />anothercustomquestion-a3:a3 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a3\" />anothercustomquestion-a2:anothercustom answer 2 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a2\" />", StringUtils.splitToArray(template.getContent(), "\n")[19]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, RadioInListEnum.class, "customquestion", null, "another");
        assertEquals(1, set_values.size());
        assertEquals("form:radio:anothercustomquestion", set_values.iterator().next());
        assertEquals("anothercustomquestion-a1:anothercustom answer 1 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a1\" />anothercustomquestion-a3:a3 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a3\" />anothercustomquestion-a2:anothercustom answer 2 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a2\" />", StringUtils.splitToArray(template.getContent(), "\n")[19]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        template.setValue("form:radio:anothercustomquestion", "already set");
        set_values = builder.generateField(template, new ConstrainedProperty("customquestion"), null, "another");
        assertEquals(0, set_values.size());
        assertEquals("already set", StringUtils.splitToArray(template.getContent(), "\n")[19]);
        template.removeValue("form:radio:anothercustomquestion");
    }

    @Test
    public void testGenerateFieldRadioCustomDynamic() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        template.addResourceBundle(new ListResourceBundle() {
            public Object[][] getContents() {
                return new Object[][]{
                    {"customquestion:a1", "dynamic first"},
                    {"customquestion:a2", "dynamic second"},
                    {"customquestion:a3", "dynamic third"}
                };
            }
        });

        set_values = builder.generateField(template, new ConstrainedProperty("customquestion").inList("a1", "a3", "a2").defaultValue("a2"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:customquestion", set_values.iterator().next());
        assertEquals("dynamic first : <input type=\"radio\" name=\"customquestion\" alt=\"customtext\" value=\"a1\" />dynamic third : <input type=\"radio\" name=\"customquestion\" alt=\"customtext\" value=\"a3\" />dynamic second : <input type=\"radio\" name=\"customquestion\" alt=\"customtext\" value=\"a2\" checked=\"checked\" />", StringUtils.splitToArray(template.getContent(), "\n")[14]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, RadioInListEnum.class, new ConstrainedProperty("customquestion").defaultValue("a2"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:customquestion", set_values.iterator().next());
        assertEquals("dynamic first : <input type=\"radio\" name=\"customquestion\" alt=\"customtext\" value=\"a1\" />dynamic third : <input type=\"radio\" name=\"customquestion\" alt=\"customtext\" value=\"a3\" />dynamic second : <input type=\"radio\" name=\"customquestion\" alt=\"customtext\" value=\"a2\" checked=\"checked\" />", StringUtils.splitToArray(template.getContent(), "\n")[14]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("customquestion").inList("a1", "a3", "a2").defaultValue("a2"), new String[]{"a1", "a3"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:customquestion", set_values.iterator().next());
        assertEquals("dynamic first : <input type=\"radio\" name=\"customquestion\" alt=\"customtext\" value=\"a1\" checked=\"checked\" />dynamic third : <input type=\"radio\" name=\"customquestion\" alt=\"customtext\" value=\"a3\" />dynamic second : <input type=\"radio\" name=\"customquestion\" alt=\"customtext\" value=\"a2\" />", StringUtils.splitToArray(template.getContent(), "\n")[14]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, RadioInListEnum.class, new ConstrainedProperty("customquestion").defaultValue("a2"), new String[]{"a1", "a3"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:customquestion", set_values.iterator().next());
        assertEquals("dynamic first : <input type=\"radio\" name=\"customquestion\" alt=\"customtext\" value=\"a1\" checked=\"checked\" />dynamic third : <input type=\"radio\" name=\"customquestion\" alt=\"customtext\" value=\"a3\" />dynamic second : <input type=\"radio\" name=\"customquestion\" alt=\"customtext\" value=\"a2\" />", StringUtils.splitToArray(template.getContent(), "\n")[14]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        template.clear();

        template.addResourceBundle(new ListResourceBundle() {
            public Object[][] getContents() {
                return new Object[][]{
                    {"customquestion:a2", "dynamic second"},
                };
            }
        });

        set_values = builder.generateField(template, new ConstrainedProperty("customquestion").inList("a1", "a3", "a2").defaultValue("a2"), new String[]{"a1", "a3"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:customquestion", set_values.iterator().next());
        assertEquals("custom answer 1 : <input type=\"radio\" name=\"customquestion\" alt=\"customtext\" value=\"a1\" checked=\"checked\" />a3 : <input type=\"radio\" name=\"customquestion\" alt=\"customtext\" value=\"a3\" />dynamic second : <input type=\"radio\" name=\"customquestion\" alt=\"customtext\" value=\"a2\" />", StringUtils.splitToArray(template.getContent(), "\n")[14]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, RadioInListEnum.class, new ConstrainedProperty("customquestion").defaultValue("a2"), new String[]{"a1", "a3"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:customquestion", set_values.iterator().next());
        assertEquals("custom answer 1 : <input type=\"radio\" name=\"customquestion\" alt=\"customtext\" value=\"a1\" checked=\"checked\" />a3 : <input type=\"radio\" name=\"customquestion\" alt=\"customtext\" value=\"a3\" />dynamic second : <input type=\"radio\" name=\"customquestion\" alt=\"customtext\" value=\"a2\" />", StringUtils.splitToArray(template.getContent(), "\n")[14]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();
    }

    @Test
    public void testGenerateFieldRadioCustomTemplateNameWithoutDefault() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;

        Template template = TemplateFactory.HTML.get("formbuilder_fields_templatename");

        set_values = builder.replaceField(template, "templatenameanothercustomquestion", "anothercustomquestion", null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:templatenameanothercustomquestion", set_values.iterator().next());
        assertEquals("<input type=\"radio\" name=\"anothercustomquestion\" />", StringUtils.splitToArray(template.getContent(), "\n")[19]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenameanothercustomquestion", new ConstrainedProperty("anothercustomquestion"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:templatenameanothercustomquestion", set_values.iterator().next());
        assertEquals("<input type=\"radio\" name=\"anothercustomquestion\" />", StringUtils.splitToArray(template.getContent(), "\n")[19]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenameanothercustomquestion", new ConstrainedProperty("anothercustomquestion").inList(null, "a1", null, "a3", "a2"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:templatenameanothercustomquestion", set_values.iterator().next());
        assertEquals("anothercustomquestion-a1:anothercustom answer 1 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a1\" />anothercustomquestion-a3:a3 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a3\" />anothercustomquestion-a2:anothercustom answer 2 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a2\" />", StringUtils.splitToArray(template.getContent(), "\n")[19]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenameanothercustomquestion", RadioInListEnum.class, "anothercustomquestion", null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:templatenameanothercustomquestion", set_values.iterator().next());
        assertEquals("anothercustomquestion-a1:anothercustom answer 1 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a1\" />anothercustomquestion-a3:a3 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a3\" />anothercustomquestion-a2:anothercustom answer 2 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a2\" />", StringUtils.splitToArray(template.getContent(), "\n")[19]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenameanothercustomquestion", new ConstrainedProperty("anothercustomquestion").inList(null, "a1", null, "a3", "a2").defaultValue("a2"), new String[]{null, "a1"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:templatenameanothercustomquestion", set_values.iterator().next());
        assertEquals("anothercustomquestion-a1:anothercustom answer 1 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a1\" />anothercustomquestion-a3:a3 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a3\" />anothercustomquestion-a2:anothercustom answer 2 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a2\" checked=\"checked\" />", StringUtils.splitToArray(template.getContent(), "\n")[19]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenameanothercustomquestion", RadioInListEnum.class, new ConstrainedProperty("anothercustomquestion").defaultValue("a2"), new String[]{null, "a1"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:templatenameanothercustomquestion", set_values.iterator().next());
        assertEquals("anothercustomquestion-a1:anothercustom answer 1 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a1\" />anothercustomquestion-a3:a3 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a3\" />anothercustomquestion-a2:anothercustom answer 2 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a2\" checked=\"checked\" />", StringUtils.splitToArray(template.getContent(), "\n")[19]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenameanothercustomquestion", new ConstrainedProperty("anothercustomquestion").inList(null, "a1", null, "a3", "a2"), new String[]{"a1", "a2"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:templatenameanothercustomquestion", set_values.iterator().next());
        assertEquals("anothercustomquestion-a1:anothercustom answer 1 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a1\" checked=\"checked\" />anothercustomquestion-a3:a3 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a3\" />anothercustomquestion-a2:anothercustom answer 2 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a2\" />", StringUtils.splitToArray(template.getContent(), "\n")[19]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenameanothercustomquestion", RadioInListEnum.class, "anothercustomquestion", new String[]{"a1", "a2"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:templatenameanothercustomquestion", set_values.iterator().next());
        assertEquals("anothercustomquestion-a1:anothercustom answer 1 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a1\" checked=\"checked\" />anothercustomquestion-a3:a3 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a3\" />anothercustomquestion-a2:anothercustom answer 2 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a2\" />", StringUtils.splitToArray(template.getContent(), "\n")[19]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenameanothercustomquestion", new ConstrainedProperty("anothercustomquestion").inList(null, "a1", null, "a3", "a2").defaultValue("a3"), new String[]{"a1", "a2"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:templatenameanothercustomquestion", set_values.iterator().next());
        assertEquals("anothercustomquestion-a1:anothercustom answer 1 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a1\" checked=\"checked\" />anothercustomquestion-a3:a3 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a3\" />anothercustomquestion-a2:anothercustom answer 2 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a2\" />", StringUtils.splitToArray(template.getContent(), "\n")[19]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenameanothercustomquestion", RadioInListEnum.class, new ConstrainedProperty("anothercustomquestion").defaultValue("a3"), new String[]{"a1", "a2"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:templatenameanothercustomquestion", set_values.iterator().next());
        assertEquals("anothercustomquestion-a1:anothercustom answer 1 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a1\" checked=\"checked\" />anothercustomquestion-a3:a3 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a3\" />anothercustomquestion-a2:anothercustom answer 2 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a2\" />", StringUtils.splitToArray(template.getContent(), "\n")[19]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenameanothercustomquestion", new ConstrainedProperty("anothercustomquestion").inList(null, "a1", null, "a3", "a2"), new String[]{"a4"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:templatenameanothercustomquestion", set_values.iterator().next());
        assertEquals("anothercustomquestion-a1:anothercustom answer 1 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a1\" />anothercustomquestion-a3:a3 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a3\" />anothercustomquestion-a2:anothercustom answer 2 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a2\" />", StringUtils.splitToArray(template.getContent(), "\n")[19]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenameanothercustomquestion", RadioInListEnum.class, "anothercustomquestion", new String[]{"a4"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:templatenameanothercustomquestion", set_values.iterator().next());
        assertEquals("anothercustomquestion-a1:anothercustom answer 1 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a1\" />anothercustomquestion-a3:a3 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a3\" />anothercustomquestion-a2:anothercustom answer 2 : <input type=\"radio\" name=\"anothercustomquestion\" value=\"a2\" />", StringUtils.splitToArray(template.getContent(), "\n")[19]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        template.setValue("form:radio:templatenameanothercustomquestion", "already set");
        set_values = builder.replaceField(template, "templatenameanothercustomquestion", new ConstrainedProperty("anothercustomquestion"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:templatenameanothercustomquestion", set_values.iterator().next());
        assertEquals("<input type=\"radio\" name=\"anothercustomquestion\" />", StringUtils.splitToArray(template.getContent(), "\n")[19]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();
    }

    @Test
    public void testGenerateFieldRadioEmptyCustomWithDefault() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;

        Template template = TemplateFactory.HTML.get("formbuilder_fields_emptycustom");

        set_values = builder.generateField(template, "emptycustomquestion", null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:emptycustomquestion", set_values.iterator().next());
        assertEquals("<input type=\"radio\" name=\"emptycustomquestion\" alt=\"emptycustomtext\" />", StringUtils.splitToArray(template.getContent(), "\n")[0]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("emptycustomquestion"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:emptycustomquestion", set_values.iterator().next());
        assertEquals("<input type=\"radio\" name=\"emptycustomquestion\" alt=\"emptycustomtext\" />", StringUtils.splitToArray(template.getContent(), "\n")[0]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("emptycustomquestion").inList(null, "a1", null, "a3", "a2"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:emptycustomquestion", set_values.iterator().next());
        assertEquals(" -  -  - ", StringUtils.splitToArray(template.getContent(), "\n")[0]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, RadioInListEnum.class, "emptycustomquestion", null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:emptycustomquestion", set_values.iterator().next());
        assertEquals(" -  -  - ", StringUtils.splitToArray(template.getContent(), "\n")[0]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        template.setValue("form:radio:emptycustomquestion", "already set");
        set_values = builder.generateField(template, new ConstrainedProperty("emptycustomquestion"), null, null);
        assertEquals(0, set_values.size());
        assertEquals("already set", StringUtils.splitToArray(template.getContent(), "\n")[0]);
        template.removeValue("form:radio:emptycustomquestion");
    }

    @Test
    public void testGenerateFieldRadioEmptyCustomWithoutDefault() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;

        Template template = TemplateFactory.HTML.get("formbuilder_fields_emptycustom");

        set_values = builder.generateField(template, "anotheremptycustomquestion", null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:anotheremptycustomquestion", set_values.iterator().next());
        assertEquals("<input type=\"radio\" name=\"anotheremptycustomquestion\" />", StringUtils.splitToArray(template.getContent(), "\n")[2]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("anotheremptycustomquestion"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:anotheremptycustomquestion", set_values.iterator().next());
        assertEquals("<input type=\"radio\" name=\"anotheremptycustomquestion\" />", StringUtils.splitToArray(template.getContent(), "\n")[2]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("anotheremptycustomquestion").inList(null, "a1", null, "a3", "a2"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:anotheremptycustomquestion", set_values.iterator().next());
        assertEquals(" -  -  - ", StringUtils.splitToArray(template.getContent(), "\n")[2]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, RadioInListEnum.class, "anotheremptycustomquestion", null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:radio:anotheremptycustomquestion", set_values.iterator().next());
        assertEquals(" -  -  - ", StringUtils.splitToArray(template.getContent(), "\n")[2]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        template.setValue("form:radio:anotheremptycustomquestion", "already set");
        set_values = builder.generateField(template, new ConstrainedProperty("anotheremptycustomquestion"), null, null);
        assertEquals(0, set_values.size());
        assertEquals("already set", StringUtils.splitToArray(template.getContent(), "\n")[2]);
        template.removeValue("form:radio:anotheremptycustomquestion");
    }

    @Test
    public void testGenerateFieldRadioEmptyCustomPrefix() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;

        Template template = TemplateFactory.HTML.get("formbuilder_fields_emptycustom");

        set_values = builder.generateField(template, "emptycustomquestion", null, "another");
        assertEquals(1, set_values.size());
        assertEquals("form:radio:anotheremptycustomquestion", set_values.iterator().next());
        assertEquals("<input type=\"radio\" name=\"anotheremptycustomquestion\" />", StringUtils.splitToArray(template.getContent(), "\n")[2]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("emptycustomquestion").inList(null, "a1", null, "a3", "a2"), null, "another");
        assertEquals(1, set_values.size());
        assertEquals("form:radio:anotheremptycustomquestion", set_values.iterator().next());
        assertEquals(" -  -  - ", StringUtils.splitToArray(template.getContent(), "\n")[2]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, RadioInListEnum.class, "emptycustomquestion", null, "another");
        assertEquals(1, set_values.size());
        assertEquals("form:radio:anotheremptycustomquestion", set_values.iterator().next());
        assertEquals(" -  -  - ", StringUtils.splitToArray(template.getContent(), "\n")[2]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        template.setValue("form:radio:anotheremptycustomquestion", "already set");
        set_values = builder.generateField(template, new ConstrainedProperty("emptycustomquestion"), null, "another");
        assertEquals(0, set_values.size());
        assertEquals("already set", StringUtils.splitToArray(template.getContent(), "\n")[2]);
        template.removeValue("form:radio:anotheremptycustomquestion");
    }

    @Test
    public void testRemoveFieldRadio() {
        FormBuilderHtml builder = new FormBuilderHtml();

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        String raw_content = template.getContent();

        builder.generateField(template, new ConstrainedProperty("question").inList(null, "a1", null, "a3", "a2"), new String[]{"a4"}, null);
        assertNotEquals(raw_content, template.getContent());
        builder.removeField(template, "question", null);
        assertEquals(raw_content, template.getContent());

        builder.generateField(template, RadioInListEnum.class, "question", new String[]{"a4"}, null);
        assertNotEquals(raw_content, template.getContent());
        builder.removeField(template, "question", null);
        assertEquals(raw_content, template.getContent());
    }

    @Test
    public void testRemoveFieldRadioTemplateName() {
        FormBuilderHtml builder = new FormBuilderHtml();

        Template template = TemplateFactory.HTML.get("formbuilder_fields_templatename");

        String raw_content = template.getContent();

        builder.replaceField(template, "templatenamequestion", new ConstrainedProperty("question").inList(null, "a1", null, "a3", "a2"), new String[]{"a4"}, null);
        assertNotEquals(raw_content, template.getContent());
        builder.removeField(template, "templatenamequestion");
        assertEquals(raw_content, template.getContent());

        builder.replaceField(template, "templatenamequestion", RadioInListEnum.class, "question", new String[]{"a4"}, null);
        assertNotEquals(raw_content, template.getContent());
        builder.removeField(template, "templatenamequestion");
        assertEquals(raw_content, template.getContent());
    }

    @Test
    public void testRemoveFieldRadioPrefix() {
        FormBuilderHtml builder = new FormBuilderHtml();

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        String raw_content = template.getContent();

        builder.generateField(template, new ConstrainedProperty("question").inList(null, "a1", null, "a3", "a2"), new String[]{"a4"}, "another");
        assertNotEquals(raw_content, template.getContent());
        builder.removeField(template, "question", "another");
        assertEquals(raw_content, template.getContent());

        builder.generateField(template, RadioInListEnum.class, "question", new String[]{"a4"}, "another");
        assertNotEquals(raw_content, template.getContent());
        builder.removeField(template, "question", "another");
        assertEquals(raw_content, template.getContent());
    }

    @Test
    public void testGenerateFieldCheckboxWithDefault() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        set_values = builder.generateField(template, "options", null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:options", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" />", StringUtils.splitToArray(template.getContent(), "\n")[24]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("options"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:options", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" />", StringUtils.splitToArray(template.getContent(), "\n")[24]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("options").inList(null, "1", null, "3", "2"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:options", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"1\" />option1<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"3\" />3<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"2\" />option2", StringUtils.splitToArray(template.getContent(), "\n")[24]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, RadioInListEnum.class, new ConstrainedProperty("options").inList(null, "1", null, "3", "2"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:options", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"1\" />option1<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"3\" />3<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"2\" />option2", StringUtils.splitToArray(template.getContent(), "\n")[24]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, CheckboxInListEnum.class, "options", null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:options", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"1\" />option1<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"3\" />3<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"2\" />option2", StringUtils.splitToArray(template.getContent(), "\n")[24]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("options").inList(null, "1", null, "3", "2").defaultValue("2"), new String[]{null, "1"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:options", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"1\" checked=\"checked\" />option1<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"3\" />3<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"2\" />option2", StringUtils.splitToArray(template.getContent(), "\n")[24]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, CheckboxInListEnum.class, new ConstrainedProperty("options").defaultValue("2"), new String[]{null, "1"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:options", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"1\" checked=\"checked\" />option1<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"3\" />3<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"2\" />option2", StringUtils.splitToArray(template.getContent(), "\n")[24]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("options").inList(null, "1", null, "3", "2"), new String[]{"1", "2"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:options", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"1\" checked=\"checked\" />option1<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"3\" />3<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"2\" checked=\"checked\" />option2", StringUtils.splitToArray(template.getContent(), "\n")[24]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, CheckboxInListEnum.class, "options", new String[]{"1", "2"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:options", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"1\" checked=\"checked\" />option1<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"3\" />3<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"2\" checked=\"checked\" />option2", StringUtils.splitToArray(template.getContent(), "\n")[24]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("options").inList(null, "1", null, "3", "2").defaultValue("3"), new String[]{"1", "2"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:options", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"1\" checked=\"checked\" />option1<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"3\" />3<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"2\" checked=\"checked\" />option2", StringUtils.splitToArray(template.getContent(), "\n")[24]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, CheckboxInListEnum.class, new ConstrainedProperty("options").defaultValue("3"), new String[]{"1", "2"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:options", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"1\" checked=\"checked\" />option1<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"3\" />3<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"2\" checked=\"checked\" />option2", StringUtils.splitToArray(template.getContent(), "\n")[24]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("options").inList(null, "1", null, "3", "2"), new String[]{"a4"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:options", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"1\" />option1<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"3\" />3<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"2\" />option2", StringUtils.splitToArray(template.getContent(), "\n")[24]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, CheckboxInListEnum.class, "options", new String[]{"a4"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:options", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"1\" />option1<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"3\" />3<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"2\" />option2", StringUtils.splitToArray(template.getContent(), "\n")[24]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        template.setValue("form:checkbox:options", "already set");
        set_values = builder.generateField(template, new ConstrainedProperty("options"), null, null);
        assertEquals(0, set_values.size());
        assertEquals("already set", StringUtils.splitToArray(template.getContent(), "\n")[24]);
        template.removeValue("form:checkbox:options");
    }

    @Test
    public void testGenerateFieldCheckboxWithoutDefault() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;
        Iterator<String> set_values_it;

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        set_values = builder.generateField(template, "otheroptions", null, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:checkbox:otheroptions", set_values_it.next());
        assertEquals("form:display:otheroptions", set_values_it.next());
        assertEquals("<input type=\"checkbox\" name=\"otheroptions\" />", StringUtils.splitToArray(template.getContent(), "\n")[28]);
        assertEquals("<div id=\"otheroptions{{v form:value/}}\"></div>", StringUtils.splitToArray(template.getContent(), "\n")[57]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("otheroptions"), null, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:checkbox:otheroptions", set_values_it.next());
        assertEquals("form:display:otheroptions", set_values_it.next());
        assertEquals("<input type=\"checkbox\" name=\"otheroptions\" />", StringUtils.splitToArray(template.getContent(), "\n")[28]);
        assertEquals("<div id=\"otheroptions{{v form:value/}}\"></div>", StringUtils.splitToArray(template.getContent(), "\n")[57]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("otheroptions").inList(null, "1", null, "3", "2"), null, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:checkbox:otheroptions", set_values_it.next());
        assertEquals("form:display:otheroptions", set_values_it.next());
        assertEquals("<input type=\"checkbox\" name=\"otheroptions\" value=\"1\" />other option 1<input type=\"checkbox\" name=\"otheroptions\" value=\"3\" />3<input type=\"checkbox\" name=\"otheroptions\" value=\"2\" />other option 2", StringUtils.splitToArray(template.getContent(), "\n")[28]);
        assertEquals("<div id=\"otheroptions{{v form:value/}}\"></div>", StringUtils.splitToArray(template.getContent(), "\n")[57]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, CheckboxInListEnum.class, "otheroptions", null, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:checkbox:otheroptions", set_values_it.next());
        assertEquals("form:display:otheroptions", set_values_it.next());
        assertEquals("<input type=\"checkbox\" name=\"otheroptions\" value=\"1\" />other option 1<input type=\"checkbox\" name=\"otheroptions\" value=\"3\" />3<input type=\"checkbox\" name=\"otheroptions\" value=\"2\" />other option 2", StringUtils.splitToArray(template.getContent(), "\n")[28]);
        assertEquals("<div id=\"otheroptions{{v form:value/}}\"></div>", StringUtils.splitToArray(template.getContent(), "\n")[57]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("otheroptions").inList(null, "1", null, "3", "2").defaultValue("3"), new String[]{"1", "2"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:checkbox:otheroptions", set_values_it.next());
        assertEquals("form:display:otheroptions", set_values_it.next());
        assertEquals("<input type=\"checkbox\" name=\"otheroptions\" value=\"1\" checked=\"checked\" />other option 1<input type=\"checkbox\" name=\"otheroptions\" value=\"3\" />3<input type=\"checkbox\" name=\"otheroptions\" value=\"2\" checked=\"checked\" />other option 2", StringUtils.splitToArray(template.getContent(), "\n")[28]);
        assertEquals("<div id=\"otheroptions1\">other option 1</div><div id=\"otheroptions2\">other option 2</div>", StringUtils.splitToArray(template.getContent(), "\n")[57]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, CheckboxInListEnum.class, new ConstrainedProperty("otheroptions").defaultValue("3"), new String[]{"1", "2"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:checkbox:otheroptions", set_values_it.next());
        assertEquals("form:display:otheroptions", set_values_it.next());
        assertEquals("<input type=\"checkbox\" name=\"otheroptions\" value=\"1\" checked=\"checked\" />other option 1<input type=\"checkbox\" name=\"otheroptions\" value=\"3\" />3<input type=\"checkbox\" name=\"otheroptions\" value=\"2\" checked=\"checked\" />other option 2", StringUtils.splitToArray(template.getContent(), "\n")[28]);
        assertEquals("<div id=\"otheroptions1\">other option 1</div><div id=\"otheroptions2\">other option 2</div>", StringUtils.splitToArray(template.getContent(), "\n")[57]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();
    }

    @Test
    public void testGenerateFieldCheckboxPrefix() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;
        Iterator<String> set_values_it;

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        set_values = builder.generateField(template, "options", null, "other");
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:checkbox:otheroptions", set_values_it.next());
        assertEquals("form:display:otheroptions", set_values_it.next());
        assertEquals("form:checkbox:otheroptions", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"otheroptions\" />", StringUtils.splitToArray(template.getContent(), "\n")[28]);
        assertEquals("<div id=\"otheroptions{{v form:value/}}\"></div>", StringUtils.splitToArray(template.getContent(), "\n")[57]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("options"), null, "other");
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:checkbox:otheroptions", set_values_it.next());
        assertEquals("form:display:otheroptions", set_values_it.next());
        assertEquals("<input type=\"checkbox\" name=\"otheroptions\" />", StringUtils.splitToArray(template.getContent(), "\n")[28]);
        assertEquals("<div id=\"otheroptions{{v form:value/}}\"></div>", StringUtils.splitToArray(template.getContent(), "\n")[57]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("options").inList(null, "1", null, "3", "2"), null, "other");
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:checkbox:otheroptions", set_values_it.next());
        assertEquals("form:display:otheroptions", set_values_it.next());
        assertEquals("<input type=\"checkbox\" name=\"otheroptions\" value=\"1\" />other option 1<input type=\"checkbox\" name=\"otheroptions\" value=\"3\" />3<input type=\"checkbox\" name=\"otheroptions\" value=\"2\" />other option 2", StringUtils.splitToArray(template.getContent(), "\n")[28]);
        assertEquals("<div id=\"otheroptions{{v form:value/}}\"></div>", StringUtils.splitToArray(template.getContent(), "\n")[57]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, CheckboxInListEnum.class, "options", null, "other");
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:checkbox:otheroptions", set_values_it.next());
        assertEquals("form:display:otheroptions", set_values_it.next());
        assertEquals("<input type=\"checkbox\" name=\"otheroptions\" value=\"1\" />other option 1<input type=\"checkbox\" name=\"otheroptions\" value=\"3\" />3<input type=\"checkbox\" name=\"otheroptions\" value=\"2\" />other option 2", StringUtils.splitToArray(template.getContent(), "\n")[28]);
        assertEquals("<div id=\"otheroptions{{v form:value/}}\"></div>", StringUtils.splitToArray(template.getContent(), "\n")[57]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("options").inList(null, "1", null, "3", "2").defaultValue("3"), new String[]{"1", "2"}, "other");
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:checkbox:otheroptions", set_values_it.next());
        assertEquals("form:display:otheroptions", set_values_it.next());
        assertEquals("<input type=\"checkbox\" name=\"otheroptions\" value=\"1\" checked=\"checked\" />other option 1<input type=\"checkbox\" name=\"otheroptions\" value=\"3\" />3<input type=\"checkbox\" name=\"otheroptions\" value=\"2\" checked=\"checked\" />other option 2", StringUtils.splitToArray(template.getContent(), "\n")[28]);
        assertEquals("<div id=\"otheroptions1\">other option 1</div><div id=\"otheroptions2\">other option 2</div>", StringUtils.splitToArray(template.getContent(), "\n")[57]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, CheckboxInListEnum.class, new ConstrainedProperty("options").defaultValue("3"), new String[]{"1", "2"}, "other");
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:checkbox:otheroptions", set_values_it.next());
        assertEquals("form:display:otheroptions", set_values_it.next());
        assertEquals("<input type=\"checkbox\" name=\"otheroptions\" value=\"1\" checked=\"checked\" />other option 1<input type=\"checkbox\" name=\"otheroptions\" value=\"3\" />3<input type=\"checkbox\" name=\"otheroptions\" value=\"2\" checked=\"checked\" />other option 2", StringUtils.splitToArray(template.getContent(), "\n")[28]);
        assertEquals("<div id=\"otheroptions1\">other option 1</div><div id=\"otheroptions2\">other option 2</div>", StringUtils.splitToArray(template.getContent(), "\n")[57]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        template.setValue("form:checkbox:otheroptions", "already set");
        template.setValue("form:display:otheroptions", "already set too");
        set_values = builder.generateField(template, new ConstrainedProperty("options"), null, "other");
        assertEquals(0, set_values.size());
        assertEquals("already set", StringUtils.splitToArray(template.getContent(), "\n")[28]);
        assertEquals("already set too", StringUtils.splitToArray(template.getContent(), "\n")[57]);
        template.removeValue("form:display:otheroptions");
        template.removeValue("form:checkbox:otheroptions");
    }

    @Test
    public void testGenerateFieldCheckboxTemplateNameWithDefault() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;

        Template template = TemplateFactory.HTML.get("formbuilder_fields_templatename");

        set_values = builder.replaceField(template, "templatenameoptions", "options", null, null);
        assertEquals(2, set_values.size());
        Iterator set_values_it = set_values.iterator();
        assertEquals("form:checkbox:templatenameoptions", set_values_it.next());
        assertEquals("form:display:templatenameoptions", set_values_it.next());
        assertEquals("<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" />", StringUtils.splitToArray(template.getContent(), "\n")[24]);
        assertEquals("<div id=\"options{{v form:value/}}\"></div>", StringUtils.splitToArray(template.getContent(), "\n")[57]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenameoptions", new ConstrainedProperty("options"), null, null);
        assertEquals(2, set_values.size());
        set_values_it = set_values.iterator();
        assertEquals("form:checkbox:templatenameoptions", set_values_it.next());
        assertEquals("form:display:templatenameoptions", set_values_it.next());
        assertEquals("<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" />", StringUtils.splitToArray(template.getContent(), "\n")[24]);
        assertEquals("<div id=\"options{{v form:value/}}\"></div>", StringUtils.splitToArray(template.getContent(), "\n")[57]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenameoptions", new ConstrainedProperty("options").inList(null, "1", null, "3", "2"), null, null);
        assertEquals(2, set_values.size());
        set_values_it = set_values.iterator();
        assertEquals("form:checkbox:templatenameoptions", set_values_it.next());
        assertEquals("form:display:templatenameoptions", set_values_it.next());
        assertEquals("<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"1\" />option1<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"3\" />3<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"2\" />option2", StringUtils.splitToArray(template.getContent(), "\n")[24]);
        assertEquals("<div id=\"options{{v form:value/}}\"></div>", StringUtils.splitToArray(template.getContent(), "\n")[57]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenameoptions", CheckboxInListEnum.class, "options", null, null);
        assertEquals(2, set_values.size());
        set_values_it = set_values.iterator();
        assertEquals("form:checkbox:templatenameoptions", set_values_it.next());
        assertEquals("form:display:templatenameoptions", set_values_it.next());
        assertEquals("<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"1\" />option1<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"3\" />3<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"2\" />option2", StringUtils.splitToArray(template.getContent(), "\n")[24]);
        assertEquals("<div id=\"options{{v form:value/}}\"></div>", StringUtils.splitToArray(template.getContent(), "\n")[57]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenameoptions", new ConstrainedProperty("options").inList(null, "1", null, "3", "2").defaultValue("2"), new String[]{null, "1"}, null);
        assertEquals(2, set_values.size());
        set_values_it = set_values.iterator();
        assertEquals("form:checkbox:templatenameoptions", set_values_it.next());
        assertEquals("form:display:templatenameoptions", set_values_it.next());
        assertEquals("<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"1\" checked=\"checked\" />option1<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"3\" />3<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"2\" />option2", StringUtils.splitToArray(template.getContent(), "\n")[24]);
        assertEquals("<div id=\"options2\">option2</div><div id=\"options1\">option1</div>", StringUtils.splitToArray(template.getContent(), "\n")[57]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenameoptions", CheckboxInListEnum.class, new ConstrainedProperty("options").defaultValue("2"), new String[]{null, "1"}, null);
        assertEquals(2, set_values.size());
        set_values_it = set_values.iterator();
        assertEquals("form:checkbox:templatenameoptions", set_values_it.next());
        assertEquals("form:display:templatenameoptions", set_values_it.next());
        assertEquals("<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"1\" checked=\"checked\" />option1<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"3\" />3<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"2\" />option2", StringUtils.splitToArray(template.getContent(), "\n")[24]);
        assertEquals("<div id=\"options2\">option2</div><div id=\"options1\">option1</div>", StringUtils.splitToArray(template.getContent(), "\n")[57]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenameoptions", new ConstrainedProperty("options").inList(null, "1", null, "3", "2"), new String[]{"1", "2"}, null);
        assertEquals(2, set_values.size());
        set_values_it = set_values.iterator();
        assertEquals("form:checkbox:templatenameoptions", set_values_it.next());
        assertEquals("form:display:templatenameoptions", set_values_it.next());
        assertEquals("<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"1\" checked=\"checked\" />option1<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"3\" />3<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"2\" checked=\"checked\" />option2", StringUtils.splitToArray(template.getContent(), "\n")[24]);
        assertEquals("<div id=\"options1\">option1</div><div id=\"options2\">option2</div>", StringUtils.splitToArray(template.getContent(), "\n")[57]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenameoptions", CheckboxInListEnum.class, "options", new String[]{"1", "2"}, null);
        assertEquals(2, set_values.size());
        set_values_it = set_values.iterator();
        assertEquals("form:checkbox:templatenameoptions", set_values_it.next());
        assertEquals("form:display:templatenameoptions", set_values_it.next());
        assertEquals("<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"1\" checked=\"checked\" />option1<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"3\" />3<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"2\" checked=\"checked\" />option2", StringUtils.splitToArray(template.getContent(), "\n")[24]);
        assertEquals("<div id=\"options1\">option1</div><div id=\"options2\">option2</div>", StringUtils.splitToArray(template.getContent(), "\n")[57]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenameoptions", new ConstrainedProperty("options").inList(null, "1", null, "3", "2").defaultValue("3"), new String[]{"1", "2"}, null);
        assertEquals(2, set_values.size());
        set_values_it = set_values.iterator();
        assertEquals("form:checkbox:templatenameoptions", set_values_it.next());
        assertEquals("form:display:templatenameoptions", set_values_it.next());
        assertEquals("<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"1\" checked=\"checked\" />option1<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"3\" />3<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"2\" checked=\"checked\" />option2", StringUtils.splitToArray(template.getContent(), "\n")[24]);
        assertEquals("<div id=\"options1\">option1</div><div id=\"options2\">option2</div>", StringUtils.splitToArray(template.getContent(), "\n")[57]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenameoptions", CheckboxInListEnum.class, new ConstrainedProperty("options").defaultValue("3"), new String[]{"1", "2"}, null);
        assertEquals(2, set_values.size());
        set_values_it = set_values.iterator();
        assertEquals("form:checkbox:templatenameoptions", set_values_it.next());
        assertEquals("form:display:templatenameoptions", set_values_it.next());
        assertEquals("<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"1\" checked=\"checked\" />option1<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"3\" />3<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"2\" checked=\"checked\" />option2", StringUtils.splitToArray(template.getContent(), "\n")[24]);
        assertEquals("<div id=\"options1\">option1</div><div id=\"options2\">option2</div>", StringUtils.splitToArray(template.getContent(), "\n")[57]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenameoptions", new ConstrainedProperty("options").inList(null, "1", null, "3", "2"), new String[]{"a4"}, null);
        assertEquals(2, set_values.size());
        set_values_it = set_values.iterator();
        assertEquals("form:checkbox:templatenameoptions", set_values_it.next());
        assertEquals("form:display:templatenameoptions", set_values_it.next());
        assertEquals("<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"1\" />option1<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"3\" />3<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"2\" />option2", StringUtils.splitToArray(template.getContent(), "\n")[24]);
        assertEquals("<div id=\"optionsa4\">a4</div>", StringUtils.splitToArray(template.getContent(), "\n")[57]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenameoptions", CheckboxInListEnum.class, "options", new String[]{"a4"}, null);
        assertEquals(2, set_values.size());
        set_values_it = set_values.iterator();
        assertEquals("form:checkbox:templatenameoptions", set_values_it.next());
        assertEquals("form:display:templatenameoptions", set_values_it.next());
        assertEquals("<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"1\" />option1<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"3\" />3<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"2\" />option2", StringUtils.splitToArray(template.getContent(), "\n")[24]);
        assertEquals("<div id=\"optionsa4\">a4</div>", StringUtils.splitToArray(template.getContent(), "\n")[57]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        template.setValue("form:checkbox:templatenameoptions", "already set");
        set_values = builder.replaceField(template, "templatenameoptions", new ConstrainedProperty("options"), null, null);
        assertEquals(2, set_values.size());
        set_values_it = set_values.iterator();
        assertEquals("form:checkbox:templatenameoptions", set_values_it.next());
        assertEquals("form:display:templatenameoptions", set_values_it.next());
        assertEquals("<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" />", StringUtils.splitToArray(template.getContent(), "\n")[24]);
        assertEquals("<div id=\"options{{v form:value/}}\"></div>", StringUtils.splitToArray(template.getContent(), "\n")[57]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();
    }

    @Test
    public void testGenerateFieldCheckboxDynamic() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        template.addResourceBundle(new ListResourceBundle() {
            public Object[][] getContents() {
                return new Object[][]{
                    {"options:1", "dynamic first"},
                    {"options:2", "dynamic second"},
                    {"options:3", "dynamic third"}
                };
            }
        });

        set_values = builder.generateField(template, new ConstrainedProperty("options").inList("1", "3", "2").defaultValue("2"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:options", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"1\" />dynamic first<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"3\" />dynamic third<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"2\" checked=\"checked\" />dynamic second", StringUtils.splitToArray(template.getContent(), "\n")[24]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, CheckboxInListEnum.class, new ConstrainedProperty("options").defaultValue("2"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:options", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"1\" />dynamic first<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"3\" />dynamic third<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"2\" checked=\"checked\" />dynamic second", StringUtils.splitToArray(template.getContent(), "\n")[24]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("options").inList("1", "3", "2").defaultValue("2"), new String[]{"3", "1"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:options", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"1\" checked=\"checked\" />dynamic first<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"3\" checked=\"checked\" />dynamic third<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"2\" />dynamic second", StringUtils.splitToArray(template.getContent(), "\n")[24]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, CheckboxInListEnum.class, new ConstrainedProperty("options").defaultValue("2"), new String[]{"3", "1"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:options", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"1\" checked=\"checked\" />dynamic first<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"3\" checked=\"checked\" />dynamic third<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"2\" />dynamic second", StringUtils.splitToArray(template.getContent(), "\n")[24]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        template.clear();

        template.addResourceBundle(new ListResourceBundle() {
            public Object[][] getContents() {
                return new Object[][]{
                    {"options:2", "dynamic second"},
                };
            }
        });

        set_values = builder.generateField(template, new ConstrainedProperty("options").inList("1", "3", "2").defaultValue("2"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:options", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"1\" />option1<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"3\" />3<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"2\" checked=\"checked\" />dynamic second", StringUtils.splitToArray(template.getContent(), "\n")[24]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, CheckboxInListEnum.class, new ConstrainedProperty("options").defaultValue("2"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:options", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"1\" />option1<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"3\" />3<input type=\"checkbox\" name=\"options\" alt=\"someblurp\" value=\"2\" checked=\"checked\" />dynamic second", StringUtils.splitToArray(template.getContent(), "\n")[24]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();
    }

    @Test
    public void testGenerateFieldCheckboxCustomWithDefault() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        set_values = builder.generateField(template, "customoptions", null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:customoptions", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" />", StringUtils.splitToArray(template.getContent(), "\n")[32]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("customoptions"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:customoptions", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" />", StringUtils.splitToArray(template.getContent(), "\n")[32]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("customoptions").inList(null, "1", null, "3", "2"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:customoptions", set_values.iterator().next());
        assertEquals("custom option 1 : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"1\" />3 : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"3\" />custom option 2 : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"2\" />", StringUtils.splitToArray(template.getContent(), "\n")[32]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, CheckboxInListEnum.class, "customoptions", null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:customoptions", set_values.iterator().next());
        assertEquals("custom option 1 : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"1\" />3 : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"3\" />custom option 2 : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"2\" />", StringUtils.splitToArray(template.getContent(), "\n")[32]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("customoptions").inList(null, "1", null, "3", "2").defaultValue("2"), new String[]{null, "1"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:customoptions", set_values.iterator().next());
        assertEquals("custom option 1 : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"1\" checked=\"checked\" />3 : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"3\" />custom option 2 : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"2\" />", StringUtils.splitToArray(template.getContent(), "\n")[32]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, CheckboxInListEnum.class, new ConstrainedProperty("customoptions").defaultValue("2"), new String[]{null, "1"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:customoptions", set_values.iterator().next());
        assertEquals("custom option 1 : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"1\" checked=\"checked\" />3 : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"3\" />custom option 2 : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"2\" />", StringUtils.splitToArray(template.getContent(), "\n")[32]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("customoptions").inList(null, "1", null, "3", "2"), new String[]{"1", "2"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:customoptions", set_values.iterator().next());
        assertEquals("custom option 1 : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"1\" checked=\"checked\" />3 : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"3\" />custom option 2 : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"2\" checked=\"checked\" />", StringUtils.splitToArray(template.getContent(), "\n")[32]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, CheckboxInListEnum.class, "customoptions", new String[]{"1", "2"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:customoptions", set_values.iterator().next());
        assertEquals("custom option 1 : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"1\" checked=\"checked\" />3 : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"3\" />custom option 2 : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"2\" checked=\"checked\" />", StringUtils.splitToArray(template.getContent(), "\n")[32]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("customoptions").inList(null, "1", null, "3", "2").defaultValue("3"), new String[]{"1", "2"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:customoptions", set_values.iterator().next());
        assertEquals("custom option 1 : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"1\" checked=\"checked\" />3 : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"3\" />custom option 2 : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"2\" checked=\"checked\" />", StringUtils.splitToArray(template.getContent(), "\n")[32]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, CheckboxInListEnum.class, new ConstrainedProperty("customoptions").defaultValue("3"), new String[]{"1", "2"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:customoptions", set_values.iterator().next());
        assertEquals("custom option 1 : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"1\" checked=\"checked\" />3 : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"3\" />custom option 2 : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"2\" checked=\"checked\" />", StringUtils.splitToArray(template.getContent(), "\n")[32]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("customoptions").inList(null, "1", null, "3", "2"), new String[]{"a4"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:customoptions", set_values.iterator().next());
        assertEquals("custom option 1 : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"1\" />3 : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"3\" />custom option 2 : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"2\" />", StringUtils.splitToArray(template.getContent(), "\n")[32]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, CheckboxInListEnum.class, "customoptions", new String[]{"a4"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:customoptions", set_values.iterator().next());
        assertEquals("custom option 1 : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"1\" />3 : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"3\" />custom option 2 : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"2\" />", StringUtils.splitToArray(template.getContent(), "\n")[32]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        template.setValue("form:checkbox:customoptions", "already set");
        set_values = builder.generateField(template, new ConstrainedProperty("customoptions"), null, null);
        assertEquals(0, set_values.size());
        assertEquals("already set", StringUtils.splitToArray(template.getContent(), "\n")[32]);
        template.removeValue("form:checkbox:customoptions");
    }

    @Test
    public void testGenerateFieldCheckboxCustomWithoutDefault() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        set_values = builder.generateField(template, "othercustomoptions", null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:othercustomoptions", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"othercustomoptions\" />", StringUtils.splitToArray(template.getContent(), "\n")[37]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("othercustomoptions"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:othercustomoptions", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"othercustomoptions\" />", StringUtils.splitToArray(template.getContent(), "\n")[37]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("othercustomoptions").inList(null, "1", null, "3", "2"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:othercustomoptions", set_values.iterator().next());
        assertEquals("othercustom option 1 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"1\" />3 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"3\" />othercustom option 2 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"2\" />", StringUtils.splitToArray(template.getContent(), "\n")[37]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, CheckboxInListEnum.class, "othercustomoptions", null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:othercustomoptions", set_values.iterator().next());
        assertEquals("othercustom option 1 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"1\" />3 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"3\" />othercustom option 2 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"2\" />", StringUtils.splitToArray(template.getContent(), "\n")[37]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("othercustomoptions").inList(null, "1", null, "3", "2").defaultValue("2"), new String[]{null, "1"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:othercustomoptions", set_values.iterator().next());
        assertEquals("othercustom option 1 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"1\" checked=\"checked\" />3 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"3\" />othercustom option 2 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"2\" />", StringUtils.splitToArray(template.getContent(), "\n")[37]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, CheckboxInListEnum.class, new ConstrainedProperty("othercustomoptions").defaultValue("2"), new String[]{null, "1"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:othercustomoptions", set_values.iterator().next());
        assertEquals("othercustom option 1 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"1\" checked=\"checked\" />3 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"3\" />othercustom option 2 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"2\" />", StringUtils.splitToArray(template.getContent(), "\n")[37]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("othercustomoptions").inList(null, "1", null, "3", "2"), new String[]{"1", "2"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:othercustomoptions", set_values.iterator().next());
        assertEquals("othercustom option 1 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"1\" checked=\"checked\" />3 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"3\" />othercustom option 2 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"2\" checked=\"checked\" />", StringUtils.splitToArray(template.getContent(), "\n")[37]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, CheckboxInListEnum.class, "othercustomoptions", new String[]{"1", "2"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:othercustomoptions", set_values.iterator().next());
        assertEquals("othercustom option 1 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"1\" checked=\"checked\" />3 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"3\" />othercustom option 2 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"2\" checked=\"checked\" />", StringUtils.splitToArray(template.getContent(), "\n")[37]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("othercustomoptions").inList(null, "1", null, "3", "2").defaultValue("3"), new String[]{"1", "2"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:othercustomoptions", set_values.iterator().next());
        assertEquals("othercustom option 1 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"1\" checked=\"checked\" />3 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"3\" />othercustom option 2 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"2\" checked=\"checked\" />", StringUtils.splitToArray(template.getContent(), "\n")[37]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, CheckboxInListEnum.class, new ConstrainedProperty("othercustomoptions").defaultValue("3"), new String[]{"1", "2"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:othercustomoptions", set_values.iterator().next());
        assertEquals("othercustom option 1 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"1\" checked=\"checked\" />3 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"3\" />othercustom option 2 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"2\" checked=\"checked\" />", StringUtils.splitToArray(template.getContent(), "\n")[37]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("othercustomoptions").inList(null, "1", null, "3", "2"), new String[]{"a4"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:othercustomoptions", set_values.iterator().next());
        assertEquals("othercustom option 1 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"1\" />3 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"3\" />othercustom option 2 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"2\" />", StringUtils.splitToArray(template.getContent(), "\n")[37]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, CheckboxInListEnum.class, "othercustomoptions", new String[]{"a4"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:othercustomoptions", set_values.iterator().next());
        assertEquals("othercustom option 1 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"1\" />3 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"3\" />othercustom option 2 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"2\" />", StringUtils.splitToArray(template.getContent(), "\n")[37]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        template.setValue("form:checkbox:othercustomoptions", "already set");
        set_values = builder.generateField(template, new ConstrainedProperty("othercustomoptions"), null, null);
        assertEquals(0, set_values.size());
        assertEquals("already set", StringUtils.splitToArray(template.getContent(), "\n")[37]);
        template.removeValue("form:checkbox:othercustomoptions");
    }

    @Test
    public void testGenerateFieldCheckboxTemplatNameCustomWithoutDefault() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;

        Template template = TemplateFactory.HTML.get("formbuilder_fields_templatename");

        set_values = builder.replaceField(template, "templatenameothercustomoptions", "othercustomoptions", null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:templatenameothercustomoptions", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"othercustomoptions\" />", StringUtils.splitToArray(template.getContent(), "\n")[37]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenameothercustomoptions", new ConstrainedProperty("othercustomoptions"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:templatenameothercustomoptions", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"othercustomoptions\" />", StringUtils.splitToArray(template.getContent(), "\n")[37]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenameothercustomoptions", new ConstrainedProperty("othercustomoptions").inList(null, "1", null, "3", "2"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:templatenameothercustomoptions", set_values.iterator().next());
        assertEquals("othercustom option 1 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"1\" />3 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"3\" />othercustom option 2 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"2\" />", StringUtils.splitToArray(template.getContent(), "\n")[37]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenameothercustomoptions", CheckboxInListEnum.class, "othercustomoptions", null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:templatenameothercustomoptions", set_values.iterator().next());
        assertEquals("othercustom option 1 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"1\" />3 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"3\" />othercustom option 2 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"2\" />", StringUtils.splitToArray(template.getContent(), "\n")[37]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenameothercustomoptions", new ConstrainedProperty("othercustomoptions").inList(null, "1", null, "3", "2").defaultValue("2"), new String[]{null, "1"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:templatenameothercustomoptions", set_values.iterator().next());
        assertEquals("othercustom option 1 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"1\" checked=\"checked\" />3 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"3\" />othercustom option 2 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"2\" />", StringUtils.splitToArray(template.getContent(), "\n")[37]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenameothercustomoptions", CheckboxInListEnum.class, new ConstrainedProperty("othercustomoptions").defaultValue("2"), new String[]{null, "1"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:templatenameothercustomoptions", set_values.iterator().next());
        assertEquals("othercustom option 1 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"1\" checked=\"checked\" />3 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"3\" />othercustom option 2 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"2\" />", StringUtils.splitToArray(template.getContent(), "\n")[37]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenameothercustomoptions", new ConstrainedProperty("othercustomoptions").inList(null, "1", null, "3", "2"), new String[]{"1", "2"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:templatenameothercustomoptions", set_values.iterator().next());
        assertEquals("othercustom option 1 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"1\" checked=\"checked\" />3 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"3\" />othercustom option 2 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"2\" checked=\"checked\" />", StringUtils.splitToArray(template.getContent(), "\n")[37]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenameothercustomoptions", CheckboxInListEnum.class, "othercustomoptions", new String[]{"1", "2"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:templatenameothercustomoptions", set_values.iterator().next());
        assertEquals("othercustom option 1 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"1\" checked=\"checked\" />3 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"3\" />othercustom option 2 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"2\" checked=\"checked\" />", StringUtils.splitToArray(template.getContent(), "\n")[37]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenameothercustomoptions", new ConstrainedProperty("othercustomoptions").inList(null, "1", null, "3", "2").defaultValue("3"), new String[]{"1", "2"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:templatenameothercustomoptions", set_values.iterator().next());
        assertEquals("othercustom option 1 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"1\" checked=\"checked\" />3 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"3\" />othercustom option 2 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"2\" checked=\"checked\" />", StringUtils.splitToArray(template.getContent(), "\n")[37]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenameothercustomoptions", CheckboxInListEnum.class, new ConstrainedProperty("othercustomoptions").defaultValue("3"), new String[]{"1", "2"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:templatenameothercustomoptions", set_values.iterator().next());
        assertEquals("othercustom option 1 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"1\" checked=\"checked\" />3 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"3\" />othercustom option 2 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"2\" checked=\"checked\" />", StringUtils.splitToArray(template.getContent(), "\n")[37]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenameothercustomoptions", new ConstrainedProperty("othercustomoptions").inList(null, "1", null, "3", "2"), new String[]{"a4"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:templatenameothercustomoptions", set_values.iterator().next());
        assertEquals("othercustom option 1 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"1\" />3 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"3\" />othercustom option 2 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"2\" />", StringUtils.splitToArray(template.getContent(), "\n")[37]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenameothercustomoptions", CheckboxInListEnum.class, "othercustomoptions", new String[]{"a4"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:templatenameothercustomoptions", set_values.iterator().next());
        assertEquals("othercustom option 1 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"1\" />3 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"3\" />othercustom option 2 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"2\" />", StringUtils.splitToArray(template.getContent(), "\n")[37]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        template.setValue("form:checkbox:templatenameothercustomoptions", "already set");
        set_values = builder.replaceField(template, "templatenameothercustomoptions", new ConstrainedProperty("othercustomoptions"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:templatenameothercustomoptions", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"othercustomoptions\" />", StringUtils.splitToArray(template.getContent(), "\n")[37]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();
    }

    @Test
    public void testGenerateFieldCheckboxCustomPrefix() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        set_values = builder.generateField(template, new ConstrainedProperty("customoptions").inList(null, "1", null, "3", "2"), null, "other");
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:othercustomoptions", set_values.iterator().next());
        assertEquals("othercustom option 1 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"1\" />3 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"3\" />othercustom option 2 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"2\" />", StringUtils.splitToArray(template.getContent(), "\n")[37]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, CheckboxInListEnum.class, "customoptions", null, "other");
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:othercustomoptions", set_values.iterator().next());
        assertEquals("othercustom option 1 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"1\" />3 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"3\" />othercustom option 2 : <input type=\"checkbox\" name=\"othercustomoptions\" value=\"2\" />", StringUtils.splitToArray(template.getContent(), "\n")[37]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        template.setValue("form:checkbox:othercustomoptions", "already set");
        set_values = builder.generateField(template, new ConstrainedProperty("customoptions"), null, "other");
        assertEquals(0, set_values.size());
        assertEquals("already set", StringUtils.splitToArray(template.getContent(), "\n")[37]);
        template.removeValue("form:checkbox:othercustomoptions");
    }

    @Test
    public void testGenerateFieldCheckboxCustomDynamic() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        template.addResourceBundle(new ListResourceBundle() {
            public Object[][] getContents() {
                return new Object[][]{
                    {"customoptions:1", "dynamic first"},
                    {"customoptions:2", "dynamic second"},
                    {"customoptions:3", "dynamic third"}
                };
            }
        });

        set_values = builder.generateField(template, new ConstrainedProperty("customoptions").inList("1", "3", "2").defaultValue("2"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:customoptions", set_values.iterator().next());
        assertEquals("dynamic first : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"1\" />dynamic third : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"3\" />dynamic second : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"2\" checked=\"checked\" />", StringUtils.splitToArray(template.getContent(), "\n")[32]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, CheckboxInListEnum.class, new ConstrainedProperty("customoptions").defaultValue("2"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:customoptions", set_values.iterator().next());
        assertEquals("dynamic first : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"1\" />dynamic third : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"3\" />dynamic second : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"2\" checked=\"checked\" />", StringUtils.splitToArray(template.getContent(), "\n")[32]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("customoptions").inList("1", "3", "2").defaultValue("2"), new String[]{"1"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:customoptions", set_values.iterator().next());
        assertEquals("dynamic first : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"1\" checked=\"checked\" />dynamic third : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"3\" />dynamic second : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"2\" />", StringUtils.splitToArray(template.getContent(), "\n")[32]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, CheckboxInListEnum.class, new ConstrainedProperty("customoptions").defaultValue("2"), new String[]{"1"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:customoptions", set_values.iterator().next());
        assertEquals("dynamic first : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"1\" checked=\"checked\" />dynamic third : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"3\" />dynamic second : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"2\" />", StringUtils.splitToArray(template.getContent(), "\n")[32]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        template.clear();

        template.addResourceBundle(new ListResourceBundle() {
            public Object[][] getContents() {
                return new Object[][]{
                    {"customoptions:2", "dynamic second"},
                };
            }
        });

        set_values = builder.generateField(template, new ConstrainedProperty("customoptions").inList("1", "3", "2").defaultValue("2"), new String[]{"1"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:customoptions", set_values.iterator().next());
        assertEquals("custom option 1 : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"1\" checked=\"checked\" />3 : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"3\" />dynamic second : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"2\" />", StringUtils.splitToArray(template.getContent(), "\n")[32]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, CheckboxInListEnum.class, new ConstrainedProperty("customoptions").defaultValue("2"), new String[]{"1"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:customoptions", set_values.iterator().next());
        assertEquals("custom option 1 : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"1\" checked=\"checked\" />3 : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"3\" />dynamic second : <input type=\"checkbox\" name=\"customoptions\" alt=\"customblurp\" value=\"2\" />", StringUtils.splitToArray(template.getContent(), "\n")[32]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();
    }

    @Test
    public void testGenerateFieldCheckboxEmptyCustomWithDefault() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;

        Template template = TemplateFactory.HTML.get("formbuilder_fields_emptycustom");

        set_values = builder.generateField(template, "emptycustomoptions", null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:emptycustomoptions", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"emptycustomoptions\" alt=\"emptycustomblurp\" />", StringUtils.splitToArray(template.getContent(), "\n")[4]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("emptycustomoptions"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:emptycustomoptions", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"emptycustomoptions\" alt=\"emptycustomblurp\" />", StringUtils.splitToArray(template.getContent(), "\n")[4]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("emptycustomoptions").inList(null, "1", null, "3", "2"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:emptycustomoptions", set_values.iterator().next());
        assertEquals(" -  -  - ", StringUtils.splitToArray(template.getContent(), "\n")[4]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, CheckboxInListEnum.class, "emptycustomoptions", null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:emptycustomoptions", set_values.iterator().next());
        assertEquals(" -  -  - ", StringUtils.splitToArray(template.getContent(), "\n")[4]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        template.setValue("form:checkbox:emptycustomoptions", "already set");
        set_values = builder.generateField(template, new ConstrainedProperty("emptycustomoptions"), null, null);
        assertEquals(0, set_values.size());
        assertEquals("already set", StringUtils.splitToArray(template.getContent(), "\n")[4]);
        template.removeValue("form:checkbox:emptycustomoptions");
    }

    @Test
    public void testGenerateFieldCheckboxEmptyCustomWithoutDefault() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;

        Template template = TemplateFactory.HTML.get("formbuilder_fields_emptycustom");

        set_values = builder.generateField(template, "otheremptycustomoptions", null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:otheremptycustomoptions", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"otheremptycustomoptions\" />", StringUtils.splitToArray(template.getContent(), "\n")[6]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("otheremptycustomoptions"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:otheremptycustomoptions", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"otheremptycustomoptions\" />", StringUtils.splitToArray(template.getContent(), "\n")[6]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("otheremptycustomoptions").inList(null, "1", null, "3", "2"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:otheremptycustomoptions", set_values.iterator().next());
        assertEquals(" -  -  - ", StringUtils.splitToArray(template.getContent(), "\n")[6]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, CheckboxInListEnum.class, "otheremptycustomoptions", null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:otheremptycustomoptions", set_values.iterator().next());
        assertEquals(" -  -  - ", StringUtils.splitToArray(template.getContent(), "\n")[6]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        template.setValue("form:checkbox:otheremptycustomoptions", "already set");
        set_values = builder.generateField(template, new ConstrainedProperty("otheremptycustomoptions"), null, null);
        assertEquals(0, set_values.size());
        assertEquals("already set", StringUtils.splitToArray(template.getContent(), "\n")[6]);
        template.removeValue("form:checkbox:otheremptycustomoptions");
    }

    @Test
    public void testGenerateFieldCheckboxEmptyCustomPrefix() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;

        Template template = TemplateFactory.HTML.get("formbuilder_fields_emptycustom");

        set_values = builder.generateField(template, "emptycustomoptions", null, "other");
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:otheremptycustomoptions", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"otheremptycustomoptions\" />", StringUtils.splitToArray(template.getContent(), "\n")[6]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("emptycustomoptions").inList(null, "1", null, "3", "2"), null, "other");
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:otheremptycustomoptions", set_values.iterator().next());
        assertEquals(" -  -  - ", StringUtils.splitToArray(template.getContent(), "\n")[6]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, CheckboxInListEnum.class, "emptycustomoptions", null, "other");
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:otheremptycustomoptions", set_values.iterator().next());
        assertEquals(" -  -  - ", StringUtils.splitToArray(template.getContent(), "\n")[6]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        template.setValue("form:checkbox:otheremptycustomoptions", "already set");
        set_values = builder.generateField(template, new ConstrainedProperty("emptycustomoptions"), null, "other");
        assertEquals(0, set_values.size());
        assertEquals("already set", StringUtils.splitToArray(template.getContent(), "\n")[6]);
        template.removeValue("form:checkbox:otheremptycustomoptions");
    }

    @Test
    public void testGenerateFieldCheckboxBooleanWithDefault() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        set_values = builder.generateField(template, "invoice", null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:invoice", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"invoice\" alt=\"atext\" />I want an invoice", StringUtils.splitToArray(template.getContent(), "\n")[42]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("invoice"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:invoice", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"invoice\" alt=\"atext\" />I want an invoice", StringUtils.splitToArray(template.getContent(), "\n")[42]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("invoice").defaultValue(true), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:invoice", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"invoice\" alt=\"atext\" checked=\"checked\" />I want an invoice", StringUtils.splitToArray(template.getContent(), "\n")[42]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("invoice").defaultValue("false"), new String[]{"true"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:invoice", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"invoice\" alt=\"atext\" checked=\"checked\" />I want an invoice", StringUtils.splitToArray(template.getContent(), "\n")[42]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("invoice").defaultValue("flam"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:invoice", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"invoice\" alt=\"atext\" />I want an invoice", StringUtils.splitToArray(template.getContent(), "\n")[42]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("invoice").defaultValue("false"), new String[]{"flum"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:invoice", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"invoice\" alt=\"atext\" />I want an invoice", StringUtils.splitToArray(template.getContent(), "\n")[42]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        template.setValue("form:checkbox:invoice", "set : ");
        set_values = builder.generateField(template, new ConstrainedProperty("invoice"), null, null);
        assertEquals(0, set_values.size());
        assertEquals("set : I want an invoice", StringUtils.splitToArray(template.getContent(), "\n")[42]);
        template.removeValue("form:checkbox:invoice");
    }

    @Test
    public void testGenerateFieldCheckboxBooleanWithoutDefault() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        set_values = builder.generateField(template, "onemoreinvoice", null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:onemoreinvoice", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"onemoreinvoice\" />I want one more invoice", StringUtils.splitToArray(template.getContent(), "\n")[43]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("onemoreinvoice"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:onemoreinvoice", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"onemoreinvoice\" />I want one more invoice", StringUtils.splitToArray(template.getContent(), "\n")[43]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("onemoreinvoice").defaultValue("false"), new String[]{"true"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:onemoreinvoice", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"onemoreinvoice\" checked=\"checked\" />I want one more invoice", StringUtils.splitToArray(template.getContent(), "\n")[43]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        template.setValue("form:checkbox:onemoreinvoice", "set : ");
        set_values = builder.generateField(template, new ConstrainedProperty("onemoreinvoice"), null, null);
        assertEquals(0, set_values.size());
        assertEquals("set : I want one more invoice", StringUtils.splitToArray(template.getContent(), "\n")[43]);
        template.removeValue("form:checkbox:onemoreinvoice");
    }

    @Test
    public void testGenerateFieldCheckboxTemplateNameBooleanWithDefault() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;

        Template template = TemplateFactory.HTML.get("formbuilder_fields_templatename");

        set_values = builder.replaceField(template, "templatenameinvoice", "invoice", null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:templatenameinvoice", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"invoice\" alt=\"atext\" />I want an invoice", StringUtils.splitToArray(template.getContent(), "\n")[42]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenameinvoice", new ConstrainedProperty("invoice"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:templatenameinvoice", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"invoice\" alt=\"atext\" />I want an invoice", StringUtils.splitToArray(template.getContent(), "\n")[42]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenameinvoice", new ConstrainedProperty("invoice").defaultValue(true), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:templatenameinvoice", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"invoice\" alt=\"atext\" checked=\"checked\" />I want an invoice", StringUtils.splitToArray(template.getContent(), "\n")[42]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenameinvoice", new ConstrainedProperty("invoice").defaultValue("false"), new String[]{"true"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:templatenameinvoice", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"invoice\" alt=\"atext\" checked=\"checked\" />I want an invoice", StringUtils.splitToArray(template.getContent(), "\n")[42]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenameinvoice", new ConstrainedProperty("invoice").defaultValue("flam"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:templatenameinvoice", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"invoice\" alt=\"atext\" />I want an invoice", StringUtils.splitToArray(template.getContent(), "\n")[42]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenameinvoice", new ConstrainedProperty("invoice").defaultValue("false"), new String[]{"flum"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:templatenameinvoice", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"invoice\" alt=\"atext\" />I want an invoice", StringUtils.splitToArray(template.getContent(), "\n")[42]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        template.setValue("form:checkbox:templatenameinvoice", "set : ");
        set_values = builder.replaceField(template, "templatenameinvoice", new ConstrainedProperty("invoice"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:templatenameinvoice", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"invoice\" alt=\"atext\" />I want an invoice", StringUtils.splitToArray(template.getContent(), "\n")[42]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();
    }

    @Test
    public void testGenerateFieldCheckboxBooleanPrefix() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        set_values = builder.generateField(template, "invoice", null, "onemore");
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:onemoreinvoice", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"onemoreinvoice\" />I want one more invoice", StringUtils.splitToArray(template.getContent(), "\n")[43]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("invoice"), null, "onemore");
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:onemoreinvoice", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"onemoreinvoice\" />I want one more invoice", StringUtils.splitToArray(template.getContent(), "\n")[43]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("invoice").defaultValue("false"), new String[]{"true"}, "onemore");
        assertEquals(1, set_values.size());
        assertEquals("form:checkbox:onemoreinvoice", set_values.iterator().next());
        assertEquals("<input type=\"checkbox\" name=\"onemoreinvoice\" checked=\"checked\" />I want one more invoice", StringUtils.splitToArray(template.getContent(), "\n")[43]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        template.setValue("form:checkbox:onemoreinvoice", "set : ");
        set_values = builder.generateField(template, new ConstrainedProperty("invoice"), null, "onemore");
        assertEquals(0, set_values.size());
        assertEquals("set : I want one more invoice", StringUtils.splitToArray(template.getContent(), "\n")[43]);
        template.removeValue("form:checkbox:onemoreinvoice");
    }

    @Test
    public void testRemoveFieldCheckbox() {
        FormBuilderHtml builder = new FormBuilderHtml();

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        String raw_content = template.getContent();

        builder.generateField(template, new ConstrainedProperty("options").inList(null, "1", null, "3", "2"), new String[]{"a4"}, null);
        assertNotEquals(raw_content, template.getContent());
        builder.removeField(template, "options", null);
        assertEquals(raw_content, template.getContent());

        builder.generateField(template, CheckboxInListEnum.class, "options", new String[]{"a4"}, null);
        assertNotEquals(raw_content, template.getContent());
        builder.removeField(template, "options", null);
        assertEquals(raw_content, template.getContent());
    }

    @Test
    public void testRemoveFieldCheckboxTemplateName() {
        FormBuilderHtml builder = new FormBuilderHtml();

        Template template = TemplateFactory.HTML.get("formbuilder_fields_templatename");

        String raw_content = template.getContent();

        builder.replaceField(template, "templatenameoptions", new ConstrainedProperty("options").inList(null, "1", null, "3", "2"), new String[]{"a4"}, null);
        assertNotEquals(raw_content, template.getContent());
        builder.removeField(template, "templatenameoptions");
        assertEquals(raw_content, template.getContent());

        builder.replaceField(template, "templatenameoptions", CheckboxInListEnum.class, "options", new String[]{"a4"}, null);
        assertNotEquals(raw_content, template.getContent());
        builder.removeField(template, "templatenameoptions");
        assertEquals(raw_content, template.getContent());
    }

    @Test
    public void testRemoveFieldCheckboxPrefix() {
        FormBuilderHtml builder = new FormBuilderHtml();

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        String raw_content = template.getContent();

        builder.generateField(template, new ConstrainedProperty("options").inList(null, "1", null, "3", "2"), new String[]{"a4"}, "other");
        assertNotEquals(raw_content, template.getContent());
        builder.removeField(template, "options", "other");
        assertEquals(raw_content, template.getContent());

        builder.generateField(template, CheckboxInListEnum.class, "options", new String[]{"a4"}, "other");
        assertNotEquals(raw_content, template.getContent());
        builder.removeField(template, "options", "other");
        assertEquals(raw_content, template.getContent());
    }

    @Test
    public void testGenerateFieldSelectWithDefault() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;
        Iterator<String> set_values_it;

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        set_values = builder.generateField(template, "colors", null, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:colors", set_values_it.next());
        assertEquals("form:display:colors", set_values_it.next());
        assertEquals("<select name=\"colors\"></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div></div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("colors"), null, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:colors", set_values_it.next());
        assertEquals("form:display:colors", set_values_it.next());
        assertEquals("<select name=\"colors\"></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div></div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, RadioInListEnum.class, new ConstrainedProperty("colors").inList("black", "red", null, "green", "blue"), null, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:colors", set_values_it.next());
        assertEquals("form:display:colors", set_values_it.next());
        assertEquals("<select name=\"colors\" size=\"3\" multiple=\"multiple\"><option value=\"black\">black</option><option value=\"red\">red spots</option><option value=\"green\">green spots</option><option value=\"blue\">blue spots</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div></div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("colors").inList("black", "red", null, "green", "blue"), null, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:colors", set_values_it.next());
        assertEquals("form:display:colors", set_values_it.next());
        assertEquals("<select name=\"colors\" size=\"3\" multiple=\"multiple\"><option value=\"black\">black</option><option value=\"red\">red spots</option><option value=\"green\">green spots</option><option value=\"blue\">blue spots</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div></div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, SelectInListEnum.class, "colors", null, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:colors", set_values_it.next());
        assertEquals("form:display:colors", set_values_it.next());
        assertEquals("<select name=\"colors\" size=\"3\" multiple=\"multiple\"><option value=\"black\">black</option><option value=\"red\">red spots</option><option value=\"green\">green spots</option><option value=\"blue\">blue spots</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div></div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("colors").inList("black", "red", null, "green", "blue").defaultValue("green"), null, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:colors", set_values_it.next());
        assertEquals("form:display:colors", set_values_it.next());
        assertEquals("<select name=\"colors\" size=\"3\" multiple=\"multiple\"><option value=\"black\">black</option><option value=\"red\">red spots</option><option value=\"green\" selected=\"selected\">green spots</option><option value=\"blue\">blue spots</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div>green spots</div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, SelectInListEnum.class, new ConstrainedProperty("colors").defaultValue("green"), null, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:colors", set_values_it.next());
        assertEquals("form:display:colors", set_values_it.next());
        assertEquals("<select name=\"colors\" size=\"3\" multiple=\"multiple\"><option value=\"black\">black</option><option value=\"red\">red spots</option><option value=\"green\" selected=\"selected\">green spots</option><option value=\"blue\">blue spots</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div>green spots</div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("colors").inList("black", "red", null, "green", "blue").defaultValue("green"), new String[]{null, "red"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:colors", set_values_it.next());
        assertEquals("form:display:colors", set_values_it.next());
        assertEquals("<select name=\"colors\" size=\"3\" multiple=\"multiple\"><option value=\"black\">black</option><option value=\"red\" selected=\"selected\">red spots</option><option value=\"green\">green spots</option><option value=\"blue\">blue spots</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div>green spots</div><div>red spots</div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, SelectInListEnum.class, new ConstrainedProperty("colors").defaultValue("green"), new String[]{null, "red"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:colors", set_values_it.next());
        assertEquals("form:display:colors", set_values_it.next());
        assertEquals("<select name=\"colors\" size=\"3\" multiple=\"multiple\"><option value=\"black\">black</option><option value=\"red\" selected=\"selected\">red spots</option><option value=\"green\">green spots</option><option value=\"blue\">blue spots</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div>green spots</div><div>red spots</div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("colors").inList("black", "red", null, "green", "blue"), new String[]{"red", "blue"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:colors", set_values_it.next());
        assertEquals("form:display:colors", set_values_it.next());
        assertEquals("<select name=\"colors\" size=\"3\" multiple=\"multiple\"><option value=\"black\">black</option><option value=\"red\" selected=\"selected\">red spots</option><option value=\"green\">green spots</option><option value=\"blue\" selected=\"selected\">blue spots</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div>red spots</div><div>blue spots</div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, SelectInListEnum.class, "colors", new String[]{"red", "blue"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:colors", set_values_it.next());
        assertEquals("form:display:colors", set_values_it.next());
        assertEquals("<select name=\"colors\" size=\"3\" multiple=\"multiple\"><option value=\"black\">black</option><option value=\"red\" selected=\"selected\">red spots</option><option value=\"green\">green spots</option><option value=\"blue\" selected=\"selected\">blue spots</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div>red spots</div><div>blue spots</div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("colors").inList("black", "red", null, "green", "blue").defaultValue("green"), new String[]{"black", "red"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:colors", set_values_it.next());
        assertEquals("form:display:colors", set_values_it.next());
        assertEquals("<select name=\"colors\" size=\"3\" multiple=\"multiple\"><option value=\"black\" selected=\"selected\">black</option><option value=\"red\" selected=\"selected\">red spots</option><option value=\"green\">green spots</option><option value=\"blue\">blue spots</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div>black</div><div>red spots</div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, SelectInListEnum.class, new ConstrainedProperty("colors").defaultValue("green"), new String[]{"black", "red"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:colors", set_values_it.next());
        assertEquals("form:display:colors", set_values_it.next());
        assertEquals("<select name=\"colors\" size=\"3\" multiple=\"multiple\"><option value=\"black\" selected=\"selected\">black</option><option value=\"red\" selected=\"selected\">red spots</option><option value=\"green\">green spots</option><option value=\"blue\">blue spots</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div>black</div><div>red spots</div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("colors").inList("black", "red", null, "green", "blue"), new String[]{"orange"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:colors", set_values_it.next());
        assertEquals("form:display:colors", set_values_it.next());
        assertEquals("<select name=\"colors\" size=\"3\" multiple=\"multiple\"><option value=\"black\">black</option><option value=\"red\">red spots</option><option value=\"green\">green spots</option><option value=\"blue\">blue spots</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div>orange</div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, SelectInListEnum.class, "colors", new String[]{"orange"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:colors", set_values_it.next());
        assertEquals("form:display:colors", set_values_it.next());
        assertEquals("<select name=\"colors\" size=\"3\" multiple=\"multiple\"><option value=\"black\">black</option><option value=\"red\">red spots</option><option value=\"green\">green spots</option><option value=\"blue\">blue spots</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div>orange</div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        template.setValue("form:select:colors", "already set");
        template.setValue("form:display:colors", "already set too");
        set_values = builder.generateField(template, new ConstrainedProperty("colors"), null, null);
        assertEquals(0, set_values.size());
        assertEquals("already set", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("already set too", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        template.removeValue("form:select:colors");
        template.removeValue("form:display:colors");
    }

    @Test
    public void testGenerateFieldSelectWithoutDefault() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        set_values = builder.generateField(template, "morecolors", null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:select:morecolors", set_values.iterator().next());
        assertEquals("<select name=\"morecolors\"></select>", StringUtils.splitToArray(template.getContent(), "\n")[48]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("morecolors"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:select:morecolors", set_values.iterator().next());
        assertEquals("<select name=\"morecolors\"></select>", StringUtils.splitToArray(template.getContent(), "\n")[48]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("morecolors").inList("black", "red", null, "green", "blue"), null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:select:morecolors", set_values.iterator().next());
        assertEquals("<select name=\"morecolors\"><option value=\"black\">black</option><option value=\"red\">more red spots</option><option value=\"green\">more green spots</option><option value=\"blue\">more blue spots</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[48]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, SelectInListEnum.class, "morecolors", null, null);
        assertEquals(1, set_values.size());
        assertEquals("form:select:morecolors", set_values.iterator().next());
        assertEquals("<select name=\"morecolors\"><option value=\"black\">black</option><option value=\"red\">more red spots</option><option value=\"green\">more green spots</option><option value=\"blue\">more blue spots</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[48]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("morecolors").inList("black", "red", null, "green", "blue").defaultValue("green"), new String[]{"black", "red"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:select:morecolors", set_values.iterator().next());
        assertEquals("<select name=\"morecolors\"><option value=\"black\" selected=\"selected\">black</option><option value=\"red\" selected=\"selected\">more red spots</option><option value=\"green\">more green spots</option><option value=\"blue\">more blue spots</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[48]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, SelectInListEnum.class, new ConstrainedProperty("morecolors").defaultValue("green"), new String[]{"black", "red"}, null);
        assertEquals(1, set_values.size());
        assertEquals("form:select:morecolors", set_values.iterator().next());
        assertEquals("<select name=\"morecolors\"><option value=\"black\" selected=\"selected\">black</option><option value=\"red\" selected=\"selected\">more red spots</option><option value=\"green\">more green spots</option><option value=\"blue\">more blue spots</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[48]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        template.setValue("form:select:morecolors", "already set");
        set_values = builder.generateField(template, new ConstrainedProperty("morecolors"), null, null);
        assertEquals(0, set_values.size());
        assertEquals("already set", StringUtils.splitToArray(template.getContent(), "\n")[48]);
        template.removeValue("form:select:morecolors");
    }

    @Test
    public void testGenerateFieldSelectTemplateNameWithDefault() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;
        Iterator<String> set_values_it;

        Template template = TemplateFactory.HTML.get("formbuilder_fields_templatename");

        set_values = builder.replaceField(template, "templatenamecolors", "colors", null, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:templatenamecolors", set_values_it.next());
        assertEquals("form:display:templatenamecolors", set_values_it.next());
        assertEquals("<select name=\"colors\"></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div></div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenamecolors", new ConstrainedProperty("colors"), null, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:templatenamecolors", set_values_it.next());
        assertEquals("form:display:templatenamecolors", set_values_it.next());
        assertEquals("<select name=\"colors\"></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div></div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenamecolors", new ConstrainedProperty("colors").inList("black", "red", null, "green", "blue"), null, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:templatenamecolors", set_values_it.next());
        assertEquals("form:display:templatenamecolors", set_values_it.next());
        assertEquals("<select name=\"colors\" size=\"3\" multiple=\"multiple\"><option value=\"black\">black</option><option value=\"red\">red spots</option><option value=\"green\">green spots</option><option value=\"blue\">blue spots</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div></div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenamecolors", SelectInListEnum.class, "colors", null, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:templatenamecolors", set_values_it.next());
        assertEquals("form:display:templatenamecolors", set_values_it.next());
        assertEquals("<select name=\"colors\" size=\"3\" multiple=\"multiple\"><option value=\"black\">black</option><option value=\"red\">red spots</option><option value=\"green\">green spots</option><option value=\"blue\">blue spots</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div></div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenamecolors", new ConstrainedProperty("colors").inList("black", "red", null, "green", "blue").defaultValue("green"), null, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:templatenamecolors", set_values_it.next());
        assertEquals("form:display:templatenamecolors", set_values_it.next());
        assertEquals("<select name=\"colors\" size=\"3\" multiple=\"multiple\"><option value=\"black\">black</option><option value=\"red\">red spots</option><option value=\"green\" selected=\"selected\">green spots</option><option value=\"blue\">blue spots</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div>green spots</div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenamecolors", SelectInListEnum.class, new ConstrainedProperty("colors").defaultValue("green"), null, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:templatenamecolors", set_values_it.next());
        assertEquals("form:display:templatenamecolors", set_values_it.next());
        assertEquals("<select name=\"colors\" size=\"3\" multiple=\"multiple\"><option value=\"black\">black</option><option value=\"red\">red spots</option><option value=\"green\" selected=\"selected\">green spots</option><option value=\"blue\">blue spots</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div>green spots</div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenamecolors", new ConstrainedProperty("colors").inList("black", "red", null, "green", "blue").defaultValue("green"), new String[]{null, "red"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:templatenamecolors", set_values_it.next());
        assertEquals("form:display:templatenamecolors", set_values_it.next());
        assertEquals("<select name=\"colors\" size=\"3\" multiple=\"multiple\"><option value=\"black\">black</option><option value=\"red\" selected=\"selected\">red spots</option><option value=\"green\">green spots</option><option value=\"blue\">blue spots</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div>green spots</div><div>red spots</div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenamecolors", SelectInListEnum.class, new ConstrainedProperty("colors").defaultValue("green"), new String[]{null, "red"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:templatenamecolors", set_values_it.next());
        assertEquals("form:display:templatenamecolors", set_values_it.next());
        assertEquals("<select name=\"colors\" size=\"3\" multiple=\"multiple\"><option value=\"black\">black</option><option value=\"red\" selected=\"selected\">red spots</option><option value=\"green\">green spots</option><option value=\"blue\">blue spots</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div>green spots</div><div>red spots</div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenamecolors", new ConstrainedProperty("colors").inList("black", "red", null, "green", "blue"), new String[]{"red", "blue"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:templatenamecolors", set_values_it.next());
        assertEquals("form:display:templatenamecolors", set_values_it.next());
        assertEquals("<select name=\"colors\" size=\"3\" multiple=\"multiple\"><option value=\"black\">black</option><option value=\"red\" selected=\"selected\">red spots</option><option value=\"green\">green spots</option><option value=\"blue\" selected=\"selected\">blue spots</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div>red spots</div><div>blue spots</div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenamecolors", SelectInListEnum.class, "colors", new String[]{"red", "blue"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:templatenamecolors", set_values_it.next());
        assertEquals("form:display:templatenamecolors", set_values_it.next());
        assertEquals("<select name=\"colors\" size=\"3\" multiple=\"multiple\"><option value=\"black\">black</option><option value=\"red\" selected=\"selected\">red spots</option><option value=\"green\">green spots</option><option value=\"blue\" selected=\"selected\">blue spots</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div>red spots</div><div>blue spots</div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenamecolors", new ConstrainedProperty("colors").inList("black", "red", null, "green", "blue").defaultValue("green"), new String[]{"black", "red"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:templatenamecolors", set_values_it.next());
        assertEquals("form:display:templatenamecolors", set_values_it.next());
        assertEquals("<select name=\"colors\" size=\"3\" multiple=\"multiple\"><option value=\"black\" selected=\"selected\">black</option><option value=\"red\" selected=\"selected\">red spots</option><option value=\"green\">green spots</option><option value=\"blue\">blue spots</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div>black</div><div>red spots</div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenamecolors", SelectInListEnum.class, new ConstrainedProperty("colors").defaultValue("green"), new String[]{"black", "red"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:templatenamecolors", set_values_it.next());
        assertEquals("form:display:templatenamecolors", set_values_it.next());
        assertEquals("<select name=\"colors\" size=\"3\" multiple=\"multiple\"><option value=\"black\" selected=\"selected\">black</option><option value=\"red\" selected=\"selected\">red spots</option><option value=\"green\">green spots</option><option value=\"blue\">blue spots</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div>black</div><div>red spots</div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenamecolors", new ConstrainedProperty("colors").inList("black", "red", null, "green", "blue"), new String[]{"orange"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:templatenamecolors", set_values_it.next());
        assertEquals("form:display:templatenamecolors", set_values_it.next());
        assertEquals("<select name=\"colors\" size=\"3\" multiple=\"multiple\"><option value=\"black\">black</option><option value=\"red\">red spots</option><option value=\"green\">green spots</option><option value=\"blue\">blue spots</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div>orange</div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.replaceField(template, "templatenamecolors", SelectInListEnum.class, "colors", new String[]{"orange"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:templatenamecolors", set_values_it.next());
        assertEquals("form:display:templatenamecolors", set_values_it.next());
        assertEquals("<select name=\"colors\" size=\"3\" multiple=\"multiple\"><option value=\"black\">black</option><option value=\"red\">red spots</option><option value=\"green\">green spots</option><option value=\"blue\">blue spots</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div>orange</div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        template.setValue("form:select:templatenamecolors", "already set");
        template.setValue("form:display:templatenamecolors", "already set too");
        set_values = builder.replaceField(template, "templatenamecolors", new ConstrainedProperty("colors"), null, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:templatenamecolors", set_values_it.next());
        assertEquals("form:display:templatenamecolors", set_values_it.next());
        assertEquals("<select name=\"colors\"></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div></div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();
    }

    @Test
    public void testGenerateFieldSelectWithOutOfListDefault() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;
        Iterator<String> set_values_it;

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        set_values = builder.generateField(template, new ConstrainedProperty("colors").inList("black", "red", null, "blue").defaultValue("green"), null, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:colors", set_values_it.next());
        assertEquals("form:display:colors", set_values_it.next());
        assertEquals("<select name=\"colors\" size=\"3\" multiple=\"multiple\"><option value=\"green\" selected=\"selected\">green spots</option><option value=\"black\">black</option><option value=\"red\">red spots</option><option value=\"blue\">blue spots</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div>green spots</div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, SelectInListEnum2.class, new ConstrainedProperty("colors").defaultValue("green"), null, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:colors", set_values_it.next());
        assertEquals("form:display:colors", set_values_it.next());
        assertEquals("<select name=\"colors\" size=\"3\" multiple=\"multiple\"><option value=\"green\" selected=\"selected\">green spots</option><option value=\"black\">black</option><option value=\"red\">red spots</option><option value=\"blue\">blue spots</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div>green spots</div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("colors").inList("black", "red", null, "blue").defaultValue("green"), new String[]{null, "red"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:colors", set_values_it.next());
        assertEquals("form:display:colors", set_values_it.next());
        assertEquals("<select name=\"colors\" size=\"3\" multiple=\"multiple\"><option value=\"green\">green spots</option><option value=\"black\">black</option><option value=\"red\" selected=\"selected\">red spots</option><option value=\"blue\">blue spots</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div>green spots</div><div>red spots</div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, SelectInListEnum2.class, new ConstrainedProperty("colors").defaultValue("green"), new String[]{null, "red"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:colors", set_values_it.next());
        assertEquals("form:display:colors", set_values_it.next());
        assertEquals("<select name=\"colors\" size=\"3\" multiple=\"multiple\"><option value=\"green\">green spots</option><option value=\"black\">black</option><option value=\"red\" selected=\"selected\">red spots</option><option value=\"blue\">blue spots</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div>green spots</div><div>red spots</div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("colors").inList("black", "red", null, "blue"), new String[]{"red", "blue"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:colors", set_values_it.next());
        assertEquals("form:display:colors", set_values_it.next());
        assertEquals("<select name=\"colors\" size=\"3\" multiple=\"multiple\"><option value=\"black\">black</option><option value=\"red\" selected=\"selected\">red spots</option><option value=\"blue\" selected=\"selected\">blue spots</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div>red spots</div><div>blue spots</div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, SelectInListEnum2.class, "colors", new String[]{"red", "blue"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:colors", set_values_it.next());
        assertEquals("form:display:colors", set_values_it.next());
        assertEquals("<select name=\"colors\" size=\"3\" multiple=\"multiple\"><option value=\"black\">black</option><option value=\"red\" selected=\"selected\">red spots</option><option value=\"blue\" selected=\"selected\">blue spots</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div>red spots</div><div>blue spots</div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("colors").inList("black", "red", null, "blue").defaultValue("green"), new String[]{"black", "red"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:colors", set_values_it.next());
        assertEquals("form:display:colors", set_values_it.next());
        assertEquals("<select name=\"colors\" size=\"3\" multiple=\"multiple\"><option value=\"green\">green spots</option><option value=\"black\" selected=\"selected\">black</option><option value=\"red\" selected=\"selected\">red spots</option><option value=\"blue\">blue spots</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div>black</div><div>red spots</div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, SelectInListEnum2.class, new ConstrainedProperty("colors").defaultValue("green"), new String[]{"black", "red"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:colors", set_values_it.next());
        assertEquals("form:display:colors", set_values_it.next());
        assertEquals("<select name=\"colors\" size=\"3\" multiple=\"multiple\"><option value=\"green\">green spots</option><option value=\"black\" selected=\"selected\">black</option><option value=\"red\" selected=\"selected\">red spots</option><option value=\"blue\">blue spots</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div>black</div><div>red spots</div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("colors").inList("black", "red", null, "blue"), new String[]{"orange"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:colors", set_values_it.next());
        assertEquals("form:display:colors", set_values_it.next());
        assertEquals("<select name=\"colors\" size=\"3\" multiple=\"multiple\"><option value=\"black\">black</option><option value=\"red\">red spots</option><option value=\"blue\">blue spots</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div>orange</div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, SelectInListEnum2.class, "colors", new String[]{"orange"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:colors", set_values_it.next());
        assertEquals("form:display:colors", set_values_it.next());
        assertEquals("<select name=\"colors\" size=\"3\" multiple=\"multiple\"><option value=\"black\">black</option><option value=\"red\">red spots</option><option value=\"blue\">blue spots</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div>orange</div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();
    }

    @Test
    public void testGenerateFieldSelectPrefix() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        set_values = builder.generateField(template, "colors", null, "more");
        assertEquals(1, set_values.size());
        assertEquals("form:select:morecolors", set_values.iterator().next());
        assertEquals("<select name=\"morecolors\"></select>", StringUtils.splitToArray(template.getContent(), "\n")[48]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("colors"), null, "more");
        assertEquals(1, set_values.size());
        assertEquals("form:select:morecolors", set_values.iterator().next());
        assertEquals("<select name=\"morecolors\"></select>", StringUtils.splitToArray(template.getContent(), "\n")[48]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("colors").inList("black", "red", null, "green", "blue"), null, "more");
        assertEquals(1, set_values.size());
        assertEquals("form:select:morecolors", set_values.iterator().next());
        assertEquals("<select name=\"morecolors\"><option value=\"black\">black</option><option value=\"red\">more red spots</option><option value=\"green\">more green spots</option><option value=\"blue\">more blue spots</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[48]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, SelectInListEnum.class, "colors", null, "more");
        assertEquals(1, set_values.size());
        assertEquals("form:select:morecolors", set_values.iterator().next());
        assertEquals("<select name=\"morecolors\"><option value=\"black\">black</option><option value=\"red\">more red spots</option><option value=\"green\">more green spots</option><option value=\"blue\">more blue spots</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[48]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("colors").inList("black", "red", null, "green", "blue").defaultValue("green"), new String[]{"black", "red"}, "more");
        assertEquals(1, set_values.size());
        assertEquals("form:select:morecolors", set_values.iterator().next());
        assertEquals("<select name=\"morecolors\"><option value=\"black\" selected=\"selected\">black</option><option value=\"red\" selected=\"selected\">more red spots</option><option value=\"green\">more green spots</option><option value=\"blue\">more blue spots</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[48]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        set_values = builder.generateField(template, SelectInListEnum.class, new ConstrainedProperty("colors").defaultValue("green"), new String[]{"black", "red"}, "more");
        assertEquals(1, set_values.size());
        assertEquals("form:select:morecolors", set_values.iterator().next());
        assertEquals("<select name=\"morecolors\"><option value=\"black\" selected=\"selected\">black</option><option value=\"red\" selected=\"selected\">more red spots</option><option value=\"green\">more green spots</option><option value=\"blue\">more blue spots</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[48]);
        template.removeValue(set_values.iterator().next());
        set_values.clear();

        template.setValue("form:select:morecolors", "already set");
        set_values = builder.generateField(template, new ConstrainedProperty("colors"), null, "more");
        assertEquals(0, set_values.size());
        assertEquals("already set", StringUtils.splitToArray(template.getContent(), "\n")[48]);
        template.removeValue("form:select:morecolors");
    }

    @Test
    public void testGenerateFieldSelectDynamic() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Collection<String> set_values;
        Iterator<String> set_values_it;

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        template.addResourceBundle(new ListResourceBundle() {
            public Object[][] getContents() {
                return new Object[][]{
                    {"colors:1", "one"},
                    {"colors:3", "three"},
                    {"colors:5", "five"},
                    {"colors:9", "nine"},
                };
            }
        });

        set_values = builder.generateField(template, new ConstrainedProperty("colors").inList("1", "3", "5", "9").defaultValue("5"), null, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:colors", set_values_it.next());
        assertEquals("form:display:colors", set_values_it.next());
        assertEquals("<select name=\"colors\" size=\"3\" multiple=\"multiple\"><option value=\"1\">one</option><option value=\"3\">three</option><option value=\"5\" selected=\"selected\">five</option><option value=\"9\">nine</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div>five</div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, SelectInListEnum3.class, new ConstrainedProperty("colors").defaultValue("5"), null, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:colors", set_values_it.next());
        assertEquals("form:display:colors", set_values_it.next());
        assertEquals("<select name=\"colors\" size=\"3\" multiple=\"multiple\"><option value=\"1\">one</option><option value=\"3\">three</option><option value=\"5\" selected=\"selected\">five</option><option value=\"9\">nine</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div>five</div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, new ConstrainedProperty("colors").inList("1", "3", "5", "9").defaultValue("5"), new String[]{"3", "9"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:colors", set_values_it.next());
        assertEquals("form:display:colors", set_values_it.next());
        assertEquals("<select name=\"colors\" size=\"3\" multiple=\"multiple\"><option value=\"1\">one</option><option value=\"3\" selected=\"selected\">three</option><option value=\"5\">five</option><option value=\"9\" selected=\"selected\">nine</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div>three</div><div>nine</div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, SelectInListEnum3.class, new ConstrainedProperty("colors").defaultValue("5"), new String[]{"3", "9"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:colors", set_values_it.next());
        assertEquals("form:display:colors", set_values_it.next());
        assertEquals("<select name=\"colors\" size=\"3\" multiple=\"multiple\"><option value=\"1\">one</option><option value=\"3\" selected=\"selected\">three</option><option value=\"5\">five</option><option value=\"9\" selected=\"selected\">nine</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div>three</div><div>nine</div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        template.clear();

        template.addResourceBundle(new ListResourceBundle() {
            public Object[][] getContents() {
                return new Object[][]{
                    {"colors:blue", "blue waves"},
                    {"colors:red", "red waves"}
                };
            }
        });

        set_values = builder.generateField(template, new ConstrainedProperty("colors").inList("black", "red", null, "green", "blue").defaultValue("green"), new String[]{null, "red"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:colors", set_values_it.next());
        assertEquals("form:display:colors", set_values_it.next());
        assertEquals("<select name=\"colors\" size=\"3\" multiple=\"multiple\"><option value=\"black\">black</option><option value=\"red\" selected=\"selected\">red waves</option><option value=\"green\">green spots</option><option value=\"blue\">blue waves</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div>green spots</div><div>red waves</div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();

        set_values = builder.generateField(template, SelectInListEnum.class, new ConstrainedProperty("colors").defaultValue("green"), new String[]{null, "red"}, null);
        set_values_it = set_values.iterator();
        assertEquals(2, set_values.size());
        assertEquals("form:select:colors", set_values_it.next());
        assertEquals("form:display:colors", set_values_it.next());
        assertEquals("<select name=\"colors\" size=\"3\" multiple=\"multiple\"><option value=\"black\">black</option><option value=\"red\" selected=\"selected\">red waves</option><option value=\"green\">green spots</option><option value=\"blue\">blue waves</option></select>", StringUtils.splitToArray(template.getContent(), "\n")[44]);
        assertEquals("<div>green spots</div><div>red waves</div>", StringUtils.splitToArray(template.getContent(), "\n")[58]);
        set_values_it = set_values.iterator();
        template.removeValue(set_values_it.next());
        template.removeValue(set_values_it.next());
        set_values.clear();
    }

    @Test
    public void testRemoveFieldSelect() {
        FormBuilderHtml builder = new FormBuilderHtml();

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        String raw_content = template.getContent();

        builder.generateField(template, new ConstrainedProperty("colors").inList("black", "red", null, "green", "blue"), new String[]{"orange"}, null);
        assertNotEquals(raw_content, template.getContent());
        builder.removeField(template, "colors", null);
        assertEquals(raw_content, template.getContent());

        builder.generateField(template, SelectInListEnum.class, "colors", new String[]{"orange"}, null);
        assertNotEquals(raw_content, template.getContent());
        builder.removeField(template, "colors", null);
        assertEquals(raw_content, template.getContent());
    }

    @Test
    public void testRemoveFieldSelectRTemplateName() {
        FormBuilderHtml builder = new FormBuilderHtml();

        Template template = TemplateFactory.HTML.get("formbuilder_fields_templatename");

        String raw_content = template.getContent();

        builder.generateField(template, new ConstrainedProperty("templatenamecolors").inList("black", "red", null, "green", "blue"), new String[]{"orange"}, null);
        assertNotEquals(raw_content, template.getContent());
        builder.removeField(template, "templatenamecolors");
        assertEquals(raw_content, template.getContent());

        builder.generateField(template, SelectInListEnum.class, "templatenamecolors", new String[]{"orange"}, null);
        assertNotEquals(raw_content, template.getContent());
        builder.removeField(template, "templatenamecolors");
        assertEquals(raw_content, template.getContent());
    }

    @Test
    public void testRemoveFieldSelectPrefix() {
        FormBuilderHtml builder = new FormBuilderHtml();

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        String raw_content = template.getContent();

        builder.generateField(template, new ConstrainedProperty("colors").inList("black", "red", null, "green", "blue"), new String[]{"orange"}, "more");
        assertNotEquals(raw_content, template.getContent());
        builder.removeField(template, "colors", "more");
        assertEquals(raw_content, template.getContent());

        builder.generateField(template, SelectInListEnum.class, "colors", new String[]{"orange"}, "more");
        assertNotEquals(raw_content, template.getContent());
        builder.removeField(template, "colors", "more");
        assertEquals(raw_content, template.getContent());
    }

    @Test
    public void testGenerateFormClassInvalidArguments() {
        try {
            FormBuilderHtml builder = new FormBuilderHtml();
            assertEquals(0, builder.generateForm(null, null, null, null).size());

            Template template = TemplateFactory.HTML.get("formbuilder_fields");
            String raw_content = template.getContent();
            assertNotNull(template);
            assertEquals(0, builder.generateForm(template, null, null, null).size());
            assertEquals(raw_content, template.getContent());
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testGenerateFormBeanInvalidArguments() {
        try {
            FormBuilderHtml builder = new FormBuilderHtml();
            assertEquals(0, builder.generateForm(null, (Object) null, null, null).size());
            assertEquals(0, builder.generateForm(null, new Object(), null, null).size());

            Template template = TemplateFactory.HTML.get("formbuilder_fields");
            String raw_content = template.getContent();
            assertNotNull(template);
            assertEquals(0, builder.generateForm(template, (Object) null, null, null).size());
            assertEquals(0, builder.generateForm(template, null, null, null).size());
            assertEquals(raw_content, template.getContent());

            try {
                builder.generateForm(template, (Object) Object.class, null, null);
                fail();
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            }
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testGenerateFormBeanNotInstantiatable() {
        FormBuilderHtml builder = new FormBuilderHtml();

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        try {
            builder.generateForm(template, PrivateBeanImpl.class, null, null);
            assertEquals(TemplateFactory.HTML.get("formbuilder_fields_out_regular_empty").getContent(), template.getContent());
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testRemoveFormInvalidArguments() {
        try {
            FormBuilderHtml builder = new FormBuilderHtml();
            builder.removeForm(null, null, null);

            Template template = TemplateFactory.HTML.get("formbuilder_fields");
            String raw_content = template.getContent();
            assertNotNull(template);
            builder.removeForm(template, null, null);
            assertEquals(raw_content, template.getContent());
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testRemoveFormBeanNotInstantiatable() {
        FormBuilderHtml builder = new FormBuilderHtml();

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        String raw_content = template.getContent();

        try {
            builder.generateForm(template, RegularBeanImpl.class, null, null);
            assertNotEquals(raw_content, template.getContent());
            builder.removeForm(template, PrivateBeanImpl.class, null);
            assertEquals(raw_content, template.getContent());
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    private static class PrivateBeanImpl extends RegularBeanImpl {
        private PrivateBeanImpl() {
        }
    }

    @Test
    public void testGenerateFormConstrainedEmpty() {
        FormBuilderHtml builder = new FormBuilderHtml();

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        try {
            builder.generateForm(template, ConstrainedBeanImpl.class, null, null);
            assertEquals(TemplateFactory.HTML.get("formbuilder_fields_out_constrained_empty").getContent(), template.getContent());
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testRemoveFormConstrainedEmpty() {
        FormBuilderHtml builder = new FormBuilderHtml();

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        String raw_content = template.getContent();

        try {
            builder.generateForm(template, ConstrainedBeanImpl.class, null, null);
            assertNotEquals(raw_content, template.getContent());
            builder.removeForm(template, ConstrainedBeanImpl.class, null);
            assertEquals(raw_content, template.getContent());
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testGenerateFormConstrainedExternalValues() {
        FormBuilderHtml builder = new FormBuilderHtml();

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        HashMap<String, String[]> values = new HashMap<String, String[]>();
        values.put("hidden", new String[]{"canyouseeme"});
        values.put("anotherhidden", new String[]{"I can't see you"});
        values.put("login", new String[]{"ikke"});
        values.put("anotherlogin", new String[]{"jullie"});
        values.put("password", new String[]{"secret"});
        values.put("anotherpassword", new String[]{"real secret"});
        values.put("comment", new String[]{"één comment"});
        values.put("anothercomment", new String[]{"this comment"});
        values.put("question", new String[]{"a2"});
        values.put("anotherquestion", new String[]{"a3"});
        values.put("customquestion", new String[]{"a1"});
        values.put("anothercustomquestion", new String[]{"a2"});
        values.put("options", new String[]{"2"});
        values.put("otheroptions", new String[]{"2", "0"});
        values.put("customoptions", new String[]{"1"});
        values.put("othercustomoptions", new String[]{"2"});
        values.put("invoice", new String[]{"1"});
        values.put("onemoreinvoice", new String[]{"0"});
        values.put("colors", new String[]{"red", "green"});
        values.put("morecolors", new String[]{"black"});
        values.put("yourcolors", new String[]{"brown"});
        try {
            builder.generateForm(template, ConstrainedBeanImpl.class, values, null);
            assertEquals(TemplateFactory.HTML.get("formbuilder_fields_out_constrained_values").getContent(), template.getContent());
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testGenerateFormConstrainedBeanValues() {
        FormBuilderHtml builder = new FormBuilderHtml();

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        ConstrainedBeanImpl bean = new ConstrainedBeanImpl();
        bean.setHidden("canyouseeme");
        bean.setAnotherhidden("I can't see you");
        bean.setLogin("ikke");
        bean.setAnotherlogin("jullie");
        bean.setPassword("secret");
        bean.setAnotherpassword("real secret");
        bean.setComment("één comment");
        bean.setAnothercomment("this comment");
        bean.setQuestion(ConstrainedBeanImpl.Question.a2);
        bean.setAnotherquestion("a3");
        bean.setCustomquestion("a1");
        bean.setAnothercustomquestion("a2");
        bean.setOptions(new int[]{2});
        bean.setOtheroptions(new int[]{2, 0});
        bean.setCustomoptions(new int[]{1});
        bean.setOthercustomoptions(new int[]{2});
        bean.setInvoice(true);
        bean.setOnemoreinvoice(false);
        bean.setColors(new String[]{"red", "green"});
        bean.setMorecolors(new String[]{"black"});
        bean.setYourcolors(new String[]{"brown"});
        try {
            builder.generateForm(template, bean, null, null);
            assertEquals(TemplateFactory.HTML.get("formbuilder_fields_out_constrained_values").getContent(), template.getContent());
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testGenerateFormConstrainedBeanValuesInvalid() {
        FormBuilderHtml builder = new FormBuilderHtml();

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        ConstrainedBeanImpl bean = new ConstrainedBeanImpl();

        bean.addValidationError(new ValidationError.INCOMPLETE("that"));
        Set<ValidationError> errors = bean.getValidationErrors();

        bean.setLogin("1234567");
        bean.setAnotherlogin(null);
        bean.setPassword(null);
        bean.setAnotherpassword("123456789abcd");
        bean.setComment(null);
        bean.setAnothercomment(null);
        bean.setQuestion(null);
        bean.setAnotherquestion("a5");
        bean.setCustomquestion(null);
        bean.setAnothercustomquestion("a6");
        bean.setOptions(new int[]{1});
        bean.setOtheroptions(null);
        bean.setCustomoptions(new int[]{1, 0});
        bean.setOthercustomoptions(new int[]{4});
        bean.setInvoice(false);
        bean.setOnemoreinvoice(true);
        bean.setColors(new String[]{"red", "green", "black"});
        bean.setMorecolors(null);
        bean.setYourcolors(new String[]{"white"});
        try {
            builder.generateForm(template, bean, null, null);
            assertEquals(TemplateFactory.HTML.get("formbuilder_fields_out_constrained_invalid").getContent(), template.getContent());
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }

        assertSame(errors, bean.getValidationErrors());
    }

    @Test
    public void testGenerateFormConstrainedEmptyPrefix() {
        FormBuilderHtml builder = new FormBuilderHtml();

        Template template = TemplateFactory.HTML.get("formbuilder_form_prefix");
        try {
            builder.generateForm(template, ConstrainedBeanImpl.class, null, "prefix_");
            assertEquals(TemplateFactory.HTML.get("formbuilder_form_prefix_out_constrained_empty").getContent(), template.getContent());
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testGenerateFormConstrainedExternalValuesPrefix() {
        FormBuilderHtml builder = new FormBuilderHtml();

        Template template = TemplateFactory.HTML.get("formbuilder_form_prefix");

        HashMap<String, String[]> values = new HashMap<String, String[]>();
        values.put("prefix_hidden", new String[]{"canyouseeme"});
        values.put("prefix_login", new String[]{"ikke"});
        values.put("prefix_password", new String[]{"secret"});
        values.put("prefix_comment", new String[]{"één comment"});
        values.put("prefix_question", new String[]{"a2"});
        values.put("prefix_options", new String[]{"2"});
        values.put("prefix_invoice", new String[]{"1"});
        values.put("prefix_colors", new String[]{"red", "green"});
        values.put("prefix_yourcolors", new String[]{"brown", "orange"});
        try {
            builder.generateForm(template, ConstrainedBeanImpl.class, values, "prefix_");
            assertEquals(TemplateFactory.HTML.get("formbuilder_form_prefix_out_constrained_values").getContent(), template.getContent());
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testGenerateFormConstrainedBeanValuesPrefix() {
        FormBuilderHtml builder = new FormBuilderHtml();

        Template template = TemplateFactory.HTML.get("formbuilder_form_prefix");

        ConstrainedBeanImpl bean = new ConstrainedBeanImpl();
        bean.setHidden("canyouseeme");
        bean.setLogin("ikke");
        bean.setPassword("secret");
        bean.setComment("één comment");
        bean.setQuestion(ConstrainedBeanImpl.Question.a2);
        bean.setOptions(new int[]{2});
        bean.setInvoice(true);
        bean.setColors(new String[]{"red", "green"});
        bean.setYourcolors(new String[]{"orange", "brown"});
        try {
            builder.generateForm(template, bean, null, "prefix_");
            assertEquals(TemplateFactory.HTML.get("formbuilder_form_prefix_out_constrained_values").getContent(), template.getContent());
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testRemoveFormConstrainedBeanValuesPrefix() {
        FormBuilderHtml builder = new FormBuilderHtml();

        Template template = TemplateFactory.HTML.get("formbuilder_form_prefix");

        String raw_content = template.getContent();

        ConstrainedBeanImpl bean = new ConstrainedBeanImpl();
        bean.setHidden("canyouseeme");
        bean.setLogin("ikke");
        bean.setPassword("secret");
        bean.setComment("één comment");
        bean.setQuestion(ConstrainedBeanImpl.Question.a2);
        bean.setOptions(new int[]{2});
        bean.setInvoice(true);
        bean.setColors(new String[]{"red", "green"});
        bean.setYourcolors(new String[]{"orange", "brown"});
        try {
            builder.generateForm(template, bean, null, "prefix_");
            assertEquals(TemplateFactory.HTML.get("formbuilder_form_prefix_out_constrained_values").getContent(), template.getContent());
            assertNotEquals(raw_content, template.getContent());
            builder.removeForm(template, RegularBeanImpl.class, "prefix_");
            assertEquals(raw_content, template.getContent());
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testRemoveFormConstrainedValues() {
        FormBuilderHtml builder = new FormBuilderHtml();

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        String raw_content = template.getContent();

        HashMap<String, String[]> values = new HashMap<String, String[]>();
        values.put("hidden", new String[]{"canyouseeme"});
        values.put("anotherhidden", new String[]{"I can't see you"});
        values.put("login", new String[]{"ikke"});
        values.put("anotherlogin", new String[]{"jullie"});
        values.put("password", new String[]{"secret"});
        values.put("anotherpassword", new String[]{"real secret"});
        values.put("comment", new String[]{"één comment"});
        values.put("anothercomment", new String[]{"this comment"});
        values.put("question", new String[]{"a2"});
        values.put("anotherquestion", new String[]{"a3"});
        values.put("customquestion", new String[]{"a1"});
        values.put("anothercustomquestion", new String[]{"a2"});
        values.put("options", new String[]{"2"});
        values.put("otheroptions", new String[]{"2", "0"});
        values.put("customoptions", new String[]{"1"});
        values.put("othercustomoptions", new String[]{"2"});
        values.put("invoice", new String[]{"1"});
        values.put("onemoreinvoice", new String[]{"0"});
        values.put("colors", new String[]{"red", "green"});
        values.put("morecolors", new String[]{"black"});
        try {
            builder.generateForm(template, ConstrainedBeanImpl.class, values, null);
            assertNotEquals(raw_content, template.getContent());
            builder.removeForm(template, ConstrainedBeanImpl.class, null);
            assertEquals(raw_content, template.getContent());
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testGenerateFormRegularEmpty() {
        FormBuilderHtml builder = new FormBuilderHtml();

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        try {
            builder.generateForm(template, RegularBeanImpl.class, null, null);
            assertEquals(TemplateFactory.HTML.get("formbuilder_fields_out_regular_empty").getContent(), template.getContent());
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testRemoveFormRegularEmpty() {
        FormBuilderHtml builder = new FormBuilderHtml();

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        String raw_content = template.getContent();

        try {
            builder.generateForm(template, RegularBeanImpl.class, null, null);
            assertNotEquals(raw_content, template.getContent());
            builder.removeForm(template, RegularBeanImpl.class, null);
            assertEquals(raw_content, template.getContent());
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testGenerateFormRegularExternalValues() {
        FormBuilderHtml builder = new FormBuilderHtml();

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        HashMap<String, String[]> values = new HashMap<String, String[]>();
        values.put("hidden", new String[]{"canyouseeme"});
        values.put("anotherhidden", new String[]{"I can't see you"});
        values.put("login", new String[]{"ikke"});
        values.put("anotherlogin", new String[]{"jullie"});
        values.put("password", new String[]{"secret"});
        values.put("anotherpassword", new String[]{"real secret"});
        values.put("comment", new String[]{"één comment"});
        values.put("anothercomment", new String[]{"this comment"});
        values.put("question", new String[]{"a2"});
        values.put("anotherquestion", new String[]{"a3"});
        values.put("customquestion", new String[]{"a1"});
        values.put("anothercustomquestion", new String[]{"a2"});
        values.put("options", new String[]{"2"});
        values.put("otheroptions", new String[]{"2", "0"});
        values.put("customoptions", new String[]{"1"});
        values.put("othercustomoptions", new String[]{"2"});
        values.put("invoice", new String[]{"1"});
        values.put("onemoreinvoice", new String[]{"0"});
        values.put("colors", new String[]{"red", "green"});
        values.put("morecolors", new String[]{"black"});
        values.put("yourcolors", new String[]{"brown"});
        try {
            builder.generateForm(template, RegularBeanImpl.class, values, null);
            assertEquals(TemplateFactory.HTML.get("formbuilder_fields_out_regular_values").getContent(), template.getContent());
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testGenerateFormRegularBeanValues() {
        FormBuilderHtml builder = new FormBuilderHtml();

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        RegularBeanImpl bean = new RegularBeanImpl();
        bean.setHidden("canyouseeme");
        bean.setAnotherhidden("I can't see you");
        bean.setLogin("ikke");
        bean.setAnotherlogin("jullie");
        bean.setPassword("secret");
        bean.setAnotherpassword("real secret");
        bean.setComment("één comment");
        bean.setAnothercomment("this comment");
        bean.setQuestion("a2");
        bean.setAnotherquestion("a3");
        bean.setCustomquestion("a1");
        bean.setAnothercustomquestion("a2");
        bean.setOptions(new int[]{2});
        bean.setOtheroptions(new int[]{2, 0});
        bean.setCustomoptions(new int[]{1});
        bean.setOthercustomoptions(new int[]{2});
        bean.setInvoice(true);
        bean.setOnemoreinvoice(false);
        bean.setColors(new RegularBeanImpl.Color[]{RegularBeanImpl.Color.red, RegularBeanImpl.Color.green});
        bean.setMorecolors(new String[]{"black"});
        bean.setYourcolors(new String[]{"brown"});
        try {
            builder.generateForm(template, bean, null, null);
            assertEquals(TemplateFactory.HTML.get("formbuilder_fields_out_regular_values").getContent(), template.getContent());
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testGenerateFormRegularEmptyPrefix() {
        FormBuilderHtml builder = new FormBuilderHtml();

        Template template = TemplateFactory.HTML.get("formbuilder_form_prefix");
        try {
            builder.generateForm(template, RegularBeanImpl.class, null, "prefix_");
            assertEquals(TemplateFactory.HTML.get("formbuilder_form_prefix_out_regular_empty").getContent(), template.getContent());
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testGenerateFormRegularExternalValuesPrefix() {
        FormBuilderHtml builder = new FormBuilderHtml();

        Template template = TemplateFactory.HTML.get("formbuilder_form_prefix");

        HashMap<String, String[]> values = new HashMap<String, String[]>();
        values.put("prefix_hidden", new String[]{"canyouseeme"});
        values.put("prefix_login", new String[]{"ikke"});
        values.put("prefix_password", new String[]{"secret"});
        values.put("prefix_comment", new String[]{"één comment"});
        values.put("prefix_question", new String[]{"a2"});
        values.put("prefix_options", new String[]{"2"});
        values.put("prefix_invoice", new String[]{"1"});
        values.put("prefix_colors", new String[]{"red", "green"});
        values.put("prefix_yourcolors", new String[]{"brown"});
        try {
            builder.generateForm(template, RegularBeanImpl.class, values, "prefix_");
            assertEquals(TemplateFactory.HTML.get("formbuilder_form_prefix_out_regular_values").getContent(), template.getContent());
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testGenerateFormRegularBeanValuesPrefix() {
        FormBuilderHtml builder = new FormBuilderHtml();

        Template template = TemplateFactory.HTML.get("formbuilder_form_prefix");

        RegularBeanImpl bean = new RegularBeanImpl();
        bean.setHidden("canyouseeme");
        bean.setLogin("ikke");
        bean.setPassword("secret");
        bean.setComment("één comment");
        bean.setQuestion("a2");
        bean.setOptions(new int[]{2});
        bean.setInvoice(true);
        bean.setColors(new RegularBeanImpl.Color[]{RegularBeanImpl.Color.red, RegularBeanImpl.Color.green});
        bean.setYourcolors(new String[]{"brown"});
        try {
            builder.generateForm(template, bean, null, "prefix_");
            assertEquals(TemplateFactory.HTML.get("formbuilder_form_prefix_out_regular_values").getContent(), template.getContent());
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testRemoveFormRegularBeanValuesPrefix() {
        FormBuilderHtml builder = new FormBuilderHtml();

        Template template = TemplateFactory.HTML.get("formbuilder_form_prefix");

        String raw_content = template.getContent();

        RegularBeanImpl bean = new RegularBeanImpl();
        bean.setHidden("canyouseeme");
        bean.setLogin("ikke");
        bean.setPassword("secret");
        bean.setComment("één comment");
        bean.setQuestion("a2");
        bean.setOptions(new int[]{2});
        bean.setInvoice(true);
        bean.setColors(new RegularBeanImpl.Color[]{RegularBeanImpl.Color.red, RegularBeanImpl.Color.green});
        bean.setYourcolors(new String[]{"brown"});
        try {
            builder.generateForm(template, bean, null, "prefix_");
            assertEquals(TemplateFactory.HTML.get("formbuilder_form_prefix_out_regular_values").getContent(), template.getContent());
            assertNotEquals(raw_content, template.getContent());
            builder.removeForm(template, RegularBeanImpl.class, "prefix_");
            assertEquals(raw_content, template.getContent());
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testRemoveFormRegularValues() {
        FormBuilderHtml builder = new FormBuilderHtml();

        Template template = TemplateFactory.HTML.get("formbuilder_fields");

        String raw_content = template.getContent();

        HashMap<String, String[]> values = new HashMap<String, String[]>();
        values.put("hidden", new String[]{"canyouseeme"});
        values.put("anotherhidden", new String[]{"I can't see you"});
        values.put("login", new String[]{"ikke"});
        values.put("anotherlogin", new String[]{"jullie"});
        values.put("password", new String[]{"secret"});
        values.put("anotherpassword", new String[]{"real secret"});
        values.put("comment", new String[]{"één comment"});
        values.put("anothercomment", new String[]{"this comment"});
        values.put("question", new String[]{"a2"});
        values.put("anotherquestion", new String[]{"a3"});
        values.put("customquestion", new String[]{"a1"});
        values.put("anothercustomquestion", new String[]{"a2"});
        values.put("options", new String[]{"2"});
        values.put("otheroptions", new String[]{"2", "0"});
        values.put("customoptions", new String[]{"1"});
        values.put("othercustomoptions", new String[]{"2"});
        values.put("invoice", new String[]{"1"});
        values.put("onemoreinvoice", new String[]{"0"});
        values.put("colors", new String[]{"red", "green"});
        values.put("morecolors", new String[]{"black"});
        try {
            builder.generateForm(template, RegularBeanImpl.class, values, null);
            assertNotEquals(raw_content, template.getContent());
            builder.removeForm(template, RegularBeanImpl.class, null);
            assertEquals(raw_content, template.getContent());
        } catch (BeanUtilsException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testSelectParameterInvalidArguments() {
        FormBuilderHtml builder = new FormBuilderHtml();
        assertEquals(0, builder.selectParameter(null, null, null).size());

        Template template = TemplateFactory.HTML.get("formbuilder_parameters");
        String raw_content = template.getContent();
        assertNotNull(template);
        assertEquals(0, builder.selectParameter(template, null, null).size());
        assertEquals(raw_content, template.getContent());
        assertEquals(0, builder.selectParameter(template, "", null).size());
        assertEquals(raw_content, template.getContent());
        assertEquals(0, builder.selectParameter(template, "name", null).size());
        assertEquals(raw_content, template.getContent());
        assertEquals(0, builder.selectParameter(template, "name", new String[0]).size());
        assertEquals(raw_content, template.getContent());
    }

    @Test
    public void testSelectParameterChecked() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Template template = TemplateFactory.HTML.get("formbuilder_parameters");
        assertEquals(0, builder.selectParameter(template, "wantsupdates", new String[]{"false", null}).size());
        Collection<String> set_values = builder.selectParameter(template, "wantsupdates", new String[]{"true"});
        assertEquals(1, set_values.size());
        assertEquals("wantsupdates:checked", set_values.iterator().next());
        assertEquals("wantsupdates checked=\"checked\"\n" +
            "orange\n" +
            "blue\n" +
            "red\n" +
            "lastname\n" +
            "lastname\n", template.getContent());
    }

    @Test
    public void testSelectParameterCheckedValues() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Template template = TemplateFactory.HTML.get("formbuilder_parameters");
        assertEquals(0, builder.selectParameter(template, "colors", new String[]{"green"}).size());
        Collection<String> set_values = builder.selectParameter(template, "colors", new String[]{"orange", "red", null, "black"});
        assertEquals(2, set_values.size());
        Iterator<String> it = set_values.iterator();
        assertEquals("colors:orange:checked", it.next());
        assertEquals("colors:red:checked", it.next());
        assertEquals("wantsupdates\n" +
            "orange checked=\"checked\"\n" +
            "blue\n" +
            "red checked=\"checked\"\n" +
            "lastname\n" +
            "lastname\n", template.getContent());
    }

    @Test
    public void testSelectParameterSelectedValues() {
        FormBuilderHtml builder = new FormBuilderHtml();
        Template template = TemplateFactory.HTML.get("formbuilder_parameters");
        assertEquals(0, builder.selectParameter(template, "lastname", new String[]{"Smith"}).size());
        Collection<String> set_values = builder.selectParameter(template, "lastname", new String[]{"Smith", null, "Kramer"});
        assertEquals(1, set_values.size());
        Iterator<String> it = set_values.iterator();
        assertEquals("lastname:Kramer:selected", it.next());
        assertEquals("wantsupdates\n" +
            "orange\n" +
            "blue\n" +
            "red\n" +
            "lastname\n" +
            "lastname selected=\"selected\"\n", template.getContent());
    }

    @Test
    public void testUnselectParameterInvalidArguments() {
        FormBuilderHtml builder = new FormBuilderHtml();
        builder.unselectParameter(null, null, null);

        Template template = TemplateFactory.HTML.get("formbuilder_parameters");
        template.setValue("wantsupdates:checked", "1");
        template.setValue("colors:orange:checked", "1");
        template.setValue("colors:blue:checked", "1");
        template.setValue("colors:red:checked", "1");
        template.setValue("lastname:Bevin:selected", "1");
        template.setValue("lastname:Kramer:selected", "1");

        String raw_content = template.getContent();
        assertEquals("wantsupdates1\n" +
            "orange1\n" +
            "blue1\n" +
            "red1\n" +
            "lastname1\n" +
            "lastname1\n", raw_content);

        assertNotNull(template);
        builder.unselectParameter(template, null, null);
        assertEquals(raw_content, template.getContent());
        builder.unselectParameter(template, "", null);
        assertEquals(raw_content, template.getContent());
        builder.unselectParameter(template, "name", null);
        assertEquals(raw_content, template.getContent());
        builder.unselectParameter(template, "name", new String[0]);
        assertEquals(raw_content, template.getContent());
    }

    @Test
    public void testUnselectParameterChecked() {
        FormBuilderHtml builder = new FormBuilderHtml();
        builder.unselectParameter(null, null, null);

        Template template = TemplateFactory.HTML.get("formbuilder_parameters");
        template.setValue("wantsupdates:checked", "1");
        template.setValue("colors:orange:checked", "1");
        template.setValue("colors:blue:checked", "1");
        template.setValue("colors:red:checked", "1");
        template.setValue("lastname:Bevin:selected", "1");
        template.setValue("lastname:Kramer:selected", "1");

        String raw_content = template.getContent();
        assertEquals("wantsupdates1\n" +
            "orange1\n" +
            "blue1\n" +
            "red1\n" +
            "lastname1\n" +
            "lastname1\n", raw_content);

        builder.unselectParameter(template, "wantsupdates", new String[]{"false", null});
        builder.unselectParameter(template, "wantsupdates", new String[]{"true"});
        assertEquals("wantsupdates\n" +
            "orange1\n" +
            "blue1\n" +
            "red1\n" +
            "lastname1\n" +
            "lastname1\n", template.getContent());
    }

    @Test
    public void testUnselectParameterCheckedValues() {
        FormBuilderHtml builder = new FormBuilderHtml();
        builder.unselectParameter(null, null, null);

        Template template = TemplateFactory.HTML.get("formbuilder_parameters");
        template.setValue("wantsupdates:checked", "1");
        template.setValue("colors:orange:checked", "1");
        template.setValue("colors:blue:checked", "1");
        template.setValue("colors:red:checked", "1");
        template.setValue("lastname:Bevin:selected", "1");
        template.setValue("lastname:Kramer:selected", "1");

        String raw_content = template.getContent();
        assertEquals("wantsupdates1\n" +
            "orange1\n" +
            "blue1\n" +
            "red1\n" +
            "lastname1\n" +
            "lastname1\n", raw_content);

        builder.unselectParameter(template, "colors", new String[]{"green"});
        builder.unselectParameter(template, "colors", new String[]{"orange", "red", null, "black"});
        assertEquals("wantsupdates1\n" +
            "orange\n" +
            "blue1\n" +
            "red\n" +
            "lastname1\n" +
            "lastname1\n", template.getContent());
    }

    @Test
    public void testUnselectParameterSelectedValues() {
        FormBuilderHtml builder = new FormBuilderHtml();
        builder.unselectParameter(null, null, null);

        Template template = TemplateFactory.HTML.get("formbuilder_parameters");
        template.setValue("wantsupdates:checked", "1");
        template.setValue("colors:orange:checked", "1");
        template.setValue("colors:blue:checked", "1");
        template.setValue("colors:red:checked", "1");
        template.setValue("lastname:Bevin:selected", "1");
        template.setValue("lastname:Kramer:selected", "1");

        String raw_content = template.getContent();
        assertEquals("wantsupdates1\n" +
            "orange1\n" +
            "blue1\n" +
            "red1\n" +
            "lastname1\n" +
            "lastname1\n", raw_content);

        builder.unselectParameter(template, "lastname", new String[]{"Smith"});
        builder.unselectParameter(template, "lastname", new String[]{"Smith", null, "Kramer"});
        assertEquals("wantsupdates1\n" +
            "orange1\n" +
            "blue1\n" +
            "red1\n" +
            "lastname1\n" +
            "lastname\n", template.getContent());
    }
}
