/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.config.exceptions;

import java.io.Serial;

public class ConfigResourceNotFoundException extends ConfigErrorException {
    @Serial
    private static final long serialVersionUID = 4477782139859668000L;

    private String configSource_ = null;
    private String xmlPath_ = null;

    public ConfigResourceNotFoundException(String configSource) {
        super("Couldn't find a valid resource for config source '" + configSource + "'.");

        configSource_ = configSource;
    }

    public ConfigResourceNotFoundException(String configSource, String xmlPath) {
        super("Couldn't find a valid resource for config source '" + configSource + "', tried xml path '" + xmlPath + "'.");

        configSource_ = configSource;
        xmlPath_ = xmlPath;
    }

    public String getConfigSource() {
        return configSource_;
    }

    public String getXmlPath() {
        return xmlPath_;
    }
}
