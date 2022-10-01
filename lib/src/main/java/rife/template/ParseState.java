/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

import java.util.ArrayList;
import java.util.List;

class ParseState {
    private final List<ParseTrail> trails_ = new ArrayList<>();

    ParseState(ParseStep begin) {
        for (ParseStep step : begin.getNextSteps()) {
            trails_.add(new ParseTrail(step));
        }
    }

    boolean process(int codePoint) {
        var result = false;

        var current_trails = new ArrayList<>(trails_);
        if (current_trails.isEmpty()) {
            return false;
        }

        for (ParseTrail trail : current_trails) {
            while (true) {
                var step = trail.getCurrent();

                // handle parse directives
                var directive = step.getDirective();
                if (directive != null) {
                    directive.applyDirective(trail);
                    advanceToNextSteps(trail, step);
                    continue;
                }

                // handle parse conditions
                var condition = step.getCondition();
                if (condition.isValid(codePoint)) {
                    trail.setLastValid(trail.getCurrent());
                    result = true;

                    if (!condition.isRepeatable()) {
                        handleValidStep(trail, step);
                    }

                    break;
                } else if (trail.hasDirective(ParseDirective.OPTIONAL) ||
                    step == trail.getLastValid_() && condition.isRepeatable()) {
                    trail.setLastValid(null);

                    handleValidStep(trail, step);
                } else {
                    trail.setLastValid(null);
                    trail.setCurrent(null);

                    trails_.remove(trail);

                    break;
                }
            }
        }

        return result;
    }

    private void handleValidStep(ParseTrail trail, ParseStep step) {
        System.out.println("handleValidStep " + step);
        trail.addToken(step);
        advanceToNextSteps(trail, step);
    }

    private void advanceToNextSteps(ParseTrail trail, ParseStep step) {
        var next_steps = step.getNextSteps();
        for (int i = 0; i < next_steps.size(); ++i) {
            var next = next_steps.get(i);
            if (0 == i) {
                trail.setCurrent(next);
            } else {
                trails_.add(trail.splitTrail(next));
            }
        }
    }
}

