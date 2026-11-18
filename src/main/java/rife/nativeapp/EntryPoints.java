/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.nativeapp;

import org.graalvm.nativeimage.ImageInfo;
import org.graalvm.nativeimage.IsolateThread;
import org.graalvm.nativeimage.PinnedObject;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.function.CFunctionPointer;
import org.graalvm.nativeimage.c.function.InvokeCFunctionPointer;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;
import org.graalvm.word.PointerBase;
import org.graalvm.word.WordFactory;
import rife.engine.SiteResponse;
import rife.engine.SiteResponseListener;

import java.util.function.BooleanSupplier;

/**
 * The C functions that an app calls in the native library of its site,
 * which native-image exports when it builds a shared library. Texts are
 * UTF-8 that ends with a zero.
 * <p>The functions that a request is given are called on the thread that
 * handles it: the status and the headers once, the content as it's written,
 * and once how it ended. Nothing is called after that, so that's where the
 * app lets go of its context.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.11.0
 */
public final class EntryPoints {
    /**
     * The version of these functions, which changes with their signatures.
     *
     * @since 1.11.0
     */
    public static final int ABI = 1;

    static final NativeApp APP = new NativeApp();

    private EntryPoints() {
    }

    // native-image takes these from the classpath into any image, an
    // executable of a site would have them too
    static final class InSharedLibrary implements BooleanSupplier {
        public boolean getAsBoolean() {
            return ImageInfo.isSharedLibrary();
        }
    }

    interface Committed extends CFunctionPointer {
        @InvokeCFunctionPointer
        void invoke(PointerBase context, int status, CCharPointer headers);
    }

    interface Content extends CFunctionPointer {
        @InvokeCFunctionPointer
        void invoke(PointerBase context, CCharPointer bytes, int length);
    }

    interface Ended extends CFunctionPointer {
        @InvokeCFunctionPointer
        void invoke(PointerBase context, int how, CCharPointer failure);
    }

    // the app lets go of its context when it's told the end, so that's told once
    private static SiteResponseListener listener(PointerBase context, Committed committed, Content content, Ended ended) {
        var over = new java.util.concurrent.atomic.AtomicBoolean();
        return new SiteResponseListener() {
            public void committed(SiteResponse response) {
                try (var lines = CTypeConversion.toCString(NativeApp.headerLines(response))) {
                    committed.invoke(context, response.getStatus(), lines.get());
                }
            }

            public void content(byte[] bytes, int offset, int length) {
                try (var pinned = PinnedObject.create(bytes)) {
                    content.invoke(context, pinned.addressOfArrayElement(offset), length);
                }
            }

            public void completed() {
                if (over.compareAndSet(false, true)) {
                    ended.invoke(context, NativeApp.COMPLETED, WordFactory.nullPointer());
                }
            }

            public void failed(Throwable exception) {
                if (!over.compareAndSet(false, true)) {
                    return;
                }
                CTypeConversion.CCharPointerHolder why;
                try {
                    why = CTypeConversion.toCString(String.valueOf(exception));
                } catch (Throwable e) {
                    // without the memory to say why, it's still said that it failed
                    ended.invoke(context, NativeApp.FAILED, WordFactory.nullPointer());
                    return;
                }
                try (why) {
                    ended.invoke(context, NativeApp.FAILED, why.get());
                }
            }

            public void cancelled() {
                if (over.compareAndSet(false, true)) {
                    ended.invoke(context, NativeApp.CANCELLED, WordFactory.nullPointer());
                }
            }
        };
    }

    @CEntryPoint(name = "rife2_abi", include = InSharedLibrary.class)
    static int abi(IsolateThread thread) {
        return ABI;
    }

    /**
     * Sets a property of the site, before its first request.
     */
    @CEntryPoint(name = "rife2_property", include = InSharedLibrary.class)
    static void property(IsolateThread thread, CCharPointer name, CCharPointer value) {
        try {
            APP.property(CTypeConversion.toJavaString(name), CTypeConversion.toJavaString(value));
        } catch (Throwable e) {
            // anything that leaves a C function ends the app
            report(e);
        }
    }

    @CEntryPoint(name = "rife2_handle", include = InSharedLibrary.class)
    static void handle(IsolateThread thread, long id, CCharPointer url, CCharPointer method, CCharPointer headers, CCharPointer body, int bodyLength,
                       PointerBase context, Committed committed, Content content, Ended ended) {
        SiteResponseListener listener;
        try {
            listener = listener(context, committed, content, ended);
        } catch (Throwable e) {
            // not even the memory for what tells the app
            ended.invoke(context, NativeApp.FAILED, WordFactory.nullPointer());
            return;
        }
        try {
            byte[] bytes = null;
            if (body.isNonNull()) {
                bytes = new byte[bodyLength];
                CTypeConversion.asByteBuffer(body, bodyLength).get(bytes);
            }
            APP.handle(id, CTypeConversion.toJavaString(url), CTypeConversion.toJavaString(method), CTypeConversion.toJavaString(headers), bytes, listener);
        } catch (Throwable e) {
            // running out of memory in an extension, for instance, before the
            // request was handed over: anything that leaves here ends the app
            listener.failed(e);
        }
    }

    // without the memory to say it, it's not said
    private static void report(Throwable e) {
        try {
            e.printStackTrace();
        } catch (Throwable ignored) {
        }
    }

    @CEntryPoint(name = "rife2_cancel", include = InSharedLibrary.class)
    static void cancel(IsolateThread thread, long id) {
        try {
            APP.cancel(id);
        } catch (Throwable e) {
            report(e);
        }
    }
}
