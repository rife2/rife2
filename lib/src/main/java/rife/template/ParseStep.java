/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

import java.util.ArrayList;
import java.util.List;

class ParseStep {
    private final ParseCondition condition_;
    private final List<ParseStep> nextSteps_;
    private final ParseDirectiveChange directive_;

    ParserToken token_;

    ParseStep() {
        condition_ = null;
        nextSteps_ = new ArrayList<>();
        directive_ = null;
        token_ = null;
    }

    ParseStep(ParseCondition condition) {
        condition_ = condition;
        nextSteps_ = new ArrayList<>();
        directive_ = null;
        token_ = null;
    }

    ParseStep(ParseDirectiveChange directive) {
        condition_ = null;
        nextSteps_ = new ArrayList<>();
        directive_ = directive;
        token_ = null;
    }

    ParseStep next(ParseStep next) {
        nextSteps_.add(next);
        return next;
    }

    ParseStep token(ParserToken token) {
        if (token_ != null) throw new IllegalArgumentException("tokens should only be set once");

        token_ = token;
        return this;
    }

    ParseStep txt(String txt) {
        var last = this;
        final var length = txt.length();
        for (var cpi = 0; cpi < length; ) {
            final var cp = txt.codePointAt(cpi);
            last = last.next(ParseSteps.chr(cp));
            cpi += Character.charCount(cp);
        }

        return last;
    }

    ParseStep chr(char chr) {
        return next(ParseSteps.chr(chr));
    }

    ParseStep ws() {
        return next(ParseSteps.ws());
    }

    ParseStep optional(ParseStep step) {
        return next(new ParseStep(new EnterDirective(ParseDirective.OPTIONAL))).next(step).next(new ParseStep(new LeaveDirective(ParseDirective.OPTIONAL)));
    }

    ParseStep identifier() {
        return next(new ParseStep(new IdentifierStart())).next(new ParseStep(new IdentitifierPart()));
    }

    ParseCondition getCondition() {
        return condition_;
    }

    List<ParseStep> getNextSteps() {
        return nextSteps_;
    }

    ParserToken getToken() {
        return token_;
    }

    ParseDirectiveChange getDirective() {
        return directive_;
    }
}
