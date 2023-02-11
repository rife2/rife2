/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife;

import rife.engine.*;
import rife.engine.annotations.Parameter;

import java.util.Random;

public class HelloContinuations extends Site {
    public static class HelloNumberGuess implements Element {
        private static final Random RANDOM = new Random();

        @Parameter int guess = -1;

        public void process(Context c) {
            var t = c.template("HelloContinuations");

            var answer = RANDOM.nextInt(100) + 1;
            var guesses = 0;

            while (guess != answer) {
                c.print(t);
                c.pause();

                guesses++;
                if (guess < 1 || guess > 100) t.setBlock("msg", "range");
                else if (answer < guess)      t.setBlock("msg", "lower");
                else if (answer > guess)      t.setBlock("msg", "higher");
            }

            t.setValue("answer", answer);
            t.setValue("guesses", guesses);
            t.setBlock("content", "success");

            c.print(t);
        }
    }

    public void setup() {
        getPost("/guess", HelloNumberGuess::new);
    }

    public static void main(String[] args) {
        new Server().start(new HelloContinuations());
    }
}
