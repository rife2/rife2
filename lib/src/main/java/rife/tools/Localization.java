/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import java.util.*;

import rife.config.RifeConfig;
import rife.resources.ResourceFinderClasspath;
import rife.resources.exceptions.ResourceFinderErrorException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.regex.Pattern;

public class Localization {
    public static final Pattern URL_PATTERN = Pattern.compile("(?:\\s*(\\w+):((?!//)[^,]+)\\s*)|(?:\\s*([^,]+)\\s*)");

    private static final HashMap<String, ResourceBundle> resourceBundlesOpened_ = new HashMap<String, ResourceBundle>();
    private static final HashMap<String, Long> resourceBundleModificationTimes_ = new HashMap<String, Long>();

    private static long sLastModificationCheck = 0;

    public static String extractLocalizedUrl(String url) {
        if (null == url) {
            return null;
        }

        if (!url.contains(":")) {
            return url;
        } else {
            var matcher = URL_PATTERN.matcher(url);
            String localized_url = null;

            var default_lang = RifeConfig.tools().getDefaultLanguage();
            String fallback_url = null;

            String group_lang = null;
            String group_url = null;
            String group_fallback = null;
            while (matcher.find()) {
                if (3 == matcher.groupCount()) {
                    group_lang = matcher.group(1);
                    group_url = matcher.group(2);
                    group_fallback = matcher.group(3);

                    if (group_fallback != null) {
                        fallback_url = group_fallback;
                    } else if (group_url != null &&
                        default_lang != null &&
                        default_lang.equals(group_lang)) {
                        localized_url = group_url;
                        break;
                    }
                }
            }

            if (null == localized_url) {
                localized_url = fallback_url;
            }

            return localized_url;
        }
    }

    public static char getChar(String key) {
        return getChar(null, key, null, null);
    }

    public static char getChar(String key, String language) {
        return getChar(null, key, language, null);
    }

    public static char getChar(String key, String language, String country) {
        return getString(null, key, null, language, country).charAt(0);
    }

    public static char getChar(String basename, String key, String language, String country) {
        return getString(basename, key, null, language, country).charAt(0);
    }

    public static String getString(String key) {
        return getString(null, key, null, null, null);
    }

    public static String getString(String key, Object[] parameters) {
        return getString(null, key, parameters, null, null);
    }

    public static String getString(String key, String language) {
        return getString(null, key, null, language, null);
    }

    public static String getString(String key, Object[] parameters, String language) {
        return getString(null, key, parameters, language, null);
    }

    public static String getString(String key, String language, String country) {
        return getString(null, key, null, language, country);
    }

    public static String getString(String key, Object[] parameters, String language, String country) {
        return getString(null, key, parameters, language, country);
    }

    public static String getString(String basename, String key, String language, String country) {
        return getString(basename, key, null, language, country);
    }

    public static String getString(String basename, String key, Object[] parameters, String language, String country) {
        var resource_bundle = getResourceBundle(basename, language, country);

        if (null != resource_bundle) {
            String result = null;

            if (null == parameters) {
                try {
                    result = resource_bundle.getString(key);
                } catch (MissingResourceException e) {
                    return key;
                }
            } else {
                String pattern_string = null;

                try {
                    pattern_string = resource_bundle.getString(key);
                } catch (MissingResourceException e) {
                    return key;
                }

                var formatter = new MessageFormat(pattern_string);
                var locale = resource_bundle.getLocale();
                if (locale != null) {
                    formatter.setLocale(locale);
                }
                result = formatter.format(parameters);
            }

            return result;
        } else {
            return key;
        }
    }

    public static Locale getLocale() {
        return getLocale(null, null);
    }

    public static Locale getLocale(String language) {
        return getLocale(language, null);
    }

    public static Locale getLocale(String language, String country) {
        if (null == language) {
            language = RifeConfig.tools().getDefaultLanguage();
        }

        if (null == country) {
            country = RifeConfig.tools().getDefaultCountry();
        }

        Locale locale = null;
        if (null != language) {
            if (null == country) {
                locale = new Locale(language);
            } else {
                locale = new Locale(language, country);
            }
        }

        return locale;
    }

    public static ResourceBundle getResourceBundle(String basename) {
        return getResourceBundle(basename, null, null);
    }

    public static ResourceBundle getResourceBundle(String basename, String language) {
        return getResourceBundle(basename, language, null);
    }

    public static ResourceBundle getResourceBundle(String basename, String language, String country) {
        if (null == basename) {
            basename = RifeConfig.tools().getDefaultResourceBundle();
        }

        var locale = getLocale(language, country);

        var result = getResourceBundle(basename, locale);
        if (null == result) {
            result = getResourceBundle(basename, Locale.ENGLISH);
        }

        return result;
    }

