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

package com.adobe.aem.commons.assetshare.components.actions.download.impl;

import com.adobe.aem.commons.assetshare.components.actions.ActionHelper;
import com.adobe.aem.commons.assetshare.components.actions.AssetDownloadHelper;
import com.adobe.aem.commons.assetshare.components.actions.download.Download;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionDispatchers;
import com.adobe.aem.commons.assetshare.util.RequireAem;
import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ExporterConstants;
import com.adobe.cq.wcm.core.components.models.form.OptionItem;
import com.adobe.cq.wcm.core.components.models.form.Options;
import com.day.cq.dam.commons.util.UIHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.*;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.apache.sling.models.factory.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.*;
import java.util.Optional;
import java.util.stream.Collectors;

import static javax.jcr.query.Query.JCR_SQL2;

@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {Download.class, ComponentExporter.class},
        resourceType = {DownloadImpl.RESOURCE_TYPE},
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
@Exporter(name = ExporterConstants.SLING_MODEL_EXPORTER_NAME, extensions = ExporterConstants.SLING_MODEL_EXTENSION)

public class DownloadImpl implements Download, ComponentExporter {
    private static final Logger log = LoggerFactory.getLogger(DownloadImpl.class);

    protected static final String RESOURCE_TYPE = "asset-share-commons/components/modals/download";

    private static final long DEFAULT_SIZE_LIMIT = -1L;
    private static final String NN_ASSET_RENDITIONS_GROUPS = "asset-renditions-groups";
    public static final String PN_ASSET_RENDITIONS_GROUP_TITLE = "assetRenditionsGroupTitle";
    public static final String PN_ARCHIVE_FILE_NAME_EXPRESSION = "archiveFileExpression";
    public static final String NN_ASSET_RENDITIONS = "asset-renditions";
    public static final String NN_ITEMS = "items";

    public static final String DEFAULT_ARCHIVE_FILE_NAME_EXPRESSION = "${asset.name.no-extension}/${asset.name.no-extension} (${rendition.name})";

    @Self
    @Required
    protected SlingHttpServletRequest request;

    @OSGiService
    @Required
    private RequireAem requireAem;

    @ValueMapValue
    @Default(values = "Assets")
    protected String zipFileName;

    @OSGiService
    @Required
    protected ActionHelper actionHelper;

    @OSGiService
    @Required
    protected AssetDownloadHelper assetDownloadHelper;

    @OSGiService
    @Required
    private ModelFactory modelFactory;

    @OSGiService
    @Required
    private AssetRenditionDispatchers assetRenditionDispatchers;

    @ValueMapValue
    private Boolean legacyMode;

    @Deprecated
    @ValueMapValue
    private Boolean excludeOriginalAssets;

    protected List<AssetRenditionsGroup> assetRenditionsGroups = null;

    protected Collection<AssetModel> assets = null;

    /***
     * Max content size retrieved from com.day.cq.dam.core.impl.servlet.AssetDownloadServlet
     */
    protected Long maxContentSize = DEFAULT_SIZE_LIMIT;

    /***
     * Potential download size of current assets
     */
    protected Long downloadContentSize = DEFAULT_SIZE_LIMIT;

    @PostConstruct
    @SuppressWarnings("squid:S1696") // Suppress catching NPE warning due to this being a function of AEM's API
    protected void init() {
        assets = actionHelper.getAssetsFromQueryParameter(request, "path");

        if (assets.isEmpty()) {
            assets = actionHelper.getPlaceholderAsset(request);
        } else {
            if (RequireAem.Distribution.CLASSIC.equals(requireAem.getDistribution())) {
                try {
                    calculateSizes();
                } catch (NullPointerException e) {
                    log.error("An NPE is known to be thrown by AEM in conditions where Dynamic Media is enabled and/or the asset has Dynamic Media attributes when trying to compute sizes.");
                }
            }
        }
    }

    @Override
    public List<AssetRenditionsGroup> getAssetRenditionsGroups() {
        if (assetRenditionsGroups == null) {
            assetRenditionsGroups = new ArrayList<>();

            Optional.ofNullable(request.getResource().getChild(NN_ASSET_RENDITIONS_GROUPS + "/" + NN_ITEMS)).ifPresent(groups -> {
                groups.getChildren().forEach(child -> getAssetRenditionGroup(child.getChild(NN_ASSET_RENDITIONS)).ifPresent(arg -> assetRenditionsGroups.add(arg)));
            });
        }

        return Collections.unmodifiableList(assetRenditionsGroups);
    }

    private Optional<AssetRenditionsGroup> getAssetRenditionGroup(final Resource group) {
        Optional<AssetRenditionsGroup> assetRenditionsGroup = Optional.empty();

        if (group != null) {
            final String title = group.getParent().getValueMap().get(PN_ASSET_RENDITIONS_GROUP_TITLE, String.class);
            final Options options = modelFactory.getModelFromWrappedRequest(request, group, Options.class);

            // Only show service-able rendition names
            final Optional<List<OptionItem>> sanitizedOptions = Optional.of(options.getItems().stream()
                    .filter(item -> assetRenditionDispatchers.isValidAssetRenditionName(item.getValue()))
                    .collect(Collectors.toList()));

            assetRenditionsGroup = sanitizedOptions
                    .filter(so -> !so.isEmpty())
                    .map(so -> new AssetRenditionsGroup(title, so));
        }

        return assetRenditionsGroup;
    }

    public Collection<AssetModel> getAssets() {
        return Collections.unmodifiableCollection(assets);
    }

    public String getZipFileName() {
        return StringUtils.removeEndIgnoreCase(zipFileName, ".zip");
    }

    @Override
    public long getMaxContentSize() {
        return this.maxContentSize;
    }

    @Override
    public long getDownloadContentSize() {
        return this.downloadContentSize;
    }

    @Override
    public String getMaxContentSizeLabel() {
        return UIHelper.getSizeLabel(getMaxContentSize(), request);
    }

    @Override
    public String getDownloadContentSizeLabel() {
        return UIHelper.getSizeLabel(getDownloadContentSize(), request);
    }

    @Override
    public boolean isAsynchronous() {
        // async downloads only available in the cloud
        return RequireAem.Distribution.CLOUD_READY.equals(requireAem.getDistribution());
    }

    @Deprecated
    @Override
    public boolean isLegacyMode() {
        if (legacyMode == null) {
            if (getAssetRenditionsGroups() != null && !getAssetRenditionsGroups().isEmpty()) {
                // Is the new renditions exist, then assume modern
                return false;
            } else {
                // modern does not exist, so check if legacy exists...
                return excludeOriginalAssets != null;
            }
        } else {
            return legacyMode;
        }
    }

    @Deprecated
    private void calculateSizes() {
        this.maxContentSize = assetDownloadHelper.getMaxContentSizeLimit();
        log.debug("Max allowed content size (in bytes) [ {} ]", this.maxContentSize);

        //check if needed to calculate max content size
        if(this.maxContentSize > 0) {
            log.debug("Max content size set, requires calculation of download  content size.");
            this.downloadContentSize = assetDownloadHelper.getAssetDownloadSize(assets, request.getResource());
            log.debug("Requested download content size (in bytes) [ {} ]", this.downloadContentSize);
        } else {
            this.downloadContentSize = DEFAULT_SIZE_LIMIT;
        }
    }

    @Nonnull
    @Override
    public String getExportedType() {
        return RESOURCE_TYPE;
    }
}