package org.deegree.services.wfs.te;

import org.junit.Assert;
import org.junit.Test;

import aero.m_click.wfs_te.model.SimpleMap;
import aero.m_click.wfs_te.strategy.SimpleStrategy;

public class LaxDuringTest {
    @Test
    public void instantInstant() {
        assertLaxDuring(SimpleMap.timePrimitive("2014-01-01T00:00:01"), SimpleMap.timePrimitive("2014-01-01T00:00:01"));
        assertNotLaxDuring(SimpleMap.timePrimitive("2014-01-01T00:00:01"), SimpleMap.timePrimitive("2014-01-01T00:00:02"));
        assertNotLaxDuring(SimpleMap.timePrimitive("2014-01-01T00:00:02"), SimpleMap.timePrimitive("2014-01-01T00:00:01"));
    }
    @Test
    public void periodInstant() {
        assertNotLaxDuring(SimpleMap.timePrimitive("2014-01-01T00:00:01", "INDETERMINATE"), SimpleMap.timePrimitive("2014-01-01T00:00:00"));
        assertNotLaxDuring(SimpleMap.timePrimitive("2014-01-01T00:00:01", "INDETERMINATE"), SimpleMap.timePrimitive("2014-01-01T00:00:01"));
        assertNotLaxDuring(SimpleMap.timePrimitive("2014-01-01T00:00:01", "INDETERMINATE"), SimpleMap.timePrimitive("2014-01-01T00:00:02"));
        assertNotLaxDuring(SimpleMap.timePrimitive("2014-01-01T00:00:01", "2014-01-01T00:00:02"), SimpleMap.timePrimitive("2014-01-01T00:00:00"));
        assertNotLaxDuring(SimpleMap.timePrimitive("2014-01-01T00:00:01", "2014-01-01T00:00:02"), SimpleMap.timePrimitive("2014-01-01T00:00:01"));
        assertNotLaxDuring(SimpleMap.timePrimitive("2014-01-01T00:00:01", "2014-01-01T00:00:02"), SimpleMap.timePrimitive("2014-01-01T00:00:02"));
        assertNotLaxDuring(SimpleMap.timePrimitive("2014-01-01T00:00:01", "2014-01-01T00:00:02"), SimpleMap.timePrimitive("2014-01-01T00:00:03"));
    }
    @Test
    public void instantPeriod() {
        assertNotLaxDuring(SimpleMap.timePrimitive("2014-01-01T00:00:00"), SimpleMap.timePrimitive("2014-01-01T00:00:01", "INDETERMINATE"));
        assertLaxDuring(SimpleMap.timePrimitive("2014-01-01T00:00:01"), SimpleMap.timePrimitive("2014-01-01T00:00:01", "INDETERMINATE"));
        assertLaxDuring(SimpleMap.timePrimitive("2014-01-01T00:00:02"), SimpleMap.timePrimitive("2014-01-01T00:00:01", "INDETERMINATE"));
        assertNotLaxDuring(SimpleMap.timePrimitive("2014-01-01T00:00:00"), SimpleMap.timePrimitive("2014-01-01T00:00:01", "2014-01-01T00:00:02"));
        assertLaxDuring(SimpleMap.timePrimitive("2014-01-01T00:00:01"), SimpleMap.timePrimitive("2014-01-01T00:00:01", "2014-01-01T00:00:02"));
        assertNotLaxDuring(SimpleMap.timePrimitive("2014-01-01T00:00:02"), SimpleMap.timePrimitive("2014-01-01T00:00:01", "2014-01-01T00:00:02"));
        assertNotLaxDuring(SimpleMap.timePrimitive("2014-01-01T00:00:03"), SimpleMap.timePrimitive("2014-01-01T00:00:01", "2014-01-01T00:00:02"));
        assertNotLaxDuring(SimpleMap.timePrimitive("2014-01-01T00:00:00"), SimpleMap.timePrimitive("2014-01-01T00:00:01", "2014-01-01T00:00:03"));
        assertLaxDuring(SimpleMap.timePrimitive("2014-01-01T00:00:01"), SimpleMap.timePrimitive("2014-01-01T00:00:01", "2014-01-01T00:00:03"));
        assertLaxDuring(SimpleMap.timePrimitive("2014-01-01T00:00:02"), SimpleMap.timePrimitive("2014-01-01T00:00:01", "2014-01-01T00:00:03"));
        assertNotLaxDuring(SimpleMap.timePrimitive("2014-01-01T00:00:03"), SimpleMap.timePrimitive("2014-01-01T00:00:01", "2014-01-01T00:00:03"));
        assertNotLaxDuring(SimpleMap.timePrimitive("2014-01-01T00:00:04"), SimpleMap.timePrimitive("2014-01-01T00:00:01", "2014-01-01T00:00:03"));
    }
    @Test
    public void periodPeriod() {
        // Begins
        assertLaxDuring(SimpleMap.timePrimitive("2014-01-01T00:00:02", "2014-01-01T00:00:05"), SimpleMap.timePrimitive("2014-01-01T00:00:02", "2014-01-01T00:00:06"));
        assertLaxDuring(SimpleMap.timePrimitive("2014-01-01T00:00:02", "2014-01-01T00:00:05"), SimpleMap.timePrimitive("2014-01-01T00:00:02", "INDETERMINATE"));
        // Ends
        assertLaxDuring(SimpleMap.timePrimitive("2014-01-01T00:00:02", "2014-01-01T00:00:05"), SimpleMap.timePrimitive("2014-01-01T00:00:01", "2014-01-01T00:00:05"));
        // During
        assertLaxDuring(SimpleMap.timePrimitive("2014-01-01T00:00:02", "2014-01-01T00:00:05"), SimpleMap.timePrimitive("2014-01-01T00:00:01", "2014-01-01T00:00:06"));
        assertLaxDuring(SimpleMap.timePrimitive("2014-01-01T00:00:02", "2014-01-01T00:00:05"), SimpleMap.timePrimitive("2014-01-01T00:00:01", "INDETERMINATE"));
        // TEquals
        assertLaxDuring(SimpleMap.timePrimitive("2014-01-01T00:00:02", "2014-01-01T00:00:05"), SimpleMap.timePrimitive("2014-01-01T00:00:02", "2014-01-01T00:00:05"));
        // BegunBy
        assertNotLaxDuring(SimpleMap.timePrimitive("2014-01-01T00:00:02", "2014-01-01T00:00:05"), SimpleMap.timePrimitive("2014-01-01T00:00:02", "2014-01-01T00:00:03"));
        // EndedBy
        assertNotLaxDuring(SimpleMap.timePrimitive("2014-01-01T00:00:02", "2014-01-01T00:00:05"), SimpleMap.timePrimitive("2014-01-01T00:00:04", "2014-01-01T00:00:05"));
        // After
        assertNotLaxDuring(SimpleMap.timePrimitive("2014-01-01T00:00:02", "2014-01-01T00:00:05"), SimpleMap.timePrimitive("2014-01-01T00:00:00", "2014-01-01T00:00:01"));
        // Before
        assertNotLaxDuring(SimpleMap.timePrimitive("2014-01-01T00:00:02", "2014-01-01T00:00:05"), SimpleMap.timePrimitive("2014-01-01T00:00:06", "2014-01-01T00:00:07"));
        assertNotLaxDuring(SimpleMap.timePrimitive("2014-01-01T00:00:02", "2014-01-01T00:00:05"), SimpleMap.timePrimitive("2014-01-01T00:00:06", "INDETERMINATE"));
        // TContains
        assertNotLaxDuring(SimpleMap.timePrimitive("2014-01-01T00:00:02", "2014-01-01T00:00:05"), SimpleMap.timePrimitive("2014-01-01T00:00:03", "2014-01-01T00:00:04"));
        // TOverlaps
        assertNotLaxDuring(SimpleMap.timePrimitive("2014-01-01T00:00:02", "2014-01-01T00:00:05"), SimpleMap.timePrimitive("2014-01-01T00:00:04", "2014-01-01T00:00:06"));
        assertNotLaxDuring(SimpleMap.timePrimitive("2014-01-01T00:00:02", "2014-01-01T00:00:05"), SimpleMap.timePrimitive("2014-01-01T00:00:04", "INDETERMINATE"));
        // OverlappedBy
        assertNotLaxDuring(SimpleMap.timePrimitive("2014-01-01T00:00:02", "2014-01-01T00:00:05"), SimpleMap.timePrimitive("2014-01-01T00:00:01", "2014-01-01T00:00:03"));
        // Meets
        assertNotLaxDuring(SimpleMap.timePrimitive("2014-01-01T00:00:02", "2014-01-01T00:00:05"), SimpleMap.timePrimitive("2014-01-01T00:00:05", "2014-01-01T00:00:06"));
        assertNotLaxDuring(SimpleMap.timePrimitive("2014-01-01T00:00:02", "2014-01-01T00:00:05"), SimpleMap.timePrimitive("2014-01-01T00:00:05", "INDETERMINATE"));
        // MetBy
        assertNotLaxDuring(SimpleMap.timePrimitive("2014-01-01T00:00:02", "2014-01-01T00:00:05"), SimpleMap.timePrimitive("2014-01-01T00:00:01", "2014-01-01T00:00:02"));
    }
    private void assertLaxDuring(SimpleMap a, SimpleMap b) {
        Assert.assertTrue(laxDuring(a, b));
    }
    private void assertNotLaxDuring(SimpleMap a, SimpleMap b) {
        Assert.assertFalse(laxDuring(a, b));
    }
    private boolean laxDuring(SimpleMap a, SimpleMap b) {
        final SimpleStrategy st = new SimpleStrategy();
        return st.laxDuring(a, b);
    }
}
