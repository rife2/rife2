/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

public class Parser {
    private final ParseStep begin_;

    public Parser() {
        begin_ = new ParseStep();
        begin_.txt("<!--").optional(ParseSteps.ws()).txt("V").ws().identifier().optional(ParseSteps.ws()).txt("/-->").token(ParserToken.V_SHORT);
//        begin_.txt("<!--").optional(ParseSteps.ws()).txt("V").ws().identifier().optional(ParseSteps.ws()).txt("-->").token(ParserToken.V_BEGIN);
//        begin_.txt("<!--/").optional(ParseSteps.ws()).txt("V").optional(ParseSteps.ws()).txt("-->").token(ParserToken.V_TERM);
//        begin_.txt("<!--").optional(ParseSteps.ws()).txt("B").ws().identifier().optional(ParseSteps.ws()).txt("-->").token(ParserToken.B_BEGIN);
//        begin_.txt("<!--/").optional(ParseSteps.ws()).txt("B").optional(ParseSteps.ws()).txt("-->").token(ParserToken.B_TERM);
//        begin_.txt("<!--").optional(ParseSteps.ws()).txt("I").ws().identifier().optional(ParseSteps.ws()).txt("/-->").token(ParserToken.I_SHORT);
//        begin_.txt("<!--").optional(ParseSteps.ws()).txt("C").optional(ParseSteps.ws()).txt("-->").token(ParserToken.C_BEGIN);
//        begin_.txt("<!--/").optional(ParseSteps.ws()).txt("C").optional(ParseSteps.ws()).txt("-->").token(ParserToken.C_TERM);
    }

    public Parsed parse(String content) {
        var result = new Parsed(this);
        var state = new ParseState(begin_);

        final var length = content.length();
        for (var i = 0; i < length; ) {
            final var cp = content.codePointAt(i);

            if (!state.process(cp)) {
                break;
            }

            i += Character.charCount(cp);
        }

        return result;
    }
}
