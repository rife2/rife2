/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

import rife.config.RifeConfig;
import rife.datastructures.EnumClass;
import rife.resources.ResourceFinder;
import rife.resources.ResourceFinderClasspath;
import rife.template.exceptions.InvalidBlockFilterException;
import rife.template.exceptions.InvalidValueFilterException;
import rife.template.exceptions.TemplateException;
import rife.template.exceptions.TemplateNotFoundException;
import rife.tools.Localization;
import rife.validation.ValidationBuilder;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class TemplateFactory extends EnumClass<String> {
    public static TemplateFactory HTML = new TemplateFactory(TemplateConfig.XML,
        ResourceFinderClasspath.instance(),
        "html", "text/html", ".html",
        new String[]
            {
                ValidationBuilder.TAG_ERRORS,
                ValidationBuilder.TAG_ERRORMESSAGE,
                TemplateFactoryFilters.TAG_LANG
            },
        new String[]
            {
                ValidationBuilder.TAG_MARK,
                ValidationBuilder.TAG_ERRORS,
                TemplateFactoryFilters.TAG_CONFIG,
                TemplateFactoryFilters.TAG_L10N,
                TemplateFactoryFilters.TAG_PARAM,
                TemplateFactoryFilters.TAG_RENDER,
                TemplateFactoryFilters.TAG_ROUTE
            },
        BeanHandlerHtml.instance(),
        EncoderHtmlSingleton.INSTANCE,
        null);

    public static TemplateFactory XML = new TemplateFactory(TemplateConfig.XML,
        ResourceFinderClasspath.instance(),
        "xml", "application/xml", ".xml",
        new String[]
            {
                TemplateFactoryFilters.TAG_LANG
            },
        new String[]
            {
                TemplateFactoryFilters.TAG_CONFIG,
                TemplateFactoryFilters.TAG_L10N,
                TemplateFactoryFilters.TAG_RENDER
            },
        BeanHandlerXml.instance(),
        EncoderXmlSingleton.INSTANCE,
        null);

    public static TemplateFactory TXT = new TemplateFactory(TemplateConfig.TXT,
        ResourceFinderClasspath.instance(),
        "txt", "text/plain", ".txt",
        new String[]
            {
                TemplateFactoryFilters.TAG_LANG
            },
        new String[]
            {
                TemplateFactoryFilters.TAG_CONFIG,
                TemplateFactoryFilters.TAG_L10N,
                TemplateFactoryFilters.TAG_RENDER
            },
        BeanHandlerPlain.instance(),
        null,
        null);

    public static TemplateFactory SQL = new TemplateFactory(TemplateConfig.TXT,
        ResourceFinderClasspath.instance(),
        "sql", "text/plain", ".sql",
        new String[]
            {
                TemplateFactoryFilters.TAG_LANG
            },
        new String[]
            {
                TemplateFactoryFilters.TAG_CONFIG,
                TemplateFactoryFilters.TAG_L10N,
                TemplateFactoryFilters.TAG_RENDER
            },
        BeanHandlerPlain.instance(),
        EncoderHtmlSingleton.INSTANCE,
        null);

    public static TemplateFactory JSON = new TemplateFactory(TemplateConfig.TXT,
        ResourceFinderClasspath.instance(),
        "json", "application/json", ".json",
        new String[]
            {
                TemplateFactoryFilters.TAG_LANG
            },
        new String[]
            {
                TemplateFactoryFilters.TAG_CONFIG,
                TemplateFactoryFilters.TAG_L10N,
                TemplateFactoryFilters.TAG_RENDER
            },
        BeanHandlerPlain.instance(),
        EncoderJsonSingleton.INSTANCE,
        null);

    private final TemplateConfig config_;
    private ResourceFinder resourceFinder_;
    private final String defaultContentType_;
    private BeanHandler beanHandler_;
    private TemplateEncoder encoder_;
    private TemplateInitializer initializer_;

    private final Parser parser_;
    private final String identifierUppercase_;

    private TemplateClassLoader lastClassloader_ = null;

    TemplateFactory(TemplateConfig config, ResourceFinder resourceFinder, String identifier,
                    String defaultContentType,
                    String extension, String[] blockFilters, String[] valueFilters,
                    BeanHandler beanHandler,
                    TemplateEncoder encoder,
                    TemplateInitializer initializer) {
        super(TemplateFactory.class, identifier);

        Pattern[] block_filter_patterns = null;
        try {
            block_filter_patterns = compileFilters(blockFilters);
        } catch (PatternSyntaxException e) {
            throw new InvalidBlockFilterException(e.getPattern());
        }

        Pattern[] value_filter_patterns = null;
        try {
            value_filter_patterns = compileFilters(valueFilters);
        } catch (PatternSyntaxException e) {
            throw new InvalidValueFilterException(e.getPattern());
        }

        config_ = config;
        identifierUppercase_ = identifier.toUpperCase();
        parser_ = new Parser(this, identifier, extension, block_filter_patterns, value_filter_patterns);
        resourceFinder_ = resourceFinder;
        beanHandler_ = beanHandler;
        encoder_ = encoder;
        initializer_ = initializer;
        defaultContentType_ = defaultContentType;
    }

    private Pattern[] compileFilters(String[] filters)
    throws PatternSyntaxException {
        if (filters != null) {
            var patterns = new Pattern[filters.length];

            for (var i = 0; i < filters.length; i++) {
                patterns[i] = Pattern.compile(filters[i]);
            }

            return patterns;
        }

        return null;
    }

    public TemplateFactory(TemplateConfig config, String identifier, TemplateFactory base) {
        super(TemplateFactory.class, identifier);

        config_ = config;
        identifierUppercase_ = identifier.toUpperCase();
        parser_ = new Parser(this, identifier, base.getParser().getExtension(), base.getParser().getBlockFilters(), base.getParser().getValueFilters());
        resourceFinder_ = base.getResourceFinder();
        beanHandler_ = base.getBeanHandler();
        encoder_ = base.getEncoder();
        initializer_ = base.getInitializer();
        defaultContentType_ = base.getDefaultContentType();
    }

    public String getIdentifierUppercase() {
        return identifierUppercase_;
    }

    public String getDefaultContentType() {
        return defaultContentType_;
    }

    public static Collection<String> getFactoryTypes() {
        return (Collection<String>) getIdentifiers(TemplateFactory.class);
    }

    public static TemplateFactory getFactory(String identifier) {
        return getMember(TemplateFactory.class, identifier);
    }

    public Template get(String name)
    throws TemplateException {
        return get(name, null, null);
    }

    public Template get(String name, TemplateTransformer transformer)
    throws TemplateException {
        return get(name, null, transformer);
    }

    public Template get(String name, String encoding)
    throws TemplateException {
        return get(name, encoding, null);
    }

    public Template get(String name, String encoding, TemplateTransformer transformer)
    throws TemplateException {
        if (null == name) throw new IllegalArgumentException("name can't be null.");
        if (name.isEmpty()) throw new IllegalArgumentException("name can't be empty.");

        try {
            var template = (AbstractTemplate) parse(name, encoding, transformer).getDeclaredConstructor().newInstance();
            template.setBeanHandler(beanHandler_);
            template.setEncoder(encoder_);
            template.setInitializer(initializer_);
            template.setDefaultContentType(defaultContentType_);

            var default_resource_bundles = RifeConfig.template().getDefaultResourceBundles(this);
            if (default_resource_bundles != null) {
                var default_bundles = new ArrayList<ResourceBundle>();
                for (var bundle_name : default_resource_bundles) {
                    // try to look it up as a filename in the classpath
                    var bundle = Localization.getResourceBundle(bundle_name);
                    if (bundle != null) {
                        default_bundles.add(bundle);
                        continue;
                    }
                }
                template.setDefaultResourceBundles(default_bundles);
            }

            template.initialize();

            return template;
        } catch (IllegalAccessException e) {
            // this should not happen
            throw new TemplateException("TemplateFactory.get() : '" + name + "' IllegalAccessException : " + e.getMessage(), e);
        } catch (InstantiationException e) {
            // this should not happen
            throw new TemplateException("TemplateFactory.get() : '" + name + "' InstantiationException : " + e.getMessage(), e);
        } catch (InvocationTargetException e) {
            // this should not happen
            throw new TemplateException("TemplateFactory.get() : '" + name + "' InvocationTargetException : " + e.getMessage(), e);
        } catch (NoSuchMethodException e) {
            // this should not happen
            throw new TemplateException("TemplateFactory.get() : '" + name + "' NoSuchMethodException : " + e.getMessage(), e);
        }
    }

    public Class parse(String name, String encoding, TemplateTransformer transformer)
    throws TemplateException {
        if (null == name) throw new IllegalArgumentException("name can't be null.");
        if (0 == name.length()) throw new IllegalArgumentException("name can't be empty.");

        try {
            return getClassLoader().loadClass(parser_.getPackage() + parser_.escapeClassname(name), false, encoding, transformer);
        } catch (ClassNotFoundException e) {
            throw new TemplateNotFoundException(name, e);
        }
    }

    public TemplateConfig getConfig() {
        return config_;
    }

    public TemplateFactory setResourceFinder(ResourceFinder resourceFinder) {
        if (null == resourceFinder) throw new IllegalArgumentException("resourceFinder can't be null.");

        resourceFinder_ = resourceFinder;

        return this;
    }

    public ResourceFinder getResourceFinder() {
        assert resourceFinder_ != null;

        return resourceFinder_;
    }

    Parser getParser() {
        assert parser_ != null;

        return parser_;
    }

    public TemplateFactory setBeanHandler(BeanHandler beanHandler) {
        beanHandler_ = beanHandler;

        return this;
    }

    public BeanHandler getBeanHandler() {
        return beanHandler_;
    }

    public TemplateFactory setEncoder(TemplateEncoder encoder) {
        encoder_ = encoder;

        return this;
    }

    public TemplateEncoder getEncoder() {
        return encoder_;
    }

    public TemplateFactory setInitializer(TemplateInitializer initializer) {
        initializer_ = initializer;

        return this;
    }

    public TemplateInitializer getInitializer() {
        return initializer_;
    }

    private TemplateClassLoader getClassLoader() {
        if (null == lastClassloader_) {
            setClassLoader(new TemplateClassLoader(this, getClass().getClassLoader()));
        }

        assert lastClassloader_ != null;

        return lastClassloader_;
    }

    synchronized void setClassLoader(TemplateClassLoader classloader) {
        lastClassloader_ = classloader;
    }
}
