/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.template.Template;
import rife.validation.ConstrainedBeanImpl;

public class GenerateFormSite extends Site {
    public void setup() {
        get("/form", c -> {
            ConstrainedBeanImpl bean = new ConstrainedBeanImpl();
            bean.setHidden("canyouseeme");
            bean.setAnotherhidden("I can't see you");
            bean.setLogin("ikke");
            bean.setAnotherlogin("jullie");
            bean.setPassword("secret");
            bean.setAnotherpassword("real secret");
            bean.setEmail("my@email.com");
            bean.setUrl("https://rife2.com");
            bean.setComment("één comment");
            bean.setAnothercomment("this comment");
            bean.setQuestion(ConstrainedBeanImpl.Question.a2);
            bean.setAnotherquestion("a3");
            bean.setCustomquestion("a1");
            bean.setAnothercustomquestion("a2");
            bean.setOptions(new int[]{2});
            bean.setOtheroptions(new int[]{2, 0});
            bean.setCustomoptions(new int[]{1});
            bean.setOthercustomoptions(new int[]{2});
            bean.setInvoice(true);
            bean.setOnemoreinvoice(false);
            bean.setColors(new String[]{"red", "green"});
            bean.setMorecolors(new String[]{"black"});

            if (c.hasParameterValue("prefix")) {
                bean.setYourcolors(new String[]{"orange", "brown"});

                Template template = c.template("formbuilder_form_prefix");
                c.generateForm(template, bean, "prefix_");

                if (c.parameterBoolean("remove")) {
                    c.removeForm(template, ConstrainedBeanImpl.class, "prefix_");
                }

                c.print(template.getContent());
            } else {
                bean.setYourcolors(new String[]{"brown"});

                Template template = c.template("formbuilder_fields");
                c.generateForm(template, bean);

                if (c.parameterBoolean("remove")) {
                    c.removeForm(template, ConstrainedBeanImpl.class);
                }

                c.print(template.getContent());
            }
        });
    }
}
