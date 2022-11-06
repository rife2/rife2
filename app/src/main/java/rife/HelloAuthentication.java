/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife;

import rife.authentication.credentialsmanagers.RoleUserAttributes;
import rife.authentication.elements.*;
import rife.authentication.sessionvalidators.MemorySessionValidator;
import rife.engine.*;
import rife.template.TemplateFactory;

public class HelloAuthentication extends Site {
    final MemorySessionValidator validator = new MemorySessionValidator();
    final AuthenticationConfig config = new AuthenticationConfig(validator);

    class AuthenticatedSection extends Router {
        Route hello = get("/hello", c -> {
            c.print(c.template("HelloAuthenticated"));
        });
        Route logout = get("/logout", new Logout(config, TemplateFactory.HTML.get("HelloLogout")));
    }

    public void setup() {
        var login = route("/login", new Login(config, TemplateFactory.HTML.get("HelloLogin")));
        var auth = group(new AuthenticatedSection() {
            public void setup() {
                before(new Authenticated(config));
            }
        });

        config.loginRoute(login).landingRoute(auth.hello);
        validator.getMemoryUsers().addUser("testUser", new RoleUserAttributes().password("testPassword"));
    }

    public static void main(String[] args) {
        new Server().start(new HelloAuthentication());
    }
}
