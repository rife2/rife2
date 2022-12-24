package rife.engine.continuations;

import rife.engine.Context;
import rife.engine.Element;

public class TestNull implements Element {
    public void process(Context c)
    throws Exception {
        String response = null;

        c.print("before null pause\n" + c.continuationId());
        c.pause();

        response = c.parameter("response");

        c.print(response);
    }
}
