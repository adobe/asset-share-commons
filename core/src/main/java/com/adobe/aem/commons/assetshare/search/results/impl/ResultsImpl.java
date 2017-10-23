/*
 * Asset Share Commons
 *
 * Copyright (C) 2017 Adobe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.adobe.aem.commons.assetshare.search.results.impl;

import com.adobe.aem.commons.assetshare.search.results.Result;
import com.adobe.aem.commons.assetshare.search.results.Results;

import java.util.Collections;
import java.util.List;

public class ResultsImpl implements Results {

    private final List<Result> results;
    private final boolean more;
    private final long total;
    private final long size;
    private final Long nextOffset;
    private final Status status;
    private long timeTaken = -1;
    private long runningTotal;

    public ResultsImpl(List<Result> results, Status status) {
        this(results, 0, results.size(), results.size(), -1, false, status);
    }

    public ResultsImpl(List<Result> results, long timeTaken) {
        this(results, timeTaken, results.size(), results.size(), -1, false, Status.SUCCESS);
    }

    public ResultsImpl(List<Result> results, long timeTaken, long runningTotal, long total, long nextOffset, boolean more) {
        this(results, timeTaken, runningTotal, total, nextOffset, more, Status.SUCCESS);

    }

    public ResultsImpl(List<Result> results, long timeTaken, long runningTotal, long total, long nextOffset, boolean more, Status status) {
        this.results = Collections.unmodifiableList(results);
        this.more = more;
        this.size = this.results.size();
        this.total = total;
        this.timeTaken = timeTaken;
        this.nextOffset = nextOffset;
        this.runningTotal = runningTotal;
        this.status = status;
    }


    public List<? extends Result> getResults() {
        return results;
    }

    public long getRunningTotal() {
        return runningTotal;
    }

    public long getSize() {
        return size;
    }

    public long getTotal() {
        return total;
    }

    public boolean isMore() {
        return more;
    }

    public long getTimeTaken() {
        return timeTaken;
    }

    public long getNextOffset() {
        return nextOffset;
    }

    @Override
    public Status getStatus() {
        return status;
    }
}
