/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.continuations;

import rife.engine.Context;
import rife.engine.Element;
import rife.engine.annotations.Parameter;

import java.util.Random;

public class TestNumberGuess implements Element {
    private static final Random RANDOM = new Random();

    @Parameter int guess = -1;

    @Override
    public void process(Context c) {
        var template = c.template("numberguess");

        var answer = RANDOM.nextInt(101);
        var guesses = 0;

        while (guess != answer) {
            c.print(template);
            c.pause();

            guesses++;
            if (answer < guess) {
                template.setBlock("indication", "lower");
            } else if (answer > guess) {
                template.setBlock("indication", "higher");
            }
        }

        template = c.template("numberguess_success");
        template.setValue("answer", answer);
        template.setValue("guesses", guesses);

        c.print(template);
    }
}
