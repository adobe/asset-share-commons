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

package com.adobe.aem.commons.assetshare.search.results;

import com.adobe.aem.commons.assetshare.search.results.impl.ResultsImpl;
import org.osgi.annotation.versioning.ProviderType;

import java.util.Collections;
import java.util.List;

@ProviderType
public interface Results {
    Results ERRING_RESULTS = new ResultsImpl(Collections.EMPTY_LIST, Status.ERROR);

    List<Result> getResults();

    long getSize();

    long getTotal();

    boolean isMore();

    long getTimeTaken();

    long getRunningTotal();

    long getNextOffset();

    Status getStatus();

    enum Status {
        SUCCESS,
        ERROR
    }
}
