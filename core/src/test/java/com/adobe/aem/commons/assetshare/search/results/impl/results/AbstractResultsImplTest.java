package com.adobe.aem.commons.assetshare.search.results.impl.results;

import com.adobe.aem.commons.assetshare.search.results.Result;
import com.adobe.aem.commons.assetshare.search.results.Results;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class AbstractResultsImplTest {

    private static class ConcreteResultsImpl extends AbstractResultsImpl {
    }

    private ConcreteResultsImpl results;

    @Before
    public void setUp() {
        results = new ConcreteResultsImpl();
    }

    @Test
    public void getResults_DefaultsToNull() {
        assertNull(results.getResults());
    }

    @Test
    public void getResults_ReturnsSetResults() {
        final List<Result> resultList = new ArrayList<>();
        results.results = resultList;

        assertSame(resultList, results.getResults());
    }

    @Test
    public void getRunningTotal_Default() {
        assertEquals(0, results.getRunningTotal());
    }

    @Test
    public void getRunningTotal_SetValue() {
        results.runningTotal = 42;

        assertEquals(42, results.getRunningTotal());
    }

    @Test
    public void getSize_Default() {
        assertEquals(0, results.getSize());
    }

    @Test
    public void getSize_SetValue() {
        results.size = 5;

        assertEquals(5, results.getSize());
    }

    @Test
    public void getTotal_Default() {
        assertEquals(0, results.getTotal());
    }

    @Test
    public void getTotal_SetValue() {
        results.total = 100;

        assertEquals(100, results.getTotal());
    }

    @Test
    public void isMoreThanTotal_Default() {
        assertFalse(results.isMoreThanTotal());
    }

    @Test
    public void isMoreThanTotal_SetTrue() {
        results.moreThanTotal = true;

        assertTrue(results.isMoreThanTotal());
    }

    @Test
    public void isMore_Default() {
        assertFalse(results.isMore());
    }

    @Test
    public void isMore_SetTrue() {
        results.more = true;

        assertTrue(results.isMore());
    }

    @Test
    public void getTimeTaken_Default() {
        assertEquals(-1, results.getTimeTaken());
    }

    @Test
    public void getTimeTaken_SetValue() {
        results.timeTaken = 250;

        assertEquals(250, results.getTimeTaken());
    }

    @Test
    public void getNextOffset_SetValue() {
        results.nextOffset = 10L;

        assertEquals(10, results.getNextOffset());
    }

    @Test
    public void getStatus_Default() {
        assertNull(results.getStatus());
    }

    @Test
    public void getStatus_SetValue() {
        results.status = Results.Status.SUCCESS;

        assertEquals(Results.Status.SUCCESS, results.getStatus());
    }

    @Test
    public void getAdditionalData_DefaultsToEmptyValueMap() {
        assertNotNull(results.getAdditionalData());
        assertTrue(results.getAdditionalData().isEmpty());
    }

    @Test
    public void getAdditionalData_ReflectsMutations() {
        results.getAdditionalData().put("foo", "bar");

        assertEquals("bar", results.getAdditionalData().get("foo"));
    }

    @Test
    public void getAdditionalData_SetValue() {
        results.additionalData = new ValueMapDecorator(new HashMap<>());
        results.additionalData.put("key", "value");

        assertEquals("value", results.getAdditionalData().get("key"));
    }
}
