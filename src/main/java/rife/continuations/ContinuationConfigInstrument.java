/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations;

/**
 * This interface needs to be implemented to configure the bytecode
 * instrumentation that enables the continuations functionalities.
 * <p>
 * IMPORTANT: Do not load any classes here, only return string literals.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public interface ContinuationConfigInstrument {
    /**
     * The name of the interface that will indicate that a class should be
     * instrumented for continuations functionalities, for instance
     * {@code ContinuableObject.class.getName()}.
     *
     * @return the name of the marker interface
     * @since 1.0
     */
    String getContinuableMarkerInterfaceName();

    /**
     * The class name of the support class that contains dummy implementations
     * of the continuation methods that are configured below, for instance
     * {@code ContinuableSupport.class.getName()}.
     * <p>If you implement these methods in your continuable classes or extend
     * these classes from a common base class with those methods that are then
     * called locally, this configuration can return {@code null} since it
     * will not be used. A class name only needs to be provided if your
     * continuable classes only implement the marker interface, and you call
     * the continuation methods on an instance of this support inside your
     * continuations logic.
     *
     * @return the name of the continuable support class; or
     * <p>{@code null} if such a support class isn't used
     * @since 1.0
     */
    default String getContinuableSupportClassName() {
        return null;
    }

    /**
     * The name of the entry method that will be invoked when a new instance
     * of a continuable class is created and its execution is started, for
     * instance {@code "execute"}.
     *
     * @return the name of the entry method
     * @since 1.0
     */
    String getEntryMethodName();

    /**
     * The ASM method descriptor of the entry method, this includes the arguments
     * and the return types. If there's no arguments nor return types,
     * this is {@code "()V"}.
     *
     * @return the ASM method descriptor for the entry method
     * @since 1.2
     */
    String getEntryMethodDescriptor();

    /**
     * The name of the method that will trigger a pause continuation, for
     * instance {@code "pause"}.
     * <p>This method should have a {@code void} return type and take no
     * arguments.
     *
     * @return the name of the pause method or
     * {@code null} if you don't use pause continuations
     * @since 1.0
     */
    default String getPauseMethodName() {
        return null;
    }

    /**
     * The name of the method that will trigger a step-back continuation, for
     * instance {@code "stepBack"}.
     * <p>This method should have a {@code void} return type and take no
     * arguments.
     *
     * @return the name of the step-back method; or
     * {@code null} if you don't use step-back continuations
     * @since 1.0
     */
    default String getStepBackMethodName() {
        return null;
    }

    /**
     * The name of the method that will trigger a call continuation, for
     * instance {@code "call"}.
     *
     * @return the name of the call method; or
     * {@code null} if you don't use call/answer continuations
     * @since 1.0
     */
    default String getCallMethodName() {
        return null;
    }

    /**
     * The ASM return type name of the call method, for instance
     * {@code "java/lang/Object"}.
     * <p>This needs to be an object, not a primitive, and you have to be
     * certain that it's compatible with the values that are sent through the
     * answer to the call continuation. It's just recommended to keep this as
     * generic as possible (hence {@code "java/lang/Object"}).
     *
     * @return the ASM return type name of the call method
     * @since 1.2
     */
    default String getCallMethodReturnTypeName() {
        return null;
    }

    /**
     * The ASM method descriptor of the call method, this includes the arguments
     * and the return types. For instance {@code ""(Ljava/lang/Object;)Ljava/lang/Object;""}
     * <p>
     * This includes the return type name that's also provided by {@link #getCallMethodReturnTypeName()}.
     * <p>
     * The array argument types that the call method takes, needs to be a single
     * object argument, not more or less than one,
     * and not a primitive. You will use this yourself in the implementation
     * of the runner that executes the continuations. If the
     * {@link rife.continuations.basic.BasicContinuableRunner} is
     * used, {@link rife.continuations.basic.CallTargetRetriever} will
     * be used to resolve the target of the call continuation by using the
     * what's provided as the argument of the method call.
     *
     * @return the ASM method descriptor of the call method
     * @since 1.2
     */
    default String getCallMethodDescriptor() {
        return null;
    }

    /**
     * The name of the method that will trigger the answer to a call
     * continuation, for instance {@code "answer"}.
     * <p>This method should have a {@code void} return type and take one
     * argument with the type {@code java.lang.Object}.
     *
     * @return the name of the answer method; or
     * {@code null} if you don't use call/answer continuations
     * @since 1.0
     */
    default String getAnswerMethodName() {
        return null;
    }
}