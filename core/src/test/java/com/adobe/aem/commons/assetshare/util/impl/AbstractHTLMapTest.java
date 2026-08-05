package com.adobe.aem.commons.assetshare.util.impl;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AbstractHTLMapTest {

    private AbstractHTLMap map;

    @Before
    public void setUp() {
        map = new AbstractHTLMap() {
            // Test double: exercises the base class' default behavior as-is.
            // Map#get(Object) is intentionally left unimplemented by AbstractHTLMap itself
            // (each concrete subclass, e.g. ModelCacheImpl, provides its own semantics).
            @Override
            public Object get(Object key) {
                return null;
            }
        };
    }

    @Test
    public void size_alwaysZero() {
        assertEquals(0, map.size());
    }

    @Test
    public void isEmpty_alwaysFalse() {
        // Odd but intentional: the base implementation always reports false, regardless of size().
        assertFalse(map.isEmpty());
    }

    @Test
    public void containsKey_alwaysFalse() {
        assertFalse(map.containsKey("anything"));
    }

    @Test
    public void containsValue_alwaysFalse() {
        assertFalse(map.containsValue("anything"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void put_throwsUnsupportedOperationException() {
        map.put("key", "value");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void remove_throwsUnsupportedOperationException() {
        map.remove("key");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void putAll_throwsUnsupportedOperationException() {
        map.putAll(Collections.singletonMap("key", "value"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void clear_throwsUnsupportedOperationException() {
        map.clear();
    }

    @Test
    public void keySet_isEmpty() {
        assertTrue(map.keySet().isEmpty());
    }

    @Test
    public void values_isEmpty() {
        assertTrue(map.values().isEmpty());
    }

    @Test
    public void entrySet_isEmpty() {
        assertTrue(map.entrySet().isEmpty());
    }
}
