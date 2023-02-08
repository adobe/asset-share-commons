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

package com.adobe.aem.commons.assetshare.util.impl;

import com.adobe.aem.commons.assetshare.util.DataSourceBuilder;
import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.EmptyDataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import com.adobe.granite.ui.components.ds.ValueMapResource;
import com.day.cq.commons.jcr.JcrConstants;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.osgi.service.component.annotations.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public final class DataSourceBuilderImpl implements DataSourceBuilder {

    /**
     * Builds the data source from the data param, and adds to the request.
     *
     * @param request the request.
     * @param data    the data to create the datasource from
     */
    public final void build(final SlingHttpServletRequest request, final Map<String, Object> data) {
        request.setAttribute(DataSource.class.getName(), EmptyDataSource.instance());

        final List<Resource> fakeResourceList = new ArrayList<>();

        handleNoneOption(request, fakeResourceList);

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            final ValueMap vm = new ValueMapDecorator(new HashMap<>());

            vm.put(TEXT, entry.getKey());
            vm.put(VALUE, entry.getValue());

            fakeResourceList.add(new ValueMapResource(request.getResourceResolver(), new ResourceMetadata(), JcrConstants.NT_UNSTRUCTURED, vm));
        }

        final DataSource ds = new SimpleDataSource(fakeResourceList.iterator());
        request.setAttribute(DataSource.class.getName(), ds);
    }

    /**
     * Handle adding "None" option to the data source results based on the datasource configuration.
     *
     * @param request          the request
     * @param fakeResourceList the dats source resource results list.
     */
    private void handleNoneOption(SlingHttpServletRequest request, List<Resource> fakeResourceList) {
        final ValueMap properties = request.getResource().getValueMap();

        final String noneLabel = properties.get(PN_NONE_TEXT, String.class);
        final String noneValue = properties.get(PN_NONE_VALUE, "");

        if (noneLabel != null) {
            final ValueMap vm = new ValueMapDecorator(new HashMap<>());

            vm.put(TEXT, noneLabel);
            vm.put(VALUE, noneValue);

            fakeResourceList.add(new ValueMapResource(request.getResourceResolver(), new ResourceMetadata(), JcrConstants.NT_UNSTRUCTURED, vm));
        }
    }
}
