/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

public class TemplateFactoryFilters {
    public static final String PREFIX_L10N = "l10n:";
    public static final String PREFIX_LANG = "lang:";
    public static final String PREFIX_COOKIE = "cookie:";
    public static final String PREFIX_PARAM = "param:";
    public static final String PREFIX_PROPERTY = "property:";
    public static final String PREFIX_ATTRIBUTE = "attribute:";
    public static final String PREFIX_RENDER = "render:";
    public static final String PREFIX_ROUTE = "route:";
    public static final String PREFIX_AUTH = "auth:";

    public static final String TAG_L10N = "^" + PREFIX_L10N + "\\s*([^:]*)(?::([^:]*))?\\s*$";
    public static final String TAG_LANG = "(?s)^(" + PREFIX_LANG + ".*):\\s*(\\w*)\\s*$";
    public static final String TAG_COOKIE = "^" + PREFIX_COOKIE + "\\s*(.*?)\\s*$";
    public static final String TAG_PARAM = "^" + PREFIX_PARAM + "\\s*(.*?)\\s*$";
    public static final String TAG_PROPERTY = "^" + PREFIX_PROPERTY + "\\s*(.*?)\\s*$";
    public static final String TAG_ATTRIBUTE = "^" + PREFIX_ATTRIBUTE + "\\s*(.*?)\\s*$";
    public static final String TAG_RENDER = "^" + PREFIX_RENDER + "\\s*(.*?)\\s*(:[^:]*)?$";
    public static final String TAG_ROUTE = "^" + PREFIX_ROUTE + "\\s*(.*?)\\s*$";
    public static final String TAG_AUTH = "^" + PREFIX_AUTH + "\\s*([^:]*?)\\s*$";
    public static final String TAG_AUTH_LOGIN = "^" + PREFIX_AUTH + "\\s*([^:]*?)\\s*::\\s*login\\s*:\\s*([^:]*?)\\s*$";
    public static final String TAG_AUTH_ROLE = "^" + PREFIX_AUTH + "\\s*([^:]*?)\\s*::\\s*role\\s*:\\s*([^:]*?)\\s*$";
}