    public static ResourceBundle getResourceBundle(String basename, Locale locale) {
        ResourceBundle result = null;
        if (null != locale) {
            var most_detailed_candidate = basename + locale;

            // see if the resource bundle reload is deactivated and thus fetch a previous copy without
            // looking up the resource
            if (!RifeConfig.tools().getResourceBundleAutoReload() ||
                System.currentTimeMillis() - sLastModificationCheck <= RifeConfig.global().getAutoReloadDelay()) {
                result = resourceBundlesOpened_.get(most_detailed_candidate);

                if (result != null) {
                    return result;
                }
            }

            if (RifeConfig.tools().getResourceBundleAutoReload()) {
                sLastModificationCheck = System.currentTimeMillis();
            }

            // build the list of possible candidates
            var candidates = new ArrayList<String>();

            var resource_bundle_id_buffer = new StringBuilder(basename);

            candidates.add(basename);

            var default_locale = Locale.getDefault();
            if (default_locale.getLanguage().length() > 0) {
                resource_bundle_id_buffer.append("_");
                resource_bundle_id_buffer.append(default_locale.getLanguage());

                candidates.add(resource_bundle_id_buffer.toString());
            }

            if (default_locale.getCountry().length() > 0) {
                resource_bundle_id_buffer.append("_");
                resource_bundle_id_buffer.append(default_locale.getCountry());

                candidates.add(resource_bundle_id_buffer.toString());
            }

            if (default_locale.getVariant().length() > 0) {
                resource_bundle_id_buffer.append("_");
                resource_bundle_id_buffer.append(default_locale.getVariant());

                candidates.add(resource_bundle_id_buffer.toString());
            }

            resource_bundle_id_buffer = new StringBuilder(basename);
            if (locale.getLanguage().length() > 0) {
                resource_bundle_id_buffer.append("_");
                resource_bundle_id_buffer.append(locale.getLanguage());

                var candidate = resource_bundle_id_buffer.toString();
                if (!candidates.contains(candidate)) {
                    candidates.add(candidate);
                }
            }

            if (locale.getCountry().length() > 0) {
                resource_bundle_id_buffer.append("_");
                resource_bundle_id_buffer.append(locale.getCountry());

                var candidate = resource_bundle_id_buffer.toString();
                if (!candidates.contains(candidate)) {
                    candidates.add(candidate);
                }
            }

            if (locale.getVariant().length() > 0) {
                resource_bundle_id_buffer.append("_");
                resource_bundle_id_buffer.append(locale.getVariant());

                var candidate = resource_bundle_id_buffer.toString();
                if (!candidates.contains(candidate)) {
                    candidates.add(candidate);
                }
            }

            while (candidates.size() > 0) {
                var resource_bundle_id = candidates.remove(candidates.size() - 1);
                try {
                    // try to load the resource bundle as a class
                    Class resource_class = null;
                    try {
                        resource_class = Localization.class.getClassLoader().loadClass(resource_bundle_id);
                    } catch (ClassNotFoundException e) {
                        resource_class = null;
                    }

                    if (resource_class != null) {
                        if (ResourceBundle.class.isAssignableFrom(resource_class)) {
                            try {
                                result = (ResourceBundle) resource_class.newInstance();

                                return result;
                            } catch (IllegalAccessException e) {
                                resource_class = null;
                                result = null;
                            } catch (InstantiationException e) {
                                resource_class = null;
                                result = null;
                            }
                        }
                    }

                    // try to load it as a properties file
                    resource_bundle_id = resource_bundle_id.replace('.', '/');

                    // no previous result found or checks should be made to see if the modification time changed
                    var name = resource_bundle_id + ".properties";
                    var resource = ResourceFinderClasspath.instance().getResource(name);
                    if (resource != null) {
                        var previous_modification = resourceBundleModificationTimes_.get(most_detailed_candidate);

                        long modification_time = -1;
                        if (RifeConfig.tools().getResourceBundleAutoReload()) {
                            try {
                                modification_time = ResourceFinderClasspath.instance().getModificationTime(resource);
                            } catch (ResourceFinderErrorException e) {
                                // don't do anything, the modification time will simply be negative
                            }
                        }

                        if (previous_modification != null &&
                            modification_time <= previous_modification.longValue()) {
                            result = resourceBundlesOpened_.get(most_detailed_candidate);
                        } else {
                            try {
                                result = new ReloadingBundle(resource);

                                resourceBundleModificationTimes_.put(most_detailed_candidate, modification_time);
                                resourceBundlesOpened_.put(most_detailed_candidate, result);
                            } catch (IOException e) {
                                result = null;
                            }
                        }
                    }
                } catch (MissingResourceException e) {
                    result = null;
                }

                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }
}

class ReloadingBundle extends ResourceBundle {
    private final Properties properties_;

    ReloadingBundle(URL resource)
    throws IOException {
        properties_ = new Properties();
        URLConnection connection = resource.openConnection();
        connection.setUseCaches(false);
        InputStream resourceAsStream = connection.getInputStream();
        properties_.load(resourceAsStream);
    }

    protected Object handleGetObject(String key) {
        return getProperties().get(key);
    }

    protected Properties getProperties() {
        return properties_;
    }

    public Enumeration getKeys() {
        return Collections.enumeration(properties_.keySet());
    }
}