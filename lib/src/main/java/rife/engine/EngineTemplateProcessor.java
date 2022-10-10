/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.engine.exceptions.EngineException;
import rife.template.Template;
import rife.template.TemplateEncoder;
import rife.template.TemplateFactoryFilters;
import rife.template.exceptions.TemplateException;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class EngineTemplateProcessor {
    private final Context context_;
    private final Template template_;
    private final TemplateEncoder encoder_;

    EngineTemplateProcessor(final Context context, final Template template) {
        context_ = context;
        template_ = template;
        encoder_ = template.getEncoder();
    }

    synchronized List<String> processTemplate()
    throws TemplateException, EngineException {
        final var set_values = new ArrayList<String>();

        processApplicationTags(set_values);
        processParameters(set_values);
        processRoutes(set_values);

        return set_values;
    }

    private void processApplicationTags(final List<String> setValues) {
        if (template_.hasValueId(Context.ID_WEBAPP_ROOT_URL) &&
            !template_.isValueSet(Context.ID_WEBAPP_ROOT_URL)) {
            template_.setValue(Context.ID_WEBAPP_ROOT_URL, context_.getWebappRootUrl(-1));
            setValues.add(Context.ID_WEBAPP_ROOT_URL);
        }

        if (template_.hasValueId(Context.ID_SERVER_ROOT_URL) &&
            !template_.isValueSet(Context.ID_SERVER_ROOT_URL)) {
            template_.setValue(Context.ID_SERVER_ROOT_URL, context_.getServerRootUrl(-1));
            setValues.add(Context.ID_SERVER_ROOT_URL);
        }

        if (template_.hasValueId(Context.ID_CONTEXT_PATH_INFO) &&
            !template_.isValueSet(Context.ID_CONTEXT_PATH_INFO)) {
            template_.setValue(Context.ID_CONTEXT_PATH_INFO, context_.pathInfo());
            setValues.add(Context.ID_CONTEXT_PATH_INFO);
        }
    }

    private void processParameters(final List<String> setValues) {
        var parameters = context_.request().getParameters();
        final var param_tags = template_.getFilteredValues(TemplateFactoryFilters.TAG_PARAM);
        if (param_tags != null) {
            for (var captured_groups : param_tags) {
                var param_value_id = captured_groups[0];
                if (!template_.isValueSet(param_value_id)) {
                    var param_name = captured_groups[1];
                    if (parameters.containsKey(param_name)) {
                        String[] param_values = parameters.get(param_name);
                        template_.setValue(param_value_id, encoder_.encode(param_values[0]));
                        setValues.add(param_value_id);
                    }
                }
            }
        }
    }

    private void processRoutes(final List<String> setValues) {
        final var site = context_.site();

        final var route_tags = template_.getFilteredValues(TemplateFactoryFilters.TAG_ROUTE);
        if (route_tags != null) {
            for (var captured_groups : route_tags) {
                var route_value_id = captured_groups[0];
                if (!template_.isValueSet(route_value_id)) {
                    var route_name = captured_groups[1];

                    try {
                        var field = site.getClass().getDeclaredField(route_name);
                        field.setAccessible(true);

                        if (!Modifier.isStatic(field.getModifiers()) &&
                            !Modifier.isTransient(field.getModifiers()) &&
                            Route.class.isAssignableFrom(field.getType())) {
                            String route_value = context_.urlFor((Route) field.get(site));
                            template_.setValue(route_value_id, route_value);
                            setValues.add(route_value_id);
                        }
                    } catch (IllegalAccessException | NoSuchFieldException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}
