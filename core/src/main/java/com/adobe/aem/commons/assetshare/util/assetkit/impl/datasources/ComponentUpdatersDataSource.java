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
package com.adobe.aem.commons.assetshare.util.assetkit.impl.datasources;

import com.adobe.aem.commons.assetshare.util.DataSourceBuilder;
import com.adobe.aem.commons.assetshare.util.assetkit.ComponentUpdater;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import java.util.Collection;
import java.util.TreeMap;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.resourceTypes=asset-share-commons/data-sources/asset-kit/component-updaters",
                "sling.servlet.methods=GET"
        },
        configurationPolicy = ConfigurationPolicy.REQUIRE
)
public class ComponentUpdatersDataSource extends SlingSafeMethodsServlet {
    private static final Logger log = LoggerFactory.getLogger(ComponentUpdatersDataSource.class);
    @Reference
    private transient DataSourceBuilder dataSourceBuilder;

    @Reference(
            policyOption = ReferencePolicyOption.GREEDY,
            cardinality = ReferenceCardinality.MULTIPLE
    )
    private transient Collection<ComponentUpdater> componentUpdaters;

    @Override
    protected final void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) {
        final TreeMap<String, Object> data = new TreeMap<>();

        if (componentUpdaters != null) {
            for (ComponentUpdater componentUpdater : componentUpdaters) {
                data.put(componentUpdater.getName(), componentUpdater.getId());
            }
        }

        dataSourceBuilder.build(request, data);
    }
}