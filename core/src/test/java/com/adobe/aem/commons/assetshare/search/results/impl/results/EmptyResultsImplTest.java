package com.adobe.aem.commons.assetshare.search.results.impl.results;

import com.adobe.aem.commons.assetshare.search.results.Results;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class EmptyResultsImplTest {

    @Test
    public void constructor_SetsEmptyResultsAndSuccessStatus() {
        final EmptyResultsImpl emptyResults = new EmptyResultsImpl();

        assertNotNull(emptyResults.getResults());
        assertTrue(emptyResults.getResults().isEmpty());
        assertEquals(Results.Status.SUCCESS, emptyResults.getStatus());
    }

    @Test
    public void getSize_DefaultsToZero() {
        final EmptyResultsImpl emptyResults = new EmptyResultsImpl();

        assertEquals(0, emptyResults.getSize());
    }

    @Test
    public void getTotal_DefaultsToZero() {
        final EmptyResultsImpl emptyResults = new EmptyResultsImpl();

        assertEquals(0, emptyResults.getTotal());
    }

    @Test
    public void isMore_DefaultsToFalse() {
        final EmptyResultsImpl emptyResults = new EmptyResultsImpl();

        assertEquals(false, emptyResults.isMore());
    }

    @Test
    public void getAdditionalData_IsNonNullAndEmpty() {
        final EmptyResultsImpl emptyResults = new EmptyResultsImpl();

        assertNotNull(emptyResults.getAdditionalData());
        assertTrue(emptyResults.getAdditionalData().isEmpty());
    }

    @Test(expected = NullPointerException.class)
    public void getNextOffset_ThrowsNpeBecauseNextOffsetIsNeverInitialized() {
        // AbstractResultsImpl#getNextOffset() unboxes the protected `Long nextOffset` field to a primitive
        // `long`. EmptyResultsImpl never sets `nextOffset`, so it remains null and this throws an NPE.
        new EmptyResultsImpl().getNextOffset();
    }
}
