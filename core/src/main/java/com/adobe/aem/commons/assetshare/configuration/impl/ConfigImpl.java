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

package com.adobe.aem.commons.assetshare.configuration.impl;

import com.adobe.aem.commons.assetshare.components.actions.share.ShareService;
import com.adobe.aem.commons.assetshare.configuration.Config;
import com.adobe.aem.commons.assetshare.configuration.impl.selectors.AlwaysUseDefaultSelectorImpl;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.util.ForcedInheritanceValueMapWrapper;
import com.adobe.aem.commons.assetshare.util.RequireAem;
import com.adobe.granite.contexthub.api.ContextHub;
import com.day.cq.commons.inherit.HierarchyNodeInheritanceValueMap;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.components.ComponentContext;
import com.day.cq.wcm.commons.WCMUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.featureflags.Features;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.factory.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.Locale;

@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {Config.class}
)
public class ConfigImpl implements Config {
    private static final Logger log = LoggerFactory.getLogger(ConfigImpl.class);

    public static final String NODE_NAME = "config";

    private static final String HTML_EXTENSION = ".html";
    private static final String[] rootResourceTypes = new String[]{"asset-share-commons/components/structure/search-page"};

    private static final String SCENE7_FEATURE_FLAG = "com.adobe.dam.asset.scene7.feature.flag";

    // Actions
    private static final String DEFAULT_VIEW_SELECTOR = "partial";
    private static final String PN_VIEW_SELECTOR = "config/actions/viewSelector";

    private static final String PN_LICENSE_ENABLED = "config/actions/license/enabled";
    private static final String PN_LICENSE_AGREEMENT_VIEW_PATH = "config/actions/license/path";

    private static final String PN_DOWNLOAD_ENABLED = "config/actions/download/enabled";
    private static final String PN_DOWNLOAD_VIEW_PATH = "config/actions/download/path";

    private static final String PN_DOWNLOADS_VIEW_PATH = "config/actions/downloads/path";

    private static final String PN_CART_ENABLED = "config/actions/cart/enabled";
    private static final String PN_CART_VIEW_PATH = "config/actions/cart/path";

    private static final String PN_SHARE_ENABLED = "config/actions/share/enabled";
    private static final String PN_SHARE_VIEW_PATH = "config/actions/share/path";

    // Asset Details
    private static final String PN_DEFAULT_ASSET_DETAILS_PATH = "config/asset-details/defaultPath";
    private static final String PN_ASSET_DETAILS_SELECTOR = "config/asset-details/selector";
    public static final String PN_PLACEHOLDER_ASSET_PATH = "config/asset-details/placeholderPath";
    public static final String PN_ASSET_REFERENCE_BY_ID = "config/asset-details/assetReferenceById";

    @SuppressWarnings("CQRules:CQBP-71")
    public static final String DEFAULT_PLACEHOLDER_ASSET_PATH = "/apps/asset-share-commons/resources/placeholder.png";

    @Self
    @Required
    private SlingHttpServletRequest request;

    @OSGiService
    @Required
    private ModelFactory modelFactory;

    @OSGiService
    @Required
    private RequireAem requireAem;

    @OSGiService(injectionStrategy = InjectionStrategy.OPTIONAL)
    private ShareService shareService;

    @SlingObject
    @Required
    private Resource requestResource;

    @OSGiService
    @Required
    private Features features;

    @OSGiService
    private ContextHub contextHub;

    private Page currentPage;

    private ValueMap properties;

    private String viewSelector;

    private String rootPath;


    @PostConstruct
    protected void init() {
        final ComponentContext componentContext = WCMUtils.getComponentContext(request);

        Resource pageResource = requestResource;
        if (componentContext != null && componentContext.getPage() != null) {
            pageResource = componentContext.getPage().getContentResource();
        }

        final PageManager pageManager = pageResource.getResourceResolver().adaptTo(PageManager.class);
        currentPage = pageManager.getContainingPage(pageResource);

        properties = new ForcedInheritanceValueMapWrapper(new HierarchyNodeInheritanceValueMap(currentPage.getContentResource()));
        viewSelector = properties.get(PN_VIEW_SELECTOR, DEFAULT_VIEW_SELECTOR);
        rootPath = getRootPath(currentPage);
    }

    @Override
    public SlingHttpServletRequest getRequest() {
        return request;
    }

    @Override
    public ResourceResolver getResourceResolver() {
        return request.getResourceResolver();
    }

    @Override
    public ValueMap getProperties() {
        return properties;
    }

    @Override
    public Locale getLocale() {
        if (currentPage != null) {
            return currentPage.getLanguage(false);
        } else {
            return Locale.getDefault();
        }
    }

    @Override
    public AssetModel getPlaceholderAsset() {
        final String path = properties.get(PN_PLACEHOLDER_ASSET_PATH, DEFAULT_PLACEHOLDER_ASSET_PATH);

        final Resource placeholderResource = requestResource.getResourceResolver().getResource(path);
        if (placeholderResource != null) {
            return modelFactory.getModelFromWrappedRequest(request, placeholderResource, AssetModel.class);
        } else {
            return null;
        }
    }

    @Override
    public String getDownloadActionUrl() {
        final String path = properties.get(PN_DOWNLOAD_VIEW_PATH, rootPath + "/actions/download") + "." + viewSelector + HTML_EXTENSION;
        return pathResolves(path) ? path : null;
    }

    @Override
    public String getDownloadsActionUrl() {
        final String path = properties.get(PN_DOWNLOADS_VIEW_PATH, rootPath + "/actions/downloads") + "." + viewSelector + HTML_EXTENSION;
        return pathResolves(path) ? path : null;
    }

