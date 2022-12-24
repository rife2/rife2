package rife.engine.continuations;

import rife.engine.Context;
import rife.engine.Element;

public class TestNoPause implements Element {
    public void process(Context c) {
        var before = "before simple pause";
        assert before != null;
        var after = "after simple pause";
        assert after != null;

        c.print(c.continuationId());
    }
}
