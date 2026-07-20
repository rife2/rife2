/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import org.junit.jupiter.api.Test;
import rife.ioc.HierarchicalProperties;
import rife.test.MockConversation;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestRouterDestroy {
    @Test
    void testDestroyPropagatesToGroupedRouters() {
        var order = new ArrayList<String>();
        var site = new Site() {
            public void setup() {
                group(new Router() {
                    public void setup() {
                        group(new Router() {
                            public void destroy() {
                                order.add("grandchild");
                            }
                        });
                    }

                    public void destroy() {
                        order.add("child");
                    }
                });
                group("/other", new Router() {
                    public void destroy() {
                        order.add("sibling");
                    }
                });
            }

            public void destroy() {
                order.add("site");
            }
        };

        var gate = new Gate();
        gate.setup(new HierarchicalProperties(), site);
        gate.destroy();

        assertEquals(List.of("grandchild", "child", "sibling", "site"), order);
    }

    @Test
    void testDestroyThroughMockConversation() {
        var order = new ArrayList<String>();
        var m = new MockConversation(new Site() {
            public void setup() {
                group("/grouped", new Router() {
                    public void destroy() {
                        order.add("grouped");
                    }
                });
            }

            public void destroy() {
                order.add("site");
            }
        });

        m.destroy();

        assertEquals(List.of("grouped", "site"), order);
    }

    @Test
    void testDestroyContinuesAfterFailure() {
        var order = new ArrayList<String>();
        var site = new Site() {
            public void setup() {
                group(new Router() {
                    public void destroy() {
                        throw new RuntimeException("destroy failure");
                    }
                });
                group("/other", new Router() {
                    public void destroy() {
                        order.add("sibling");
                    }
                });
            }

            public void destroy() {
                order.add("site");
            }
        };

        var gate = new Gate();
        gate.setup(new HierarchicalProperties(), site);
        gate.destroy();

        assertEquals(List.of("sibling", "site"), order);
    }
}
