/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.config;

import rife.template.TemplateFactory;
import rife.tools.StringUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Time;
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

    public static Global global() {
        return instance().global;
    }

    public static Engine engine() {
        return instance().engine;
    }

    public static Server server() {
        return instance().server;
    }

    public static Template template() {
        return instance().template;
    }

    public static Tools tools() {
        return instance().tools;
    }

    public final Global global = new Global();
    public final Engine engine = new Engine();
    public final Server server = new Server();
    public final Template template = new Template();
    public final Tools tools = new Tools();

    public class Global {
        private String tempPath_ = StringUtils.stripFromEnd(System.getProperty("java.io.tmpdir"), File.separator);
        private int autoReloadDelay_ = DEFAULT_AUTO_RELOAD_DELAY;

        public static final int DEFAULT_AUTO_RELOAD_DELAY = 10 * 1000;

        public String tempPath() {
            return tempPath_;
        }

        public Global tempPath(String path) {
            if (null == path) throw new IllegalArgumentException("path can't be null.");
            if (path.isEmpty()) throw new IllegalArgumentException("path can't be empty.");
            tempPath_ = path;
            return this;
        }

        public int autoReloadDelay() {
            return autoReloadDelay_;
        }

        public Global autoReloadDelay(int delay) {
            autoReloadDelay_ = delay;
            return this;
        }
    }

    public class Engine {
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
        public static final boolean DEFAULT_GZIP_COMPRESSION = false;
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

        public String defaultContentType() {
            return defaultContentType_;
        }

        public Engine defaultContentType(String type) {
            if (null == type) throw new IllegalArgumentException("type can't be null.");
            if (type.isEmpty()) throw new IllegalArgumentException("type can't be empty.");
            defaultContentType_ = type;
            return this;
        }

        public boolean prettyEngineExceptions() {
            return prettyEngineExceptions_;
        }

        public Engine prettyEngineExceptions(boolean flag) {
            prettyEngineExceptions_ = flag;
            return this;
        }

        public boolean logEngineExceptions() {
            return logEngineExceptions_;
        }

        public Engine logEngineExceptions(boolean flag) {
            logEngineExceptions_ = flag;
            return this;
        }

        public String fileUploadPath() {
            if (null == fileUploadPath_) {
                return RifeConfig.this.global.tempPath() + File.separator + DEFAULT_FILE_UPLOAD_RIFE_FOLDER;
            }
            return fileUploadPath_;
        }

        public Engine fileUploadPath(String path) {
            if (path != null &&
                path.isEmpty()) throw new IllegalArgumentException("path can't be empty.");
            fileUploadPath_ = path;
            return this;
        }

        public long fileUploadSizeLimit() {
            return fileUploadSizeLimit_;
        }

        public Engine fileUploadSizeLimit(long limit) {
            fileUploadSizeLimit_ = limit;
            return this;
        }

        public boolean fileUploadSizeCheck() {
            return fileUploadSizeCheck_;
        }

        public Engine fileUploadSizeCheck(boolean flag) {
            fileUploadSizeCheck_ = flag;
            return this;
        }

        public boolean fileUploadSizeException() {
            return fileUploadSizeException_;
        }

        public Engine fileUploadSizeException(boolean flag) {
            fileUploadSizeException_ = flag;
            return this;
        }

        public boolean gzipCompression() {
            return gzipCompression_;
        }

        public Engine gzipCompression(boolean flag) {
            gzipCompression_ = flag;
            return this;
        }

        public Collection<String> gzipCompressionTypes() {
            return gzipCompressionTypes_;
        }

        public Engine gzipCompressionTypes(Collection<String> types) {
            gzipCompressionTypes_ = types;
            return this;
        }

        public int localForwardPort() {
            return localForwardPort_;
        }

        public Engine localForwardPort(int port) {
            if (port <= 0) port = -1;
            localForwardPort_ = port;
            return this;
        }

        public String proxyRootUrl() {
            return proxyRootUrl_;
        }

        public Engine proxyRootUrl(String url) {
            proxyRootUrl_ = url;
            return this;
        }

        public String webappContextPath() {
            return webappContextPath_;
        }

        public Engine webappContextPath(String path) {
            webappContextPath_ = path;
            return this;
        }

        public Charset requestEncoding() {
            return requestEncoding_;
        }

        public Engine requestEncoding(Charset encoding) {
            requestEncoding_ = encoding;
            return this;
        }

        public Charset responseEncoding() {
            return responseEncoding_;
        }

        public Engine responseEncoding(Charset encoding) {
            responseEncoding_ = encoding;
            return this;
        }
    }

    public class Server {
        private int port_ = DEFAULT_PORT;

        public static final int DEFAULT_PORT = 4567;

        public int port() {
            return port_;
        }

        public Server port(int port) {
            port_ = port;
            return this;
        }
    }

    public class Template {
        private boolean autoReload_ = DEFAULT_TEMPLATE_AUTO_RELOAD;
        private String generationPath_ = null;
        private boolean generateClasses_ = DEFAULT_GENERATE_CLASSES;
        private String defaultEncoding_ = null;
        private HashMap<String, Collection<String>> defaultResourceBundles_ = null;

        public static final boolean DEFAULT_TEMPLATE_AUTO_RELOAD = true;
        public static final String DEFAULT_TEMPLATES_RIFE_FOLDER = "rife_templates";
        public static final boolean DEFAULT_GENERATE_CLASSES = false;

        public boolean autoReload() {
            return autoReload_;
        }

        public Template autoReload(boolean flag) {
            autoReload_ = flag;
            return this;
        }

        public String generationPath() {
            String generation_path = generationPath_;

            if (null == generation_path) {
                return RifeConfig.this.global.tempPath() + File.separator + DEFAULT_TEMPLATES_RIFE_FOLDER;
            }

            generation_path += File.separator;

            return generation_path;
        }

        public Template generationPath(String path) {
            if (null == path) throw new IllegalArgumentException("path can't be null.");
            if (path.isEmpty()) throw new IllegalArgumentException("path can't be empty.");
            generationPath_ = path;
            return this;
        }

        public boolean generateClasses() {
            return generateClasses_;
        }

        public Template generateClasses(boolean generate) {
            generateClasses_ = generate;
            return this;
        }

        public String defaultEncoding() {
            return defaultEncoding_;
        }

        public Template defaultEncoding(String encoding) {
            if (null == encoding) throw new IllegalArgumentException("encoding can't be null.");
            if (encoding.isEmpty()) throw new IllegalArgumentException("encoding can't be empty.");
            defaultEncoding_ = encoding;
            return this;
        }

        public Collection<String> defaultResourceBundles(TemplateFactory factory) {
            Collection<String> result = null;

            if (defaultResourceBundles_ != null) {
                result = defaultResourceBundles_.get(factory.getIdentifierUppercase());
            }

            return result;
        }

        public String defaultResourceBundle(TemplateFactory factory) {
            Collection<String> result = defaultResourceBundles(factory);
            if (null == result || 0 == result.size()) {
                return null;
            }
            return result.iterator().next();
        }

        public Template defaultResourceBundles(TemplateFactory factory, Collection<String> bundles) {
            if (null == defaultResourceBundles_) {
                defaultResourceBundles_ = new HashMap<>();
            }

            defaultResourceBundles_.put(factory.getIdentifierUppercase(), bundles);

            return this;
        }
    }

    public class Tools {
        private boolean resourceBundleAutoReload_ = DEFAULT_RESOURCE_BUNDLE_AUTO_RELOAD;
        private String defaultResourceBundle_ = DEFAULT_RESOURCE_BUNDLE;
        private String defaultLanguage_ = DEFAULT_DEFAULT_LANGUAGE;
        private String defaultCountry_ = DEFAULT_DEFAULT_COUNTRY;
        private TimeZone defaultTimeZone_ = DEFAULT_DEFAULT_TIMEZONE;
        private String defaultInputDateFormat_ = DEFAULT_INPUT_DATE_FORMAT;
        private int maxVisualUrlLength_ = DEFAULT_MAX_VISUAL_URL_LENGTH;

        public static final boolean DEFAULT_RESOURCE_BUNDLE_AUTO_RELOAD = true;
        public static final String DEFAULT_RESOURCE_BUNDLE = null;
        public static final String DEFAULT_DEFAULT_LANGUAGE = "en";
        public static final String DEFAULT_DEFAULT_COUNTRY = null;
        public static final TimeZone DEFAULT_DEFAULT_TIMEZONE = TimeZone.getTimeZone("EST");
        public static final String DEFAULT_INPUT_DATE_FORMAT = "yyyy-MM-dd HH:mm";
        public static final int DEFAULT_MAX_VISUAL_URL_LENGTH = 70;

        public boolean resourcebundleAutoReload() {
            return resourceBundleAutoReload_;
        }

        public Tools resourcebundleAutoReload(boolean flag) {
            resourceBundleAutoReload_ = flag;
            return this;
        }

        public String defaultResourceBundle() {
            return defaultResourceBundle_;
        }

        public Tools defaultResourceBundle(String name) {
            if (name != null &&
                name.isEmpty()) throw new IllegalArgumentException("name can't be empty.");
            defaultResourceBundle_ = name;
            return this;
        }

        public String defaultLanguage() {
            return defaultLanguage_;
        }

        public Tools defaultLanguage(String abbreviation) {
            if (null == abbreviation) throw new IllegalArgumentException("abbreviation can't be null.");
            if (abbreviation.isEmpty()) throw new IllegalArgumentException("abbreviation can't be empty.");
            defaultLanguage_ = abbreviation;
            return this;
        }

        public String defaultCountry() {
            return defaultCountry_;
        }

        public Tools defaultCountry(String countryCode) {
            if (null == countryCode) throw new IllegalArgumentException("countryCode can't be null.");
            if (countryCode.isEmpty()) throw new IllegalArgumentException("countryCode can't be empty.");
            defaultCountry_ = countryCode;
            return this;
        }

        public TimeZone defaultTimeZone() {
            TimeZone result = defaultTimeZone_;

            if (null == result) {
                result = TimeZone.getDefault();
            }

            return result;
        }

        public Tools defaultTimeZone(TimeZone timeZone) {
            defaultTimeZone_ = timeZone;
            return this;
        }

        public DateFormat defaultInputDateFormat() {
            SimpleDateFormat sf = new SimpleDateFormat(defaultInputDateFormat_);
            sf.setTimeZone(defaultTimeZone());
            return sf;
        }

        public Tools defaultInputDateFormat(String format) {
            if (null == format) throw new IllegalArgumentException("format can't be null.");
            if (format.isEmpty()) throw new IllegalArgumentException("format can't be empty.");
            defaultInputDateFormat_ = format;
            return this;
        }

        public int maxVisualUrlLength() {
            return maxVisualUrlLength_;
        }

        public Tools maxVisualUrlLength(int length) {
            maxVisualUrlLength_ = length;
            return this;
        }
    }

}
