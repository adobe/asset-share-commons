package com.adobe.aem.commons.assetshare.search.results.impl.results;

import com.adobe.aem.commons.assetshare.search.results.Results;

import java.util.Collections;

/**
 * Results that represent a generic erring state.
 */
public final class ErringResultsImpl extends AbstractResultsImpl implements Results {
    public ErringResultsImpl() {
        results = Collections.EMPTY_LIST;
        status = Status.ERROR;
        timeTaken = 0;
    }
}
