package com.adobe.aem.commons.assetshare.search.results;

import org.apache.sling.api.resource.ValueMap;

import java.util.List;

public class ResultsWrapper implements Results {
    private final Results wrappedResult;

    public ResultsWrapper(Results wrappedResult) {
        this.wrappedResult = wrappedResult;
    }

    public Results getWrappedResult() {
        return wrappedResult;
    }

    @Override
    public List<Result> getResults() {
        return getWrappedResult().getResults();
    }

    @Override
    public long getSize() {
        return getWrappedResult().getSize();
    }

    @Override
    public long getTotal() {
        return getWrappedResult().getTotal();
    }

    @Override
    public boolean isMore() {
        return getWrappedResult().isMore();
    }

    @Override
    public long getTimeTaken() {
        return getWrappedResult().getTimeTaken();
    }

    @Override
    public long getRunningTotal() {
        return getWrappedResult().getRunningTotal();
    }

    @Override
    public long getNextOffset() {
        return getWrappedResult().getNextOffset();
    }

    @Override
    public Status getStatus() {
        return getWrappedResult().getStatus();
    }

    @Override
    public ValueMap getAdditionalData() {
        return getWrappedResult().getAdditionalData();
    }
}
