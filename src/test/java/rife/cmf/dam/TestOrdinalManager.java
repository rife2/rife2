/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import rife.database.Datasource;
import rife.database.TestDatasources;
import rife.database.querymanagers.generic.GenericQueryManager;
import rife.database.querymanagers.generic.GenericQueryManagerFactory;

import static org.junit.jupiter.api.Assertions.*;

public class TestOrdinalManager {
    private OrdinalManager ordinalManager_ = null;
    private GenericQueryManager<Ordered> orderedManager_ = null;
    private OrdinalManager ordinalRestrictedManager_ = null;
    private GenericQueryManager<OrderedRestricted> orderedRestrictedManager_ = null;

    protected void setup(Datasource datasource) {
        orderedManager_ = GenericQueryManagerFactory.instance(datasource, Ordered.class);
        ordinalManager_ = new OrdinalManager(datasource, orderedManager_.getTable(), "priority");
        orderedManager_.install();
        orderedRestrictedManager_ = GenericQueryManagerFactory.instance(datasource, OrderedRestricted.class);
        ordinalRestrictedManager_ = new OrdinalManager(datasource, orderedRestrictedManager_.getTable(), "priority", "restricted");
        orderedRestrictedManager_.install();
    }

    protected void tearDown() {
        orderedManager_.remove();
        orderedRestrictedManager_.remove();
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testGetDirection(Datasource datasource) {
        setup(datasource);
        try {
            assertSame(OrdinalManager.UP, OrdinalManager.Direction.getDirection("up"));
            assertSame(OrdinalManager.DOWN, OrdinalManager.Direction.getDirection("down"));
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testInitializationIllegalArguments(Datasource datasource) {
        setup(datasource);
        try {
            try {
                new OrdinalManager(null, "table", "ordinal");
                fail();
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            }

            try {
                new OrdinalManager(datasource, null, "ordinal");
                fail();
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            }

            try {
                new OrdinalManager(datasource, "table", null);
                fail();
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            }

            try {
                new OrdinalManager(datasource, "table", "ordinal", null);
                fail();
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            }
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testInitialization(Datasource datasource) {
        setup(datasource);
        try {
            assertEquals(ordinalManager_.getTable(), orderedManager_.getTable());
            assertEquals(ordinalManager_.getOrdinalColumn(), "priority");
            assertNull(ordinalManager_.getRestrictColumn());
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testInitializationRestricted(Datasource datasource) {
        setup(datasource);
        try {
            assertEquals(ordinalRestrictedManager_.getTable(), orderedRestrictedManager_.getTable());
            assertEquals(ordinalRestrictedManager_.getOrdinalColumn(), "priority");
            assertEquals(ordinalRestrictedManager_.getRestrictColumn(), "restricted");
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testFree(Datasource datasource) {
        setup(datasource);
        try {
            var ordered1 = orderedManager_.save(new Ordered().name("ordered 1").priority(0));
            var ordered2 = orderedManager_.save(new Ordered().name("ordered 2").priority(1));
            var ordered3 = orderedManager_.save(new Ordered().name("ordered 3").priority(2));
            var ordered4 = orderedManager_.save(new Ordered().name("ordered 4").priority(3));
            var ordered5 = orderedManager_.save(new Ordered().name("ordered 5").priority(4));

            assertTrue(ordinalManager_.free(1));
            assertEquals(0, orderedManager_.restore(ordered1).getPriority());
            assertEquals(2, orderedManager_.restore(ordered2).getPriority());
            assertEquals(3, orderedManager_.restore(ordered3).getPriority());
            assertEquals(4, orderedManager_.restore(ordered4).getPriority());
            assertEquals(5, orderedManager_.restore(ordered5).getPriority());
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testFreeRestricted(Datasource datasource) {
        setup(datasource);
        try {
            var ordered1 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 1").priority(0).restricted(1));
            var ordered2 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 2").priority(1).restricted(1));
            var ordered3 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 3").priority(2).restricted(1));
            var ordered4 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 4").priority(0).restricted(2));
            var ordered5 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 5").priority(1).restricted(2));

            assertTrue(ordinalRestrictedManager_.free(1, 1));
            assertEquals(0, orderedRestrictedManager_.restore(ordered1).getPriority());
            assertEquals(2, orderedRestrictedManager_.restore(ordered2).getPriority());
            assertEquals(3, orderedRestrictedManager_.restore(ordered3).getPriority());
            assertEquals(0, orderedRestrictedManager_.restore(ordered4).getPriority());
            assertEquals(1, orderedRestrictedManager_.restore(ordered5).getPriority());

            assertTrue(ordinalRestrictedManager_.free(2, 0));
            assertEquals(0, orderedRestrictedManager_.restore(ordered1).getPriority());
            assertEquals(2, orderedRestrictedManager_.restore(ordered2).getPriority());
            assertEquals(3, orderedRestrictedManager_.restore(ordered3).getPriority());
            assertEquals(1, orderedRestrictedManager_.restore(ordered4).getPriority());
            assertEquals(2, orderedRestrictedManager_.restore(ordered5).getPriority());
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testFreeOutOfBounds(Datasource datasource) {
        setup(datasource);
        try {
            var ordered1 = orderedManager_.save(new Ordered().name("ordered 1").priority(0));
            var ordered2 = orderedManager_.save(new Ordered().name("ordered 2").priority(1));
            var ordered3 = orderedManager_.save(new Ordered().name("ordered 3").priority(2));
            var ordered4 = orderedManager_.save(new Ordered().name("ordered 4").priority(3));
            var ordered5 = orderedManager_.save(new Ordered().name("ordered 5").priority(4));

            assertTrue(ordinalManager_.free(5));
            assertEquals(0, orderedManager_.restore(ordered1).getPriority());
            assertEquals(1, orderedManager_.restore(ordered2).getPriority());
            assertEquals(2, orderedManager_.restore(ordered3).getPriority());
            assertEquals(3, orderedManager_.restore(ordered4).getPriority());
            assertEquals(4, orderedManager_.restore(ordered5).getPriority());

            assertFalse(ordinalManager_.free(-1));
            assertEquals(0, orderedManager_.restore(ordered1).getPriority());
            assertEquals(1, orderedManager_.restore(ordered2).getPriority());
            assertEquals(2, orderedManager_.restore(ordered3).getPriority());
            assertEquals(3, orderedManager_.restore(ordered4).getPriority());
            assertEquals(4, orderedManager_.restore(ordered5).getPriority());
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testFreeOutOfBoundsRestricted(Datasource datasource) {
        setup(datasource);
        try {
            var ordered1 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 1").priority(0).restricted(1));
            var ordered2 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 2").priority(1).restricted(1));
            var ordered3 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 3").priority(2).restricted(1));
            var ordered4 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 4").priority(0).restricted(2));
            var ordered5 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 5").priority(1).restricted(2));

            assertTrue(ordinalRestrictedManager_.free(1, 3));
            assertEquals(0, orderedRestrictedManager_.restore(ordered1).getPriority());
            assertEquals(1, orderedRestrictedManager_.restore(ordered2).getPriority());
            assertEquals(2, orderedRestrictedManager_.restore(ordered3).getPriority());
            assertEquals(0, orderedRestrictedManager_.restore(ordered4).getPriority());
            assertEquals(1, orderedRestrictedManager_.restore(ordered5).getPriority());

            assertFalse(ordinalRestrictedManager_.free(2, -1));
            assertEquals(0, orderedRestrictedManager_.restore(ordered1).getPriority());
            assertEquals(1, orderedRestrictedManager_.restore(ordered2).getPriority());
            assertEquals(2, orderedRestrictedManager_.restore(ordered3).getPriority());
            assertEquals(0, orderedRestrictedManager_.restore(ordered4).getPriority());
            assertEquals(1, orderedRestrictedManager_.restore(ordered5).getPriority());
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testFreeOutOfBoundsUnknownRestricted(Datasource datasource) {
        setup(datasource);
        try {
            var ordered1 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 1").priority(0).restricted(1));
            var ordered2 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 2").priority(1).restricted(1));
            var ordered3 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 3").priority(2).restricted(1));
            var ordered4 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 4").priority(0).restricted(2));
            var ordered5 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 5").priority(1).restricted(2));

            assertTrue(ordinalRestrictedManager_.free(3, 1));
            assertEquals(0, orderedRestrictedManager_.restore(ordered1).getPriority());
            assertEquals(1, orderedRestrictedManager_.restore(ordered2).getPriority());
            assertEquals(2, orderedRestrictedManager_.restore(ordered3).getPriority());
            assertEquals(0, orderedRestrictedManager_.restore(ordered4).getPriority());
            assertEquals(1, orderedRestrictedManager_.restore(ordered5).getPriority());
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testMove(Datasource datasource) {
        setup(datasource);
        try {
            var ordered1 = orderedManager_.save(new Ordered().name("ordered 1").priority(0));
            var ordered2 = orderedManager_.save(new Ordered().name("ordered 2").priority(1));
            var ordered3 = orderedManager_.save(new Ordered().name("ordered 3").priority(2));
            var ordered4 = orderedManager_.save(new Ordered().name("ordered 4").priority(3));
            var ordered5 = orderedManager_.save(new Ordered().name("ordered 5").priority(4));

            assertTrue(ordinalManager_.free(3));
            assertTrue(ordinalManager_.update(1, 3));
            assertEquals(0, orderedManager_.restore(ordered1).getPriority());
            assertEquals(2, orderedManager_.restore(ordered3).getPriority());
            assertEquals(3, orderedManager_.restore(ordered2).getPriority());
            assertEquals(4, orderedManager_.restore(ordered4).getPriority());
            assertEquals(5, orderedManager_.restore(ordered5).getPriority());
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testTighten(Datasource datasource) {
        setup(datasource);
        try {
            var ordered1 = orderedManager_.save(new Ordered().name("ordered 1").priority(3));
            var ordered2 = orderedManager_.save(new Ordered().name("ordered 2").priority(13));
            var ordered3 = orderedManager_.save(new Ordered().name("ordered 3").priority(28));
            var ordered4 = orderedManager_.save(new Ordered().name("ordered 4").priority(56));
            var ordered5 = orderedManager_.save(new Ordered().name("ordered 5").priority(300));

            assertTrue(ordinalManager_.tighten());
            assertEquals(0, orderedManager_.restore(ordered1).getPriority());
            assertEquals(1, orderedManager_.restore(ordered2).getPriority());
            assertEquals(2, orderedManager_.restore(ordered3).getPriority());
            assertEquals(3, orderedManager_.restore(ordered4).getPriority());
            assertEquals(4, orderedManager_.restore(ordered5).getPriority());
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testTightenRestricted(Datasource datasource) {
        setup(datasource);
        try {
            var ordered1 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 1").priority(3).restricted(1));
            var ordered2 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 2").priority(13).restricted(1));
            var ordered3 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 3").priority(28).restricted(1));
            var ordered4 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 4").priority(56).restricted(2));
            var ordered5 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 5").priority(300).restricted(2));

            assertTrue(ordinalRestrictedManager_.tighten(1));
            assertEquals(0, orderedRestrictedManager_.restore(ordered1).getPriority());
            assertEquals(1, orderedRestrictedManager_.restore(ordered2).getPriority());
            assertEquals(2, orderedRestrictedManager_.restore(ordered3).getPriority());
            assertEquals(56, orderedRestrictedManager_.restore(ordered4).getPriority());
            assertEquals(300, orderedRestrictedManager_.restore(ordered5).getPriority());

            assertTrue(ordinalRestrictedManager_.tighten(2));
            assertEquals(0, orderedRestrictedManager_.restore(ordered1).getPriority());
            assertEquals(1, orderedRestrictedManager_.restore(ordered2).getPriority());
            assertEquals(2, orderedRestrictedManager_.restore(ordered3).getPriority());
            assertEquals(0, orderedRestrictedManager_.restore(ordered4).getPriority());
            assertEquals(1, orderedRestrictedManager_.restore(ordered5).getPriority());

            assertFalse(ordinalRestrictedManager_.tighten(3));
            assertEquals(0, orderedRestrictedManager_.restore(ordered1).getPriority());
            assertEquals(1, orderedRestrictedManager_.restore(ordered2).getPriority());
            assertEquals(2, orderedRestrictedManager_.restore(ordered3).getPriority());
            assertEquals(0, orderedRestrictedManager_.restore(ordered4).getPriority());
            assertEquals(1, orderedRestrictedManager_.restore(ordered5).getPriority());
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testObtainInsertOrdinal(Datasource datasource) {
        setup(datasource);
        try {
            orderedManager_.save(new Ordered().name("ordered 1").priority(0));
            orderedManager_.save(new Ordered().name("ordered 2").priority(1));
            orderedManager_.save(new Ordered().name("ordered 3").priority(2));
            orderedManager_.save(new Ordered().name("ordered 4").priority(3));
            orderedManager_.save(new Ordered().name("ordered 5").priority(4));
            assertEquals(5, ordinalManager_.obtainInsertOrdinal());
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testObtainInsertOrdinalRestricted(Datasource datasource) {
        setup(datasource);
        try {
            orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 1").priority(0).restricted(1));
            orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 2").priority(1).restricted(1));
            orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 3").priority(2).restricted(1));
            orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 4").priority(0).restricted(2));
            orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 5").priority(1).restricted(2));
            assertEquals(3, ordinalRestrictedManager_.obtainInsertOrdinal(1));
            assertEquals(2, ordinalRestrictedManager_.obtainInsertOrdinal(2));
            assertEquals(0, ordinalRestrictedManager_.obtainInsertOrdinal(3));
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testMoveDown(Datasource datasource) {
        setup(datasource);
        try {
            var ordered1 = orderedManager_.save(new Ordered().name("ordered 1").priority(0));
            var ordered2 = orderedManager_.save(new Ordered().name("ordered 2").priority(1));
            var ordered3 = orderedManager_.save(new Ordered().name("ordered 3").priority(2));
            var ordered4 = orderedManager_.save(new Ordered().name("ordered 4").priority(3));
            var ordered5 = orderedManager_.save(new Ordered().name("ordered 5").priority(4));

            assertTrue(ordinalManager_.move(OrdinalManager.DOWN, 3));
            assertEquals(0, orderedManager_.restore(ordered1).getPriority());
            assertEquals(1, orderedManager_.restore(ordered2).getPriority());
            assertEquals(2, orderedManager_.restore(ordered3).getPriority());
            assertEquals(3, orderedManager_.restore(ordered5).getPriority());
            assertEquals(4, orderedManager_.restore(ordered4).getPriority());
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testMoveDownRestricted(Datasource datasource) {
        setup(datasource);
        try {
            var ordered1 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 1").priority(0).restricted(1));
            var ordered2 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 2").priority(1).restricted(1));
            var ordered3 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 3").priority(2).restricted(1));
            var ordered4 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 4").priority(0).restricted(2));
            var ordered5 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 5").priority(1).restricted(2));

            assertTrue(ordinalRestrictedManager_.move(OrdinalManager.DOWN, 1, 1));
            assertEquals(0, orderedRestrictedManager_.restore(ordered1).getPriority());
            assertEquals(1, orderedRestrictedManager_.restore(ordered3).getPriority());
            assertEquals(2, orderedRestrictedManager_.restore(ordered2).getPriority());
            assertEquals(0, orderedRestrictedManager_.restore(ordered4).getPriority());
            assertEquals(1, orderedRestrictedManager_.restore(ordered5).getPriority());

            assertTrue(ordinalRestrictedManager_.move(OrdinalManager.DOWN, 2, 0));
            assertEquals(0, orderedRestrictedManager_.restore(ordered1).getPriority());
            assertEquals(1, orderedRestrictedManager_.restore(ordered3).getPriority());
            assertEquals(2, orderedRestrictedManager_.restore(ordered2).getPriority());
            assertEquals(0, orderedRestrictedManager_.restore(ordered5).getPriority());
            assertEquals(1, orderedRestrictedManager_.restore(ordered4).getPriority());
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testMoveUp(Datasource datasource) {
        setup(datasource);
        try {
            var ordered1 = orderedManager_.save(new Ordered().name("ordered 1").priority(0));
            var ordered2 = orderedManager_.save(new Ordered().name("ordered 2").priority(1));
            var ordered3 = orderedManager_.save(new Ordered().name("ordered 3").priority(2));
            var ordered4 = orderedManager_.save(new Ordered().name("ordered 4").priority(3));
            var ordered5 = orderedManager_.save(new Ordered().name("ordered 5").priority(4));

            assertTrue(ordinalManager_.move(OrdinalManager.UP, 3));
            assertEquals(0, orderedManager_.restore(ordered1).getPriority());
            assertEquals(1, orderedManager_.restore(ordered2).getPriority());
            assertEquals(2, orderedManager_.restore(ordered4).getPriority());
            assertEquals(3, orderedManager_.restore(ordered3).getPriority());
            assertEquals(4, orderedManager_.restore(ordered5).getPriority());
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testMoveUpRestricted(Datasource datasource) {
        setup(datasource);
        try {
            var ordered1 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 1").priority(0).restricted(1));
            var ordered2 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 2").priority(1).restricted(1));
            var ordered3 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 3").priority(2).restricted(1));
            var ordered4 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 4").priority(0).restricted(2));
            var ordered5 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 5").priority(1).restricted(2));

            assertTrue(ordinalRestrictedManager_.move(OrdinalManager.UP, 1, 2));
            assertEquals(0, orderedRestrictedManager_.restore(ordered1).getPriority());
            assertEquals(1, orderedRestrictedManager_.restore(ordered3).getPriority());
            assertEquals(2, orderedRestrictedManager_.restore(ordered2).getPriority());
            assertEquals(0, orderedRestrictedManager_.restore(ordered4).getPriority());
            assertEquals(1, orderedRestrictedManager_.restore(ordered5).getPriority());

            assertTrue(ordinalRestrictedManager_.move(OrdinalManager.UP, 2, 1));
            assertEquals(0, orderedRestrictedManager_.restore(ordered1).getPriority());
            assertEquals(1, orderedRestrictedManager_.restore(ordered3).getPriority());
            assertEquals(2, orderedRestrictedManager_.restore(ordered2).getPriority());
            assertEquals(0, orderedRestrictedManager_.restore(ordered5).getPriority());
            assertEquals(1, orderedRestrictedManager_.restore(ordered4).getPriority());
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testMoveOther(Datasource datasource) {
        setup(datasource);
        try {
            var ordered1 = orderedManager_.save(new Ordered().name("ordered 1").priority(0));
            var ordered2 = orderedManager_.save(new Ordered().name("ordered 2").priority(1));
            var ordered3 = orderedManager_.save(new Ordered().name("ordered 3").priority(2));
            var ordered4 = orderedManager_.save(new Ordered().name("ordered 4").priority(3));
            var ordered5 = orderedManager_.save(new Ordered().name("ordered 5").priority(4));

            assertTrue(ordinalManager_.move(3, 1));
            assertEquals(0, orderedManager_.restore(ordered1).getPriority());
            assertEquals(1, orderedManager_.restore(ordered4).getPriority());
            assertEquals(2, orderedManager_.restore(ordered2).getPriority());
            assertEquals(3, orderedManager_.restore(ordered3).getPriority());
            assertEquals(4, orderedManager_.restore(ordered5).getPriority());

            assertTrue(ordinalManager_.move(2, 4));
            assertEquals(0, orderedManager_.restore(ordered1).getPriority());
            assertEquals(1, orderedManager_.restore(ordered4).getPriority());
            assertEquals(2, orderedManager_.restore(ordered3).getPriority());
            assertEquals(3, orderedManager_.restore(ordered2).getPriority());
            assertEquals(4, orderedManager_.restore(ordered5).getPriority());
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testMoveOtherRestricted(Datasource datasource) {
        setup(datasource);
        try {
            var ordered1 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 1").priority(0).restricted(1));
            var ordered2 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 2").priority(1).restricted(1));
            var ordered3 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 3").priority(2).restricted(1));
            var ordered4 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 4").priority(0).restricted(2));
            var ordered5 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 5").priority(1).restricted(2));

            assertTrue(ordinalRestrictedManager_.move(1, 2, 0));
            assertEquals(0, orderedRestrictedManager_.restore(ordered3).getPriority());
            assertEquals(1, orderedRestrictedManager_.restore(ordered1).getPriority());
            assertEquals(2, orderedRestrictedManager_.restore(ordered2).getPriority());
            assertEquals(0, orderedRestrictedManager_.restore(ordered4).getPriority());
            assertEquals(1, orderedRestrictedManager_.restore(ordered5).getPriority());

            assertTrue(ordinalRestrictedManager_.move(2, 1, 0));
            assertEquals(0, orderedRestrictedManager_.restore(ordered3).getPriority());
            assertEquals(1, orderedRestrictedManager_.restore(ordered1).getPriority());
            assertEquals(2, orderedRestrictedManager_.restore(ordered2).getPriority());
            assertEquals(0, orderedRestrictedManager_.restore(ordered5).getPriority());
            assertEquals(1, orderedRestrictedManager_.restore(ordered4).getPriority());

            assertTrue(ordinalRestrictedManager_.move(1, 0, 2));
            assertEquals(0, orderedRestrictedManager_.restore(ordered1).getPriority());
            assertEquals(1, orderedRestrictedManager_.restore(ordered3).getPriority());
            assertEquals(2, orderedRestrictedManager_.restore(ordered2).getPriority());
            assertEquals(0, orderedRestrictedManager_.restore(ordered5).getPriority());
            assertEquals(1, orderedRestrictedManager_.restore(ordered4).getPriority());

            assertTrue(ordinalRestrictedManager_.move(2, 0, 1));
            assertEquals(0, orderedRestrictedManager_.restore(ordered1).getPriority());
            assertEquals(1, orderedRestrictedManager_.restore(ordered3).getPriority());
            assertEquals(2, orderedRestrictedManager_.restore(ordered2).getPriority());
            assertEquals(0, orderedRestrictedManager_.restore(ordered5).getPriority());
            assertEquals(1, orderedRestrictedManager_.restore(ordered4).getPriority());
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testMoveDownExtremity(Datasource datasource) {
        setup(datasource);
        try {
            var ordered1 = orderedManager_.save(new Ordered().name("ordered 1").priority(0));
            var ordered2 = orderedManager_.save(new Ordered().name("ordered 2").priority(1));
            var ordered3 = orderedManager_.save(new Ordered().name("ordered 3").priority(2));
            var ordered4 = orderedManager_.save(new Ordered().name("ordered 4").priority(3));
            var ordered5 = orderedManager_.save(new Ordered().name("ordered 5").priority(4));

            assertTrue(ordinalManager_.move(OrdinalManager.DOWN, 4));
            assertEquals(0, orderedManager_.restore(ordered1).getPriority());
            assertEquals(1, orderedManager_.restore(ordered2).getPriority());
            assertEquals(2, orderedManager_.restore(ordered3).getPriority());
            assertEquals(3, orderedManager_.restore(ordered4).getPriority());
            assertEquals(4, orderedManager_.restore(ordered5).getPriority());
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testMoveDownExtremityRestricted(Datasource datasource) {
        setup(datasource);
        try {
            var ordered1 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 1").priority(0).restricted(1));
            var ordered2 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 2").priority(1).restricted(1));
            var ordered3 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 3").priority(2).restricted(1));
            var ordered4 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 4").priority(0).restricted(2));
            var ordered5 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 5").priority(1).restricted(2));

            assertTrue(ordinalRestrictedManager_.move(OrdinalManager.DOWN, 1, 2));
            assertEquals(0, orderedRestrictedManager_.restore(ordered1).getPriority());
            assertEquals(1, orderedRestrictedManager_.restore(ordered2).getPriority());
            assertEquals(2, orderedRestrictedManager_.restore(ordered3).getPriority());
            assertEquals(0, orderedRestrictedManager_.restore(ordered4).getPriority());
            assertEquals(1, orderedRestrictedManager_.restore(ordered5).getPriority());

            assertTrue(ordinalRestrictedManager_.move(OrdinalManager.DOWN, 2, 1));
            assertEquals(0, orderedRestrictedManager_.restore(ordered1).getPriority());
            assertEquals(1, orderedRestrictedManager_.restore(ordered2).getPriority());
            assertEquals(2, orderedRestrictedManager_.restore(ordered3).getPriority());
            assertEquals(0, orderedRestrictedManager_.restore(ordered4).getPriority());
            assertEquals(1, orderedRestrictedManager_.restore(ordered5).getPriority());
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testMoveUpExtremity(Datasource datasource) {
        setup(datasource);
        try {
            var ordered1 = orderedManager_.save(new Ordered().name("ordered 1").priority(0));
            var ordered2 = orderedManager_.save(new Ordered().name("ordered 2").priority(1));
            var ordered3 = orderedManager_.save(new Ordered().name("ordered 3").priority(2));
            var ordered4 = orderedManager_.save(new Ordered().name("ordered 4").priority(3));
            var ordered5 = orderedManager_.save(new Ordered().name("ordered 5").priority(4));

            assertFalse(ordinalManager_.move(OrdinalManager.UP, 0));
            assertEquals(0, orderedManager_.restore(ordered1).getPriority());
            assertEquals(1, orderedManager_.restore(ordered2).getPriority());
            assertEquals(2, orderedManager_.restore(ordered3).getPriority());
            assertEquals(3, orderedManager_.restore(ordered4).getPriority());
            assertEquals(4, orderedManager_.restore(ordered5).getPriority());
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testMoveUpExtremityRestricted(Datasource datasource) {
        setup(datasource);
        try {
            var ordered1 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 1").priority(0).restricted(1));
            var ordered2 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 2").priority(1).restricted(1));
            var ordered3 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 3").priority(2).restricted(1));
            var ordered4 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 4").priority(0).restricted(2));
            var ordered5 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 5").priority(1).restricted(2));

            assertFalse(ordinalRestrictedManager_.move(OrdinalManager.UP, 1, 0));
            assertEquals(0, orderedRestrictedManager_.restore(ordered1).getPriority());
            assertEquals(1, orderedRestrictedManager_.restore(ordered2).getPriority());
            assertEquals(2, orderedRestrictedManager_.restore(ordered3).getPriority());
            assertEquals(0, orderedRestrictedManager_.restore(ordered4).getPriority());
            assertEquals(1, orderedRestrictedManager_.restore(ordered5).getPriority());

            assertFalse(ordinalRestrictedManager_.move(OrdinalManager.UP, 2, 0));
            assertEquals(0, orderedRestrictedManager_.restore(ordered1).getPriority());
            assertEquals(1, orderedRestrictedManager_.restore(ordered2).getPriority());
            assertEquals(2, orderedRestrictedManager_.restore(ordered3).getPriority());
            assertEquals(0, orderedRestrictedManager_.restore(ordered4).getPriority());
            assertEquals(1, orderedRestrictedManager_.restore(ordered5).getPriority());
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testMoveOtherExtremity(Datasource datasource) {
        setup(datasource);
        try {
            var ordered1 = orderedManager_.save(new Ordered().name("ordered 1").priority(0));
            var ordered2 = orderedManager_.save(new Ordered().name("ordered 2").priority(1));
            var ordered3 = orderedManager_.save(new Ordered().name("ordered 3").priority(2));
            var ordered4 = orderedManager_.save(new Ordered().name("ordered 4").priority(3));
            var ordered5 = orderedManager_.save(new Ordered().name("ordered 5").priority(4));

            assertTrue(ordinalManager_.move(3, 0));
            assertEquals(0, orderedManager_.restore(ordered4).getPriority());
            assertEquals(1, orderedManager_.restore(ordered1).getPriority());
            assertEquals(2, orderedManager_.restore(ordered2).getPriority());
            assertEquals(3, orderedManager_.restore(ordered3).getPriority());
            assertEquals(4, orderedManager_.restore(ordered5).getPriority());

            assertTrue(ordinalManager_.move(2, 8));
            assertEquals(0, orderedManager_.restore(ordered4).getPriority());
            assertEquals(1, orderedManager_.restore(ordered1).getPriority());
            assertEquals(2, orderedManager_.restore(ordered3).getPriority());
            assertEquals(3, orderedManager_.restore(ordered5).getPriority());
            assertEquals(4, orderedManager_.restore(ordered2).getPriority());
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testMoveOtherExtremityRestricted(Datasource datasource) {
        setup(datasource);
        try {
            var ordered1 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 1").priority(0).restricted(1));
            var ordered2 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 2").priority(1).restricted(1));
            var ordered3 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 3").priority(2).restricted(1));
            var ordered4 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 4").priority(0).restricted(2));
            var ordered5 = orderedRestrictedManager_.save(new OrderedRestricted().name("ordered 5").priority(1).restricted(2));

            assertTrue(ordinalRestrictedManager_.move(1, 2, 0));
            assertEquals(0, orderedRestrictedManager_.restore(ordered3).getPriority());
            assertEquals(1, orderedRestrictedManager_.restore(ordered1).getPriority());
            assertEquals(2, orderedRestrictedManager_.restore(ordered2).getPriority());
            assertEquals(0, orderedRestrictedManager_.restore(ordered4).getPriority());
            assertEquals(1, orderedRestrictedManager_.restore(ordered5).getPriority());

            assertTrue(ordinalRestrictedManager_.move(2, 1, 0));
            assertEquals(0, orderedRestrictedManager_.restore(ordered3).getPriority());
            assertEquals(1, orderedRestrictedManager_.restore(ordered1).getPriority());
            assertEquals(2, orderedRestrictedManager_.restore(ordered2).getPriority());
            assertEquals(0, orderedRestrictedManager_.restore(ordered5).getPriority());
            assertEquals(1, orderedRestrictedManager_.restore(ordered4).getPriority());

            assertTrue(ordinalRestrictedManager_.move(1, 0, 6));
            assertEquals(0, orderedRestrictedManager_.restore(ordered1).getPriority());
            assertEquals(1, orderedRestrictedManager_.restore(ordered2).getPriority());
            assertEquals(2, orderedRestrictedManager_.restore(ordered3).getPriority());
            assertEquals(0, orderedRestrictedManager_.restore(ordered5).getPriority());
            assertEquals(1, orderedRestrictedManager_.restore(ordered4).getPriority());

            assertTrue(ordinalRestrictedManager_.move(2, 0, 7));
            assertEquals(0, orderedRestrictedManager_.restore(ordered1).getPriority());
            assertEquals(1, orderedRestrictedManager_.restore(ordered2).getPriority());
            assertEquals(2, orderedRestrictedManager_.restore(ordered3).getPriority());
            assertEquals(0, orderedRestrictedManager_.restore(ordered4).getPriority());
            assertEquals(1, orderedRestrictedManager_.restore(ordered5).getPriority());
        } finally {
            tearDown();
        }
    }
}
