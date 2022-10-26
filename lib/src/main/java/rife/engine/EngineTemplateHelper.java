/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.engine.exceptions.EngineException;
import rife.forms.FormBuilder;
import rife.template.BeanHandler;
import rife.template.Template;
import rife.tools.exceptions.BeanUtilsException;

import java.util.Collection;
import java.util.Collections;

class EngineTemplateHelper {
    // TODO : authentication
//	static void evaluateExpressionRoleUserTags(ElementContext context, List<String> setValues, Template template, String id)
//	{
//		if (template.hasFilteredBlocks(ElementContext.TAG_OGNL_ROLEUSER) ||
//			template.hasFilteredBlocks(ElementContext.TAG_MVEL_ROLEUSER) ||
//			template.hasFilteredBlocks(ElementContext.TAG_GROOVY_ROLEUSER) ||
//			template.hasFilteredBlocks(ElementContext.TAG_JANINO_ROLEUSER))
//		{
//			RoleUserIdentity identity = (RoleUserIdentity)context.getRequestState().getRequestAttribute(Identified.IDENTITY_ATTRIBUTE_NAME);
//
//			if (identity != null)
//			{
//				RoleUserAttributes attributes = identity.getAttributes();
//				HashMap<String, Object> map = new HashMap<String, Object>();
//				map.put("login", identity.getLogin());
//				map.put("password", attributes.getPassword());
//				map.put("userId", attributes.getUserId());
//				map.put("roles", attributes.getRoles());
//
//				if (template.hasFilteredBlocks(ElementContext.TAG_OGNL_ROLEUSER))
//				{
//					String language_id = id;
//					if (language_id != null)
//					{
//						language_id = ElementContext.PREFIX_OGNL_ROLEUSER + language_id;
//					}
//					FilteredTagProcessorOgnl.instance().processTags(setValues, template, template.getFilteredBlocks(ElementContext.TAG_OGNL_ROLEUSER), language_id, RoleUserAttributes.class, "user", attributes, map);
//				}
//
//				if (template.hasFilteredBlocks(ElementContext.TAG_MVEL_ROLEUSER))
//				{
//					String language_id = id;
//					if (language_id != null)
//					{
//						language_id = ElementContext.PREFIX_MVEL_ROLEUSER + language_id;
//					}
//					FilteredTagProcessorMvel.instance().processTags(setValues, template, template.getFilteredBlocks(ElementContext.TAG_MVEL_ROLEUSER), language_id, RoleUserAttributes.class, "user", attributes, map);
//				}
//
//				if (template.hasFilteredBlocks(ElementContext.TAG_GROOVY_ROLEUSER))
//				{
//					String language_id = id;
//					if (language_id != null)
//					{
//						language_id = ElementContext.PREFIX_GROOVY_ROLEUSER + language_id;
//					}
//					FilteredTagProcessorGroovy.instance().processTags(setValues, template, template.getFilteredBlocks(ElementContext.TAG_GROOVY_ROLEUSER), language_id, RoleUserAttributes.class, "user", attributes, map);
//				}
//
//				if (template.hasFilteredBlocks(ElementContext.TAG_JANINO_ROLEUSER))
//				{
//					String language_id = id;
//					if (language_id != null)
//					{
//						language_id = ElementContext.PREFIX_JANINO_ROLEUSER + language_id;
//					}
//					FilteredTagProcessorJanino.instance().processTags(setValues, template, template.getFilteredBlocks(ElementContext.TAG_JANINO_ROLEUSER), language_id, RoleUserAttributes.class, "user", attributes, map);
//				}
//			}
//		}
//	}
//
//	static void processEmbeddedElementsEarly(ElementContext context, Template template, ElementSupport embeddingElement)
//	{
//		// process the embedded elements
//		if (template.hasFilteredValues(ElementContext.TAG_ELEMENT))
//		{
//			List<String[]> element_tags = template.getFilteredValues(ElementContext.TAG_ELEMENT);
//			for (String[] captured_groups : element_tags)
//			{
//				// only embed the element if the value hasn't been set in the
//				// template yet and is declared as 'early'
//				if (!template.isValueSet(captured_groups[0]) &&
//					captured_groups[2].equals("-"))
//				{
//					context.processEmbeddedElement(captured_groups[0], template, embeddingElement, captured_groups[3], captured_groups[4], null);
//				}
//			}
//
//			for (String[] captured_groups : element_tags)
//			{
//				// only embed the element if the value hasn't been set in the
//				// template yet and has no specific priority
//				if (!template.isValueSet(captured_groups[0]) &&
//					0 == captured_groups[2].length())
//				{
//					context.processEmbeddedElement(captured_groups[0], template, embeddingElement, captured_groups[3], captured_groups[4], null);
//				}
//			}
//		}
//	}

