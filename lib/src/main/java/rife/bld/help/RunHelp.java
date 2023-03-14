/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.help;

import rife.bld.CommandHelp;
import rife.tools.StringUtils;

/**
 * Provides help for the run command.
 *
 * @since 1.5
 */
public class RunHelp implements CommandHelp {
    public String getDescription() {
        return "Runs the project";
    }

    public String getHelp(String topic) {
        return StringUtils.replace("""
            Runs the project.
                        
            Usage : ${topic}""", "${topic}", topic);
    }
}
