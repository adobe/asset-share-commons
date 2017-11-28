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

import java.util.Collections;
import java.util.List;

public class FolderResultsImpl extends AbstractResultsImpl implements Results {

    public FolderResultsImpl(List<Result> results, Long timeTaken) {
        super();

        this.results = Collections.unmodifiableList(results);
        this.more = false;
        this.size = this.results.size();
        this.total = this.results.size();
        this.timeTaken = timeTaken;
        this.nextOffset = -1l;
        this.runningTotal = results.size();
        this.status = Status.SUCCESS;
    }
}