    // TODO : will most likely be removed
//	static Collection<String> selectInputParameter(Template template, String name, String[] values)
//	{
//		return selectParameter(template, ElementContext.PREFIX_INPUT + name, values);
//	}
//
//	static Collection<String> selectOutputParameter(Template template, String name, String[] values)
//	{
//		return selectParameter(template, ElementContext.PREFIX_OUTPUT + name, values);
//	}
//
//	static void setExitQuery(ElementContext context, Template template, String name, String pathinfo, String[] outputValues)
//	throws TemplateException, EngineException
//	{
//		context.getElementInfo().validateExitName(name);
//
//		List<String> set_values = new FlowLinkFlowTagsProcessor(context, context.getOutputs().aggregateValues(), template, pathinfo, outputValues)
//			.focusOnName(name)
//			.onlyProcessQueryTags()
//			.replaceExistingValues()
//			.processTags();
//		if (0 == set_values.size())
//		{
//			throw new TemplateException("Couldn't find any query template values for exit '" + name + "' (" + ElementContext.PREFIX_EXIT_QUERY + ").");
//		}
//	}
//
//	static void setExitForm(ElementContext context, Template template, String name, String pathinfo, String[] outputValues)
//	throws TemplateException, EngineException
//	{
//		context.getElementInfo().validateExitName(name);
//
//		List<String> set_values = new FlowLinkFlowTagsProcessor(context, context.getOutputs().aggregateValues(), template, pathinfo, outputValues)
//			.focusOnName(name)
//			.onlyProcessFormTags()
//			.replaceExistingValues()
//			.processTags();
//		if (0 == set_values.size())
//		{
//			throw new TemplateException("Couldn't find any form template values for exit '" + name + "' (" + ElementContext.PREFIX_EXIT_FORM + ", " + ElementContext.PREFIX_EXIT_PARAMS + ", " + ElementContext.PREFIX_EXIT_PARAMSJS + ").");
//		}
//	}

    static Collection<String> selectParameter(Template template, String name, String[] values) {
        assert name != null;
        assert name.length() > 0;

        BeanHandler bean_handler = template.getBeanHandler();
        if (null == bean_handler) {
            return Collections.emptyList();
        }
        FormBuilder form_builder = bean_handler.getFormBuilder();
        if (null == form_builder) {
            return Collections.emptyList();
        }

        return form_builder.selectParameter(template, name, values);
    }

    static void generateForm(Template template, Object beanInstance, String prefix)
    throws EngineException {
        BeanHandler bean_handler = template.getBeanHandler();
        if (null == bean_handler) {
            return;
        }
        FormBuilder form_builder = bean_handler.getFormBuilder();
        if (null == form_builder) {
            return;
        }
        try {
            form_builder.removeForm(template, beanInstance.getClass(), prefix);
            form_builder.generateForm(template, beanInstance, null, prefix);
        } catch (BeanUtilsException e) {
            throw new EngineException(e);
        }
    }

    static void generateEmptyForm(Template template, Class beanClass, String prefix)
    throws EngineException {
        BeanHandler bean_handler = template.getBeanHandler();
        if (null == bean_handler) {
            return;
        }
        FormBuilder form_builder = bean_handler.getFormBuilder();
        if (null == form_builder) {
            return;
        }
        try {
            form_builder.removeForm(template, beanClass, prefix);
            form_builder.generateForm(template, beanClass, null, prefix);
        } catch (BeanUtilsException e) {
            throw new EngineException(e);
        }
    }

