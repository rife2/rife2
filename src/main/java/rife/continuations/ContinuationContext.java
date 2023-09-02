/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations;

import rife.continuations.exceptions.ContinuableLocalVariableUncloneableException;
import rife.tools.ExceptionUtils;
import rife.tools.UniqueIDGenerator;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

/**
 * Contains all contextual data of one particular continuation.
 * <p>It also provides some static retrieval methods to be able to access
 * active continuations.
 * <p>Active continuations are managed in a {@link ContinuationManager} so that
 * they can be easily retrieved.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see ContinuationManager
 * @since 1.0
 */
public class ContinuationContext implements Cloneable {
    private static final ThreadLocal<ContinuationContext> ACTIVE_CONTEXT = new ThreadLocal<>();
    private static final ThreadLocal<WeakReference<ContinuationContext>> LAST_CONTEXT = new ThreadLocal<>();

    private final transient ContinuationManager manager_;

    private Object continuable_ = null;
    private CallState createdCallState_ = null;
    private CallState activeCallState_ = null;
    private Object callAnswer_ = null;
    private String id_ = null;
    private String parentId_ = null;
    private List<String> relatedIds_ = null;
    private long start_ = -1;

    private int label_ = -1;
    private boolean paused_ = false;

    private ContinuationStack localVars_ = null;
    private ContinuationStack localStack_ = null;

    private final ReadWriteLock lock_ = new ReentrantReadWriteLock();
    private final Lock readLock_ = lock_.readLock();
    private final Lock writeLock_ = lock_.writeLock();

    /**
     * [PRIVATE AND UNSUPPORTED] Creates a new continuation context or resets
     * its expiration time.
     * <p>This method is used by the instrumented bytecode that provides
     * continuations support, it's not intended for general use.
     *
     * @return a new {@code ContinuationContext}, or the active one with
     * its expiration time being reset
     * @since 1.0
     */
    public static ContinuationContext createOrResetContext(Object executingInstance) {
        var context = getActiveContext();
        if (null == context ||
            context.getContinuable() == null ||
            executingInstance.getClass() != context.getContinuable().getClass()) {

            var config = ContinuationConfigRuntime.getActiveConfigRuntime();
            context = new ContinuationContext(config.getContinuationManager(executingInstance), executingInstance);

            // check if the last continuation created a call continuation, in that case
            // pass the call state on to this new continuation
            var last_context = getLastContext();
            if (last_context != null) {
                CallState call_state = null;
                if (last_context.getCreatedCallState() != null) {
                    call_state = last_context.getCreatedCallState();
                } else {
                    call_state = last_context.getActiveCallState();
                }

                if (call_state != null) {
                    context.setActiveCallState(call_state);
                }
            }
        } else {
            context.resetStart();
        }

        setActiveContext(context);

        // preserve a reference to the last executed continuation, so that it's
        // possible to detect call continuations
        LAST_CONTEXT.set(new WeakReference<>(context));

        return context;
    }

    /**
     * Clears the active currently continuation context for the executing thread.
     *
     * @since 1.0
     */
    public static void clearActiveContext() {
        ACTIVE_CONTEXT.remove();
    }

    /**
     * Retrieves the identifier of the currently active continuation for the
     * current thread.
     *
     * @return the identifier of the currently active continuation as a unique
     * string; or
     * <p>{@code null} if no continuation is currently active
     * @see #getActiveContext
     * @since 1.0
     */
    public static String getActiveContextId() {
        var context = ACTIVE_CONTEXT.get();
        if (null == context) {
            return null;
        }
        return context.getId();
    }

    /**
     * Retrieves the currently active continuation for the executing thread.
     *
     * @return the currently active continuation; or
     * <p>{@code null} if no continuation is currently active
     * @see #getActiveContextId
     * @since 1.0
     */
    public static ContinuationContext getActiveContext() {
        return ACTIVE_CONTEXT.get();
    }

    /**
     * Replaces the active continuation context for the executing thread.
     *
     * @param context the new {@code ContinuationContext} that will be active; or
     *                {@code null} if no continuation context should be active
     * @see #setActiveContext
     * @since 1.0
     */
    public static void setActiveContext(ContinuationContext context) {
        ACTIVE_CONTEXT.set(context);
    }

