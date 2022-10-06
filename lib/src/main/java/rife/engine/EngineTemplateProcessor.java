/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.engine.exceptions.EngineException;
import rife.template.Template;
import rife.template.TemplateEncoder;
import rife.template.exceptions.TemplateException;

import java.util.ArrayList;
import java.util.List;

public class EngineTemplateProcessor {
    private final Context mContext;
    private final Template mTemplate;
    private final TemplateEncoder mEncoder;

    EngineTemplateProcessor(final Context context, final Template template) {
        mContext = context;
        mTemplate = template;
        mEncoder = template.getEncoder();
    }

    synchronized List<String> processTemplate()
    throws TemplateException, EngineException {
        final var set_values = new ArrayList<String>();

        processApplicationTags(set_values);

        return set_values;
    }

    private void processApplicationTags(final List<String> setValues) {
        // set the webapp root
        if (mTemplate.hasValueId(Context.ID_WEBAPP_ROOT_URL) &&
            !mTemplate.isValueSet(Context.ID_WEBAPP_ROOT_URL)) {
            mTemplate.setValue(Context.ID_WEBAPP_ROOT_URL, mContext.getWebappRootUrl(-1));
            setValues.add(Context.ID_WEBAPP_ROOT_URL);
        }

        // set the server root
        if (mTemplate.hasValueId(Context.ID_SERVER_ROOT_URL) &&
            !mTemplate.isValueSet(Context.ID_SERVER_ROOT_URL)) {
            mTemplate.setValue(Context.ID_SERVER_ROOT_URL, mContext.getServerRootUrl(-1));
            setValues.add(Context.ID_SERVER_ROOT_URL);
        }
    }

}
