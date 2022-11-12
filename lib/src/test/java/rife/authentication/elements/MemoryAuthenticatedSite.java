/*
 * Copyright 2001-2022 Geert Bevin (jdevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.elements;

import rife.authentication.credentialsmanagers.RoleUserAttributes;
import rife.authentication.sessionvalidators.MemorySessionValidator;
import rife.engine.*;
import rife.template.TemplateFactory;

public class MemoryAuthenticatedSite extends Site {
    final MemorySessionValidator validator = new MemorySessionValidator();
    final AuthenticationConfig config = new AuthenticationConfig(validator);
    final AuthenticationConfig configAdmin = new AuthenticationConfig(validator);
    final AuthenticationConfig configMaint = new AuthenticationConfig(validator);
    final AuthenticationConfig configNotEnforced = new AuthenticationConfig(validator);

    static class AuthenticatedSection extends Router {
        AuthenticatedSection(AuthenticationConfig config) {
            landing = get("/landing", c -> c.print("Landing"));
            logout = get("/logout", new Logout(config, TemplateFactory.HTML.get("authentication.logout")));
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
    }

    Route login = route("/login", new Login(config, TemplateFactory.HTML.get("authentication.login")));
    AuthenticatedSection auth = group(new AuthenticatedSection(config) {
        public void setup() {
            before(new Authenticated(config));
        }
    });
    Route loginAdmin = route("/loginAdmin", new Login(configAdmin, TemplateFactory.HTML.get("authentication.login")));
    AuthenticatedSection authAdmin = group("/admin", new AuthenticatedSection(configAdmin) {
        public void setup() {
            before(new Authenticated(configAdmin));
        }
    });
    Route loginMaint = route("/loginMaint", new Login(configMaint, TemplateFactory.HTML.get("authentication.login")));
    AuthenticatedSection authMaint = group("/maint", new AuthenticatedSection(configMaint) {
        public void setup() {
            before(new Authenticated(configMaint));
        }
    });
    Route loginNotEnforced = route("/loginNotEnforced", new Login(configNotEnforced, TemplateFactory.HTML.get("authentication.login")));
    AuthenticatedSection authNotEnforced = group("/notEnforced", new AuthenticatedSection(configNotEnforced) {
        public void setup() {
            before(new Authenticated(configNotEnforced));
        }
    });

    public void setup() {
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

        validator.getMemoryUsers()
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
}