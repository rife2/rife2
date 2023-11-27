/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.authentication.elements.Identified;
import rife.engine.exceptions.EngineException;
import rife.template.Template;
import rife.template.TemplateEncoder;
import rife.template.TemplateFactoryFilters;
import rife.template.exceptions.TemplateException;

import java.util.ArrayList;
import java.util.List;

class EngineTemplateProcessor {
    public static final String ID_WEBAPP_ROOT_URL = "webapp:rootUrl";
    public static final String ID_SERVER_ROOT_URL = "server:rootUrl";
    public static final String ID_CONTEXT_PATH_INFO = "context:pathInfo";
    public static final String ID_CONTEXT_PARAM_RANDOM = "context:paramRandom";
    public static final String ID_CONTEXT_PARAM_CONT_ID = "context:paramContId";
    public static final String ID_CONTEXT_CONT_ID = "context:contId";

    private final Context context_;
    private final Template template_;
    private final TemplateEncoder encoder_;

    EngineTemplateProcessor(final Context context, final Template template) {
        context_ = context;
        template_ = template;
        encoder_ = template.getEncoder();
    }

    List<String> processTemplate()
    throws TemplateException, EngineException {
        final var set_values = new ArrayList<String>();

        processApplicationTags(set_values);
        processParameters(set_values);
        processProperties(set_values);
        processConfig(set_values);
        processAttributes(set_values);
        processCookies(set_values);
        processRoutes(set_values);
        processAuthentication(set_values);

        template_.addGeneratedValues(set_values);

        return set_values;
    }

    private void processApplicationTags(final List<String> setValues) {
        if (template_.hasValueId(ID_WEBAPP_ROOT_URL) &&
            !template_.isValueSet(ID_WEBAPP_ROOT_URL)) {
            template_.setValue(ID_WEBAPP_ROOT_URL, context_.webappRootUrl(-1));
            setValues.add(ID_WEBAPP_ROOT_URL);
        }

        if (template_.hasValueId(ID_SERVER_ROOT_URL) &&
            !template_.isValueSet(ID_SERVER_ROOT_URL)) {
            template_.setValue(ID_SERVER_ROOT_URL, context_.serverRootUrl(-1));
            setValues.add(ID_SERVER_ROOT_URL);
        }

        if (template_.hasValueId(ID_CONTEXT_PATH_INFO) &&
            !template_.isValueSet(ID_CONTEXT_PATH_INFO)) {
            var path_info = context_.pathInfo();
            if (!path_info.isEmpty()) {
                path_info = "/" + path_info;
            }
            template_.setValue(ID_CONTEXT_PATH_INFO, path_info);
            setValues.add(ID_CONTEXT_PATH_INFO);
        }

        if (template_.hasValueId(ID_CONTEXT_PARAM_RANDOM) &&
            !template_.isValueSet(ID_CONTEXT_PARAM_RANDOM)) {
            template_.setValue(ID_CONTEXT_PARAM_RANDOM, SpecialParameters.RND + "=" + context_.site().RND);
            setValues.add(ID_CONTEXT_PARAM_RANDOM);
        }

        if (template_.hasValueId(ID_CONTEXT_PARAM_CONT_ID) &&
            !template_.isValueSet(ID_CONTEXT_PARAM_CONT_ID)) {
            if (context_.continuationId() != null) {
                template_.setValue(ID_CONTEXT_PARAM_CONT_ID, SpecialParameters.CONT_ID + "=" + context_.continuationId());
            }
            setValues.add(ID_CONTEXT_PARAM_CONT_ID);
        }

        if (template_.hasValueId(ID_CONTEXT_CONT_ID) &&
            !template_.isValueSet(ID_CONTEXT_CONT_ID)) {
            if (context_.continuationId() != null) {
                template_.setValue(ID_CONTEXT_CONT_ID, context_.continuationId());
            }
            setValues.add(ID_CONTEXT_CONT_ID);
        }
    }