    /**
     * Retrieves the last active continuation for the executing thread.
     *
     * @return the last active continuation; or
     * <p>{@code null} if no continuation was active
     * @since 1.0
     */
    public static ContinuationContext getLastContext() {
        var reference = LAST_CONTEXT.get();
        if (reference != null) {
            return reference.get();
        }
        return null;
    }

    private ContinuationContext(ContinuationManager manager, Object continuable) {
        manager_ = manager;
        continuable_ = continuable;

        resetId();
        resetStart();

        label_ = -1;

        localVars_ = new ContinuationStack().initialize();
        localStack_ = new ContinuationStack().initialize();
    }

    /**
     * Retrieves the manager of this {@code ContinuationContext}.
     *
     * @return this continuation's manager instance
     * @since 1.0
     */
    public ContinuationManager getManager() {
        return manager_;
    }

    /**
     * Registers this continuation in its manager, so that it can be retrieved later.
     *
     * @since 1.0
     */
    public void registerContext() {
        manager_.addContext(this);
    }

    /**
     * Makes sure that this {@code ContinuationContext} is not the active
     * one.
     *
     * @since 1.0
     */
    public void deactivate() {
        if (this == getActiveContext()) {
            clearActiveContext();
        }
    }

    /**
     * Removes this {@code ContinuationContext} instance from its {@link
     * ContinuationManager}.
     *
     * @since 1.0
     */
    public void remove() {
        writeLock_.lock();
        try {
            manager_.removeContext(id_);
            deactivate();
        } finally {
            writeLock_.unlock();
        }
    }

    /**
     * Removes the entire continuation tree that this
     * {@code ContinuationContext} instance belongs to from its {@link
     * ContinuationManager}.
     *
     * @since 1.0
     */
    public void removeContextTree() {
        manager_.writeLock_.lock();
        try {
            manager_.removeContext(id_);

            if (relatedIds_ != null) {

                ContinuationContext child;
                for (var id : relatedIds_) {
                    child = manager_.getContext(id);
                    if (child != null) {
                        child.removeContextTree();
                    }
                }
            }

            var parent = getParentContext();
            if (parent != null) {
                parent.removeContextTree();
            }

            deactivate();
        } finally {
            manager_.writeLock_.unlock();
        }
    }

    /**
     * Retrieves the unique identifier of the parent continuation of this
     * {@code ContinuationContext} instance.
     *
     * @return the parent's identifier as a unique string; or
     * <p>{@code null} if this {@code ContinuationContext} has no
     * parent
     * @see #getParentContext
     * @since 1.0
     */
    public String getParentContextId() {
        readLock_.lock();
        try {
            return parentId_;
        } finally {
            readLock_.unlock();
        }
    }

    /**
     * Retrieves the parent {@code ContinuationContext} of this
     * {@code ContinuationContext} instance.
     *
     * @return the parent {@code ContinuationContext}; or
     * <p>{@code null} if this {@code ContinuationContext} has no
     * parent
     * @see #getParentContextId
     * @since 1.0
     */
    public ContinuationContext getParentContext() {
        return manager_.getContext(getParentContextId());
    }

    /**
     * Retrieves the answer that the call continuation stored in this context.
     *
     * @return the call continuation's answer; or
     * <p>{@code null} if no answer was provided or the corresponding
     * continuation wasn't a call continuation
     * @since 1.0
     */
    public Object getCallAnswer() {
        readLock_.lock();
        try {
            return callAnswer_;
        } finally {
            readLock_.unlock();
        }
    }

    /**
     * [PRIVATE AND UNSUPPORTED] Sets whether the continuation if paused.
     * <p>This method is used by the internals that provide continuations
     * support, it's not intended for general use.
     *
     * @param paused {@code true} if the continuation is paused; or
     *               <p>{@code false} otherwise
     * @see #isPaused()
     * @since 1.0
     */
    public void setPaused(boolean paused) {
        writeLock_.lock();
        try {
            paused_ = paused;
        } finally {
            writeLock_.unlock();
        }
    }

    /**
     * Indicates whether this continuation is actually paused and can be resumed.
     *
     * @return {@code true} if the continuation is paused; or
     * <p>{@code false} otherwise
     * @since 1.0
     */
    public boolean isPaused() {
        readLock_.lock();
        try {
            return paused_;
        } finally {
            readLock_.unlock();
        }
    }

