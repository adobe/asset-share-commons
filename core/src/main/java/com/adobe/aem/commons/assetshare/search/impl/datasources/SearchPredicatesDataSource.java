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

package com.adobe.aem.commons.assetshare.search.impl.datasources;

import com.adobe.aem.commons.assetshare.search.searchpredicates.SearchPredicate;
import com.adobe.aem.commons.assetshare.util.DataSourceBuilder;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import javax.servlet.Servlet;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.resourceTypes=asset-share-commons/data-sources/search-predicates",
                "sling.servlet.methods=GET"
        },
        configurationPolicy = ConfigurationPolicy.REQUIRE
)
public class SearchPredicatesDataSource extends SlingSafeMethodsServlet {

    @Reference
    private DataSourceBuilder dataSourceBuilder;

    @Reference(cardinality = ReferenceCardinality.MULTIPLE)
    private transient Collection<SearchPredicate> searchPredicates;

    @Override
    protected final void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response) {
        final Map<String, Object> data = new TreeMap<>();

        searchPredicates.stream().forEach(gp -> {
            data.put(gp.getLabel(), gp.getName());
        });

        dataSourceBuilder.build(request, data);
    }
}