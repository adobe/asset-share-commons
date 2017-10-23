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

package com.adobe.aem.commons.assetshare.search;

import com.day.cq.search.PredicateGroup;
import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.RepositoryException;
import java.util.Map;

public interface SearchSafety {
    // Checks if a Query Builder query is safe (Xpath, SQL2)
    boolean isSafe(ResourceResolver resourceResolver, PredicateGroup predicateGroup) throws RepositoryException;

    // Checks if a Query Builder query is safe (Xpath, SQL2)
    boolean isSafe(ResourceResolver resourceResolver, Map<String, String> queryBuilderParams) throws RepositoryException;

    // Checks if any native query is safe (Xpath, SQL2)
    boolean isSafe(ResourceResolver resourceResolver, String language, String statement) throws RepositoryException;
}
