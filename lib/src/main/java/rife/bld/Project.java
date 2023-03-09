/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld;

public abstract class Project extends Cli {
    public String name = null;
    public String pkg = null;
    public String version = null;

    public abstract void setup();

    public void start(String[] args) {
        setup();
        new Cli().processArguments(this, args);
    }
}
