/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
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
}