    private void processParameters(final List<String> setValues) {
        var parameters = context_.parameters();
        final var param_tags = template_.getFilteredValues(TemplateFactoryFilters.TAG_PARAM);
        if (param_tags != null) {
            for (var captured_groups : param_tags) {
                var param_value_id = captured_groups[0];
                if (!template_.isValueSet(param_value_id)) {
                    var param_name = captured_groups[1];
                    if (parameters.containsKey(param_name)) {
                        var param_values = parameters.get(param_name);
                        template_.setValue(param_value_id, encoder_.encode(param_values[0]));
                        setValues.add(param_value_id);
                    }
                }
            }
        }
    }

    private void processProperties(final List<String> setValues) {
        var properties = context_.properties();
        final var prop_tags = template_.getFilteredValues(TemplateFactoryFilters.TAG_PROPERTY);
        if (prop_tags != null) {
            for (var captured_groups : prop_tags) {
                var prop_value_id = captured_groups[0];
                if (!template_.isValueSet(prop_value_id)) {
                    var prop_name = captured_groups[1];
                    if (properties.contains(prop_name)) {
                        var prop_value = properties.getValueString(prop_name);
                        template_.setValue(prop_value_id, encoder_.encode(prop_value));
                        setValues.add(prop_value_id);
                    }
                }
            }
        }
    }

    private void processConfig(final List<String> setValues) {
        var config = context_.site().config();
        final var config_tags = template_.getFilteredValues(TemplateFactoryFilters.TAG_CONFIG);
        if (config_tags != null) {
            for (var captured_groups : config_tags) {
                var config_value_id = captured_groups[0];
                if (!template_.isValueSet(config_value_id)) {
                    var param_name = captured_groups[1];
                    var param_value = config.getString(param_name);
                    if (param_value != null) {
                        template_.setValue(config_value_id, encoder_.encode(param_value));
                        setValues.add(config_value_id);
                    }
                }
            }
        }
    }

    private void processAttributes(final List<String> setValues) {
        var attribute_names = context_.attributeNames();
        final var attr_tags = template_.getFilteredValues(TemplateFactoryFilters.TAG_ATTRIBUTE);
        if (attr_tags != null) {
            for (var captured_groups : attr_tags) {
                var attr_value_id = captured_groups[0];
                if (!template_.isValueSet(attr_value_id)) {
                    var attr_name = captured_groups[1];
                    String attr_value = null;
                    if (template_.hasAttribute(attr_name)) {
                        attr_value = String.valueOf(template_.getAttribute(attr_name));
                    }
                    else if (attribute_names.contains(attr_name)) {
                        attr_value = String.valueOf(context_.attribute(attr_name));
                    }
                    if (attr_value != null) {
                        template_.setValue(attr_value_id, encoder_.encode(attr_value));
                        setValues.add(attr_value_id);
                    }
                }
            }
        }
    }

    private void processCookies(final List<String> setValues) {
        final var cookie_tags = template_.getFilteredValues(TemplateFactoryFilters.TAG_COOKIE);
        if (cookie_tags != null) {
            for (var captured_groups : cookie_tags) {
                var cookie_value_id = captured_groups[0];
                if (!template_.isValueSet(cookie_value_id)) {
                    var cookie_name = captured_groups[1];
                    if (context_.hasCookie(cookie_name)) {
                        template_.setValue(cookie_value_id, encoder_.encode(context_.cookieValue(cookie_name)));
                        setValues.add(cookie_value_id);
                    }
                }
            }
        }
    }

