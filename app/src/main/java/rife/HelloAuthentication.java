/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife;

import rife.authentication.credentialsmanagers.RoleUserAttributes;
import rife.authentication.elements.Authenticated;
import rife.authentication.elements.AuthenticationConfig;
import rife.authentication.elements.Login;
import rife.authentication.sessionvalidators.MemorySessionValidator;
import rife.engine.Route;
import rife.engine.Router;
import rife.engine.Site;
import rife.server.Server;
import rife.template.TemplateFactory;

public class HelloAuthentication extends Site {
    final MemorySessionValidator validator = new MemorySessionValidator();
    final AuthenticationConfig config = new AuthenticationConfig(validator);

    static class AuthenticatedSection extends Router {
        final Route hello = get("/hello", c -> c.print("Hello World"));
    }

    public void setup() {
        var login = route("/login", new Login(config, TemplateFactory.HTML.get("HelloLogin")));
        var auth = group(new AuthenticatedSection() {
            public void setup() {
                before(new Authenticated(config));
            }
        });

        config.loginRoute(login).landingRoute(auth.hello);
        validator.getMemoryUsers().addUser("testUser", new RoleUserAttributes("testPassword"));
    }

    public static void main(String[] args) {
        new Server().start(new HelloAuthentication());
    }
}
