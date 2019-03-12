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

package com.adobe.aem.commons.assetshare.configuration.impl.datasources;

import com.adobe.aem.commons.assetshare.util.DataSourceBuilder;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.resourceTypes=asset-share-commons/data-sources/result-resource-types",
                "sling.servlet.methods=GET"
        },
        configurationPolicy = ConfigurationPolicy.REQUIRE
)
public class SearchResultsResourceTypeDataSource extends SlingSafeMethodsServlet {
    private static final Logger log = LoggerFactory.getLogger(SearchResultsResourceTypeDataSource.class);

    @Reference
    private QueryBuilder queryBuilder;

    @Reference
    private DataSourceBuilder dataSourceBuilder;

    @Override
    protected final void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response) {

        final ValueMap properties = request.getResource().getValueMap();
        final String[] extensionTypes = properties.get("extensionTypes", new String[]{});
        final String[] resourceTypes = properties.get("resourceTypes", new String[]{});
        final String[] resourceSuperTypes = properties.get("resourceSuperTypes", new String[]{});

        final Map<String, String> params = new HashMap<>();
        params.put("type", "cq:Component");
        params.put("path", "/apps");

        params.put("group.p.or", "true");

        int index = 1;

        if (extensionTypes.length > 0) {
            params.put("group.2_property", "extensionType");

            for (final String extensionType : extensionTypes) {
                params.put("group.2_property." + index++ + "_value", extensionType);
            }
        }

        if (resourceTypes.length > 0) {
            params.put("group.2_property", "sling:resourceType");

            for (final String resourceType : resourceTypes) {
                params.put("group.2_property." + index++ + "_value", resourceType);
            }
        }

        if (resourceSuperTypes.length > 0) {
            params.put("group.3_property", "sling:resourceSuperType");

            for (final String resourceSuperType : resourceSuperTypes) {
                params.put("group.3_property." + index++ + "_value", resourceSuperType);
            }
        }

        final Query query = queryBuilder.createQuery(PredicateGroup.create(params), request.getResourceResolver().adaptTo(Session.class));
        final Map<String, Object> data = new LinkedHashMap<>();

        for (final Hit hit : query.getResult().getHits()) {
            try {
                data.put(hit.getProperties().get("jcr:title", hit.getTitle()), hit.getPath());
            } catch (RepositoryException e) {
                log.error("Could not collect Search Results ResourceType for data source.");
            }
        }

        dataSourceBuilder.build(request, data);
    }
}