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

package com.adobe.aem.commons.assetshare.search.results.impl.results;

import com.adobe.aem.commons.assetshare.search.results.Result;
import com.adobe.aem.commons.assetshare.search.results.Results;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;

import java.util.*;

public abstract class AbstractResultsImpl implements Results {
    protected List<Result> results;
    protected boolean moreThanTotal = false;
    protected boolean more = false;
    protected long total = 0;
    protected long size = 0;
    protected Long nextOffset;
    protected Status status;
    protected long timeTaken = -1;
    protected long runningTotal;
    protected ValueMap additionalData = new ValueMapDecorator(new HashMap<>());

    @Override
    public List<Result> getResults() {
        return results;
    }

    @Override
    public long getRunningTotal() {
        return runningTotal;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public long getTotal() {
        return total;
    }

    @Override
    public boolean isMoreThanTotal() {
        return moreThanTotal;
    }

    @Override
    public boolean isMore() {
        return more;
    }

    @Override
    public long getTimeTaken() {
        return timeTaken;
    }

    @Override
    public long getNextOffset() {
        return nextOffset;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public ValueMap getAdditionalData() {
        return additionalData;
    }
}
