/*
 * Copyright 2001-2023 Geert Bevin (jdevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.elements;

import rife.authentication.credentialsmanagers.RoleUserAttributes;
import rife.authentication.sessionvalidators.MemorySessionValidator;
import rife.engine.Route;
import rife.engine.Router;
import rife.engine.Site;
import rife.template.TemplateFactory;

public class MemoryAuthenticatedSite extends Site {
    final MemorySessionValidator validator = new MemorySessionValidator();
    final AuthConfig config = new AuthConfig(validator);
    final AuthConfig configAdmin = new AuthConfig(validator);
    final AuthConfig configMaint = new AuthConfig(validator);
    final AuthConfig configNotEnforced = new AuthConfig(validator);

    public MemoryAuthenticatedSite() {
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

    static class AuthenticatedSection extends Router {
        AuthenticatedSection(AuthConfig config) {
            landing = get("/landing", c -> c.print("Landing"));
            logout = get("/logout", new Logout(config, TemplateFactory.HTML.get("authentication.logout")));
            template = get("/template", c -> c.print(c.template("filtered_tags_auth")));
            get("/username", c -> c.print(config.identityAttribute(c) != null ? config.identityAttribute(c).getLogin() : "not logged in"));
            group(new Router() {
                @Override
                public void setup() {
                    before(new Logout(config));
                    get("/beforelogout", c -> c.print("logged out"));
                }
            });
        }

        final Route landing;
        final Route logout;
        final Route template;
    }

    final Route login = route("/login", new Login(config, TemplateFactory.HTML.get("authentication.login")));
    final AuthenticatedSection auth = group(new AuthenticatedSection(config) {
        @Override
        public void setup() {
            before(new Authenticated(config));
        }
    });
    final Route loginAdmin = route("/loginAdmin", new Login(configAdmin, TemplateFactory.HTML.get("authentication.login")));
    final AuthenticatedSection authAdmin = group("/admin", new AuthenticatedSection(configAdmin) {
        @Override
        public void setup() {
            before(new Authenticated(configAdmin));
        }
    });
    final Route loginMaint = route("/loginMaint", new Login(configMaint, TemplateFactory.HTML.get("authentication.login")));
    final AuthenticatedSection authMaint = group("/maint", new AuthenticatedSection(configMaint) {
        @Override
        public void setup() {
            before(new Authenticated(configMaint));
        }
    });
    final Route loginNotEnforced = route("/loginNotEnforced", new Login(configNotEnforced, TemplateFactory.HTML.get("authentication.login")));
    final AuthenticatedSection authNotEnforced = group("/notEnforced", new AuthenticatedSection(configNotEnforced) {
        @Override
        public void setup() {
            before(new Authenticated(configNotEnforced));
        }
    });

    @Override
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
    }
}