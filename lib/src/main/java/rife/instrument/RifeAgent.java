/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.instrument;

import java.lang.instrument.Instrumentation;

/**
 * The RIFE instrumentation agent will modify the bytecode of the classes
 * that are loaded to provide new capabilities that are otherwise provided by
 * the class-loader.
 * <p>To activate the agent you need to execute the Java application with the
 * proper argument, for example:
 * <pre>java -javaagent:/path/to/rife-agent-1.6-jdk15.jar com.your.mainClass</pre>
 * <p>When the agent is active the class-loader will automatically be disabled
 * to ensure that they are not conflicting with each other. The agent is
 * packaged in its own jar file which should correspond to the RIFE version
 * that you are using in your application.
 * <p>Note that this agent doesn't entirely replace the functionality of the
 * class-loader, during development mode you might want to make use of the
 * automatic re-compilation of classes that is only provided by the
 * class-loader. However, during production the agent is the recommended
 * choice.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class RifeAgent {
    public static final String AGENT_ACTIVE_PROPERTY = "rife.agent.active";

    public static void premain(String agentArguments, Instrumentation instrumentation) {
        System.getProperties().setProperty(AGENT_ACTIVE_PROPERTY, String.valueOf(true));

        instrumentation.addTransformer(new InitialTransformer());
//        instrumentation.addTransformer(new LazyLoadTransformer());
        instrumentation.addTransformer(new FinalTransformer());
    }
}
