/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.template.Template;
import rife.validation.ConstrainedBeanImpl;

public class GenerateEmptyFormSite extends Site {
    public void setup() {
        get("/form_empty", c -> {
            if (c.hasParameterValue("prefix")) {
                Template template = c.template("formbuilder_form_prefix");
                c.generateEmptyForm(template, ConstrainedBeanImpl.class, "prefix_");

                if (c.parameterBoolean("remove")) {
                    c.removeForm(template, ConstrainedBeanImpl.class, "prefix_");
                }

                c.print(template.getContent());
            } else {
                Template template = c.template("formbuilder_fields");
                c.generateEmptyForm(template, ConstrainedBeanImpl.class);

                if (c.parameterBoolean("remove")) {
                    c.removeForm(template, ConstrainedBeanImpl.class);
                }

                c.print(template.getContent());
            }
        });
    }
}