    /**
     * [PRIVATE AND UNSUPPORTED] Set the number of the bytecode label where
     * the continuation has to resume execution from.
     * <p>This method is used by the instrumented bytecode that provides
     * continuations support, it's not intended for general use.
     *
     * @param label the number of the resumed bytecode label
     * @since 1.0
     */
    public void setLabel(int label) {
        writeLock_.lock();
        try {
            label_ = label;
        } finally {
            writeLock_.unlock();
        }
    }

    /**
     * [PRIVATE AND UNSUPPORTED] Set the number of the bytecode label where
     * the continuation has to resume execution from.
     * <p>This method is used by the instrumented bytecode that provides
     * continuations support, it's not intended for general use.
     *
     * @since 1.0
     */
    public void clearLabel() {
        setLabel(-1);
    }

    /**
     * [PRIVATE AND UNSUPPORTED] Retrieves the number of the bytecode label
     * where the continuation has to resume execution from.
     * <p>This method is used by the instrumented bytecode that provides
     * continuations support, it's not intended for general use.
     *
     * @return the number of the resumed bytecode label; or
     * <p>{@code -1} if no label number has been set
     * @since 1.0
     */
    public int getLabel() {
        readLock_.lock();
        try {
            return label_;
        } finally {
            readLock_.unlock();
        }
    }

    /**
     * [PRIVATE AND UNSUPPORTED] Retrieves the local variable stack of this
     * continuation.
     * <p>This method is used by the instrumented bytecode that provides
     * continuations support, it's not intended for general use.
     *
     * @return this continuation's local variable stack
     * @since 1.0
     */
    public ContinuationStack getLocalVars() {
        readLock_.lock();
        try {
            return localVars_;
        } finally {
            readLock_.unlock();
        }
    }

    /**
     * [PRIVATE AND UNSUPPORTED] Retrieves the local operand stack of this
     * continuation.
     * <p>This method is used by the instrumented bytecode that provides
     * continuations support, it's not intended for general use.
     *
     * @return this continuation's local operand stack
     * @since 1.0
     */
    public ContinuationStack getLocalStack() {
        readLock_.lock();
        try {
            return localStack_;
        } finally {
            readLock_.unlock();
        }
    }

    private void resetStart() {
        writeLock_.lock();
        try {
            start_ = System.currentTimeMillis();
        } finally {
            writeLock_.unlock();
        }
    }

    void resetId() {
        writeLock_.lock();
        try {
            id_ = UniqueIDGenerator.generate().toString();
        } finally {
            writeLock_.unlock();
        }
    }

    /**
     * Retrieves the continuation ID.
     * <p>Note that this ID is not necessarily present in the manager and that
     * trying to retrieve a continuation afterward from its ID is never
     * guaranteed to give a result.
     *
     * @return the unique ID of this continuation.
     * @since 1.0
     */
    public String getId() {
        readLock_.lock();
        try {
            return id_;
        } finally {
            readLock_.unlock();
        }
    }

    /**
     * [PRIVATE AND UNSUPPORTED] Set the ID of this continuation's parent.
     * <p>This method is used by the instrumented bytecode that provides
     * continuations support, it's not intended for general use.
     *
     * @param id the ID of this continuation's parent
     * @see #getParentId()
     * @since 1.0
     */
    public void setParentId(String id) {
        writeLock_.lock();
        try {
            parentId_ = id;
        } finally {
            writeLock_.unlock();
        }
    }

    /**
     * Retrieves the ID of this continuation's parent.
     *
     * @return the ID of this continuation's parent continuation; or
     * <p>{@code null} if this continuation has no parent.
     * @since 1.0
     */
    public String getParentId() {
        readLock_.lock();
        try {
            return parentId_;
        } finally {
            readLock_.unlock();
        }
    }

    /**
     * [PRIVATE AND UNSUPPORTED] Associates the ID of another continuation to
     * this continuation.
     * <p>This method is used by the instrumented bytecode that provides
     * continuations support, it's not intended for general use.
     *
     * @param id the ID of another continuation that's related to this
     *           continuation
     * @since 1.0
     */
    public void addRelatedId(String id) {
        writeLock_.lock();
        try {
            if (null == relatedIds_) {
                relatedIds_ = new ArrayList<>();
            }
            relatedIds_.add(id);
        } finally {
            writeLock_.unlock();
        }
    }

