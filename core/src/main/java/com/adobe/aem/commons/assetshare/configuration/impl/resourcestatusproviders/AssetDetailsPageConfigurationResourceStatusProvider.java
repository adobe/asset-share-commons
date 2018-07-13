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

import com.adobe.aem.commons.assetshare.util.ForcedInheritanceValueMapWrapper;
import com.adobe.granite.resourcestatus.ResourceStatus;
import com.adobe.granite.resourcestatus.ResourceStatusProvider;
import com.day.cq.commons.inherit.HierarchyNodeInheritanceValueMap;
import com.day.cq.dam.commons.util.DamUtil;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.commons.status.EditorResourceStatus;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
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

import static com.adobe.aem.commons.assetshare.configuration.impl.ConfigImpl.DEFAULT_PLACEHOLDER_ASSET_PATH;
import static com.adobe.aem.commons.assetshare.configuration.impl.ConfigImpl.PN_PLACEHOLDER_ASSET_PATH;

@Component(service = {ResourceStatusProvider.class})
@Designate(ocd = AssetDetailsPageConfigurationResourceStatusProvider.Cfg.class)
public class AssetDetailsPageConfigurationResourceStatusProvider implements ResourceStatusProvider {
    private static final Logger log = LoggerFactory.getLogger(AssetDetailsPageConfigurationResourceStatusProvider.class);

    private static final String STATUS_PROVIDER_TYPE = "asset-share-commons__asset-details-page-configuration";

    private static final String KEY_SHORT_MESSAGE = "shortMessage";

    private static final String VALUE_ICON = "alert";

    private static final String DEFAULT_PAGE_RESOURCE_TYPE = "asset-share-commons/components/structure/details-page";

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
                "Missing placeholder asset",
                "Configure a valid placehold asset on the  Search Page's page properties.");

        builder.setVariant(EditorResourceStatus.Variant.ERROR);
        builder.setIcon(VALUE_ICON);
        // error -> 300000
        builder.setPriority(300000);
        builder.addData(KEY_SHORT_MESSAGE, "Missing Search Results component");

        resourceStatuses.add(builder.build());

        return resourceStatuses;
    }

    /**
     * A method to check if the asset details page has a satisfactory
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

        return !hasPlaceholderAsset(resource);
    }

    private boolean hasPlaceholderAsset(Resource resource) {
        final PageManager pageManager = resource.getResourceResolver().adaptTo(PageManager.class);
        final Page currentPage = pageManager.getContainingPage(resource);
        final ValueMap properties = new ForcedInheritanceValueMapWrapper(new HierarchyNodeInheritanceValueMap(currentPage.getContentResource()));

        final String path = properties.get(PN_PLACEHOLDER_ASSET_PATH, DEFAULT_PLACEHOLDER_ASSET_PATH);
        final Resource placeholderResource = resource.getResourceResolver().getResource(path);

        if (placeholderResource != null) {
            return DamUtil.resolveToAsset(placeholderResource) != null;
        }

        return false;
    }

    @Activate
    protected void activate(Cfg cfg) {
        this.cfg = cfg;
    }

    @ObjectClassDefinition(name = "Asset Share Commons - Asset Details Page Configuration Status")
    public @interface Cfg {
        @AttributeDefinition(
                name = "Asset Details Page sling:resourceTypes",
                description = "A list of sling:resourceTypes that identify Asset Details pages."
        )
        String[] pageResourceTypes() default {DEFAULT_PAGE_RESOURCE_TYPE};
    }
}
