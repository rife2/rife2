/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

public class EnterDirective implements ParseDirectiveChange {
    final ParseDirective directive_;

    public EnterDirective(ParseDirective directive) {
        directive_ = directive;
    }

    @Override
    public void applyDirective(ParseTrail trail) {
        trail.addDirective(directive_);
    }
}
