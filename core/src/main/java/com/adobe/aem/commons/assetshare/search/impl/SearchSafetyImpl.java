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
import com.day.cq.search.QueryBuilder;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.*;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class SearchSafetyImpl implements SearchSafety {
    private static Logger log = LoggerFactory.getLogger(SearchSafetyImpl.class);

    //private static Pattern XPATH_UNION_PATTERN = Pattern.compile("\\((/.+)(\\s|\\s)(/.+)\\).+");

    @Override
    public boolean isSafe(ResourceResolver resourceResolver, Map<String, String> queryBuilderParams) throws RepositoryException {
        return isSafe(resourceResolver, PredicateGroup.create(queryBuilderParams));
    }

    @Override
    public boolean isSafe(ResourceResolver resourceResolver, PredicateGroup predicateGroup) throws RepositoryException {
        final QueryBuilder queryBuilder = resourceResolver.adaptTo(QueryBuilder.class);
        final com.day.cq.search.Query query = queryBuilder.createQuery(predicateGroup, resourceResolver.adaptTo(Session.class));

        return isSafe(resourceResolver, Query.XPATH, query.getResult().getQueryStatement());
    }

    @Override
    public boolean isSafe(ResourceResolver resourceResolver, String language, String statement) throws RepositoryException {
        /**
         * Unfortunately because Oak explain cannot handle UNIONs at this time (fix is pending release) we cannot consistently check for safe queries, so for now we will not check at all.
         * In the future this hook will be implemented to help ensure only safe queries are allow.
         */
        return true;

        /*
        boolean traversal = false;

        final long start = System.currentTimeMillis();
        if (Query.XPATH.equalsIgnoreCase(language) && XPATH_UNION_PATTERN.matcher(statement).matches()) {
            log.info("Unable to EXPLAIN the xpath query [ {} ], due to UNIONing xpath query which EXPLAIN does not support. Accepting the risk and allowing the query to proceed.", statement);

            return true;
        } else {

            final QueryManager queryManager = resourceResolver.adaptTo(Session.class).getWorkspace().getQueryManager();

            final Query query = queryManager.createQuery("explain " + statement, language);
            final QueryResult queryResult = query.execute();

            final RowIterator rows = queryResult.getRows();
            final Row firstRow = rows.nextRow();

            if (ArrayUtils.contains(queryResult.getColumnNames(), "plan")) {
                final String plan = firstRow.getValue("plan").getString();
                traversal = StringUtils.contains(plan, " /* traverse ");
            } else {
                log.info("Unable to EXPLAIN the query [ {} ], likely due to UNIONing xpath query which EXPLAIN does not support. Accepting the risk and allowing the query to proceed.", statement);
            }

            if (log.isDebugEnabled()) {
                log.debug("Explaining query [ {} ] took {} ms", statement, System.currentTimeMillis() - start);
            }
        }
        return !traversal;
        */
    }
}