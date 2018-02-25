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

package com.adobe.aem.commons.assetshare.search.impl;

import com.adobe.aem.commons.assetshare.search.SearchSafety;
import com.day.cq.search.PredicateGroup;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;

import javax.jcr.RepositoryException;
import java.util.Map;

@Component
public class SearchSafetyImpl implements SearchSafety {

    @Override
    public boolean isSafe(ResourceResolver resourceResolver, Map<String, String> queryBuilderParams) throws RepositoryException {
        return isSafe(resourceResolver, PredicateGroup.create(queryBuilderParams));
    }

    @Override
    public boolean isSafe(ResourceResolver resourceResolver, PredicateGroup predicateGroup) throws RepositoryException {
        /**
         * Unfortunately because Oak explain cannot handle UNIONs in AEM 6.3 SP1, this time (fix in AEM 6.4) we cannot consistently check for safe queries, so for now we will not check at all.
         * In the future this hook will be implemented to help ensure only safe queries are allow.
         */

         return true;
    }

    @Override
    public boolean isSafe(ResourceResolver resourceResolver, String language, String statement) throws RepositoryException {
        /**
         * Unfortunately because Oak explain cannot handle UNIONs in AEM 6.3 SP1, (fix in AEM 6.4) we cannot consistently check for safe queries, so for now we will not check at all.
         * In the future this hook will be implemented to help ensure only safe queries are allow.
         */
        return true;
    }
}