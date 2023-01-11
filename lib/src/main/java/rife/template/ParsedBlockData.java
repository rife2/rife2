/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

import java.io.Serial;
import java.util.ArrayList;

class ParsedBlockData extends ArrayList<ParsedBlockPart> {
    @Serial private static final long serialVersionUID = -6957434329992948164L;

    ParsedBlockData() {
        super();
    }

    void addPart(ParsedBlockPart part) {
        assert part != null;

        add(part);
    }

    int countParts() {
        return size();
    }

    ParsedBlockPart getPart(int index) {
        assert index >= 0;

        return get(index);
    }

    ParsedBlockPart getLastPart() {
        if (isEmpty()) {
            return null;
        }

        return get(size()-1);
    }
}
