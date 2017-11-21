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

import com.adobe.aem.commons.assetshare.search.providers.FacetPostProcessor;
import com.adobe.aem.commons.assetshare.search.results.Result;
import com.adobe.aem.commons.assetshare.search.results.Results;
import com.day.cq.search.facets.Facet;
import com.day.cq.search.result.SearchResult;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ValueMap;

import javax.jcr.RepositoryException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class QueryBuilderResultsImpl extends AbstractResultsImpl implements Results {
    private final SlingHttpServletRequest request;
    private final List<Result> results;
    private final SearchResult searchResult;
    private final FacetPostProcessor facetPostProcessor;

    private Map<String, Facet> facets = null;


    public QueryBuilderResultsImpl(SlingHttpServletRequest request,
                                      List<Result> results,
                                      SearchResult searchResult,
                                      FacetPostProcessor facetPostProcessor)  {

        this.request = request;
        this.facetPostProcessor = facetPostProcessor;
        this.results = Collections.unmodifiableList(results);
        this.searchResult = searchResult;

        this.more = searchResult.hasMore() || runningTotal < searchResult.getTotalMatches();
        this.size = this.results.size();
        this.total = searchResult.getTotalMatches();
        this.timeTaken = searchResult.getExecutionTimeMillis();
        this.nextOffset = searchResult.getNextPage() != null ? searchResult.getNextPage().getStart() : -1;
        this.runningTotal = searchResult.getStartIndex() + searchResult.getHits().size();
        this.status = Status.SUCCESS;
    }

    final SearchResult getSearchResult() {
        return this.searchResult;
    }

    final Map<String, Facet> getFacets() throws RepositoryException {
        if (facets == null) {
            Map<String, Facet> tmp = Collections.EMPTY_MAP;

            if (getSearchResult() != null) {
                tmp = getSearchResult().getFacets();
            }

            if (facetPostProcessor != null) {
                facets = facetPostProcessor.process(request, searchResult, tmp);
            }
        }

        return facets;
    }

    @Override
    public ValueMap getAdditionalData() {
        return null;
    }
}