    @Override
    public String getLicenseActionUrl() {
        final String path = properties.get(PN_LICENSE_AGREEMENT_VIEW_PATH, rootPath + "/actions/license") + "." + viewSelector + HTML_EXTENSION;
        return pathResolves(path) ? path : null;
    }

    @Override
    public String getShareActionUrl() {
        final String path = properties.get(PN_SHARE_VIEW_PATH, rootPath + "/actions/share") + "." + viewSelector + HTML_EXTENSION;
        return pathResolves(path) ? path : null;
    }

    @Override
    public String getCartActionUrl() {
        final String path = properties.get(PN_CART_VIEW_PATH, rootPath + "/actions/cart") + "." + viewSelector + HTML_EXTENSION;
        return pathResolves(path) ? path : null;
    }

    @Override
    public boolean isShareEnabled() {
        return shareService != null &&
                compareEnablementValue(properties, PN_SHARE_ENABLED, ActionEnablements.ALWAYS) &&
                getShareActionUrl() != null;
    }

    @Override
    public boolean isDownloadEnabled() {
        return compareEnablementValue(properties, PN_DOWNLOAD_ENABLED, ActionEnablements.ALWAYS) &&
                getDownloadActionUrl() != null;
    }

    @Override
    public boolean isDownloadEnabledCart() {
        if(isCartEnabled()) {
            return compareEnablementValue(properties, PN_DOWNLOAD_ENABLED, ActionEnablements.ALWAYS, ActionEnablements.CART) &&
                    getDownloadActionUrl() != null;

        }

        return false;
    }

    @Override
    public boolean isShareEnabledCart() {
        if(isCartEnabled() && shareService != null) {
            return compareEnablementValue(properties, PN_SHARE_ENABLED, ActionEnablements.ALWAYS, ActionEnablements.CART) &&
                    getShareActionUrl() != null;
        }

        return false;
    }

    private boolean compareEnablementValue(ValueMap properties, String propertyName, ActionEnablements... validValues) {
        String strValue = properties.get(propertyName, ActionEnablements.NEVER.value);
        ActionEnablements enablementVal = ActionEnablements.forValue(strValue);
        for(ActionEnablements enablement : validValues) {
            if(enablement == enablementVal) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isCartEnabled() {
        return properties.get(PN_CART_ENABLED, false) &&
                getCartActionUrl() != null;
    }

    @Override
    public boolean isLicenseEnabled() {
        return properties.get(PN_LICENSE_ENABLED, false) &&
                getLicenseActionUrl() != null;
    }

    @Override
    public String getAssetDetailsSelector() {
        return properties.get(PN_ASSET_DETAILS_SELECTOR, AlwaysUseDefaultSelectorImpl.ID);
    }

    @Override
    public boolean getAssetDetailReferenceById()  {
        return properties.get(PN_ASSET_REFERENCE_BY_ID, false);
    }

    @Override
    public String getAssetDetailsPath() {
        return properties.get(PN_DEFAULT_ASSET_DETAILS_PATH, rootPath + "/details");
    }

    @Override
    public String getAssetDetailsUrl() {
        return getAssetDetailsPath() + ".html";
    }

    @Override
    public String getRootPath() {
        return getRootPath(currentPage);
    }

    @Override
    public boolean isContextHubEnabled() {
        final HierarchyNodeInheritanceValueMap properties = new HierarchyNodeInheritanceValueMap(currentPage.getContentResource());

        String path = properties.get("cq:contextHubPath", String.class);

        if (StringUtils.isNotBlank(path)) {
            Resource resource = request.getResourceResolver().getResource(path);
            if (resource != null) {
                return resource.isResourceType("granite/contexthub/cloudsettings/components/baseconfiguration");
            }
        }

        return false;
    }

    @Override
    public boolean isAemClassic() {
        return RequireAem.Distribution.CLASSIC.equals(requireAem.getDistribution());
    }

    private boolean pathResolves(final String path) {
        final Resource resource = request.getResourceResolver().resolve(request, path);
        return resource != null && !ResourceUtil.isNonExistingResource(resource);
    }

    /**
     * Finds the first root page (Search Page) for this Asset Share Commons page tree.
     *
     * @param currentPage
     * @return the path to the root page or / if none can be found
     */
    private String getRootPath(Page currentPage) {
        final ResourceResolver resourceResolver = request.getResourceResolver();
        Page page = currentPage;

        do {
            if (page != null) {
                for (final String resourceType : rootResourceTypes) {
                    if (page.getContentResource() != null
                            && resourceResolver.isResourceType(page.getContentResource(), resourceType)) {
                        return page.getPath();
                    }

                    page = page.getParent();
                }
            }
        } while (page != null);

        if (currentPage != null) {
            log.warn("Could not find a valid Asset Share Commons root page for [ {} ]. Check to ensure a parent page sling:resourceSuperTypes one of [ {} ]", currentPage.getPath(), StringUtils.join(rootResourceTypes, ","));
        } else {
            log.warn("Could not find a valid Asset Share Commons root page for because the current page could not be resolved.");
        }

        return "/";
    }

    private enum ActionEnablements {
        ALWAYS("true"),
        NEVER("false"),
        CART("cart");

        private String value;

        ActionEnablements(String value) {
            this.value=value;
        }

        public static ActionEnablements forValue(String value) {
            for(ActionEnablements enablement : ActionEnablements.values()) {
                if(enablement.value.equalsIgnoreCase(value)) {
                    return enablement;
                }
            }

            return ActionEnablements.NEVER;
        }
    }
}