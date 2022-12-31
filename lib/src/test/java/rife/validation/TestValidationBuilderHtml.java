/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

import org.junit.jupiter.api.Test;
import rife.validation.exceptions.MissingMarkingBlockException;
import rife.template.Template;
import rife.template.TemplateFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestValidationBuilderHtml {
    @Test
    public void testInstantiate() {
        ValidationBuilderHtml builder = new ValidationBuilderHtml();
        assertNotNull(builder);
    }

    @Test
    public void testClone() {
        ValidationBuilderHtml builder1 = new ValidationBuilderHtml();
        ValidationBuilderHtml builder2 = (ValidationBuilderHtml) builder1.clone();
        assertNotNull(builder2);
        assertNotSame(builder1, builder2);
    }

    @Test
    public void testSetFallbackErrorAreaInvalidArguments() {
        ValidationBuilderHtml builder = new ValidationBuilderHtml();
        builder.setFallbackErrorArea(null, null);

        Template template = TemplateFactory.HTML.get("validationbuilder_errors_fallbackarea_basic");
        builder.setFallbackErrorArea(template, null);
        assertEquals("\n", template.getContent());
    }

    @Test
    public void testSetFallbackErrorAreaBasic() {
        ValidationBuilderHtml builder = new ValidationBuilderHtml();

        Template template = TemplateFactory.HTML.get("validationbuilder_errors_fallbackarea_basic");
        builder.setFallbackErrorArea(template, "my message");
        assertEquals(TemplateFactory.HTML.get("validationbuilder_errors_fallbackarea_basic_out").getContent(), template.getContent());
    }

    @Test
    public void testSetFallbackErrorAreaWildcardFormatted() {
        ValidationBuilderHtml builder = new ValidationBuilderHtml();

        Template template = TemplateFactory.HTML.get("validationbuilder_errors_fallbackarea_wildcardformatted");
        builder.setFallbackErrorArea(template, "my message");
        assertEquals(TemplateFactory.HTML.get("validationbuilder_errors_fallbackarea_wildcardformatted_out").getContent(), template.getContent());
    }

    @Test
    public void testSetFallbackErrorAreaFormatted() {
        ValidationBuilderHtml builder = new ValidationBuilderHtml();

        Template template = TemplateFactory.HTML.get("validationbuilder_errors_fallbackarea_formatted");
        builder.setFallbackErrorArea(template, "my message");
        assertEquals(TemplateFactory.HTML.get("validationbuilder_errors_fallbackarea_formatted_out").getContent(), template.getContent());
    }

    @Test
    public void testSetFallbackErrorAreaWildcardDecorated() {
        ValidationBuilderHtml builder = new ValidationBuilderHtml();

        Template template = TemplateFactory.HTML.get("validationbuilder_errors_fallbackarea_wildcarddecorated");
        builder.setFallbackErrorArea(template, "my message");
        assertEquals(TemplateFactory.HTML.get("validationbuilder_errors_fallbackarea_wildcarddecorated_out").getContent(), template.getContent());
    }

    @Test
    public void testSetFallbackErrorAreaDecorated() {
        ValidationBuilderHtml builder = new ValidationBuilderHtml();

        Template template = TemplateFactory.HTML.get("validationbuilder_errors_fallbackarea_decorated");
        builder.setFallbackErrorArea(template, "my message");
        assertEquals(TemplateFactory.HTML.get("validationbuilder_errors_fallbackarea_decorated_out").getContent(), template.getContent());
    }

    @Test
    public void testGenerateValidationErrorsInvalidArguments() {
        ValidationBuilderHtml builder = new ValidationBuilderHtml();
        assertEquals(0, builder.generateValidationErrors(null, null, null, null).size());

        Template template = TemplateFactory.HTML.get("validationbuilder_errors_raw");
        String raw_content = template.getContent();
        assertNotNull(template);
        assertEquals(0, builder.generateValidationErrors(template, null, null, null).size());
        assertEquals(raw_content, template.getContent());
        assertEquals(0, builder.generateValidationErrors(template, null, null, null).size());
        assertEquals(raw_content, template.getContent());
        assertEquals(0, builder.generateValidationErrors(template, new ArrayList<ValidationError>(), null, null).size());
        assertEquals(raw_content, template.getContent());
    }

    @Test
    public void testGenerateValidationErrorsNovalues() {
        ValidationBuilderHtml builder = new ValidationBuilderHtml();

        Template template = TemplateFactory.HTML.get("validationbuilder_errors_novalues");

        ConstrainedBeanImpl bean = new ConstrainedBeanImpl();
        bean.validate();
        builder.generateValidationErrors(template, bean.getValidationErrors(), bean.getValidatedSubjects(), null);
        assertEquals(TemplateFactory.HTML.get("validationbuilder_errors_novalues").getContent(), template.getContent());
    }

    @Test
    public void testGenerateValidationErrorsRaw() {
        ValidationBuilderHtml builder = new ValidationBuilderHtml();

        Template template = TemplateFactory.HTML.get("validationbuilder_errors_raw");

        ConstrainedBeanImpl bean = new ConstrainedBeanImpl();
        bean.validate();
        bean.addValidationError(new ValidationError.WRONG_FORMAT("login"));
        builder.generateValidationErrors(template, bean.getValidationErrors(), bean.getValidatedSubjects(), null);
        assertEquals(TemplateFactory.HTML.get("validationbuilder_errors_raw_out").getContent(), template.getContent());
    }

    @Test
    public void testGenerateValidationErrorsRawFallbackblock() {
        ValidationBuilderHtml builder = new ValidationBuilderHtml();

        Template template = TemplateFactory.HTML.get("validationbuilder_errors_raw_fallbackblock");

        ConstrainedBeanImpl bean = new ConstrainedBeanImpl();
        bean.validate();
        bean.addValidationError(new ValidationError.WRONG_FORMAT("login"));
        builder.generateValidationErrors(template, bean.getValidationErrors(), bean.getValidatedSubjects(), null);
        assertEquals(TemplateFactory.HTML.get("validationbuilder_errors_raw_fallbackblock_out").getContent(), template.getContent());
    }

    @Test
    public void testGenerateValidationErrorsMessages() {
        ValidationBuilderHtml builder = new ValidationBuilderHtml();

        Template template = TemplateFactory.HTML.get("validationbuilder_errors_messages");

        ConstrainedBeanImpl bean = new ConstrainedBeanImpl();
        bean.setAnotherlogin("123456789012345678901");
        bean.setPassword("1234567890");
        bean.setColors(new String[]{"invalid"});
        bean.validate();
        builder.generateValidationErrors(template, bean.getValidationErrors(), bean.getValidatedSubjects(), null);
        assertEquals(TemplateFactory.HTML.get("validationbuilder_errors_messages_out").getContent(), template.getContent());
    }

    @Test
    public void testGenerateValidationErrorsMessagesPrefix() {
        ValidationBuilderHtml builder = new ValidationBuilderHtml();

        Template template = TemplateFactory.HTML.get("validationbuilder_errors_messages_prefix");

        ConstrainedBeanImpl bean = new ConstrainedBeanImpl();
        bean.setPassword("1234567890");
        bean.setColors(new String[]{"invalid"});
        bean.validate();
        builder.generateValidationErrors(template, bean.getValidationErrors(), bean.getValidatedSubjects(), "prefix_");
        assertEquals(TemplateFactory.HTML.get("validationbuilder_errors_messages_prefix_out").getContent(), template.getContent());
    }

    @Test
    public void testGenerateValidationErrorsFormattedmessages() {
        ValidationBuilderHtml builder = new ValidationBuilderHtml();

        Template template = TemplateFactory.HTML.get("validationbuilder_errors_formattedmessages");

        ConstrainedBeanImpl bean = new ConstrainedBeanImpl();
        bean.setAnotherlogin("123456789012345678901");
        bean.setPassword("1234567890");
        bean.setColors(new String[]{"invalid"});
        bean.validate();
        builder.generateValidationErrors(template, bean.getValidationErrors(), bean.getValidatedSubjects(), null);
        assertEquals(TemplateFactory.HTML.get("validationbuilder_errors_formattedmessages_out").getContent(), template.getContent());
    }

    @Test
    public void testGenerateValidationErrorsFormattedmessagesNocontent() {
        ValidationBuilderHtml builder = new ValidationBuilderHtml();

        Template template = TemplateFactory.HTML.get("validationbuilder_errors_formattedmessages_nocontent");

        ConstrainedBeanImpl bean = new ConstrainedBeanImpl();
        bean.setAnotherlogin("123456789012345678901");
        bean.setPassword("1234567890");
        bean.setColors(new String[]{"invalid"});
        bean.validate();
        builder.generateValidationErrors(template, bean.getValidationErrors(), bean.getValidatedSubjects(), null);
        assertEquals(TemplateFactory.HTML.get("validationbuilder_errors_formattedmessages_nocontent_out").getContent(), template.getContent());
    }

    @Test
    public void testGenerateValidationErrorsFormattedmessagesPrefix() {
        ValidationBuilderHtml builder = new ValidationBuilderHtml();

        Template template = TemplateFactory.HTML.get("validationbuilder_errors_formattedmessages_prefix");

        ConstrainedBeanImpl bean = new ConstrainedBeanImpl();
        bean.setAnotherlogin("123456789012345678901");
        bean.setPassword("1234567890");
        bean.setColors(new String[]{"invalid"});
        bean.validate();
        builder.generateValidationErrors(template, bean.getValidationErrors(), bean.getValidatedSubjects(), "prefix_");
        assertEquals(TemplateFactory.HTML.get("validationbuilder_errors_formattedmessages_prefix_out").getContent(), template.getContent());
    }

    @Test
    public void testGenerateValidationErrorsPositionedmessages() {
        ValidationBuilderHtml builder = new ValidationBuilderHtml();

        Template template = TemplateFactory.HTML.get("validationbuilder_errors_positionedmessages");

        ConstrainedBeanImpl bean = new ConstrainedBeanImpl();
        bean.validate();
        builder.generateValidationErrors(template, bean.getValidationErrors(), bean.getValidatedSubjects(), null);
        assertEquals(TemplateFactory.HTML.get("validationbuilder_errors_positionedmessages_out1").getContent(), template.getContent());

        bean.resetValidation();
        bean.validate();
        bean.makeSubjectValid("anotherlogin");
        bean.makeSubjectValid("anotherpassword");
        builder.generateValidationErrors(template, bean.getValidationErrors(), bean.getValidatedSubjects(), null);
        assertEquals(TemplateFactory.HTML.get("validationbuilder_errors_positionedmessages_out2").getContent(), template.getContent());

        bean.resetValidation();
        bean.validate();
        bean.makeSubjectValid("login");
        bean.makeSubjectValid("customquestion");
        builder.generateValidationErrors(template, bean.getValidationErrors(), bean.getValidatedSubjects(), null);
        assertEquals(TemplateFactory.HTML.get("validationbuilder_errors_positionedmessages_out3").getContent(), template.getContent());
    }

    @Test
    public void testGenerateValidationErrorsPositionedmessagesPrefix() {
        ValidationBuilderHtml builder = new ValidationBuilderHtml();

        Template template = TemplateFactory.HTML.get("validationbuilder_errors_positionedmessages_prefix");

        ConstrainedBeanImpl bean = new ConstrainedBeanImpl();
        bean.validate();
        builder.generateValidationErrors(template, bean.getValidationErrors(), bean.getValidatedSubjects(), "prefix_");
        assertEquals(TemplateFactory.HTML.get("validationbuilder_errors_positionedmessages_prefix_out1").getContent(), template.getContent());

        bean.resetValidation();
        bean.validate();
        bean.makeSubjectValid("anotherlogin");
        bean.makeSubjectValid("anotherpassword");
        builder.generateValidationErrors(template, bean.getValidationErrors(), bean.getValidatedSubjects(), "prefix_");
        assertEquals(TemplateFactory.HTML.get("validationbuilder_errors_positionedmessages_prefix_out2").getContent(), template.getContent());

        bean.resetValidation();
        bean.validate();
        bean.makeSubjectValid("login");
        bean.makeSubjectValid("customquestion");
        builder.generateValidationErrors(template, bean.getValidationErrors(), bean.getValidatedSubjects(), "prefix_");
        assertEquals(TemplateFactory.HTML.get("validationbuilder_errors_positionedmessages_prefix_out3").getContent(), template.getContent());
    }

    @Test
    public void testGenerateValidationErrorsDecoratedmessages() {
        ValidationBuilderHtml builder = new ValidationBuilderHtml();

        Template template = TemplateFactory.HTML.get("validationbuilder_errors_decoratedmessages");

        ConstrainedBeanImpl bean = new ConstrainedBeanImpl();
        bean.validate();
        builder.generateValidationErrors(template, bean.getValidationErrors(), bean.getValidatedSubjects(), null);
        assertEquals(TemplateFactory.HTML.get("validationbuilder_errors_decoratedmessages_out1").getContent(), template.getContent());

        bean.resetValidation();
        bean.validate();
        bean.makeSubjectValid("anotherlogin");
        bean.makeSubjectValid("anotherpassword");
        builder.generateValidationErrors(template, bean.getValidationErrors(), bean.getValidatedSubjects(), null);
        assertEquals(TemplateFactory.HTML.get("validationbuilder_errors_decoratedmessages_out2").getContent(), template.getContent());

        bean.resetValidation();
        bean.validate();
        bean.makeSubjectValid("login");
        bean.makeSubjectValid("customquestion");
        builder.generateValidationErrors(template, bean.getValidationErrors(), bean.getValidatedSubjects(), null);
        assertEquals(TemplateFactory.HTML.get("validationbuilder_errors_decoratedmessages_out3").getContent(), template.getContent());
    }

    @Test
    public void testGenerateValidationErrorsDecoratedmessagesPrefix() {
        ValidationBuilderHtml builder = new ValidationBuilderHtml();

        Template template = TemplateFactory.HTML.get("validationbuilder_errors_decoratedmessages_prefix");

        ConstrainedBeanImpl bean = new ConstrainedBeanImpl();
        bean.validate();
        builder.generateValidationErrors(template, bean.getValidationErrors(), bean.getValidatedSubjects(), "prefix_");
        assertEquals(TemplateFactory.HTML.get("validationbuilder_errors_decoratedmessages_prefix_out1").getContent(), template.getContent());

        bean.resetValidation();
        bean.validate();
        bean.makeSubjectValid("anotherlogin");
        bean.makeSubjectValid("anotherpassword");
        builder.generateValidationErrors(template, bean.getValidationErrors(), bean.getValidatedSubjects(), "prefix_");
        assertEquals(TemplateFactory.HTML.get("validationbuilder_errors_decoratedmessages_prefix_out2").getContent(), template.getContent());

        bean.resetValidation();
        bean.validate();
        bean.makeSubjectValid("login");
        bean.makeSubjectValid("customquestion");
        builder.generateValidationErrors(template, bean.getValidationErrors(), bean.getValidatedSubjects(), "prefix_");
        assertEquals(TemplateFactory.HTML.get("validationbuilder_errors_decoratedmessages_prefix_out3").getContent(), template.getContent());
    }

    @Test
    public void testRemoveValidationErrorsInvalidArguments() {
        ValidationBuilderHtml builder = new ValidationBuilderHtml();
        builder.removeValidationErrors(null, null, null);

        Template template = TemplateFactory.HTML.get("validationbuilder_errors_raw");
        ConstrainedBeanImpl bean = new ConstrainedBeanImpl();
        bean.validate();
        bean.addValidationError(new ValidationError.WRONG_FORMAT("login"));
        builder.generateValidationErrors(template, bean.getValidationErrors(), bean.getValidatedSubjects(), null);
        String raw_content = template.getContent();
        builder.removeValidationErrors(template, null, null);
        assertEquals(raw_content, template.getContent());
        builder.removeValidationErrors(template, new ArrayList<String>(), null);
        assertEquals(raw_content, template.getContent());
    }

    @Test
    public void testRemoveValidationErrorsNovalues() {
        ValidationBuilderHtml builder = new ValidationBuilderHtml();

        Template template = TemplateFactory.HTML.get("validationbuilder_errors_novalues");

        ConstrainedBeanImpl bean = new ConstrainedBeanImpl();
        bean.validate();
        builder.generateValidationErrors(template, bean.getValidationErrors(), bean.getValidatedSubjects(), null);
        assertEquals(TemplateFactory.HTML.get("validationbuilder_errors_novalues").getContent(), template.getContent());
        builder.removeValidationErrors(template, bean.getValidatedSubjects(), null);
        assertEquals(TemplateFactory.HTML.get("validationbuilder_errors_novalues").getContent(), template.getContent());
    }

    @Test
    public void testRemoveValidationErrorsRaw() {
        ValidationBuilderHtml builder = new ValidationBuilderHtml();

        Template template = TemplateFactory.HTML.get("validationbuilder_errors_raw");

        ConstrainedBeanImpl bean = new ConstrainedBeanImpl();
        bean.validate();
        bean.addValidationError(new ValidationError.WRONG_FORMAT("login"));
        builder.generateValidationErrors(template, bean.getValidationErrors(), bean.getValidatedSubjects(), null);
        assertEquals(TemplateFactory.HTML.get("validationbuilder_errors_raw_out").getContent(), template.getContent());

        builder.removeValidationErrors(template, bean.getValidatedSubjects(), null);
        assertEquals(TemplateFactory.HTML.get("validationbuilder_errors_raw").getContent(), template.getContent());
    }

    @Test
    public void testRemoveValidationErrorsDecoratedmessages() {
        ValidationBuilderHtml builder = new ValidationBuilderHtml();

        Template template = TemplateFactory.HTML.get("validationbuilder_errors_decoratedmessages");

        ConstrainedBeanImpl bean = new ConstrainedBeanImpl();
        bean.validate();
        builder.generateValidationErrors(template, bean.getValidationErrors(), bean.getValidatedSubjects(), null);
        assertEquals(TemplateFactory.HTML.get("validationbuilder_errors_decoratedmessages_out1").getContent(), template.getContent());

        builder.removeValidationErrors(template, bean.getValidatedSubjects(), null);
        assertEquals(TemplateFactory.HTML.get("validationbuilder_errors_decoratedmessages").getContent(), template.getContent());
    }

    @Test
    public void testRemoveValidationErrorsDecoratedmessagesMissingSubjects() {
        ValidationBuilderHtml builder = new ValidationBuilderHtml();

        Template template = TemplateFactory.HTML.get("validationbuilder_errors_decoratedmessages");

        ConstrainedBeanImpl bean = new ConstrainedBeanImpl();
        bean.validate();
        builder.generateValidationErrors(template, bean.getValidationErrors(), bean.getValidatedSubjects(), null);
        assertEquals(TemplateFactory.HTML.get("validationbuilder_errors_decoratedmessages_out1").getContent(), template.getContent());

        List<String> subjects = bean.getValidatedSubjects();
        subjects.remove(0);
        subjects.remove(0);
        subjects.remove(0);
        subjects.remove(0);
        subjects.remove(0);
        builder.removeValidationErrors(template, subjects, null);
        assertEquals(TemplateFactory.HTML.get("validationbuilder_errors_decoratedmessages_partly_removed").getContent(), template.getContent());
    }

    @Test
    public void testRemoveValidationErrorsDecoratedmessagesPrefix() {
        ValidationBuilderHtml builder = new ValidationBuilderHtml();

        Template template = TemplateFactory.HTML.get("validationbuilder_errors_decoratedmessages_prefix");

        ConstrainedBeanImpl bean = new ConstrainedBeanImpl();
        bean.validate();
        builder.generateValidationErrors(template, bean.getValidationErrors(), bean.getValidatedSubjects(), "prefix_");
        assertEquals(TemplateFactory.HTML.get("validationbuilder_errors_decoratedmessages_prefix_out1").getContent(), template.getContent());

        builder.removeValidationErrors(template, bean.getValidatedSubjects(), "prefix_");
        assertEquals(TemplateFactory.HTML.get("validationbuilder_errors_decoratedmessages_prefix").getContent(), template.getContent());
    }

    @Test
    public void testGenerateErrorMarkingsInvalidArguments() {
        ValidationBuilderHtml builder = new ValidationBuilderHtml();
        assertEquals(0, builder.generateErrorMarkings(null, null, null, null).size());

        Template template = TemplateFactory.HTML.get("validationbuilder_mark_simple");
        String raw_content = template.getContent();
        assertNotNull(template);
        assertEquals(0, builder.generateErrorMarkings(template, null, null, null).size());
        assertEquals(raw_content, template.getContent());
        assertEquals(0, builder.generateErrorMarkings(template, new ArrayList<ValidationError>(), null, null).size());
        assertEquals(raw_content, template.getContent());
    }

    @Test
    public void testGenerateErrorMarkingsNoValues() {
        ValidationBuilderHtml builder = new ValidationBuilderHtml();

        Template template = TemplateFactory.HTML.get("validationbuilder_errors_novalues");

        ConstrainedBeanImpl bean = new ConstrainedBeanImpl();
        bean.validate();
        builder.generateErrorMarkings(template, bean.getValidationErrors(), bean.getValidatedSubjects(), null);
        assertEquals(TemplateFactory.HTML.get("validationbuilder_errors_novalues").getContent(), template.getContent());
    }

    @Test
    public void testGenerateErrorMarkingsMising() {
        ValidationBuilderHtml builder = new ValidationBuilderHtml();

        Template template = TemplateFactory.HTML.get("validationbuilder_mark_missing");

        ConstrainedBeanImpl bean = new ConstrainedBeanImpl();
        bean.validate();
        try {
            builder.generateErrorMarkings(template, bean.getValidationErrors(), bean.getValidatedSubjects(), null);
            fail("exception not thrown");
        } catch (MissingMarkingBlockException e) {
            assertEquals("mark:error", e.getBlockId());
        }
    }

    @Test
    public void testGenerateErrorMarkingsSimple() {
        ValidationBuilderHtml builder = new ValidationBuilderHtml();

        Template template = TemplateFactory.HTML.get("validationbuilder_mark_simple");

        ConstrainedBeanImpl bean = new ConstrainedBeanImpl();
        bean.validate();
        builder.generateErrorMarkings(template, bean.getValidationErrors(), bean.getValidatedSubjects(), null);
        assertEquals(TemplateFactory.HTML.get("validationbuilder_mark_simple_out").getContent(), template.getContent());
    }

    @Test
    public void testGenerateErrorMarkingsPositioned() {
        ValidationBuilderHtml builder = new ValidationBuilderHtml();

        Template template = TemplateFactory.HTML.get("validationbuilder_mark_positioned");

        ConstrainedBeanImpl bean = new ConstrainedBeanImpl();
        bean.validate();
        builder.generateErrorMarkings(template, bean.getValidationErrors(), bean.getValidatedSubjects(), null);
        assertEquals(TemplateFactory.HTML.get("validationbuilder_mark_positioned_out1").getContent(), template.getContent());

        bean.resetValidation();
        bean.validate();
        bean.makeSubjectValid("anotherlogin");
        bean.makeSubjectValid("anotherpassword");
        bean.makeSubjectValid("anothercustomquestion");
        bean.addValidationError(new ValidationError.INCOMPLETE("customoptions"));
        builder.generateErrorMarkings(template, bean.getValidationErrors(), bean.getValidatedSubjects(), null);
        assertEquals(TemplateFactory.HTML.get("validationbuilder_mark_positioned_out2").getContent(), template.getContent());

        bean.resetValidation();
        bean.validate();
        bean.makeSubjectValid("login");
        bean.makeSubjectValid("customquestion");
        bean.makeSubjectValid("options");
        bean.addValidationError(new ValidationError.INCOMPLETE("customoptions"));
        builder.generateErrorMarkings(template, bean.getValidationErrors(), bean.getValidatedSubjects(), null);
        assertEquals(TemplateFactory.HTML.get("validationbuilder_mark_positioned_out3").getContent(), template.getContent());
    }

    @Test
    public void testGenerateErrorMarkingsSelective() {
        ValidationBuilderHtml builder = new ValidationBuilderHtml();

        Template template = TemplateFactory.HTML.get("validationbuilder_mark_selective");

        ConstrainedBeanImpl bean = new ConstrainedBeanImpl();
        bean.validate();
        builder.generateErrorMarkings(template, bean.getValidationErrors(), bean.getValidatedSubjects(), null);
        assertEquals(TemplateFactory.HTML.get("validationbuilder_mark_selective_out1").getContent(), template.getContent());

        bean.resetValidation();
        bean.validate();
        bean.makeSubjectValid("anotherlogin");
        bean.makeSubjectValid("anotherpassword");
        bean.makeSubjectValid("anothercustomquestion");
        bean.addValidationError(new ValidationError.INCOMPLETE("customoptions"));
        builder.generateErrorMarkings(template, bean.getValidationErrors(), bean.getValidatedSubjects(), null);
        assertEquals(TemplateFactory.HTML.get("validationbuilder_mark_selective_out2").getContent(), template.getContent());

        bean.resetValidation();
        bean.validate();
        bean.makeSubjectValid("login");
        bean.makeSubjectValid("customquestion");
        bean.makeSubjectValid("options");
        bean.addValidationError(new ValidationError.INCOMPLETE("customoptions"));
        builder.generateErrorMarkings(template, bean.getValidationErrors(), bean.getValidatedSubjects(), null);
        assertEquals(TemplateFactory.HTML.get("validationbuilder_mark_selective_out3").getContent(), template.getContent());
    }

    @Test
    public void testGenerateErrorMarkingsSimplePrefix() {
        ValidationBuilderHtml builder = new ValidationBuilderHtml();

        Template template = TemplateFactory.HTML.get("validationbuilder_mark_simple_prefix");

        ConstrainedBeanImpl bean = new ConstrainedBeanImpl();
        bean.validate();
        builder.generateErrorMarkings(template, bean.getValidationErrors(), bean.getValidatedSubjects(), "prefix_");
        assertEquals(TemplateFactory.HTML.get("validationbuilder_mark_simple_out").getContent(), template.getContent());
    }

    @Test
    public void testGenerateErrorMarkingsPositionedPrefix() {
        ValidationBuilderHtml builder = new ValidationBuilderHtml();

        Template template = TemplateFactory.HTML.get("validationbuilder_mark_positioned_prefix");

        ConstrainedBeanImpl bean = new ConstrainedBeanImpl();
        bean.validate();
        builder.generateErrorMarkings(template, bean.getValidationErrors(), bean.getValidatedSubjects(), "prefix_");
        assertEquals(TemplateFactory.HTML.get("validationbuilder_mark_positioned_out1").getContent(), template.getContent());

        bean.resetValidation();
        bean.validate();
        bean.makeSubjectValid("anotherlogin");
        bean.makeSubjectValid("anotherpassword");
        bean.makeSubjectValid("anothercustomquestion");
        bean.addValidationError(new ValidationError.INCOMPLETE("customoptions"));
        builder.generateErrorMarkings(template, bean.getValidationErrors(), bean.getValidatedSubjects(), "prefix_");
        assertEquals(TemplateFactory.HTML.get("validationbuilder_mark_positioned_out2").getContent(), template.getContent());

        bean.resetValidation();
        bean.validate();
        bean.makeSubjectValid("login");
        bean.makeSubjectValid("customquestion");
        bean.makeSubjectValid("options");
        bean.addValidationError(new ValidationError.INCOMPLETE("customoptions"));
        builder.generateErrorMarkings(template, bean.getValidationErrors(), bean.getValidatedSubjects(), "prefix_");
        assertEquals(TemplateFactory.HTML.get("validationbuilder_mark_positioned_out3").getContent(), template.getContent());
    }

    @Test
    public void testGenerateErrorMarkingsSelectivePrefix() {
        ValidationBuilderHtml builder = new ValidationBuilderHtml();

        Template template = TemplateFactory.HTML.get("validationbuilder_mark_selective_prefix");

        ConstrainedBeanImpl bean = new ConstrainedBeanImpl();
        bean.validate();
        builder.generateErrorMarkings(template, bean.getValidationErrors(), bean.getValidatedSubjects(), "prefix_");
        assertEquals(TemplateFactory.HTML.get("validationbuilder_mark_selective_out1").getContent(), template.getContent());

        bean.resetValidation();
        bean.validate();
        bean.makeSubjectValid("anotherlogin");
        bean.makeSubjectValid("anotherpassword");
        bean.makeSubjectValid("anothercustomquestion");
        bean.addValidationError(new ValidationError.INCOMPLETE("customoptions"));
        builder.generateErrorMarkings(template, bean.getValidationErrors(), bean.getValidatedSubjects(), "prefix_");
        assertEquals(TemplateFactory.HTML.get("validationbuilder_mark_selective_out2").getContent(), template.getContent());

        bean.resetValidation();
        bean.validate();
        bean.makeSubjectValid("login");
        bean.makeSubjectValid("customquestion");
        bean.makeSubjectValid("options");
        bean.addValidationError(new ValidationError.INCOMPLETE("customoptions"));
        builder.generateErrorMarkings(template, bean.getValidationErrors(), bean.getValidatedSubjects(), "prefix_");
        assertEquals(TemplateFactory.HTML.get("validationbuilder_mark_selective_out3").getContent(), template.getContent());
    }

    @Test
    public void testRemoveErrorMarkingsInvalidArguments() {
        ValidationBuilderHtml builder = new ValidationBuilderHtml();
        builder.removeErrorMarkings(null, null, null);

        Template template = TemplateFactory.HTML.get("validationbuilder_mark_simple");
        ConstrainedBeanImpl bean = new ConstrainedBeanImpl();
        bean.validate();
        builder.generateErrorMarkings(template, bean.getValidationErrors(), bean.getValidatedSubjects(), null);
        assertEquals(TemplateFactory.HTML.get("validationbuilder_mark_simple_out").getContent(), template.getContent());
        String raw_content = template.getContent();
        builder.removeErrorMarkings(template, null, null);
        assertEquals(raw_content, template.getContent());
        builder.removeErrorMarkings(template, new ArrayList<String>(), null);
        assertEquals(raw_content, template.getContent());
    }

    @Test
    public void testRemoveErrorMarkingsNoValues() {
        ValidationBuilderHtml builder = new ValidationBuilderHtml();

        Template template = TemplateFactory.HTML.get("validationbuilder_errors_novalues");

        ConstrainedBeanImpl bean = new ConstrainedBeanImpl();
        bean.validate();
        builder.generateErrorMarkings(template, bean.getValidationErrors(), bean.getValidatedSubjects(), null);
        assertEquals(TemplateFactory.HTML.get("validationbuilder_errors_novalues").getContent(), template.getContent());
        builder.removeErrorMarkings(template, bean.getValidatedSubjects(), null);
        assertEquals(TemplateFactory.HTML.get("validationbuilder_errors_novalues").getContent(), template.getContent());
    }

    @Test
    public void testRemoveErrorMarkingsPositioned() {
        ValidationBuilderHtml builder = new ValidationBuilderHtml();

        Template template = TemplateFactory.HTML.get("validationbuilder_mark_positioned");

        ConstrainedBeanImpl bean = new ConstrainedBeanImpl();
        bean.validate();
        builder.generateErrorMarkings(template, bean.getValidationErrors(), bean.getValidatedSubjects(), null);
        assertEquals(TemplateFactory.HTML.get("validationbuilder_mark_positioned_out1").getContent(), template.getContent());
        builder.removeErrorMarkings(template, bean.getValidatedSubjects(), null);
        assertEquals(TemplateFactory.HTML.get("validationbuilder_mark_positioned").getContent(), template.getContent());
    }

    @Test
    public void testRemoveErrorMarkingsPositionedPrefix() {
        ValidationBuilderHtml builder = new ValidationBuilderHtml();

        Template template = TemplateFactory.HTML.get("validationbuilder_mark_positioned_prefix");

        ConstrainedBeanImpl bean = new ConstrainedBeanImpl();
        bean.validate();
        builder.generateErrorMarkings(template, bean.getValidationErrors(), bean.getValidatedSubjects(), "prefix_");
        assertEquals(TemplateFactory.HTML.get("validationbuilder_mark_positioned_out1").getContent(), template.getContent());
        builder.removeErrorMarkings(template, bean.getValidatedSubjects(), "prefix_");
        assertEquals(TemplateFactory.HTML.get("validationbuilder_mark_positioned_prefix").getContent(), template.getContent());
    }

    @Test
    public void testRemoveErrorMarkingsPositionedMissingSubjects() {
        ValidationBuilderHtml builder = new ValidationBuilderHtml();

        Template template = TemplateFactory.HTML.get("validationbuilder_mark_positioned");

        ConstrainedBeanImpl bean = new ConstrainedBeanImpl();
        bean.validate();
        builder.generateErrorMarkings(template, bean.getValidationErrors(), bean.getValidatedSubjects(), null);
        assertEquals(TemplateFactory.HTML.get("validationbuilder_mark_positioned_out1").getContent(), template.getContent());

        List<String> subjects = bean.getValidatedSubjects();
        subjects.remove(0);
        subjects.remove(0);
        subjects.remove(0);
        subjects.remove(0);
        subjects.remove(0);
        builder.removeErrorMarkings(template, bean.getValidatedSubjects(), null);
        assertEquals(TemplateFactory.HTML.get("validationbuilder_mark_positioned_partly_removed").getContent(), template.getContent());
    }
}
