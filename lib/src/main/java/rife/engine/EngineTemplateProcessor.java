/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.authentication.elements.AuthConfig;
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
    public static final String ID_CONTEXT_CONT_ID = "context:contId";

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
        processCookies(set_values);
        processRoutes(set_values);
        processAuthentication(set_values);

        return set_values;
    }

    private void processApplicationTags(final List<String> setValues) {
        if (template_.hasValueId(ID_WEBAPP_ROOT_URL)) {
            template_.setValue(ID_WEBAPP_ROOT_URL, context_.webappRootUrl(-1));
            setValues.add(ID_WEBAPP_ROOT_URL);
        }

        if (template_.hasValueId(ID_SERVER_ROOT_URL)) {
            template_.setValue(ID_SERVER_ROOT_URL, context_.serverRootUrl(-1));
            setValues.add(ID_SERVER_ROOT_URL);
        }

        if (template_.hasValueId(ID_CONTEXT_PATH_INFO)) {
            var path_info = context_.pathInfo();
            if (!path_info.isEmpty()) {
                path_info = "/" + path_info;
            }
            template_.setValue(ID_CONTEXT_PATH_INFO, path_info);
            setValues.add(ID_CONTEXT_PATH_INFO);
        }

        if (template_.hasValueId(ID_CONTEXT_PARAM_RANDOM)) {
            template_.setValue(ID_CONTEXT_PARAM_RANDOM, SpecialParameters.RND + "=" + context_.site().RND);
            setValues.add(ID_CONTEXT_PARAM_RANDOM);
        }

        if (template_.hasValueId(ID_CONTEXT_CONT_ID)) {
            if (context_.continuationId() != null) {
                template_.setValue(ID_CONTEXT_CONT_ID, SpecialParameters.CONT_ID + "=" + context_.continuationId());
            }
            setValues.add(ID_CONTEXT_CONT_ID);
        }
    }

    private void processParameters(final List<String> setValues) {
        var parameters = context_.request().getParameters();
        final var param_tags = template_.getFilteredValues(TemplateFactoryFilters.TAG_PARAM);
        if (param_tags != null) {
            for (var captured_groups : param_tags) {
                var param_value_id = captured_groups[0];
                var param_name = captured_groups[1];
                if (parameters.containsKey(param_name)) {
                    String[] param_values = parameters.get(param_name);
                    template_.setValue(param_value_id, encoder_.encode(param_values[0]));
                    setValues.add(param_value_id);
                }
            }
        }
    }

    private void processCookies(final List<String> setValues) {
        final var cookie_tags = template_.getFilteredValues(TemplateFactoryFilters.TAG_COOKIE);
        if (cookie_tags != null) {
            for (var captured_groups : cookie_tags) {
                var cookie_value_id = captured_groups[0];
                var cookie_name = captured_groups[1];
                if (context_.hasCookie(cookie_name)) {
                    template_.setValue(cookie_value_id, encoder_.encode(context_.cookieValue(cookie_name)));
                    setValues.add(cookie_value_id);
                }
            }
        }
    }

    private void processRoutes(final List<String> setValues) {
        final var route_tags = template_.getFilteredValues(TemplateFactoryFilters.TAG_ROUTE);
        if (route_tags != null) {
            for (var captured_groups : route_tags) {
                var route_value_id = captured_groups[0];
                var path = captured_groups[1];
                Route route;
                if (path.isEmpty()) {
                    route = context_.route();
                } else {
                    route = context_.route().router().resolveRoute(path);
                }

                if (route != null) {
                    var route_value = context_.urlFor(route);
                    template_.setValue(route_value_id, route_value);
                    setValues.add(route_value_id);
                }
            }
        }
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
                    for (var block_groups : auth_login_block_tags) {
                        var auth_block_id = block_groups[0];
                        if (block_groups[1].equals(auth_differentiator) &&
                            identity.getLogin().equals(block_groups[2])) {
                            template_.setBlock(auth_value_id, auth_block_id);
                            setValues.add(auth_value_id);
                        }
                    }

                    // handle authenticated role blocks assignment
                    for (var block_groups : auth_role_block_tags) {
                        var auth_block_id = block_groups[0];
                        if (block_groups[1].equals(auth_differentiator) &&
                            identity.getAttributes().isInRole(block_groups[2])) {
                            template_.setBlock(auth_value_id, auth_block_id);
                            setValues.add(auth_value_id);
                        }
                    }

                    // handle authenticated blocks assignment
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
