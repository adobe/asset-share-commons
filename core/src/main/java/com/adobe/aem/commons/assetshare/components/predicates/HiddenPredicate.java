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

import java.util.Map;

@ProviderType
public interface HiddenPredicate extends Predicate {
    /**
     * @return a PredicateGroup that represents the HiddenPredicate configuration.
     */
    PredicateGroup getPredicateGroup();

    /**
     * Deprecated - use PredicateGroup getPredicateGroup() instead.
     *
     * @param groupId the groupId to namespace these QueryBuilder parameters to.
     * @return a map of query builder parameters for this predicate, namespaced by the groupId.
     */
    @Deprecated
    Map<String, String> getParams(int groupId);

}

