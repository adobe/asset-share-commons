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

package com.adobe.aem.commons.assetshare.components.structure.impl;

import com.adobe.aem.commons.assetshare.components.structure.Header;
import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ExporterConstants;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.designer.Style;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ScriptVariable;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {Header.class, ComponentExporter.class},
        resourceType = {HeaderImpl.RESOURCE_TYPE}
)
@Exporter(name = ExporterConstants.SLING_MODEL_EXPORTER_NAME, extensions = ExporterConstants.SLING_MODEL_EXTENSION)
public class HeaderImpl implements Header {
    protected static final String RESOURCE_TYPE = "asset-share-commons/components/structure/header";

    private static final String DEFAULT_REL_PATH = "root/main/header";

    @ScriptVariable
    private Style currentStyle;

    @ScriptVariable
    private Page currentPage;

    @ScriptVariable
    private PageManager pageManager;

    private String relativeHeaderPath;
    private Resource headerResource;
    private ValueMap headerProperties;
    private List<Header.NavigationItem> items;
    private String rootPath;
    private String logoPath;
    private String siteTitle;

    @PostConstruct
    private void initModel() {
        relativeHeaderPath = currentStyle.get(PN_POLICY_REL_PATH, DEFAULT_REL_PATH);
        setHeaderResource();
    }

    /**
     * Look beneath current resource for Header resource
     * if not defined, iterate up content tree to find header resource
     */
    private void setHeaderResource() {
        //target page where navigation is defined
        Page targetPage = currentPage;
        headerResource = targetPage.getContentResource(relativeHeaderPath);

        //iterate until we find a header resource that is not null or finish traversing the hierarchy
        while (isEmptyHeader(headerResource) && targetPage != null) {
            targetPage = targetPage.getParent();
            if (targetPage != null) {
                headerResource = targetPage.getContentResource(relativeHeaderPath);
            }
        }

        if (headerResource != null) {
            headerProperties = headerResource.getValueMap();
        }
    }

    /**
     * Return true if the header resource is null or not populated
     *
     * @param headerResource the header content resource.
     * @return true if header is empty
     */
    private boolean isEmptyHeader(Resource headerResource) {
        if (headerResource != null) {

            final String rootPathValue = headerResource.getValueMap().get(PN_ROOT_PATH, String.class);

            if (headerResource.hasChildren() || StringUtils.isNotBlank(rootPathValue)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Collection<NavigationItem> getItems() {
        if (items == null) {
            items = createNavigationItems();
        }
        return new ArrayList<>(items);
    }

    private List<Header.NavigationItem> createNavigationItems() {
        List<Header.NavigationItem> navigationItems = new ArrayList<>();

        if (headerResource != null) {
            //get multi-value properties beneath header resource
            Resource pagesRes = headerResource.getChild(PAGES_NODE);
            if (pagesRes != null) {
                Iterator<Resource> childResources = pagesRes.listChildren();
                while (childResources.hasNext()) {
                    NavigationItem navItem = createNavItem(childResources.next());
                    if (navItem != null) {
                        navigationItems.add(navItem);
                    }
                }
            }
        }

        return navigationItems;
    }


    /***
     * evaluates resource to determine if the link is an external url or a relative page
     * @param resource
     * @return NavigationItem
     */
    private NavigationItem createNavItem(Resource resource) {

        ValueMap linkProperties = resource.getValueMap();
        String url = linkProperties.get(PN_PATH, String.class);
        String text = linkProperties.get(PN_TEXT, String.class);
        boolean isActive = false;
        boolean isHierarchyActive = false;
        Page navPage = null;

        if (StringUtils.isNotBlank(url)) {

            //indicates that the path is a relative path
            if (url.startsWith("/content")) {
                navPage = pageManager.getPage(url);
                if (StringUtils.isBlank(text)) {
                    //use nav title or fall back to title
                    text = StringUtils.isNotBlank(navPage.getNavigationTitle()) ? navPage.getNavigationTitle() : navPage.getTitle();
                }
                isActive = currentPage.getPath().equals(url) ? true : false;
                isHierarchyActive = currentPage.getPath().startsWith(url) ? true : false;
            }
            Header.NavigationItem navItem = new NavigationItemImpl(navPage, isActive, isHierarchyActive, null, url, text);
            return navItem;
        }

        return null;
    }

    @Override
    public String getNavigationRoot() {
        if (rootPath == null) {
            rootPath = getHeaderProperty(PN_ROOT_PATH);
        }

        return rootPath;
    }

    @Override
    public String getLogoPath() {
        if (logoPath == null) {
            logoPath = getHeaderProperty(PN_LOGO_PATH);
        }

        return logoPath;
    }

    @Override
    public String getSiteTitle() {
        if (siteTitle == null) {
            siteTitle = getHeaderProperty(PN_SITE_TITLE);
        }

        // Check old property name for backwards compatibility
        if (siteTitle == null) {
            siteTitle = getHeaderProperty(JcrConstants.JCR_TITLE);
        }

        return siteTitle;
    }


    private String getHeaderProperty(final String propertyName) {
        if (headerProperties == null) { return null; }

        // Check beneath header resource first, check design second
        return headerProperties.get(propertyName, currentStyle.get(propertyName, String.class));
    }

    @Override
    public boolean isReady() {
        if (getItems().size() > 0 ||
                StringUtils.isNotBlank(getLogoPath()) ||
                StringUtils.isNotBlank(getSiteTitle())) {
            return true;
        } else {
            return false;
        }
    }

    @Nonnull
    @Override
    public String getExportedType() {
        return RESOURCE_TYPE;
    }
}