    /**
     * Returns the object instance in which this continuation was executing.
     *
     * @return this continuation's active object
     * @since 1.0
     */
    public Object getContinuable() {
        return continuable_;
    }

    /**
     * Sets the call continuation's state when a new call continuation is
     * created.
     * <p>This state initiates a call continuation and should be set when
     * a new call happens, after that it should never be changed.
     *
     * @param createdCallState this call continuation's creation state
     * @see #getCreatedCallState()
     * @since 1.0
     */
    public void setCreatedCallState(CallState createdCallState) {
        writeLock_.lock();
        try {
            createdCallState_ = createdCallState;
        } finally {
            writeLock_.unlock();
        }
    }

    /**
     * Retrieves this continuation's call continuation creation state.
     * <p>If this returns a non-null value, you can detect from it that this
     * was a call continuation.
     *
     * @return this continuation
     * @see #setCreatedCallState(CallState)
     * @since 1.0
     */
    public CallState getCreatedCallState() {
        readLock_.lock();
        try {
            return createdCallState_;
        } finally {
            readLock_.unlock();
        }
    }

    /**
     * Sets the active call state for this continuation.
     * <p>This mainly passes on the call state that was created during a call
     * continuation. It allows quick retrieval of the active call state when
     * an answer occurs.
     *
     * @param callState the active call state
     * @see #setCreatedCallState(CallState)
     * @since 1.0
     */
    public void setActiveCallState(CallState callState) {
        writeLock_.lock();
        try {
            activeCallState_ = callState;
        } finally {
            writeLock_.unlock();
        }
    }

    /**
     * Retrieves the call state that is active during this continuation.
     *
     * @return the active {@code CallState}; or
     * <p>{@code null} if no call state was active for this continuation
     */
    public CallState getActiveCallState() {
        readLock_.lock();
        try {
            return activeCallState_;
        } finally {
            readLock_.unlock();
        }
    }

    long getStart() {
        readLock_.lock();
        try {
            return start_;
        } finally {
            readLock_.unlock();
        }
    }

    /**
     * Set the answer to a call continuation.
     *
     * @param answer the object that will be the call continuation's answer; or
     *               {@code null} if there was no answer
     * @since 1.0
     */
    public void setCallAnswer(Object answer) {
        writeLock_.lock();
        try {
            callAnswer_ = answer;
        } finally {
            writeLock_.unlock();
        }
    }

    /**
     * [PRIVATE AND UNSUPPORTED] Creates a cloned instance of this
     * continuation context, this clone is not a perfect copy but is intended
     * to be a child continuation and all context data is set up for that.
     * <p>This method is used by the instrumented bytecode that provides
     * continuations support, it's not intended for general use.
     *
     * @return a clone of this continuation for use as a child continuation
     * @since 1.0
     */
    @Override
    public ContinuationContext clone()
    throws CloneNotSupportedException {
        ContinuationContext new_continuationcontext = null;
        try {
            new_continuationcontext = (ContinuationContext) super.clone();
        } catch (CloneNotSupportedException e) {
            // this should never happen
            Logger.getLogger("rife.continuations").severe(ExceptionUtils.getExceptionStackTrace(e));
        }

        readLock_.lock();
        try {

            if (continuable_ instanceof CloneableContinuable continuable) {
                new_continuationcontext.continuable_ = continuable.clone();
            } else {
                throw new CloneNotSupportedException(continuable_.getClass().getName() + " can't be cloned.");
            }
            new_continuationcontext.callAnswer_ = null;

            new_continuationcontext.id_ = UniqueIDGenerator.generate().toString();
            new_continuationcontext.parentId_ = id_;
            new_continuationcontext.paused_ = false;

            try {
                new_continuationcontext.localVars_ = localVars_.clone(new_continuationcontext.continuable_);
                new_continuationcontext.localStack_ = localStack_.clone(new_continuationcontext.continuable_);
            } catch (CloneNotSupportedException e) {
                throw new ContinuableLocalVariableUncloneableException(continuable_.getClass(), e.getMessage(), e);
            }
        } finally {
            readLock_.unlock();
        }

        if (new_continuationcontext != null) {
            addRelatedId(new_continuationcontext.id_);
        }

        return new_continuationcontext;
    }
}
