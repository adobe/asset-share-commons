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

import com.adobe.aem.commons.assetshare.search.results.impl.results.ErringResultsImpl;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.annotation.versioning.ProviderType;

import java.util.List;
import java.util.Map;

/**
 * Interface that describes a result set; This is a common interface for results from any search provider.
 */
@ProviderType
public interface Results {
    Results ERRING_RESULTS = new ErringResultsImpl();

    enum Status {
        SUCCESS,
        ERROR
    }

    /**
     * @return a list of results.
     */
    List<Result> getResults();

    /**
     * @return the number of results in this results object (equivalent of `getResults().size()`)
     */
    long getSize();

    /**
     * @return the total number of results for the search. This will be greater than or equal to getSize(). This may be a guess as well.
     */
    long getTotal();

    /**
     * @return true if there are more results. false if there are no more results.
     */
    boolean isMore();

    /**
     * @return the time taken to execute this search in milliseconds.
     */
    long getTimeTaken();

    /**
     * @return the number of results returned so far; this is computed using offsets and limits.
     */
    long getRunningTotal();

    /**
     * @return the next offset if the client would like to load more.
     */
    long getNextOffset();

    /**
     * @return the status of the search (Success or Error)
     */
    Status getStatus();

    /**
     * This method acts as a flexible extension point to expose additional data.
     *
     * Note: the returning ValueMap supports modifying operations (ie. put, putAll).
     *
     * @return a ValueMap (optionally) populated with additional data by applications implementing Asset Share Commons.
     */
    ValueMap getAdditionalData();
}
