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

package com.adobe.aem.commons.assetshare.components.predicates;

import com.day.cq.search.PredicateGroup;
import org.osgi.annotation.versioning.ProviderType;

import java.util.List;
import java.util.Map;

/**
 * Represents the overarching QueryBuilder predicate rules that apply general to the search.
 *
 * This is called PagePredicate as these values can come from multiple components across the Search page, however
 * most are sourced from the Search Results component.
 */
@ProviderType
public interface PagePredicate extends Predicate {
    /**
     * The QB parameter types that getPredicateGroup(..) can build (or ask it to not build).
     */
    enum ParamTypes {
        NODE_TYPE,
        PATH,
        GUESS_TOTAL,
        OFFSET,
        LIMIT,
        HIDDEN_PREDICATES,
        SEARCH_PREDICATES,
        ORDERBY;
    }

    /**
     * @return the property which to order by.
     */
    String getOrderBy();

    /**
     * @return the sort order; asc or desc.
     */
    String getOrderBySort();

    /**
     * @return the limit.
     */
    int getLimit();

    /**
     * @return the configured guessTotal value.
     */
    String getGuessTotal();

    /**
     * @return a list of absolute JCR paths that a query should be restricted to.
     */
    List<String> getPaths();

    /**
     * Creates a predicate group that represents the top-level query configuration that fallback on or are based on the page.
     * This method builds out a fixed set of QB predicates.

     * @return the predicate group that contains the query predicates.
     */
    PredicateGroup getPredicateGroup();

    /**
     * Creates a predicate group that represents the top-level query configuration that fallback on or are based on the page.
     * This method builds out a fixed set of QB predicates.
     *
     * @param excludeParamTypes the query parameter types to exclude when building this.
     * @return the predicate group that contains the query predicates.
     */
    PredicateGroup getPredicateGroup(ParamTypes... excludeParamTypes);


    /**
     * Deprecated. Use getPredicateGroup() instead.
     *
     * @return a map of representation of the QueryBuilder predicate params.
     */
    @Deprecated
    Map<String, String> getParams();

    /**
     * Deprecated. Use getPredicateGroup(ParamTypes... excludeParamTypes) instead.
     *
     * @param excludeParamTypes the query parameter types to exclude when building this.
     * @return the predicate group that contains the query predicates.
     */
    @Deprecated
    Map<String, String> getParams(ParamTypes... excludeParamTypes);

}
