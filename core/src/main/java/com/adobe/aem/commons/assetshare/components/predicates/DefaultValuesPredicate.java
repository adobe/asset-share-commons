/*
 * Asset Share Commons
 *
 * Copyright (C) 2023 Adobe
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
import org.osgi.annotation.versioning.ConsumerType;

/**
 * This interface is used to make Search Predicate components that can have default values (options marked as "Active"). 
 * Examples of this include the ASC PropertyPredicate and PathPredicate components.
 * Any predicate components that can have default values should both implement this interface, and list it as Sling Model adapters.
 * getPredicateGroup() should return a QueryBuilder PredicateGroup that is populated with query parameters that reflect the default values of the component.
 * See ProperyPredicateImpl.java and PathPredicateImpl.java for examples.
 */
@ConsumerType
public interface DefaultValuesPredicate {

    /**
     * @return a PredicateGroup that represents the default/active query configuration for the component.
     */
    PredicateGroup getPredicateGroup();
}
