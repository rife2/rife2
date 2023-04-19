/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.config;

import rife.config.exceptions.DateFormatInitializationException;
import rife.continuations.ContinuationConfigRuntimeDefaults;
import rife.template.TemplateFactory;
import rife.tools.Localization;
import rife.tools.StringUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;

/**
 * Provides configuration over RIFE2 itself.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class RifeConfig {
    protected RifeConfig() {
    }

    /**
     * Returns the shared singleton instance of the
     * {@code RifeConfig} class.
     *
     * @return the singleton {@code RifeConfig} instance
     * @since 1.0
     */
    public static RifeConfig instance() {
        return RifeConfigSingleton.INSTANCE;
    }

    public static GlobalConfig global() {
        return instance().global;
    }

    public static AuthenticationConfig authentication() {
        return instance().authentication;
    }

    public static DatabaseConfig database() {
        return instance().database;
    }

    public static CmfConfig cmf() {
        return instance().cmf;
    }

    public static EngineConfig engine() {
        return instance().engine;
    }

    public static ResourcesConfig resources() {
        return instance().resources;
    }

    public static SchedulerConfig scheduler() {
        return instance().scheduler;
    }

    public static TemplateConfig template() {
        return instance().template;
    }

    public static ToolsConfig tools() {
        return instance().tools;
    }

    public static XmlConfig xml() {
        return instance().xml;
    }

    public final GlobalConfig global = new GlobalConfig();
    public final AuthenticationConfig authentication = new AuthenticationConfig();
    public final DatabaseConfig database = new DatabaseConfig();
    public final CmfConfig cmf = new CmfConfig();
    public final EngineConfig engine = new EngineConfig();
    public final ResourcesConfig resources = new ResourcesConfig();
    public final SchedulerConfig scheduler = new SchedulerConfig();
    public final TemplateConfig template = new TemplateConfig();
    public final ToolsConfig tools = new ToolsConfig();
    public final XmlConfig xml = new XmlConfig();

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

    public class AuthenticationConfig {
        private int loginMinimumLength_ = DEFAULT_LOGIN_MINIMUM_LENGTH;
        private int loginMaximumLength_ = DEFAULT_LOGIN_MAXIMUM_LENGTH;
        private int passwordMinimumLength_ = DEFAULT_PASSWORD_MINIMUM_LENGTH;
        private int passwordMaximumLength_ = DEFAULT_PASSWORD_MAXIMUM_LENGTH;
        private int roleNameMaximumLength_ = DEFAULT_ROLE_NAME_MAXIMUM_LENGTH;
        private long sessionDuration_ = DEFAULT_SESSION_DURATION;
        private int sessionPurgeFrequency_ = DEFAULT_SESSION_PURGE_FREQUENCY;
        private int sessionPurgeScale_ = DEFAULT_SESSION_PURGE_SCALE;
        private boolean sessionRestrictAuthData_ = DEFAULT_SESSION_RESTRICT_AUTH_DATA;
        private long rememberDuration_ = DEFAULT_REMEMBER_DURATION;
        private int rememberPurgeFrequency_ = DEFAULT_REMEMBER_PURGE_FREQUENCY;
        private int rememberPurgeScale_ = DEFAULT_REMEMBER_PURGE_SCALE;
        private String tableRole_ = DEFAULT_TABLE_ROLE;
        private String sequenceRole_ = DEFAULT_SEQUENCE_ROLE;
        private String tableUser_ = DEFAULT_TABLE_USER;
        private String tableRoleLink_ = DEFAULT_TABLE_ROLELINK;
        private String tableAuthentication_ = DEFAULT_TABLE_AUTHENTICATION;
        private String tableRemember_ = DEFAULT_TABLE_REMEMBER;

        public static final int DEFAULT_LOGIN_MINIMUM_LENGTH = 5;
        public static final int DEFAULT_LOGIN_MAXIMUM_LENGTH = 40;
        public static final int DEFAULT_PASSWORD_MINIMUM_LENGTH = 5;
        public static final int DEFAULT_PASSWORD_MAXIMUM_LENGTH = 100;
        public static final int DEFAULT_ROLE_NAME_MAXIMUM_LENGTH = 20;
        public static final long DEFAULT_SESSION_DURATION = 1000 * 60 * 20;        // 20 minutes
        public static final int DEFAULT_SESSION_PURGE_FREQUENCY = 20;              // 20 out of 1000 times, means 1/50th of the time
        public static final int DEFAULT_SESSION_PURGE_SCALE = 1000;
        public static final boolean DEFAULT_SESSION_RESTRICT_AUTH_DATA = true;
        public static final long DEFAULT_REMEMBER_DURATION = 1000L * 60L * 60L * 24L * 30L * 3L;    // 3 months
        public static final int DEFAULT_REMEMBER_PURGE_FREQUENCY = 20;             // 20 out of 1000 times, means 1/50th of the time
        public static final int DEFAULT_REMEMBER_PURGE_SCALE = 1000;
        public static final String DEFAULT_TABLE_ROLE = "AuthRole";
        public static final String DEFAULT_SEQUENCE_ROLE = "SEQ_AUTHROLE";
        public static final String DEFAULT_TABLE_USER = "AuthUser";
        public static final String DEFAULT_TABLE_ROLELINK = "AuthRoleLink";
        public static final String DEFAULT_TABLE_AUTHENTICATION = "Authentication";
        public static final String DEFAULT_TABLE_REMEMBER = "AuthRemember";

        public int getLoginMinimumLength() {
            return loginMinimumLength_;
        }

        public AuthenticationConfig setLoginMinimumLength(int length) {
            loginMinimumLength_ = length;
            return this;
        }

        public int getLoginMaximumLength() {
            return loginMaximumLength_;
        }

        public AuthenticationConfig setLoginMaximumLength(int length) {
            loginMaximumLength_ = length;
            return this;
        }

        public int getPasswordMinimumLength() {
            return passwordMinimumLength_;
        }

        public AuthenticationConfig setPasswordMinimumLength(int length) {
            passwordMinimumLength_ = length;
            return this;
        }

        public int getPasswordMaximumLength() {
            return passwordMaximumLength_;
        }

        public AuthenticationConfig setPasswordMaximumLength(int length) {
            passwordMaximumLength_ = length;
            return this;
        }

        public int getRoleNameMaximumLength() {
            return roleNameMaximumLength_;
        }

        public AuthenticationConfig setRoleNameMaximumLength(int length) {
            roleNameMaximumLength_ = length;
            return this;
        }

        public long getSessionDuration() {
            return sessionDuration_;
        }

        public AuthenticationConfig setSessionDuration(long duration) {
            sessionDuration_ = duration;
            return this;
        }

        public int getSessionPurgeFrequency() {
            return sessionPurgeFrequency_;
        }

        public AuthenticationConfig setSessionPurgeFrequency(int frequency) {
            sessionPurgeFrequency_ = frequency;
            return this;
        }

        public int getSessionPurgeScale() {
            return sessionPurgeScale_;
        }

        public AuthenticationConfig setSessionPurgeScale(int scale) {
            sessionPurgeScale_ = scale;
            return this;
        }

        public boolean getSessionRestrictAuthData() {
            return sessionRestrictAuthData_;
        }

        public AuthenticationConfig setSessionRestrictAuthData(boolean restrict) {
            sessionRestrictAuthData_ = restrict;
            return this;
        }

        public long getRememberDuration() {
            return rememberDuration_;
        }

        public AuthenticationConfig setRememberDuration(long duration) {
            rememberDuration_ = duration;
            return this;
        }

        public int getRememberPurgeFrequency() {
            return rememberPurgeFrequency_;
        }

        public AuthenticationConfig setRememberPurgeFrequency(int frequency) {
            rememberPurgeFrequency_ = frequency;
            return this;
        }

        public int getRememberPurgeScale() {
            return rememberPurgeScale_;
        }

        public AuthenticationConfig setRememberPurgeScale(int scale) {
            rememberPurgeScale_ = scale;
            return this;
        }

        public String getTableRole() {
            return tableRole_;
        }

        public AuthenticationConfig setTableRole(String name) {
            tableRole_ = name;
            return this;
        }

        public String getSequenceRole() {
            return sequenceRole_;
        }

        public AuthenticationConfig setSequenceRole(String name) {
            sequenceRole_ = name;
            return this;
        }

        public String getTableUser() {
            return tableUser_;
        }

        public AuthenticationConfig setTableUser(String name) {
            tableUser_ = name;
            return this;
        }

        public String getTableRoleLink() {
            return tableRoleLink_;
        }

        public AuthenticationConfig setTableRoleLink(String name) {
            tableRoleLink_ = name;
            return this;
        }

        public String getTableAuthentication() {
            return tableAuthentication_;
        }

        public AuthenticationConfig setTableAuthentication(String name) {
            tableAuthentication_ = name;
            return this;
        }

        public String getTableRemember() {
            return tableRemember_;
        }

        public AuthenticationConfig setTableRemember(String name) {
            tableRemember_ = name;
            return this;
        }
    }

    public class CmfConfig {
        private String sequenceContentRepository_ = DEFAULT_SEQUENCE_CONTENT_REPOSITORY;
        private String sequenceContentInfo_ = DEFAULT_SEQUENCE_CONTENT_INFO;
        private String tableContentRepository_ = DEFAULT_TABLE_CONTENT_REPOSITORY;
        private String tableContentInfo_ = DEFAULT_TABLE_CONTENT_INFO;
        private String tableContentAttribute_ = DEFAULT_TABLE_CONTENT_ATTRIBUTE;
        private String tableContentProperty_ = DEFAULT_TABLE_CONTENT_PROPERTY;
        private String tableContentStoreImage_ = DEFAULT_TABLE_CONTENT_STORE_IMAGE;
        private String tableContentStoreText_ = DEFAULT_TABLE_CONTENT_STORE_TEXT;
        private String tableContentStoreRawInfo_ = DEFAULT_TABLE_CONTENT_STORE_RAW_INFO;
        private String tableContentStoreRawChunk_ = DEFAULT_TABLE_CONTENT_STORE_RAW_CHUNK;

        private static final String DEFAULT_SEQUENCE_CONTENT_REPOSITORY = "SEQ_CONTENT_REPOSITORY";
        private static final String DEFAULT_SEQUENCE_CONTENT_INFO = "SEQ_CONTENT_INFO";
        private static final String DEFAULT_TABLE_CONTENT_REPOSITORY = "ContentRepository";
        private static final String DEFAULT_TABLE_CONTENT_INFO = "ContentInfo";
        private static final String DEFAULT_TABLE_CONTENT_ATTRIBUTE = "ContentAttribute";
        private static final String DEFAULT_TABLE_CONTENT_PROPERTY = "ContentProperty";
        private static final String DEFAULT_TABLE_CONTENT_STORE_IMAGE = "ContentStoreImage";
        private static final String DEFAULT_TABLE_CONTENT_STORE_TEXT = "ContentStoreText";
        private static final String DEFAULT_TABLE_CONTENT_STORE_RAW_INFO = "ContentStoreRawInfo";
        private static final String DEFAULT_TABLE_CONTENT_STORE_RAW_CHUNK = "ContentStoreRawChunk";

        public String getSequenceContentRepository() {
            return sequenceContentRepository_;
        }

        public CmfConfig setSequenceContentRepository(String sequence) {
            sequenceContentRepository_ = sequence;
            return this;
        }

        public String getSequenceContentInfo() {
            return sequenceContentInfo_;
        }

        public CmfConfig setSequenceContentInfo(String sequence) {
            sequenceContentInfo_ = sequence;
            return this;
        }

        public String getTableContentRepository() {
            return tableContentRepository_;
        }

        public CmfConfig setTableContentRepository(String table) {
            tableContentRepository_ = table;
            return this;
        }

        public String getTableContentInfo() {
            return tableContentInfo_;
        }

        public CmfConfig setTableContentInfo(String table) {
            tableContentInfo_ = table;
            return this;
        }

        public String getTableContentAttribute() {
            return tableContentAttribute_;
        }

        public CmfConfig setTableContentAttribute(String table) {
            tableContentAttribute_ = table;
            return this;
        }

        public String getTableContentProperty() {
            return tableContentProperty_;
        }

        public CmfConfig setTableContentProperty(String table) {
            tableContentProperty_ = table;
            return this;
        }

        public String getTableContentStoreImage() {
            return tableContentStoreImage_;
        }

        public CmfConfig setTableContentStoreImage(String table) {
            tableContentStoreImage_ = table;
            return this;
        }

        public String getTableContentStoreText() {
            return tableContentStoreText_;
        }

        public CmfConfig setTableContentStoreText(String table) {
            tableContentStoreText_ = table;
            return this;
        }

        public String getTableContentStoreRawInfo() {
            return tableContentStoreRawInfo_;
        }

        public CmfConfig setTableContentStoreRawInfo(String table) {
            tableContentStoreRawInfo_ = table;
            return this;
        }

        public String getTableContentStoreRawChunk() {
            return tableContentStoreRawChunk_;
        }

        public CmfConfig setTableContentStoreRawChunk(String table) {
            tableContentStoreRawChunk_ = table;
            return this;
        }
    }

    public class DatabaseConfig {
        private int transactionTimeout_ = DEFAULT_TRANSACTION_TIMEOUT;
        private boolean sqlDebugTrace_ = DEFAULT_SQL_DEBUG_TRACE;

        private static final int DEFAULT_TRANSACTION_TIMEOUT = 0;    // 0 seconds : turned off
        private static final boolean DEFAULT_SQL_DEBUG_TRACE = false;

        public int getTransactionTimeout() {
            return transactionTimeout_;
        }

        public DatabaseConfig setTransactionTimeout(int timeout) {
            transactionTimeout_ = timeout;
            return this;
        }

        public boolean getSqlDebugTrace() {
            return sqlDebugTrace_;
        }

        public DatabaseConfig setSqlDebugTrace(boolean flag) {
            sqlDebugTrace_ = flag;
            return this;
        }
    }

    public class EngineConfig {
        private long continuationDuration_ = ContinuationConfigRuntimeDefaults.DEFAULT_CONTINUATION_DURATION;
        private int continuationPurgeFrequency_ = ContinuationConfigRuntimeDefaults.DEFAULT_CONTINUATION_PURGE_FREQUENCY;
        private int continuationPurgeScale_ = ContinuationConfigRuntimeDefaults.DEFAULT_CONTINUATION_PURGE_SCALE;

        private String defaultContentType_ = DEFAULT_DEFAULT_CONTENT_TYPE;
        private boolean prettyEngineExceptions_ = DEFAULT_PRETTY_ENGINE_EXCEPTIONS;
        private boolean logEngineExceptions_ = DEFAULT_LOG_ENGINE_EXCEPTIONS;
        private String fileUploadPath_ = DEFAULT_FILE_UPLOAD_PATH;
        private long fileUploadSizeLimit_ = DEFAULT_FILE_UPLOAD_SIZE_LIMIT;
        private boolean fileUploadSizeCheck_ = DEFAULT_FILE_UPLOAD_SIZE_CHECK;
        private boolean fileUploadSizeException_ = DEFAULT_FILE_UPLOAD_SIZE_EXCEPTION;
        private boolean globalNoCacheHeaders_ = DEFAULT_GLOBAL_NO_CACHE_HEADERS;
        private boolean gzipCompression_ = DEFAULT_GZIP_COMPRESSION;
        private Collection<String> gzipCompressionTypes_ = DEFAULT_GZIP_COMPRESSION_TYPES;
        private String proxyRootUrl_ = DEFAULT_PROXY_ROOT_URL;
        private String webappContextPath_ = DEFAULT_WEBAPP_CONTEXT_PATH;
        private Set<String> passThroughSuffixes = DEFAULT_PASS_THROUGH_SUFFIXES;
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
        public static final boolean DEFAULT_GLOBAL_NO_CACHE_HEADERS = false;
        public static final boolean DEFAULT_GZIP_COMPRESSION = true;
        public static final Collection<String> DEFAULT_GZIP_COMPRESSION_TYPES = List.of(
            "text/html",
            "text/xml",
            "text/plain",
            "text/css",
            "text/javascript",
            "application/xml",
            "application/xhtml+xml",
            "image/svg+xml"
        );
        public static final String DEFAULT_PROXY_ROOT_URL = null;
        public static final String DEFAULT_WEBAPP_CONTEXT_PATH = null;
        public static final Set<String> DEFAULT_PASS_THROUGH_SUFFIXES = new HashSet<>() {{
            add("gif");
            add("png");
            add("jpg");
            add("jpeg");
            add("bmp");
            add("ico");
            add("css");
            add("js");
            add("swf");
            add("html");
            add("htm");
            add("htc");
            add("class");
            add("jar");
            add("zip");
            add("arj");
            add("gz");
            add("z");
            add("wav");
            add("mp3");
            add("mp4");
            add("m4a");
            add("wma");
            add("mpg");
            add("avi");
            add("ogg");
            add("txt");
            add("ttf");
            add("otf");
        }};

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

        public long getContinuationDuration() {
            return continuationDuration_;
        }

        public EngineConfig setContinuationDuration(long duration) {
            continuationDuration_ = duration;
            return this;
        }

        public int getContinuationPurgeFrequency() {
            return continuationPurgeFrequency_;
        }

        public EngineConfig setContinuationPurgeFrequency(int frequency) {
            continuationPurgeFrequency_ = frequency;
            return this;
        }

        public int getContinuationPurgeScale() {
            return continuationPurgeScale_;
        }

        public EngineConfig setContinuationPurgeScale(int frequency) {
            continuationPurgeScale_ = frequency;
            return this;
        }

        public boolean getGlobalNoCacheHeaders() {
            return globalNoCacheHeaders_;
        }

        public EngineConfig setGlobalNoCacheHeaders(boolean flag) {
            globalNoCacheHeaders_ = flag;
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

        public Set<String> getPassThroughSuffixes() {
            return passThroughSuffixes;
        }

        public EngineConfig setPassThroughSuffixes(Set<String> suffixes) {
            passThroughSuffixes = suffixes;
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

    public class Mime {
        private static final Map<String, String> DEFAULT_MIME_MAPPING = new HashMap<>() {{
            put("ez", "application/andrew-inset");
            put("jnlp", "application/jnlp");
            put("hqx", "application/mac-binhex40");
            put("cpt", "application/mac-compactpro");
            put("mathml", "application/mathml+xml");
            put("bin", "application/octet-stream");
            put("dms", "application/octet-stream");
            put("lha", "application/octet-stream");
            put("lzh", "application/octet-stream");
            put("exe", "application/octet-stream");
            put("class", "application/octet-stream");
            put("so", "application/octet-stream");
            put("dll", "application/octet-stream");
            put("dmg", "application/octet-stream");
            put("oda", "application/oda");
            put("ogg", "application/ogg");
            put("pdf", "application/pdf");
            put("ai", "application/postscript");
            put("eps", "application/postscript");
            put("ps", "application/postscript");
            put("rdf", "application/rdf+xml");
            put("smi", "application/smil");
            put("smil", "application/smil");
            put("gram", "application/srgs");
            put("grxml", "application/srgs+xml");
            put("mif", "application/vnd.mif");
            put("xls", "application/vnd.ms-excel");
            put("ppt", "application/vnd.ms-powerpoint");
            put("rm", "application/vnd.rn-realmedia");
            put("bcpio", "application/x-bcpio");
            put("vcd", "application/x-cdlink");
            put("pgn", "application/x-chess-pgn");
            put("cpio", "application/x-cpio");
            put("csh", "application/x-csh");
            put("dcr", "application/x-director");
            put("dir", "application/x-director");
            put("dxr", "application/x-director");
            put("dvi", "application/x-dvi");
            put("spl", "application/x-futuresplash");
            put("gtar", "application/x-gtar");
            put("hdf", "application/x-hdf");
            put("js", "application/x-javascript");
            put("skp", "application/x-koan");
            put("skd", "application/x-koan");
            put("skt", "application/x-koan");
            put("skm", "application/x-koan");
            put("latex", "application/x-latex");
            put("nc", "application/x-netcdf");
            put("cdf", "application/x-netcdf");
            put("sh", "application/x-sh");
            put("shar", "application/x-shar");
            put("swf", "application/x-shockwave-flash");
            put("sit", "application/x-stuffit");
            put("sv4cpio", "application/x-sv4cpio");
            put("sv4crc", "application/x-sv4crc");
            put("tar", "application/x-tar");
            put("tcl", "application/x-tcl");
            put("tex", "application/x-tex");
            put("texinfo", "application/x-texinfo");
            put("texi", "application/x-texinfo");
            put("t", "application/x-troff");
            put("tr", "application/x-troff");
            put("roff", "application/x-troff");
            put("man", "application/x-troff-man");
            put("me", "application/x-troff-me");
            put("ms", "application/x-troff-ms");
            put("ustar", "application/x-ustar");
            put("src", "application/x-wais-source");
            put("xhtml", "application/xhtml+xml");
            put("xht", "application/xhtml+xml");
            put("xslt", "application/xslt+xml");
            put("xml", "application/xml");
            put("xsl", "application/xml");
            put("dtd", "application/xml-dtd");
            put("zip", "application/zip");
            put("au", "audio/basic");
            put("snd", "audio/basic");
            put("mid", "audio/midi");
            put("midi", "audio/midi");
            put("kar", "audio/midi");
            put("mpga", "audio/mpeg");
            put("mp2", "audio/mpeg");
            put("mp3", "audio/mpeg");
            put("aif", "audio/x-aiff");
            put("aiff", "audio/x-aiff");
            put("aifc", "audio/x-aiff");
            put("m3u", "audio/x-mpegurl");
            put("ram", "audio/x-pn-realaudio");
            put("ra", "audio/x-pn-realaudio");
            put("wav", "audio/x-wav");
            put("pdb", "chemical/x-pdb");
            put("xyz", "chemical/x-xyz");
            put("bmp", "image/bmp");
            put("cgm", "image/cgm");
            put("gif", "image/gif");
            put("ief", "image/ief");
            put("jpeg", "image/jpeg");
            put("jpg", "image/jpeg");
            put("jpe", "image/jpeg");
            put("png", "image/png");
            put("svg", "image/svg+xml");
            put("tiff", "image/tiff");
            put("tif", "image/tiff");
            put("djvu", "image/vnd.djvu");
            put("djv", "image/vnd.djvu");
            put("wbmp", "image/vnd.wap.wbmp");
            put("ras", "image/x-cmu-raster");
            put("ico", "image/x-icon");
            put("pnm", "image/x-portable-anymap");
            put("pbm", "image/x-portable-bitmap");
            put("pgm", "image/x-portable-graymap");
            put("ppm", "image/x-portable-pixmap");
            put("rgb", "image/x-rgb");
            put("xbm", "image/x-xbitmap");
            put("xpm", "image/x-xpixmap");
            put("xwd", "image/x-xwindowdump");
            put("igs", "model/iges");
            put("iges", "model/iges");
            put("msh", "model/mesh");
            put("mesh", "model/mesh");
            put("silo", "model/mesh");
            put("wrl", "model/vrml");
            put("vrml", "model/vrml");
            put("ics", "text/calendar");
            put("ifb", "text/calendar");
            put("css", "text/css");
            put("html", "text/html");
            put("htm", "text/html");
            put("asc", "text/plain");
            put("txt", "text/plain");
            put("rtx", "text/richtext");
            put("rtf", "text/rtf");
            put("sgml", "text/sgml");
            put("sgm", "text/sgml");
            put("tsv", "text/tab-separated-values");
            put("wml", "text/vnd.wap.wml");
            put("wmls", "text/vnd.wap.wmlscript");
            put("etx", "text/x-setext");
            put("htc", "text/x-component");
            put("mpeg", "video/mpeg");
            put("mpg", "video/mpeg");
            put("mpe", "video/mpeg");
            put("qt", "video/quicktime");
            put("mov", "video/quicktime");
            put("mxu", "video/vnd.mpegurl");
            put("m4u", "video/vnd.mpegurl");
            put("avi", "video/x-msvideo");
            put("movie", "video/x-sgi-movie");
            put("ice", "x-conference/x-cooltalk");
        }};

        public static String getMimeType(String extension) {
            return DEFAULT_MIME_MAPPING.get(extension);
        }
    }

    public class ResourcesConfig {
        private String tableResources_ = DEFAULT_TABLE_RESOURCES;

        private static final String DEFAULT_TABLE_RESOURCES = "Resources";

        public String getTableResources() {
            return tableResources_;
        }

        public ResourcesConfig setTableResources(String name) {
            tableResources_ = name;
            return this;
        }
    }

    public class SchedulerConfig {
        private int taskTypeMaximumLength_ = DEFAULT_TASK_TYPE_MAXIMUM_LENGTH;
        private int taskFrequencyMaximumLength_ = DEFAULT_TASK_FREQUENCY_MAXIMUM_LENGTH;
        private int taskoptionValueMaximumLength_ = DEFAULT_TASKOPTION_VALUE_MAXIMUM_LENGTH;
        private int taskoptionNameMaximumLength_ = DEFAULT_TASKOPTION_NAME_MAXIMUM_LENGTH;
        private String tableTask_ = DEFAULT_TABLE_TASK;
        private String sequenceTask_ = DEFAULT_SEQUENCE_TASK;
        private String tableTaskOption_ = DEFAULT_TABLE_TASKOPTION;

        public static final int DEFAULT_TASK_TYPE_MAXIMUM_LENGTH = 255;
        public static final int DEFAULT_TASK_FREQUENCY_MAXIMUM_LENGTH = 255;
        public static final int DEFAULT_TASKOPTION_VALUE_MAXIMUM_LENGTH = 255;
        public static final int DEFAULT_TASKOPTION_NAME_MAXIMUM_LENGTH = 255;
        public static final String DEFAULT_TABLE_TASK = "SchedTask";
        public static final String DEFAULT_SEQUENCE_TASK = "SEQ_SCHEDTASK";
        public static final String DEFAULT_TABLE_TASKOPTION = "SchedTaskOption";

        public int getTaskTypeMaximumLength() {
            return taskTypeMaximumLength_;
        }

        public SchedulerConfig setTaskTypeMaximumLength(int length) {
            taskTypeMaximumLength_ = length;
            return this;
        }

        public int getTaskFrequencyMaximumLength() {
            return taskFrequencyMaximumLength_;
        }

        public SchedulerConfig setTaskFrequencyMaximumLength(int length) {
            taskFrequencyMaximumLength_ = length;
            return this;
        }

        public int getTaskOptionValueMaximumLength() {
            return taskoptionValueMaximumLength_;
        }

        public SchedulerConfig setTaskOptionValueMaximumLength(int length) {
            taskoptionValueMaximumLength_ = length;
            return this;
        }

        public int getTaskOptionNameMaximumLength() {
            return taskoptionNameMaximumLength_;
        }

        public SchedulerConfig setTaskOptionNameMaximumLength(int length) {
            taskoptionNameMaximumLength_ = length;
            return this;
        }

        public String getTableTask() {
            return tableTask_;
        }

        public SchedulerConfig setTableTask(String name) {
            tableTask_ = name;
            return this;
        }

        public String getSequenceTask() {
            return sequenceTask_;
        }

        public SchedulerConfig setSequenceTask(String name) {
            sequenceTask_ = name;
            return this;
        }

        public String getTableTaskOption() {
            return tableTaskOption_;
        }

        public SchedulerConfig setTableTaskOption(String name) {
            tableTaskOption_ = name;
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
            var generation_path = generationPath_;

            if (null == generation_path || generation_path.isEmpty()) {
                return RifeConfig.this.global.getTempPath() + File.separator + DEFAULT_TEMPLATES_RIFE_FOLDER;
            }

            generation_path += File.separator;

            return generation_path;
        }

        public TemplateConfig setGenerationPath(String path) {
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
            var result = getDefaultResourceBundles(factory);
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
        private String defaultInputTimeFormat_ = DEFAULT_INPUT_TIME_FORMAT;
        private String defaultLongDateFormat_;
        private int maxVisualUrlLength_ = DEFAULT_MAX_VISUAL_URL_LENGTH;

        public static final boolean DEFAULT_RESOURCE_BUNDLE_AUTO_RELOAD = true;
        public static final String DEFAULT_RESOURCE_BUNDLE = null;
        public static final String DEFAULT_DEFAULT_LANGUAGE = "en";
        public static final String DEFAULT_DEFAULT_COUNTRY = null;
        public static final TimeZone DEFAULT_DEFAULT_TIMEZONE = TimeZone.getTimeZone("EST");
        public static final String DEFAULT_INPUT_DATE_FORMAT = "yyyy-MM-dd HH:mm";
        public static final String DEFAULT_INPUT_TIME_FORMAT = "HH:mm";
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

            if (null == countryCode) {
                defaultCountry_ = DEFAULT_DEFAULT_COUNTRY;
            } else {
                defaultCountry_ = countryCode;
            }
            return this;
        }

        public TimeZone getDefaultTimeZone() {
            TimeZone result = defaultTimeZone_;

            if (null == result) {
                result = TimeZone.getDefault();
            }

            return result;
        }

        public ZoneId getDefaultZoneId() {
            return getDefaultTimeZone().toZoneId();
        }

        public ToolsConfig setDefaultTimeZone(TimeZone timeZone) {
            defaultTimeZone_ = timeZone;
            return this;
        }

        public DateFormat getSimpleDateFormat(String pattern) {
            try {
                var sf = new SimpleDateFormat(pattern, Localization.getLocale());
                sf.setTimeZone(getDefaultTimeZone());
                return sf;
            } catch (IllegalArgumentException e) {
                throw new DateFormatInitializationException(e.getMessage());
            }
        }

        public DateTimeFormatter getDateTimeFormatter(String pattern) {
            try {
                return DateTimeFormatter.ofPattern(pattern, Localization.getLocale()).withZone(getDefaultZoneId());
            } catch (IllegalArgumentException e) {
                throw new DateFormatInitializationException(e.getMessage());
            }
        }

        public Calendar getCalendarInstance(int year, int month, int date, int hourOfDay, int minute, int seconds) {
            return getCalendarInstance(year, month, date, hourOfDay, minute, seconds, 0);
        }

        public Calendar getCalendarInstance(int year, int month, int date, int hourOfDay, int minute, int seconds, int milliseconds) {
            var cal = getCalendarInstance();
            cal.set(year, month, date, hourOfDay, minute, seconds);
            cal.set(Calendar.MILLISECOND, milliseconds);
            return cal;
        }

        public Calendar getCalendarInstance() {
            return GregorianCalendar.getInstance(RifeConfig.tools().getDefaultTimeZone(), Localization.getLocale());
        }

        public Calendar getSystemCalendarInstance(int year, int month, int date, int hourOfDay, int minute, int seconds) {
            return getSystemCalendarInstance(year, month, date, hourOfDay, minute, seconds, 0);
        }

        public Calendar getSystemCalendarInstance(int year, int month, int date, int hourOfDay, int minute, int seconds, int milliseconds) {
            var cal = getSystemCalendarInstance();
            cal.set(year, month, date, hourOfDay, minute, seconds);
            cal.set(Calendar.MILLISECOND, milliseconds);
            return cal;
        }

        public Calendar getSystemCalendarInstance() {
            return GregorianCalendar.getInstance(TimeZone.getDefault(), Localization.getLocale());
        }

        public DateFormat getDefaultShortDateFormat() {
            if (defaultShortDateFormat_ != null) {
                return getSimpleDateFormat(defaultShortDateFormat_);
            } else {
                if (0 != getDefaultLanguage().compareToIgnoreCase(DEFAULT_DEFAULT_LANGUAGE)) {
                    var sf = DateFormat.getDateInstance(DateFormat.SHORT, Localization.getLocale());
                    sf.setTimeZone(getDefaultTimeZone());
                    return sf;
                }

                var sf = DateFormat.getDateInstance(DateFormat.SHORT, Locale.ENGLISH);
                sf.setTimeZone(getDefaultTimeZone());
                return sf;
            }
        }

        public DateTimeFormatter getDefaultShortDateTimeFormatter() {
            if (defaultShortDateFormat_ != null) {
                return getDateTimeFormatter(defaultShortDateFormat_);
            } else {
                return DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).localizedBy(Localization.getLocale()).withZone(getDefaultZoneId());
            }
        }

        public ToolsConfig setDefaultShortDatePattern(String pattern) {
            if (null != pattern &&
                pattern.isEmpty()) throw new IllegalArgumentException("format can't be empty.");
            defaultShortDateFormat_ = pattern;
            return this;
        }

        public DateFormat getDefaultLongDateFormat() {
            if (defaultLongDateFormat_ != null) {
                return getSimpleDateFormat(defaultLongDateFormat_);
            } else {
                if (0 != getDefaultLanguage().compareToIgnoreCase(DEFAULT_DEFAULT_LANGUAGE)) {
                    var sf = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Localization.getLocale());
                    sf.setTimeZone(getDefaultTimeZone());
                    return sf;
                }

                var sf = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.ENGLISH);
                sf.setTimeZone(getDefaultTimeZone());
                return sf;
            }
        }

        public DateTimeFormatter getDefaultLongDateTimeFormatter() {
            if (defaultLongDateFormat_ != null) {
                return getDateTimeFormatter(defaultLongDateFormat_);
            } else {
                return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).localizedBy(Localization.getLocale()).withZone(getDefaultZoneId());
            }
        }

        public ToolsConfig setDefaultLongDatePattern(String pattern) {
            if (null != pattern &&
                pattern.isEmpty()) throw new IllegalArgumentException("format can't be empty.");
            defaultLongDateFormat_ = pattern;
            return this;
        }

        public DateFormat getDefaultInputDateFormat() {
            return getSimpleDateFormat(defaultInputDateFormat_);
        }

        public DateTimeFormatter getDefaultInputDateTimeFormatter() {
            return getDateTimeFormatter(defaultInputDateFormat_);
        }

        public ToolsConfig setDefaultInputDatePattern(String pattern) {
            if (null != pattern &&
                pattern.isEmpty()) throw new IllegalArgumentException("format can't be empty.");
            defaultInputDateFormat_ = pattern;
            return this;
        }

        public DateFormat getDefaultInputTimeFormat() {
            return getSimpleDateFormat(defaultInputTimeFormat_);
        }

        public DateTimeFormatter getDefaultInputTimeFormatter() {
            return getDateTimeFormatter(defaultInputTimeFormat_);
        }

        public ToolsConfig setDefaultInputTimeFormat(String format) {
            if (null != format &&
                format.isEmpty()) throw new IllegalArgumentException("format can't be empty.");
            defaultInputTimeFormat_ = format;
            return this;
        }

        public DateFormat getConcisePreciseDateFormat() {
            return getSimpleDateFormat("yyyyMMddHHmmssSSSZ");
        }

        public DateFormat getConcisePreciseTimeFormat() {
            return getSimpleDateFormat("HHmmssSSSZ");
        }

        public DateTimeFormatter getConcisePreciseDateTimeFormatter() {
            return getDateTimeFormatter("yyyyMMddHHmmssSSSZ");
        }

        public DateTimeFormatter getConcisePreciseTimeFormatter() {
            return getDateTimeFormatter("HHmmssSSSZ");
        }

        public int getMaxVisualUrlLength() {
            return maxVisualUrlLength_;
        }

        public ToolsConfig setMaxVisualUrlLength(int length) {
            maxVisualUrlLength_ = length;
            return this;
        }
    }

    public class XmlConfig {
        private boolean xmlValidation_ = DEFAULT_XML_VALIDATION;

        private static final boolean DEFAULT_XML_VALIDATION = false;

        public boolean getXmlValidation() {
            return xmlValidation_;
        }

        public XmlConfig setXmlValidation(boolean flag) {
            xmlValidation_ = flag;
            return this;
        }
    }
}
