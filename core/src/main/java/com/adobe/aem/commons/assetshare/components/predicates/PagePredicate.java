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

@ProviderType
public interface PagePredicate extends Predicate {
    enum ParamTypes {
        NODE_TYPE,
        PATH,
        GUESS_TOTAL,
        LIMIT,
        HIDDEN_PREDICATES,
        SEARCH_PREDICATES;
    }

    String getOrderBy();

    String getOrderBySort();

    int getLimit();

    String getGuessTotal();

    List<String> getPaths();

    PredicateGroup getPredicateGroup();

    PredicateGroup getPredicateGroup(ParamTypes... excludeParamTypes);

    /**
     * Deprecated. Use getPredicateGroup() instead.
     */
    @Deprecated
    Map<String, String> getParams();

    /**
     * Deprecated. Use getPredicateGroup(ParamTypes... excludeParamTypes) instead.
     */
    @Deprecated
    Map<String, String> getParams(ParamTypes... excludeParamTypes);

}
