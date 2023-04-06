/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import java.awt.Canvas;
import java.awt.Component;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.image.ImageObserver;

public final class ImageWaiter {
    private static Canvas waitComponent_ = null;

    private ImageWaiter() {
        // no-op
    }

    public static void wait(Image image) {
        if (waitComponent_ == null) {
            waitComponent_ = new Canvas();
        }
        wait(image, waitComponent_);
    }

    public static void wait(Image image, Component component) {
        var tracker = new MediaTracker(component);
        tracker.addImage(image, 0);
        try {
            tracker.waitForAll();
        } catch (InterruptedException e) {
            // do nothing
        }
    }

    public static void wait(final Image image, final Component component, final ImageObserver imageobserver) {
        final var tracker = new MediaTracker(component);

        new Thread(() -> {
            tracker.addImage(image, 0);
            try {
                tracker.waitForAll();
            } catch (InterruptedException e) {
                // do nothing
            }

            imageobserver.imageUpdate(image, 0, 0, 0, image.getWidth(null), image.getHeight(null));
        }).start();
    }
}

