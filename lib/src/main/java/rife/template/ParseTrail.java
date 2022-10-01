/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ParseTrail {
    private ParseStep current_;
    private ParseStep lastValid_;
    private final List<ParserToken> tokens_ = new ArrayList<>();
    private final Map<ParseDirective, Integer> directives_ = new HashMap<>();

    ParseTrail(ParseStep start) {
        current_ = start;
    }

    ParseStep getCurrent() {
        return current_;
    }

    void setCurrent(ParseStep current_) {
        current_ = current_;
    }

    ParseStep getLastValid_() {
        return lastValid_;
    }

    void setLastValid(ParseStep lastValid) {
        lastValid_ = lastValid;
    }

    void addToken(ParseStep step) {
        var token = step.getToken();
        if (token != null) {
            tokens_.add(token);
            System.out.println("token " + token);
        }
    }

    ParseTrail splitTrail(ParseStep step) {
        var split = new ParseTrail(step);
        split.tokens_.addAll(tokens_);
        return split;
    }

    void addDirective(ParseDirective directive) {
        var count = 1;
        if (directives_.containsKey(directive)) {
            count = directives_.get(directive) + 1;
        }
        directives_.put(directive, count);
    }

    void removeDirective(ParseDirective directive) {
        if (directives_.containsKey(directive)) {
            var count = directives_.get(directive) - 1;
            if (0 == count) {
                directives_.remove(directive);
            } else {
                directives_.put(directive, count);
            }
        }
    }

    boolean hasDirective(ParseDirective directive) {
        return directives_.containsKey(directive);
    }
}
