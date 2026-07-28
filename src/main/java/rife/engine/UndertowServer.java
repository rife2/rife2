/*
 * Copyright 2026 Erik C. Thauvin (https://erik.thauvin.net/)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.security.idm.Account;
import io.undertow.security.idm.Credential;
import io.undertow.security.idm.IdentityManager;
import io.undertow.security.idm.PasswordCredential;
import io.undertow.server.handlers.encoding.ContentEncodingRepository;
import io.undertow.server.handlers.encoding.DeflateEncodingProvider;
import io.undertow.server.handlers.encoding.EncodingHandler;
import io.undertow.server.handlers.encoding.GzipEncodingProvider;
import io.undertow.server.handlers.resource.PathResourceManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.*;
import io.undertow.servlet.handlers.DefaultServlet;
import io.undertow.servlet.util.ImmediateInstanceFactory;
import jakarta.servlet.DispatcherType;
import rife.ioc.HierarchicalProperties;
import rife.servlet.RifeFilter;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.*;

import static io.undertow.servlet.Servlets.deployment;

/**
 * Embedded Undertow server for RIFE2.
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.10
 */
public class UndertowServer {
    private static final int DEFAULT_RESOURCE_TRANSFER_MIN_SIZE = 1024;
    private static final String REALM = "RIFE2";
    private static final String UNDERTOW_PREFIX = "[Undertow] ";

    private final HierarchicalProperties properties_;
    private final Map<String, char[]> accounts_ = new HashMap<>();
    private final Map<String, Set<String>> roles_ = new HashMap<>();

    private Path resourcePath_ = Path.of(".").toAbsolutePath().normalize();
    private String host_ = "localhost";
    private int port_ = 8080;
    private boolean enableHttp2_ = false;
    private boolean enableCompression_ = false;
    private int connectionTimeout_ = -1;
    private SSLContext sslContext_;

    private volatile Undertow undertow_;
    private volatile DeploymentManager manager_;

    public UndertowServer() {
        properties_ = new HierarchicalProperties().parent(HierarchicalProperties.createSystemInstance());
    }

