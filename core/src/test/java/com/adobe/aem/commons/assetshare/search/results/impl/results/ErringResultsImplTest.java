package com.adobe.aem.commons.assetshare.search.results.impl.results;

import com.adobe.aem.commons.assetshare.search.results.Results;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ErringResultsImplTest {

    @Test
    public void constructor_SetsEmptyResultsAndSuccessStatusAndZeroTimeTaken() {
        final ErringResultsImpl erringResults = new ErringResultsImpl();

        assertNotNull(erringResults.getResults());
        assertTrue(erringResults.getResults().isEmpty());
        // Note: Despite the class name "Erring", the status is set to SUCCESS. See Results interface's
        // Results.ERRING_RESULTS = new ErringResultsImpl() usage in SearchImpl for error scenarios.
        assertEquals(Results.Status.SUCCESS, erringResults.getStatus());
        assertEquals(0, erringResults.getTimeTaken());
    }

    @Test
    public void getSize_DefaultsToZero() {
        final ErringResultsImpl erringResults = new ErringResultsImpl();

        assertEquals(0, erringResults.getSize());
    }

    @Test
    public void getTotal_DefaultsToZero() {
        final ErringResultsImpl erringResults = new ErringResultsImpl();

        assertEquals(0, erringResults.getTotal());
    }

    @Test
    public void isMore_DefaultsToFalse() {
        final ErringResultsImpl erringResults = new ErringResultsImpl();

        assertEquals(false, erringResults.isMore());
    }

    @Test
    public void getAdditionalData_IsNonNullAndEmpty() {
        final ErringResultsImpl erringResults = new ErringResultsImpl();

        assertNotNull(erringResults.getAdditionalData());
        assertTrue(erringResults.getAdditionalData().isEmpty());
    }
}
