package com.adobe.aem.commons.assetshare.search.results.impl.results;

import com.adobe.aem.commons.assetshare.search.results.Results;

import java.util.Collections;

/**
 * Results that represent a generic erring state.
 */
public final class EmptyResultsImpl extends AbstractResultsImpl implements Results {
    public EmptyResultsImpl() {
        results = Collections.EMPTY_LIST;
        status = Status.ERROR;
    }
}