    static void removeForm(Template template, Class beanClass, String prefix)
    throws EngineException {
        BeanHandler bean_handler = template.getBeanHandler();
        if (null == bean_handler) {
            return;
        }
        FormBuilder form_builder = bean_handler.getFormBuilder();
        if (null == form_builder) {
            return;
        }
        try {
            form_builder.removeForm(template, beanClass, prefix);
        } catch (BeanUtilsException e) {
            throw new EngineException(e);
        }
    }

    // TODO : will most likely be removed
//	static void setSubmissionQuery(ElementContext context, Template template, String name, String pathinfo, String[] parameterValues)
//	throws TemplateException, EngineException
//	{
//		context.getElementInfo().validateSubmissionName(name);
//
//		List<String> set_values = new SubmissionFlowTagsProcessor(context, context.getOutputs().aggregateValues(), template, pathinfo, parameterValues)
//			.focusOnName(name)
//			.onlyProcessQueryTags()
//			.replaceExistingValues()
//			.processTags();
//		if (0 == set_values.size())
//		{
//			throw new TemplateException("Couldn't find any query template values for submission '" + name + "' (" + ElementContext.PREFIX_SUBMISSION_QUERY + ").");
//		}
//	}
//
//	static void setSubmissionForm(ElementContext context, Template template, String name, String pathinfo, String[] parameterValues)
//	throws TemplateException, EngineException
//	{
//		context.getElementInfo().validateSubmissionName(name);
//
//		List<String> set_values = new SubmissionFlowTagsProcessor(context, context.getOutputs().aggregateValues(), template, pathinfo, parameterValues)
//			.focusOnName(name)
//			.onlyProcessFormTags()
//			.replaceExistingValues()
//			.processTags();
//		if (0 == set_values.size())
//		{
//			throw new TemplateException("Couldn't find any form template values for submission '" + name + "' (" + ElementContext.PREFIX_SUBMISSION_FORM + ", " + ElementContext.PREFIX_SUBMISSION_PARAMS + ", " + ElementContext.PREFIX_SUBMISSION_PARAMSJS + ").");
//		}
//	}
//
//	static Collection<String> selectSubmissionParameter(Template template, String name, String[] values)
//	{
//		return selectParameter(template, ElementContext.PREFIX_PARAM + name, values);
//	}
//
//	static void setSubmissionBean(Template template, Object beanInstance, String prefix, boolean encode)
//	throws TemplateException, EngineException
//	{
//		if (null == prefix)
//		{
//			prefix = "";
//		}
//		template.setBean(beanInstance, ElementContext.PREFIX_PARAM+prefix, encode);
//	}
//
//	static void removeSubmissionBean(Template template, Object beanInstance)
//	throws TemplateException, EngineException
//	{
//		template.removeBean(beanInstance, ElementContext.PREFIX_PARAM);
//	}
//
//	static String getFlowEntityNameFromFieldTag(final ElementSupport element, final String tag, final String fieldName)
//	throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, ExpectedStringConstantFieldException
//	{
//		String name;
//		final String class_name;
//		final String class_fieldname;
//
//		// check if this field tag refers to the field of the current
//		// element
//		final int field_separator = fieldName.lastIndexOf('.');
//		if (-1 == field_separator)
//		{
//			class_fieldname = fieldName;
//			class_name = null;
//		}
//		else
//		{
//			class_fieldname = fieldName.substring(field_separator+1);
//			class_name = fieldName.substring(0, field_separator);
//		}
//
//		// find the class that the field belongs to
//		final Class element_class;
//		if (null == class_name)
//		{
//			element_class = element.getClass();
//		}
//		else
//		{
//			element_class = Class.forName(class_name);
//		}
//
//		// retrieve the field
//		final Field class_field = element_class.getDeclaredField(class_fieldname);
//
//		// ensure that the field is a string constant
//		if (!Modifier.isFinal(class_field.getModifiers()) ||
//			!Modifier.isStatic(class_field.getModifiers()) ||
//			!Modifier.isPublic(class_field.getModifiers()) ||
//			class_field.getType() != String.class)
//		{
//			throw new ExpectedStringConstantFieldException(class_name, class_fieldname);
//		}
//		else
//		{
//			name = (String)class_field.get(null);
//		}
//
//		return name;
//	}
}