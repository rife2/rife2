/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations.instrument;

import rife.continuations.ContinuationConfigInstrument;
import rife.instrument.FinalTransformer;
import rife.instrument.InitialTransformer;

import java.lang.instrument.Instrumentation;

/**
 * Provides a continuations instrumentation agent that will modify
 * the bytecode of the classes that are loaded. It enhances the classes with
 * continuations capabilities that are otherwise provided by a class-loader.
 * <p>To activate the agent you need to execute the Java application with the
 * proper argument, for example:
 * <pre>java -javaagent:/path/to/rife2-continuations-agent-[version].jar=com.your.ContinuationConfigInstrumentClass com.your.mainClass</pre>
 * <p>When the agent is active the {@link rife.continuations.basic.BasicContinuableClassLoader} will
 * automatically be disabled to ensure that they are not conflicting with each
 * other. The agent is packaged in its own jar file which should correspond
 * to the RIFE2 version that you are using in your application.
 * <p>It is possible to debug the bytecode instrumentation by using the
 * functionalities provided by the {@link InitialTransformer} and
 * {@link FinalTransformer} transformers that are included in this agent.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class ContinuationsAgent {
    public static void premain(String agentArguments, Instrumentation instrumentation) {
        if (null == agentArguments) throw new IllegalArgumentException("expecting the fully qualified class name of a ContinuationConfigInstrument class");
        ContinuationConfigInstrument config;
        try {
            var config_class = Class.forName(agentArguments);
            config = (ContinuationConfigInstrument) config_class.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error while creating an instance of the instrumentation configuration with class name '" + agentArguments + "'", e);
        }

        instrumentation.addTransformer(new InitialTransformer());
        instrumentation.addTransformer(new ContinuationsTransformer(config));
        instrumentation.addTransformer(new FinalTransformer());
    }
}