    /**
     * Converts a char array to a UTF-8 byte array without going through an
     * intermediate {@link String}, so the secret isn't retained in the string pool.
     * <p>
     * Only the freshly-encoded byte buffer is zeroed afterward; the caller's source
     * {@code chars} array is never modified, since {@link CharBuffer#wrap(char[])}
     * aliases the given array rather than copying it — zeroing it here would corrupt
     * the caller's stored copy (e.g. an account's password) on first use.
     * <p>
     * Caller is responsible for zeroing the returned array.
     */
    private static byte[] toBytes(char[] chars) {
        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(charBuffer);
        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes);
        if (byteBuffer.hasArray()) {
            Arrays.fill(byteBuffer.array(), (byte) 0);
        } else {
            for (int i = byteBuffer.position(); i < byteBuffer.limit(); i++) {
                byteBuffer.put(i, (byte) 0);
            }
        }
        return bytes;
    }

    private static byte[] sha256(byte[] data) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Configures whether HTTP/2 should be enabled.
     *
     * @param enableHttp2 true to enable HTTP/2
     * @return the instance of the server that's being configured
     * @since 1.10
     */
    public UndertowServer enableHttp2(boolean enableHttp2) {
        enableHttp2_ = enableHttp2;
        return this;
    }

    /**
     * Configures whether response compression should be enabled.
     *
     * @param enableCompression true to enable compression
     * @return the instance of the server that's being configured
     * @since 1.10
     */
    public UndertowServer enableCompression(boolean enableCompression) {
        enableCompression_ = enableCompression;
        return this;
    }

    /**
     * Sets the connection timeout in milliseconds.
     *
     * @param timeout the timeout in milliseconds
     * @return the instance of the server that's being configured
     * @since 1.10
     */
    public UndertowServer connectionTimeout(int timeout) {
        connectionTimeout_ = timeout;
        return this;
    }

    /**
     * Sets the {@link SSLContext} used for HTTPS.
     *
     * @param sslContext the SSL context
     * @return the instance of the server that's being configured
     * @since 1.10
     */
    public UndertowServer sslContext(SSLContext sslContext) {
        sslContext_ = Objects.requireNonNull(sslContext, "sslContext must not be null");
        return this;
    }

    /**
     * Sets a JKS keystore for HTTPS from a file path.
     *
     * @param keyStorePath     the path to the JKS keystore
     * @param keyStorePassword the keystore password
     * @return the instance of the server that's being configured
     * @since 1.10
     */
    public UndertowServer keyStore(String keyStorePath, String keyStorePassword) {
        Objects.requireNonNull(keyStorePath, "keyStorePath must not be null");
        return keyStore(Path.of(keyStorePath), keyStorePassword);
    }

    /**
     * Sets a JKS keystore for HTTPS from a file path.
     *
     * @param keyStorePath     the path to the JKS keystore
     * @param keyStorePassword the keystore password
     * @return the instance of the server that's being configured
     * @since 1.10
     */
    public UndertowServer keyStore(Path keyStorePath, String keyStorePassword) {
        Objects.requireNonNull(keyStorePath, "keyStorePath must not be null");
        Objects.requireNonNull(keyStorePassword, "keyStorePassword must not be null");
        char[] chars = keyStorePassword.toCharArray();
        try {
            return keyStore(keyStorePath, chars);
        } finally {
            Arrays.fill(chars, '\0');
        }
    }

    /**
     * Sets a keystore for HTTPS from a file path using a char array password.
     * Tries the JVM default keystore type (PKCS12 on modern JDKs) first, falling back to JKS.
     *
     * @param keyStorePath     the path to the keystore
     * @param keyStorePassword the keystore password as char array
     * @return the instance of the server that's being configured
     * @since 1.10
     */
    public UndertowServer keyStore(Path keyStorePath, char[] keyStorePassword) {
        Objects.requireNonNull(keyStorePath, "keyStorePath must not be null");
        Objects.requireNonNull(keyStorePassword, "keyStorePassword must not be null");
        char[] pwdCopy = Arrays.copyOf(keyStorePassword, keyStorePassword.length);
        try (InputStream is = Files.newInputStream(keyStorePath)) {
            KeyStore ks;
            try {
                ks = KeyStore.getInstance(KeyStore.getDefaultType());
                ks.load(is, pwdCopy);
            } catch (Exception firstEx) {
                try (InputStream is2 = Files.newInputStream(keyStorePath)) {
                    ks = KeyStore.getInstance("JKS");
                    ks.load(is2, pwdCopy);
                } catch (Exception secondEx) {
                    secondEx.addSuppressed(firstEx);
                    throw secondEx;
                }
            }
            var kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, pwdCopy);
            var tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);
            var ctx = SSLContext.getInstance("TLS");
            ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            sslContext_ = ctx;
            return this;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load keystore: " + keyStorePath, e);
        } finally {
            Arrays.fill(pwdCopy, '\0');
        }
    }

    /**
     * Adds an account for BASIC authentication.
     *
     * @param id       the account id
     * @param password the password
     * @return the instance of the server that's being configured
     * @since 1.10
     */
    public UndertowServer addAccount(String id, String password) {
        Objects.requireNonNull(password, "password must not be null");
        char[] chars = password.toCharArray();
        try {
            return addAccount(id, chars);
        } finally {
            Arrays.fill(chars, '\0');
        }
    }

    /**
     * Adds an account for BASIC authentication using a char array to avoid String retention.
     *
     * @param id       the account id
     * @param password the password as char array (will be copied)
     * @return the instance of the server that's being configured
     * @since 1.10
     */
    public UndertowServer addAccount(String id, char[] password) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(password, "password must not be null");
        accounts_.put(id, Arrays.copyOf(password, password.length));
        return this;
    }

    /**
     * Adds an account with roles for BASIC authentication.
     *
     * @param id       the account id
     * @param password the password
     * @param roles    the roles assigned to the account
     * @return the instance of the server that's being configured
     * @since 1.10
     */
    public UndertowServer addAccount(String id, String password, String... roles) {
        Objects.requireNonNull(roles, "roles must not be null");
        addAccount(id, password);
        for (String role : roles) addRole(id, role);
        return this;
    }

    /**
     * Adds an account with roles for BASIC authentication using a char array password.
     *
     * @param id       the account id
     * @param password the password as char array
     * @param roles    the roles assigned to the account
     * @return the instance of the server that's being configured
     * @since 1.10
     */
    public UndertowServer addAccount(String id, char[] password, String... roles) {
        Objects.requireNonNull(roles, "roles must not be null");
        addAccount(id, password);
        for (String role : roles) addRole(id, role);
        return this;
    }

    /**
     * Adds a role to an existing account.
     *
     * @param id   the account id
     * @param role the role name
     * @return the instance of the server that's being configured
     * @since 1.10
     */
    public UndertowServer addRole(String id, String role) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(role, "role must not be null");
        if (!accounts_.containsKey(id)) {
            System.out.println(UNDERTOW_PREFIX + "Adding role to unknown account: " + id);
        }
        roles_.computeIfAbsent(id, k -> new HashSet<>()).add(role);
        return this;
    }

    /**
     * Sets the resource path used by the {@link PathResourceManager}.
     *
     * @param resourcePath the path to static resources
     * @return the instance of the server that's being configured
     * @since 1.10
     */
    public UndertowServer resourcePath(String resourcePath) {
        Objects.requireNonNull(resourcePath, "resourcePath must not be null");
        return resourcePath(Path.of(resourcePath));
    }

    /**
     * Sets the resource path used by the {@link PathResourceManager}.
     *
     * @param resourcePath the path to static resources
     * @return the instance of the server that's being configured
     * @since 1.10
     */
    public UndertowServer resourcePath(Path resourcePath) {
        Objects.requireNonNull(resourcePath, "resourcePath must not be null");
        resourcePath_ = resourcePath.toAbsolutePath().normalize();
        return this;
    }

    /**
     * Sets the document root for static resources.
     * <p>
     * Alias for {@link #resourcePath(Path)}.
     *
     * @param documentRoot the document root path
     * @return the instance of the server that's being configured
     * @since 1.10
     */
    public UndertowServer documentRoot(String documentRoot) {
        return resourcePath(documentRoot);
    }

    /**
     * Sets the document root for static resources.
     * <p>
     * Alias for {@link #resourcePath(Path)}.
     *
     * @param documentRoot the document root path
     * @return the instance of the server that's being configured
     * @since 1.10
     */
    public UndertowServer documentRoot(Path documentRoot) {
        return resourcePath(documentRoot);
    }

    /**
     * Sets the host address the HTTP listener will bind to.
     *
     * @param host the hostname or IP address
     * @return the instance of the server that's being configured
     * @since 1.10
     */
    public UndertowServer host(String host) {
        host_ = Objects.requireNonNull(host, "host must not be null");
        return this;
    }

    /**
     * Sets the port the HTTP or HTTPS listener will listen on.
     *
     * @param port the port number, between 0 and 65535
     * @return the instance of the server that's being configured
     * @throws IllegalArgumentException if port is out of range
     * @since 1.10
     */
    public UndertowServer port(int port) {
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("port must be between 0 and 65535, was: " + port);
        }
        port_ = port;
        return this;
    }

    /**
     * Returns the hierarchical properties used to configure RIFE2.
     *
     * @return the properties instance
     * @since 1.10
     */
    public HierarchicalProperties properties() {
        return properties_;
    }

    /**
     * Starts the Undertow server with the given RIFE2 site.
     *
     * @param site the RIFE2 site to serve
     * @return the instance of the started server
     * @since 1.10
     */
    public UndertowServer start(Site site) {
        Objects.requireNonNull(site, "site must not be null");
        if (enableHttp2_ && sslContext_ == null) {
            System.out.println(UNDERTOW_PREFIX +
                "HTTP/2 enabled without TLS: will use h2c (not supported by browsers). Use sslContext() for h2.");
        }
        try {
            var rifeFilter = new RifeFilter();
            rifeFilter.init(properties_, site);
            var rifeFilterInfo = new FilterInfo("RIFE2", RifeFilter.class, new ImmediateInstanceFactory<>(rifeFilter))
                .setAsyncSupported(true);
            var defaultServlet = Servlets.servlet("default-servlet", DefaultServlet.class)
                .addMapping("/")
                .setAsyncSupported(true);
            DeploymentInfo deployment = deployment()
                .setClassLoader(UndertowServer.class.getClassLoader())
                .setContextPath("/")
                .setDeploymentName("rife2")
                .setResourceManager(new PathResourceManager(resourcePath_, DEFAULT_RESOURCE_TRANSFER_MIN_SIZE))
                .addFilter(rifeFilterInfo)
                .addFilterUrlMapping("RIFE2", "/*", DispatcherType.REQUEST)
                .addFilterUrlMapping("RIFE2", "/*", DispatcherType.FORWARD)
                .addFilterUrlMapping("RIFE2", "/*", DispatcherType.ASYNC)
                .addServlet(defaultServlet);

            if (!accounts_.isEmpty()) {
                deployment.setIdentityManager(new InMemoryIdentityManager(accounts_, roles_));
                deployment.addSecurityConstraint(new SecurityConstraint()
                    .addWebResourceCollection(new WebResourceCollection().addUrlPattern("/*"))
                    .setEmptyRoleSemantic(SecurityInfo.EmptyRoleSemantic.AUTHENTICATE));
                deployment.setLoginConfig(new LoginConfig("BASIC", REALM));
            }

            manager_ = Servlets.defaultContainer().addDeployment(deployment);
            manager_.deploy();
            var servletHandler = manager_.start();
            var pathHandler = Handlers.path(Handlers.redirect("/")).addPrefixPath("/", servletHandler);

            io.undertow.server.HttpHandler rootHandler = pathHandler;
            if (enableCompression_) {
                rootHandler = new EncodingHandler(pathHandler,
                    new ContentEncodingRepository()
                        .addEncodingHandler("gzip", new GzipEncodingProvider(), 100)
                        .addEncodingHandler("deflate", new DeflateEncodingProvider(), 50));
            }

            var builder = Undertow.builder();

            if (sslContext_ != null) {
                builder.addHttpsListener(port_, host_, sslContext_);
            } else {
                builder.addHttpListener(port_, host_);
            }

            builder.setHandler(rootHandler);

            if (enableHttp2_) {
                builder.setServerOption(UndertowOptions.ENABLE_HTTP2, true);
            }

            if (connectionTimeout_ >= 0) {
                builder.setServerOption(UndertowOptions.NO_REQUEST_TIMEOUT, connectionTimeout_);
                builder.setServerOption(UndertowOptions.IDLE_TIMEOUT, connectionTimeout_);
                builder.setServerOption(UndertowOptions.REQUEST_PARSE_TIMEOUT, connectionTimeout_);
                builder.setSocketOption(org.xnio.Options.READ_TIMEOUT, connectionTimeout_);
            }

            undertow_ = builder.build();
            undertow_.start();
            return this;
        } catch (Exception e) {
            // Undeploy/stop anything that was already brought up before the failure,
            // so a failed start() doesn't leave a registered deployment or a bound listener behind.
            stop();
            throw new RuntimeException("Failed to start Undertow server", e);
        }
    }

    /**
     * Stops the server and undeploys the deployment.
     *
     * @since 1.10
     */
    public void stop() {
        try {
            if (manager_ != null) {
                var deployment = manager_.getDeployment();
                var deploymentInfo = deployment != null ? deployment.getDeploymentInfo() : null;
                try {
                    manager_.stop();
                } catch (Exception e) {
                    System.err.println(UNDERTOW_PREFIX + "Failed to stop deployment manager: " + e.getLocalizedMessage());
                }
                try {
                    manager_.undeploy();
                } catch (Exception e) {
                    System.err.println(UNDERTOW_PREFIX + "Failed to undeploy deployment: " + e.getLocalizedMessage());
                }
                if (deploymentInfo != null) {
                    try {
                        Servlets.defaultContainer().removeDeployment(deploymentInfo);
                    } catch (Exception e) {
                        System.err.println(UNDERTOW_PREFIX + "Failed to remove deployment: " + e.getLocalizedMessage());
                    }
                }
                manager_ = null;
            }
            if (undertow_ != null) {
                undertow_.stop();
                undertow_ = null;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to stop server", e);
        }
    }

    private record InMemoryIdentityManager(Map<String, char[]> accounts,
                                           Map<String, Set<String>> roles) implements IdentityManager {
        // Fixed-length placeholder compared against on every call, including when the account id is
        // unknown, so verification takes the same time whether or not the id exists. Without this,
        // the early return for a missing id makes lookups measurably faster than a real password
        // comparison, leaking valid usernames through timing.
        private static final char[] DUMMY_PASSWORD = "timing-attack-mitigation-placeholder".toCharArray();

        private InMemoryIdentityManager(Map<String, char[]> accounts, Map<String, Set<String>> roles) {
            var accountsCopy = new HashMap<String, char[]>(accounts.size());
            accounts.forEach((id, password) -> accountsCopy.put(id, Arrays.copyOf(password, password.length)));
            this.accounts = Map.copyOf(accountsCopy);

            var rolesCopy = new HashMap<String, Set<String>>(roles.size());
            roles.forEach((id, roleSet) -> rolesCopy.put(id, Set.copyOf(roleSet)));
            this.roles = Map.copyOf(rolesCopy);
        }

        @Override
        public Account verify(Account account) {
            if (account == null) return null;
            String name = account.getPrincipal().getName();
            if (!accounts.containsKey(name)) return null;
            return account;
        }

        @Override
        public Account verify(Credential credential) {
            return null;
        }

        @Override
        public Account verify(String id, Credential credential) {
            if (!(credential instanceof PasswordCredential pc)) return null;

            char[] expected = accounts.get(id);
            boolean found = expected != null;
            char[] toCheck = found ? expected : DUMMY_PASSWORD;

            byte[] expectedBytes = toBytes(toCheck);
            byte[] providedBytes = toBytes(pc.getPassword());
            byte[] expectedHash;
            byte[] providedHash;
            boolean matches;
            try {
                // Hash to fixed length so MessageDigest.isEqual doesn't leak length
                expectedHash = sha256(expectedBytes);
                providedHash = sha256(providedBytes);
                matches = MessageDigest.isEqual(expectedHash, providedHash);
            } finally {
                Arrays.fill(expectedBytes, (byte) 0);
                Arrays.fill(providedBytes, (byte) 0);
            }

            if (!found || !matches) return null;

            return new Account() {
                @Override
                public Principal getPrincipal() {
                    return () -> id;
                }

                @Override
                public Set<String> getRoles() {
                    return roles.getOrDefault(id, Set.of());
                }
            };
        }
    }
}