package rife.engine.continuations;

import rife.engine.Context;
import rife.engine.Element;

public class TestPauseElement implements Element {
    public void process(Context c)
    throws Exception {
        var before = "before simple pause";
        var after = "after simple pause";

        c.print(before + "\n" + c.continuationId());
        c.pause();
        c.print(after);
    }
}
