/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.config;

import rife.config.exceptions.DateFormatInitializationException;
import rife.template.TemplateFactory;
import rife.tools.Localization;
import rife.tools.StringUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class RifeConfig {
    protected RifeConfig() {
    }

    /**
     * Returns the shared singleton instance of the
     * <code>RifeConfig</code> class.
     *
     * @return the singleton <code>RifeConfig</code> instance
     * @since 2.0
     */
    public static RifeConfig instance() {
        return RifeConfigSingleton.INSTANCE;
    }

    public static GlobalConfig global() {
        return instance().global;
    }

    public static EngineConfig engine() {
        return instance().engine;
    }

    public static ServerConfig server() {
        return instance().server;
    }

    public static TemplateConfig template() {
        return instance().template;
    }

    public static ToolsConfig tools() {
        return instance().tools;
    }

    public final GlobalConfig global = new GlobalConfig();
    public final EngineConfig engine = new EngineConfig();
    public final ServerConfig server = new ServerConfig();
    public final TemplateConfig template = new TemplateConfig();
    public final ToolsConfig tools = new ToolsConfig();

    public class GlobalConfig {
        private String tempPath_ = StringUtils.stripFromEnd(System.getProperty("java.io.tmpdir"), File.separator);
        private int autoReloadDelay_ = DEFAULT_AUTO_RELOAD_DELAY;

        public static final int DEFAULT_AUTO_RELOAD_DELAY = 10 * 1000;

        public String getTempPath() {
            return tempPath_;
        }

        public GlobalConfig setTempPath(String path) {
            if (null == path) throw new IllegalArgumentException("path can't be null.");
            if (path.isEmpty()) throw new IllegalArgumentException("path can't be empty.");
            tempPath_ = path;
            return this;
        }

        public int getAutoReloadDelay() {
            return autoReloadDelay_;
        }

        public GlobalConfig setAutoReloadDelay(int delay) {
            autoReloadDelay_ = delay;
            return this;
        }
    }

    public class EngineConfig {
        private String defaultContentType_ = DEFAULT_DEFAULT_CONTENT_TYPE;
        private boolean prettyEngineExceptions_ = DEFAULT_PRETTY_ENGINE_EXCEPTIONS;
        private boolean logEngineExceptions_ = DEFAULT_LOG_ENGINE_EXCEPTIONS;
        private String fileUploadPath_ = DEFAULT_FILE_UPLOAD_PATH;
        private long fileUploadSizeLimit_ = DEFAULT_FILE_UPLOAD_SIZE_LIMIT;
        private boolean fileUploadSizeCheck_ = DEFAULT_FILE_UPLOAD_SIZE_CHECK;
        private boolean fileUploadSizeException_ = DEFAULT_FILE_UPLOAD_SIZE_EXCEPTION;
        private boolean gzipCompression_ = DEFAULT_GZIP_COMPRESSION;
        private Collection<String> gzipCompressionTypes_ = DEFAULT_GZIP_COMPRESSION_TYPES;
        private int localForwardPort_ = DEFAULT_LOCAL_FORWARD_PORT;
        private String proxyRootUrl_ = DEFAULT_PROXY_ROOT_URL;
        private String webappContextPath_ = DEFAULT_WEBAPP_CONTEXT_PATH;
        private Charset requestEncoding_ = DEFAULT_REQUEST_ENCODING;
        private Charset responseEncoding_ = DEFAULT_RESPONSE_ENCODING;

        public static final String DEFAULT_DEFAULT_CONTENT_TYPE = "text/html";
        public static final boolean DEFAULT_PRETTY_ENGINE_EXCEPTIONS = true;
        public static final boolean DEFAULT_LOG_ENGINE_EXCEPTIONS = true;
        public static final String DEFAULT_FILE_UPLOAD_PATH = null;
        public static final String DEFAULT_FILE_UPLOAD_RIFE_FOLDER = "rife_uploads";
        public static final long DEFAULT_FILE_UPLOAD_SIZE_LIMIT = 1024 * 1024 * 2;    // 2MB
        public static final boolean DEFAULT_FILE_UPLOAD_SIZE_CHECK = true;
        public static final boolean DEFAULT_FILE_UPLOAD_SIZE_EXCEPTION = false;
        public static final boolean DEFAULT_GZIP_COMPRESSION = true;
        public static final Collection<String> DEFAULT_GZIP_COMPRESSION_TYPES = List.of(
            "text/html",
            "text/xml",
            "text/plain",
            "text/css",
            "text/javascript",
            "application/xml",
            "application/xhtml+xml"
        );
        public static final int DEFAULT_LOCAL_FORWARD_PORT = -1;
        public static final String DEFAULT_PROXY_ROOT_URL = null;
        public static final String DEFAULT_WEBAPP_CONTEXT_PATH = null;
        public static final Charset DEFAULT_REQUEST_ENCODING = StandardCharsets.UTF_8;
        public static final Charset DEFAULT_RESPONSE_ENCODING = StandardCharsets.UTF_8;

        public String getDefaultContentType() {
            return defaultContentType_;
        }

        public EngineConfig setDefaultContentType(String type) {
            if (null == type) throw new IllegalArgumentException("type can't be null.");
            if (type.isEmpty()) throw new IllegalArgumentException("type can't be empty.");
            defaultContentType_ = type;
            return this;
        }

        public boolean getPrettyEngineExceptions() {
            return prettyEngineExceptions_;
        }

        public EngineConfig setPrettyEngineExceptions(boolean flag) {
            prettyEngineExceptions_ = flag;
            return this;
        }

        public boolean getLogEngineExceptions() {
            return logEngineExceptions_;
        }

        public EngineConfig setLogEngineExceptions(boolean flag) {
            logEngineExceptions_ = flag;
            return this;
        }

        public String getFileUploadPath() {
            if (null == fileUploadPath_) {
                return RifeConfig.this.global.getTempPath() + File.separator + DEFAULT_FILE_UPLOAD_RIFE_FOLDER;
            }
            return fileUploadPath_;
        }

        public EngineConfig setFileUploadPath(String path) {
            if (path != null &&
                path.isEmpty()) throw new IllegalArgumentException("path can't be empty.");
            fileUploadPath_ = path;
            return this;
        }

        public long getFileUploadSizeLimit() {
            return fileUploadSizeLimit_;
        }

        public EngineConfig setFileUploadSizeLimit(long limit) {
            fileUploadSizeLimit_ = limit;
            return this;
        }

        public boolean getFileUploadSizeCheck() {
            return fileUploadSizeCheck_;
        }

        public EngineConfig setFileUploadSizeCheck(boolean flag) {
            fileUploadSizeCheck_ = flag;
            return this;
        }

        public boolean getFileUploadSizeException() {
            return fileUploadSizeException_;
        }

        public EngineConfig setFileUploadSizeException(boolean flag) {
            fileUploadSizeException_ = flag;
            return this;
        }

        public boolean getGzipCompression() {
            return gzipCompression_;
        }

        public EngineConfig setGzipCompression(boolean flag) {
            gzipCompression_ = flag;
            return this;
        }

        public Collection<String> getGzipCompressionTypes() {
            return gzipCompressionTypes_;
        }

        public EngineConfig setGzipCompressionTypes(Collection<String> types) {
            gzipCompressionTypes_ = types;
            return this;
        }

        public int getLocalForwardPort() {
            return localForwardPort_;
        }

        public EngineConfig setLocalForwardPort(int port) {
            if (port <= 0) port = -1;
            localForwardPort_ = port;
            return this;
        }

        public String getProxyRootUrl() {
            return proxyRootUrl_;
        }

        public EngineConfig setProxyRootUrl(String url) {
            proxyRootUrl_ = url;
            return this;
        }

        public String getWebappContextPath() {
            return webappContextPath_;
        }

        public EngineConfig setWebappContextPath(String path) {
            webappContextPath_ = path;
            return this;
        }

        public Charset getRequestEncoding() {
            return requestEncoding_;
        }

        public EngineConfig setRequestEncoding(Charset encoding) {
            requestEncoding_ = encoding;
            return this;
        }

        public Charset getResponseEncoding() {
            return responseEncoding_;
        }

        public EngineConfig setResponseEncoding(Charset encoding) {
            responseEncoding_ = encoding;
            return this;
        }
    }

    public class ServerConfig {
        private int port_ = DEFAULT_PORT;

        public static final int DEFAULT_PORT = 4567;

        public int getPort() {
            return port_;
        }

        public ServerConfig setPort(int port) {
            port_ = port;
            return this;
        }
    }

    public class TemplateConfig {
        private boolean autoReload_ = DEFAULT_TEMPLATE_AUTO_RELOAD;
        private String generationPath_ = null;
        private boolean generateClasses_ = DEFAULT_GENERATE_CLASSES;
        private String defaultEncoding_ = null;
        private HashMap<String, Collection<String>> defaultResourceBundles_ = null;

        public static final boolean DEFAULT_TEMPLATE_AUTO_RELOAD = true;
        public static final String DEFAULT_TEMPLATES_RIFE_FOLDER = "rife_templates";
        public static final boolean DEFAULT_GENERATE_CLASSES = false;

        public boolean getAutoReload() {
            return autoReload_;
        }

        public TemplateConfig setAutoReload(boolean flag) {
            autoReload_ = flag;
            return this;
        }

        public String getGenerationPath() {
            String generation_path = generationPath_;

            if (null == generation_path) {
                return RifeConfig.this.global.getTempPath() + File.separator + DEFAULT_TEMPLATES_RIFE_FOLDER;
            }

            generation_path += File.separator;

            return generation_path;
        }

        public TemplateConfig setGenerationPath(String path) {
            if (null == path) throw new IllegalArgumentException("path can't be null.");
            if (path.isEmpty()) throw new IllegalArgumentException("path can't be empty.");
            generationPath_ = path;
            return this;
        }

        public boolean getGenerateClasses() {
            return generateClasses_;
        }

        public TemplateConfig setGenerateClasses(boolean generate) {
            generateClasses_ = generate;
            return this;
        }

        public String getDefaultEncoding() {
            return defaultEncoding_;
        }

        public TemplateConfig setDefaultEncoding(String encoding) {
            if (null == encoding) throw new IllegalArgumentException("encoding can't be null.");
            if (encoding.isEmpty()) throw new IllegalArgumentException("encoding can't be empty.");
            defaultEncoding_ = encoding;
            return this;
        }

        public Collection<String> getDefaultResourceBundles(TemplateFactory factory) {
            Collection<String> result = null;

            if (defaultResourceBundles_ != null) {
                result = defaultResourceBundles_.get(factory.getIdentifierUppercase());
            }

            return result;
        }

        public String getDefaultResourceBundle(TemplateFactory factory) {
            Collection<String> result = getDefaultResourceBundles(factory);
            if (null == result || 0 == result.size()) {
                return null;
            }
            return result.iterator().next();
        }

        public TemplateConfig setDefaultResourceBundles(TemplateFactory factory, Collection<String> bundles) {
            if (null == defaultResourceBundles_) {
                defaultResourceBundles_ = new HashMap<>();
            }

            defaultResourceBundles_.put(factory.getIdentifierUppercase(), bundles);

            return this;
        }
    }

    public class ToolsConfig {
        private boolean resourceBundleAutoReload_ = DEFAULT_RESOURCE_BUNDLE_AUTO_RELOAD;
        private String defaultResourceBundle_ = DEFAULT_RESOURCE_BUNDLE;
        private String defaultLanguage_ = DEFAULT_DEFAULT_LANGUAGE;
        private String defaultCountry_ = DEFAULT_DEFAULT_COUNTRY;
        private TimeZone defaultTimeZone_ = DEFAULT_DEFAULT_TIMEZONE;
        private String defaultShortDateFormat_;
        private String defaultInputDateFormat_ = DEFAULT_INPUT_DATE_FORMAT;
        private String defaultLongDateFormat_;
        private int maxVisualUrlLength_ = DEFAULT_MAX_VISUAL_URL_LENGTH;

        public static final boolean DEFAULT_RESOURCE_BUNDLE_AUTO_RELOAD = true;
        public static final String DEFAULT_RESOURCE_BUNDLE = null;
        public static final String DEFAULT_DEFAULT_LANGUAGE = "en";
        public static final String DEFAULT_DEFAULT_COUNTRY = null;
        public static final TimeZone DEFAULT_DEFAULT_TIMEZONE = TimeZone.getTimeZone("EST");
        public static final String DEFAULT_INPUT_DATE_FORMAT = "yyyy-MM-dd HH:mm";
        public static final int DEFAULT_MAX_VISUAL_URL_LENGTH = 70;

        public boolean getResourceBundleAutoReload() {
            return resourceBundleAutoReload_;
        }

        public ToolsConfig setResourceBundleAutoReload(boolean flag) {
            resourceBundleAutoReload_ = flag;
            return this;
        }

        public String getDefaultResourceBundle() {
            return defaultResourceBundle_;
        }

        public ToolsConfig setDefaultResourceBundle(String name) {
            if (name != null &&
                name.isEmpty()) throw new IllegalArgumentException("name can't be empty.");
            defaultResourceBundle_ = name;
            return this;
        }

        public String getDefaultLanguage() {
            return defaultLanguage_;
        }

        public ToolsConfig setDefaultLanguage(String abbreviation) {
            if (abbreviation != null &&
                abbreviation.isEmpty()) throw new IllegalArgumentException("abbreviation can't be empty.");

            if (null == abbreviation) {
                defaultLanguage_ = DEFAULT_DEFAULT_LANGUAGE;
            } else {
                defaultLanguage_ = abbreviation;
            }
            return this;
        }

        public String getDefaultCountry() {
            return defaultCountry_;
        }

        public ToolsConfig setDefaultCountry(String countryCode) {
            if (null != countryCode &&
                countryCode.isEmpty()) throw new IllegalArgumentException("countryCode can't be empty.");
            defaultCountry_ = countryCode;
            return this;
        }

        public TimeZone getDefaultTimeZone() {
            TimeZone result = defaultTimeZone_;

            if (null == result) {
                result = TimeZone.getDefault();
            }

            return result;
        }

        public ToolsConfig setDefaultTimeZone(TimeZone timeZone) {
            defaultTimeZone_ = timeZone;
            return this;
        }

        public DateFormat getDefaultShortDateFormat() {
            if (defaultShortDateFormat_ != null) {
                SimpleDateFormat sf;
                try {
                    sf = new SimpleDateFormat(defaultShortDateFormat_, Localization.getLocale());
                    sf.setTimeZone(getDefaultTimeZone());
                } catch (IllegalArgumentException e) {
                    throw new DateFormatInitializationException(e.getMessage());
                }

                return sf;
            } else {
                if (0 != getDefaultLanguage().compareToIgnoreCase(DEFAULT_DEFAULT_LANGUAGE)) {
                    return DateFormat.getDateInstance(DateFormat.SHORT, Localization.getLocale());
                }

                return DateFormat.getDateInstance(DateFormat.SHORT, Locale.ENGLISH);
            }
        }

        public ToolsConfig setDefaultShortDateFormat(String format) {
            if (null != format &&
                format.isEmpty()) throw new IllegalArgumentException("format can't be empty.");
            defaultShortDateFormat_ = format;
            return this;
        }

        public DateFormat getDefaultLongDateFormat() {
            if (defaultLongDateFormat_ != null) {
                SimpleDateFormat sf;
                try {
                    sf = new SimpleDateFormat(defaultLongDateFormat_, Localization.getLocale());
                    sf.setTimeZone(getDefaultTimeZone());
                } catch (IllegalArgumentException e) {
                    throw new DateFormatInitializationException(e.getMessage());
                }

                return sf;
            } else {
                if (0 != getDefaultLanguage().compareToIgnoreCase(DEFAULT_DEFAULT_LANGUAGE)) {
                    return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Localization.getLocale());
                }

                return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.ENGLISH);
            }
        }

        public ToolsConfig setDefaultLongDateFormat(String format) {
            if (null != format &&
                format.isEmpty()) throw new IllegalArgumentException("format can't be empty.");
            defaultLongDateFormat_ = format;
            return this;
        }

        public DateFormat getDefaultInputDateFormat() {
            SimpleDateFormat sf = new SimpleDateFormat(defaultInputDateFormat_);
            sf.setTimeZone(getDefaultTimeZone());
            return sf;
        }

        public ToolsConfig setDefaultInputDateFormat(String format) {
            if (null != format &&
                format.isEmpty()) throw new IllegalArgumentException("format can't be empty.");
            defaultInputDateFormat_ = format;
            return this;
        }

        public int getMaxVisualUrlLength() {
            return maxVisualUrlLength_;
        }

        public ToolsConfig setMaxVisualUrlLength(int length) {
            maxVisualUrlLength_ = length;
            return this;
        }
    }

}