    private void processRoutes(final List<String> setValues) {
        final var route_tags = template_.getFilteredValues(TemplateFactoryFilters.TAG_ROUTE);
        if (route_tags != null) {
            for (var captured_groups : route_tags) {
                var route_value_id = captured_groups[0];
                if (!template_.isValueSet(route_value_id)) {
                    var route = resolveRoute(captured_groups[1]);
                    if (route != null) {
                        var route_value = context_.urlFor(route);
                        template_.setValue(route_value_id, route_value);
                        setValues.add(route_value_id);
                    }
                }
            }
        }

        final var route_action_tags = template_.getFilteredValues(TemplateFactoryFilters.TAG_ROUTE_ACTION);
        if (route_action_tags != null) {
            for (var captured_groups : route_action_tags) {
                var route_value_id = captured_groups[0];
                if (!template_.isValueSet(route_value_id)) {
                    var route = resolveRoute(captured_groups[1]);
                    if (route != null) {
                        var segments = context_.urlFor(route).generateSegments();
                        template_.setValue(route_value_id, segments.path() + segments.fragment());
                        setValues.add(route_value_id);
                    }
                }
            }
        }

        final var route_inputs_tags = template_.getFilteredValues(TemplateFactoryFilters.TAG_ROUTE_INPUTS);
        if (route_inputs_tags != null) {
            for (var captured_groups : route_inputs_tags) {
                var route_value_id = captured_groups[0];
                if (!template_.isValueSet(route_value_id)) {
                    var route = resolveRoute(captured_groups[1]);
                    if (route != null) {
                        var segments = context_.urlFor(route).generateSegments();
                        var builder = new StringBuilder();
                        segments.appendFormInputParameters(builder);
                        template_.setValue(route_value_id, builder.toString());
                        setValues.add(route_value_id);
                    }
                }
            }
        }
    }

    private Route resolveRoute(String path) {
        Route route;
        if (path.isEmpty()) {
            route = context_.route();
        } else {
            route = context_.route().router().resolveRoute(path);
        }
        return route;
    }

    private void processAuthentication(final List<String> setValues) {
        var identified = Identified.getIdentifiedElementInRequest(context_);
        if (identified == null) {
            return;
        }

        final var identity = identified.getAuthConfig().identityAttribute(context_);
        if (identity != null) {
            final var auth_value_tags = template_.getFilteredValues(TemplateFactoryFilters.TAG_AUTH);
            final var auth_block_tags = template_.getFilteredBlocks(TemplateFactoryFilters.TAG_AUTH);
            final var auth_role_block_tags = template_.getFilteredBlocks(TemplateFactoryFilters.TAG_AUTH_ROLE);
            final var auth_login_block_tags = template_.getFilteredBlocks(TemplateFactoryFilters.TAG_AUTH_LOGIN);
            if (auth_value_tags != null) {
                for (var captured_groups : auth_value_tags) {
                    var auth_value_id = captured_groups[0];
                    var auth_differentiator = captured_groups[1];

                    // handle authenticated login blocks assignment
                    if (!template_.isValueSet(auth_value_id)) {
                        for (var block_groups : auth_login_block_tags) {
                            var auth_block_id = block_groups[0];
                            if (block_groups[1].equals(auth_differentiator) &&
                                identity.getLogin().equals(block_groups[2])) {
                                template_.setBlock(auth_value_id, auth_block_id);
                                setValues.add(auth_value_id);
                            }
                        }
                    }

                    // handle authenticated role blocks assignment
                    if (!template_.isValueSet(auth_value_id)) {
                        for (var block_groups : auth_role_block_tags) {
                            var auth_block_id = block_groups[0];
                            if (block_groups[1].equals(auth_differentiator) &&
                                identity.getAttributes().isInRole(block_groups[2])) {
                                template_.setBlock(auth_value_id, auth_block_id);
                                setValues.add(auth_value_id);
                            }
                        }
                    }

                    // handle authenticated blocks assignment
                    if (!template_.isValueSet(auth_value_id)) {
                        for (var block_groups : auth_block_tags) {
                            var auth_block_id = block_groups[0];
                            if (block_groups[1].equals(auth_differentiator)) {
                                template_.setBlock(auth_value_id, auth_block_id);
                                setValues.add(auth_value_id);
                            }
                        }
                    }
                }
            }
        }
    }
}
