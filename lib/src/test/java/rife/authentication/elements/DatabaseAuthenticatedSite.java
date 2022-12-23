/*
 * Copyright 2001-2022 Geert Bevin (jdevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.elements;

import rife.authentication.credentialsmanagers.RoleUserAttributes;
import rife.authentication.sessionvalidators.*;
import rife.database.Datasource;
import rife.engine.*;
import rife.template.TemplateFactory;

public class DatabaseAuthenticatedSite extends Site implements AutoCloseable {
    DatabaseSessionValidator validator;
    AuthConfig config;
    AuthConfig configAdmin;
    AuthConfig configMaint;
    AuthConfig configNotEnforced;

    public DatabaseAuthenticatedSite(Datasource datasource) {
        validator = DatabaseSessionValidatorFactory.getInstance(datasource);
        config = new AuthConfig(validator);
        configAdmin = new AuthConfig(validator);
        configMaint = new AuthConfig(validator);
        configNotEnforced = new AuthConfig(validator);

        remove();

        validator.getSessionManager().install();
        validator.getCredentialsManager().install();
        validator.getRememberManager().install();

        validator.getCredentialsManager()
            .addRole("admin")
            .addRole("maint")
            .addUser("guest", new RoleUserAttributes()
                .password("guestpass"))
            .addUser("jdevin", new RoleUserAttributes("yeolpass")
                .role("admin")
                .role("maint"))
            .addUser("guestencrypted", new RoleUserAttributes()
                .password("SHA:duH5g2aTTgh6206iakXKII5qs0A="))
            .addUser("jdevinencrypted", new RoleUserAttributes("MD5:JJSy0mVyeMFG9f21yHQVyg==")
                .role("admin")
                .role("maint"))
            .addUser("johndoe", new RoleUserAttributes()
                .password("thepassofbass")
                .role("maint"))
            .addUser("johndoeencrypted", new RoleUserAttributes()
                .password("SHA:gQiKJIbndDSD37lfjKG4wFXpf0s=")
                .role("maint"));

    }

    public void close() {
        remove();
    }

    static class AuthenticatedSection extends Router {
        AuthenticatedSection(AuthConfig config) {
            landing = get("/landing", c -> c.print("Landing"));
            logout = get("/logout", new Logout(config, TemplateFactory.HTML.get("authentication.logout")));
            template = get("/template", c -> c.print(c.template("filtered_tags_auth")));
            get("/username", c -> c.print(config.identityAttribute(c) != null ? config.identityAttribute(c).getLogin() : "not logged in"));
            group(new Router() {
                public void setup() {
                    before(new Logout(config));
                    get("/beforelogout", c -> c.print("logged out"));
                }
            });
        }

        Route landing;
        Route logout;
        Route template;
    }

    Route login;
    AuthenticatedSection auth;
    Route loginAdmin;
    AuthenticatedSection authAdmin;
    Route loginMaint;
    AuthenticatedSection authMaint;
    Route loginNotEnforced;
    AuthenticatedSection authNotEnforced;

    public void remove() {
        try {
            validator.getRememberManager().remove();
        } catch (Exception ignored) {
        }
        try {
            validator.getCredentialsManager().remove();
        } catch (Exception ignored) {
        }
        try {
            validator.getSessionManager().remove();
        } catch (Exception ignored) {
        }
    }

    public void setup() {
        login = route("/login", new Login(config, TemplateFactory.HTML.get("authentication.login")));
        auth = group(new AuthenticatedSection(config) {
            public void setup() {
                before(new Authenticated(config));
            }
        });
        loginAdmin = route("/loginAdmin", new Login(configAdmin, TemplateFactory.HTML.get("authentication.login")));
        authAdmin = group("/admin", new AuthenticatedSection(configAdmin) {
            public void setup() {
                before(new Authenticated(configAdmin));
            }
        });
        loginMaint = route("/loginMaint", new Login(configMaint, TemplateFactory.HTML.get("authentication.login")));
        authMaint = group("/maint", new AuthenticatedSection(configMaint) {
            public void setup() {
                before(new Authenticated(configMaint));
            }
        });
        loginNotEnforced = route("/loginNotEnforced", new Login(configNotEnforced, TemplateFactory.HTML.get("authentication.login")));
        authNotEnforced = group("/notEnforced", new AuthenticatedSection(configNotEnforced) {
            public void setup() {
                before(new Authenticated(configNotEnforced));
            }
        });
        Route loginRemember = route("/loginRemember", new Login(config, TemplateFactory.HTML.get("authentication.loginRemember")));
        MemoryAuthenticatedSite.AuthenticatedSection authRemember = group(new MemoryAuthenticatedSite.AuthenticatedSection(config) {
            public void setup() {
                before(new Authenticated(config));
            }
        });

        config
            .loginRoute(login)
            .landingRoute(auth.landing);
        configAdmin
            .role("admin")
            .loginRoute(loginAdmin)
            .landingRoute(authAdmin.landing);
        configMaint
            .role("maint")
            .loginRoute(loginMaint)
            .landingRoute(authMaint.landing);
        configNotEnforced
            .enforceAuthentication(false)
            .loginRoute(loginNotEnforced)
            .landingRoute(authNotEnforced.landing);
    }
}