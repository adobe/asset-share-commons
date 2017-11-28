package com.adobe.aem.commons.assetshare.search.results.impl.results;

import com.adobe.aem.commons.assetshare.search.results.Results;

import java.util.Collections;

/**
 * Results that represent a generic empty (but successful) state.
 */
public final class ErringResultsImpl extends AbstractResultsImpl implements Results {
    public ErringResultsImpl() {
        results = Collections.EMPTY_LIST;
        status = Status.SUCCESS;
        timeTaken = 0;
    }
}
