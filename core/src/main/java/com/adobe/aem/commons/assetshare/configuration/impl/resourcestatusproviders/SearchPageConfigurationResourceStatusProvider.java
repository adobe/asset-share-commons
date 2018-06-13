/*
 * Asset Share Commons
 *
 * Copyright (C) 2018 Adobe
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

package com.adobe.aem.commons.assetshare.configuration.impl.resourcestatusproviders;

import com.adobe.aem.commons.assetshare.util.ResourceTypeVisitor;
import com.adobe.granite.resourcestatus.ResourceStatus;
import com.adobe.granite.resourcestatus.ResourceStatusProvider;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.commons.status.EditorResourceStatus;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Component(service = {ResourceStatusProvider.class})
@Designate(ocd = SearchPageConfigurationResourceStatusProvider.Cfg.class)
public class SearchPageConfigurationResourceStatusProvider implements ResourceStatusProvider {
    private static final Logger log = LoggerFactory.getLogger(SearchPageConfigurationResourceStatusProvider.class);

    private static final String STATUS_PROVIDER_TYPE = "asset-share-commons__search-page-configuration";

    private static final String KEY_SHORT_MESSAGE = "shortMessage";

    private static final String VALUE_ICON = "beaker";
    private static final int VALUE_PRIORITY = 200000;

    private static final String DEFAULT_PAGE_RESOURCE_TYPE = "asset-share-commons/components/structure/search-page";
    private static final String DEFAULT_COMPONENT_RESOURCE_TYPE = "asset-share-commons/components/search/results";

    private Cfg cfg;

    public String getType() {
        return STATUS_PROVIDER_TYPE;
    }

    public List<ResourceStatus> getStatuses(final Resource resource) {

        if (!accepts(resource)) {
            return Collections.EMPTY_LIST;
        }

        final List<ResourceStatus> resourceStatuses = new LinkedList<ResourceStatus>();

        EditorResourceStatus.Builder builder = new EditorResourceStatus.Builder(
                getType(),
                "Missing Search Results component",
                "Add a 'Search Results' component to this page.");

        builder.setVariant(EditorResourceStatus.Variant.WARNING);
        builder.setIcon(VALUE_ICON);
        // warning -> 200000
        builder.setPriority(VALUE_PRIORITY);
        builder.addData(KEY_SHORT_MESSAGE, "Missing Search Results component");

        resourceStatuses.add(builder.build());

        return resourceStatuses;
    }

    /**
     * A method to check if the resource has a status to report by this provider.
     *
     * @param resource a resource that is part of the page that is to be checked.
     * @return true if this resource's page is a candidate for the status.
     */
    private boolean accepts(Resource resource) {
        final PageManager pageManager = resource.getResourceResolver().adaptTo(PageManager.class);
        final Page page = pageManager.getContainingPage(resource);

        if (page == null ||
                Arrays.stream(cfg.pageResourceTypes()).noneMatch(resourceType -> page.getContentResource().isResourceType(resourceType))) {
            // Must be a Page, under /content that is of a sling:resourceType in cfg.getResourceTypes()
            return false;
        }

        final ResourceTypeVisitor visitor = new ResourceTypeVisitor(cfg.componentResourceTypes());
        visitor.accept(page.getContentResource());

        return visitor.getResources().size() == 0;
    }


    @Activate
    protected void activate(Cfg cfg) {
        this.cfg = cfg;
    }

    @ObjectClassDefinition(name = "Asset Share Commons - Search Page Configuration Status")
    public @interface Cfg {
        @AttributeDefinition(
                name = "Search Page sling:resourceTypes",
                description = "A list of sling:resourceTypes that identify the Search Pages."
        )
        String[] pageResourceTypes() default {DEFAULT_PAGE_RESOURCE_TYPE};

        @AttributeDefinition(
                name = "Search Results component sling:resourceTypes",
                description = "A list of sling:resourceTypes that identify the Search Results components."
        )
        String[] componentResourceTypes() default {DEFAULT_COMPONENT_RESOURCE_TYPE};
    }
}
