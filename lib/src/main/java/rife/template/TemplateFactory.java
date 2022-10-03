/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

import rife.config.RifeConfig;
import rife.resources.ResourceFinder;
import rife.resources.ResourceFinderClasspath;
import rife.template.exceptions.TemplateException;
import rife.template.exceptions.TemplateNotFoundException;
import rife.tools.Localization;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public enum TemplateFactory {
    HTML(ResourceFinderClasspath.instance(),
        "html", "text/html", ".html",
        BeanHandlerXhtml.getInstance(), EncoderHtmlSingleton.INSTANCE,
        null);

    private TemplateClassLoader lastClassloader_ = null;
    private final Parser parser_;
    private BeanHandler beanHandler_;
    private TemplateEncoder encoder_;
    private ResourceFinder resourceFinder_;
    private TemplateInitializer initializer_;
    private final String identifierUppercase_;
    private final String defaultContentType_;

    TemplateFactory(ResourceFinder resourceFinder, String identifier,
                    String defaultContentType,
                    String extension, /* TODO String[] blockFilters, String[] valueFilters,  */
                    BeanHandler beanHandler,
                    TemplateEncoder encoder,
                    TemplateInitializer initializer) {
        // TODO
//        Pattern[] block_filter_patterns = null;
//        try {
//            block_filter_patterns = compileFilters(blockFilters);
//        } catch (PatternSyntaxException e) {
//            throw new InvalidBlockFilterException(e.getPattern());
//        }
//
//        Pattern[] value_filter_patterns = null;
//        try {
//            value_filter_patterns = compileFilters(valueFilters);
//        } catch (PatternSyntaxException e) {
//            throw new InvalidValueFilterException(e.getPattern());
//        }

        identifierUppercase_ = identifier.toUpperCase();
        parser_ = new Parser(this, identifier, extension);
        resourceFinder_ = resourceFinder;
        beanHandler_ = beanHandler;
        encoder_ = encoder;
        initializer_ = initializer;
        defaultContentType_ = defaultContentType;
    }

    private Pattern[] compileFilters(String[] filters)
    throws PatternSyntaxException {
        if (filters != null) {
            Pattern[] patterns = new Pattern[filters.length];

            for (int i = 0; i < filters.length; i++) {
                patterns[i] = Pattern.compile(filters[i]);
            }

            return patterns;
        }

        return null;
    }

//    public TemplateFactory(String identifier, TemplateFactory base) {
//        super(TemplateFactory.class, identifier);
//
//        mIdentifierUppercase = identifier.toUpperCase();
//        mParser = new Parser(this, identifier, base.getParser().getConfigs(), base.getParser().getExtension(), base.getParser().getBlockFilters(), base.getParser().getValueFilters());
//        mResourceFinder = base.getResourceFinder();
//        beanHandler_ = base.getBeanHandler();
//        mEncoder = base.getEncoder();
//        mInitializer = base.getInitializer();
//        mDefaultContentType = base.getDefaultContentType();
//
//        assert mParser != null;
//    }

    public String getIdentifierUppercase() {
        return identifierUppercase_;
    }

    public String getDefaultContentType() {
        return defaultContentType_;
    }

    public static Collection<String> getFactoryTypes() {
        var types = new HashSet<String>();
        for (var v : TemplateFactory.values()) {
            types.add(v.getIdentifierUppercase());
        }
        return types;
    }

    public static TemplateFactory getFactory(String identifier) {
        return TemplateFactory.valueOf(identifier);
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

            var default_resource_bundles = RifeConfig.instance().template.defaultResourceBundles(this);
            if (default_resource_bundles != null) {
                ArrayList<ResourceBundle> default_bundles = new ArrayList<ResourceBundle>();
                for (String bundle_name : default_resource_bundles) {
                    // try to look it up as a filename in the classpath
                    ResourceBundle bundle = Localization.getResourceBundle(bundle_name);
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
