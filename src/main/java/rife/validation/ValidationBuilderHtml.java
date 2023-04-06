/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

public class ValidationBuilderHtml extends AbstractValidationBuilder {
    protected String formatLine(String content) {
        return content + "<br />\n";
    }
}
