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

package com.adobe.aem.commons.assetshare.search.providers.impl;

import com.adobe.aem.commons.assetshare.components.predicates.PagePredicate;
import com.adobe.aem.commons.assetshare.search.QueryParameterPostProcessor;
import com.adobe.aem.commons.assetshare.search.SearchSafety;
import com.adobe.aem.commons.assetshare.search.UnsafeSearchException;
import com.adobe.aem.commons.assetshare.search.providers.QuerySearchPostProcessor;
import com.adobe.aem.commons.assetshare.search.providers.QuerySearchPreProcessor;
import com.adobe.aem.commons.assetshare.search.providers.SearchProvider;
import com.adobe.aem.commons.assetshare.search.results.AssetResult;
import com.adobe.aem.commons.assetshare.search.results.Result;
import com.adobe.aem.commons.assetshare.search.results.Results;
import com.adobe.aem.commons.assetshare.search.results.impl.results.QueryBuilderResultsImpl;
import com.day.cq.search.Predicate;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.factory.ModelFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.osgi.framework.Constants.SERVICE_RANKING;

@Component(property = {
        SERVICE_RANKING + ":Integer=" + Integer.MIN_VALUE
})
public class QuerySearchProviderImpl implements SearchProvider {
    private static final Logger log = LoggerFactory.getLogger(QuerySearchProviderImpl.class);

    @Reference
    private SearchSafety searchSafety;

    @Reference
    private QueryBuilder queryBuilder;

    @Reference
    private ModelFactory modelFactory;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    private QuerySearchPreProcessor querySearchPreProcessor;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    private QuerySearchPostProcessor querySearchPostProcessor;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    private QueryParameterPostProcessor queryParametersPostProcessor;

    public boolean accepts(SlingHttpServletRequest request) {
        // This is the default with the lowest service ranking
        return true;
    }

    public Results getResults(final SlingHttpServletRequest request) throws UnsafeSearchException, RepositoryException {
        final ResourceResolver resourceResolver = request.getResourceResolver();
        final PredicateGroup predicates;

        if (querySearchPreProcessor != null) {
            predicates = querySearchPreProcessor.process(request, getParams(request));
        } else {
            predicates = PredicateGroup.create(getParams(request));
        }

        if (!searchSafety.isSafe(request.getResourceResolver(), predicates)) {
            throw new UnsafeSearchException("Search query will initiate an traversing query");
        }

        debugPreQuery(predicates.getParameters());

        final Query query = queryBuilder.createQuery(predicates, resourceResolver.adaptTo(Session.class));
        final SearchResult searchResult = query.getResult();

        debugPostQuery(searchResult);

        final List<Result> results = new ArrayList<>();
        for (final Hit hit : searchResult.getHits()) {
            try {
                final AssetResult assetSearchResult = modelFactory.getModelFromWrappedRequest(request,
                        resourceResolver.getResource(hit.getPath()), AssetResult.class);
                if (assetSearchResult != null) {
                    results.add(assetSearchResult);
                }
            } catch (RepositoryException e) {
                log.error("Could not retrieve search result", e);
            }
        }

        debugPostAdaptation(results);

        final QueryBuilderResultsImpl resultsImpl = new QueryBuilderResultsImpl(results, searchResult);

        if (querySearchPostProcessor != null) {
            return querySearchPostProcessor.process(request, query, resultsImpl, searchResult);
        } else {
            return resultsImpl;
        }
    }

    /**
     * Generates the QueryBuilder query params from the Page Predicate settings and the request attributes.
     *
     * @param request
     * @return the QueryBuilder parameter map.
     */
    private Map<String, String> getParams(final SlingHttpServletRequest request) {
        Map<String, String> params = new HashMap<>();

        for (final Map.Entry<String, RequestParameter[]> entry : request.getRequestParameterMap().entrySet()) {
            params.put(entry.getKey(), entry.getValue()[0].getString());
        }

        final PagePredicate pagePredicate = request.adaptTo(PagePredicate.class);
        params.putAll(pagePredicate.getParams());

        // If not provided, use the defaults set on the Search Component resource
        if (params.get(Predicate.ORDER_BY) == null) {
            params.put(Predicate.ORDER_BY, pagePredicate.getOrderBy());
        }
        if (params.get(Predicate.ORDER_BY + "." + Predicate.PARAM_SORT) == null) {
            params.put(Predicate.ORDER_BY + "." + Predicate.PARAM_SORT, pagePredicate.getOrderBySort());
        }

        cleanParams(params);

        if (queryParametersPostProcessor != null) {
            params = queryParametersPostProcessor.process(request, params);
        }

        return params;
    }

    private void cleanParams(Map<String, String> params) {
        params.remove("mode");
        params.remove("layout");
    }

    private void debugPreQuery(Map <String, String> params) {
        if (log.isDebugEnabled()) {

            final Map<String, String> sortedParams = new TreeMap<>();
            sortedParams.putAll(params);

            final StringBuilder sb = new StringBuilder();
            for(final Map.Entry<String, String> parameter : sortedParams.entrySet()) {
                sb.append("\n" + parameter.getKey() + " = " + parameter.getValue());
            }

            log.debug("Query Builder Parameters: {}", sb.toString());
        }
    }

    private void debugPostQuery(SearchResult searchResult) {
        if (log.isDebugEnabled()) {
            log.debug("Executed query statement:\n{}", searchResult.getQueryStatement());
            log.debug("Search results - Hits size [ {} ]", searchResult.getHits().size());
            log.debug("Search results - Page count [ {} ]", searchResult.getResultPages().size());
            log.debug("Search results - Page start index [ {} ]", searchResult.getStartIndex());
            log.debug("Search results - Running total [ {} ]", searchResult.getStartIndex() + searchResult.getHits().size());
            log.debug("Search results - Has more results [ {} ]", searchResult.hasMore());
            log.debug("Search results - Total matches [ {} ]", searchResult.getTotalMatches());
            log.debug("Search results - Execution time in ms [ {} ]", searchResult.getExecutionTimeMillis());
        }
    }

    private void debugPostAdaptation(List<Result> results) {
        if (log.isDebugEnabled()) {
            log.debug("Adapted [ {} ] results to Result models", results.size());
        }
    }
}