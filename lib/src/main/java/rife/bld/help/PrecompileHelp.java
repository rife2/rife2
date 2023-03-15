/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.help;

import rife.bld.CommandHelp;
import rife.tools.StringUtils;

/**
 * Provides help for the precompile command.
 *
 * @since 1.5
 */
public class PrecompileHelp implements CommandHelp {
    public String getSummary() {
        return "Compiles RIFE2 templates to class files";
    }

    public String getDescription(String topic) {
        return StringUtils.replace("""
            Compiles RIFE2 templates to class files
                        
            Usage : ${topic}""", "${topic}", topic);
    }
}
