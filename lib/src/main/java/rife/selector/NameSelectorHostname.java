/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.selector;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Selects a name according to the current hostname.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see NameSelector
 * @since 2.0
 */
public class NameSelectorHostname implements NameSelector {
    public String getActiveName() {
        try {
            var address = InetAddress.getLocalHost();
            return address.getHostName().toLowerCase();
        } catch (UnknownHostException e) {
            // do nothing
        }
        return "";
    }
}
