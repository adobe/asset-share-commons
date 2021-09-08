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

package com.adobe.aem.commons.assetshare.components.details.impl;

import com.adobe.aem.commons.assetshare.components.details.EditorLinks;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.util.RequireAem;
import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ExporterConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.mozilla.javascript.commonjs.module.Require;

import javax.annotation.Nonnull;

@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {EditorLinks.class, ComponentExporter.class},
        resourceType = {EditorLinksImpl.RESOURCE_TYPE},
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
@Exporter(name = ExporterConstants.SLING_MODEL_EXPORTER_NAME, extensions = ExporterConstants.SLING_MODEL_EXTENSION)
public class EditorLinksImpl extends AbstractEmptyTextComponent implements EditorLinks {
    protected static final String RESOURCE_TYPE = "asset-share-commons/components/details/editor-links";

    private static final String ASSET_DETAILS_PREFIX = "/assetdetails.html";
    private static final String ASSET_FOLDER_PREFIX = "/assets.html";

    @Self
    @Required
    private SlingHttpServletRequest request;

    @Self
    @Required
    private AssetModel asset;

    @OSGiService
    @Required
    private transient RequireAem requireAem;

    @ValueMapValue
    private String assetDetailsLinkLabel;

    @ValueMapValue
    private String assetFolderLinkLabel;

    @Override
    public String getAssetDetailsEditorPath() {
        return request.getResourceResolver().map(ASSET_DETAILS_PREFIX + asset.getPath());
    }

    @Override
    public String getAssetFolderEditorPath() {
        return request.getResourceResolver().map(request, ASSET_FOLDER_PREFIX + asset.getPath());
    }

    @Override
    public boolean isEmpty() {
        return !isReady();
    }

    @Override
    public boolean isReady() {
        final ResourceResolver resourceResolver = request.getResourceResolver();

        final boolean publishInstance = RequireAem.ServiceType.PUBLISH.equals(requireAem.getServiceType());
        final boolean missingLabels = StringUtils.isBlank(assetDetailsLinkLabel) && StringUtils.isBlank(assetFolderLinkLabel);
        final boolean missingTargetResources = resourceResolver.resolve(request, ASSET_DETAILS_PREFIX + asset.getPath()) == null ||
                resourceResolver.resolve(request, ASSET_FOLDER_PREFIX + asset.getPath()) == null;

        if (asset == null || publishInstance || missingLabels || missingTargetResources) {
            return false;
        }

        return true;
    }

    @Nonnull
    @Override
    public String getExportedType() {
        return RESOURCE_TYPE;
    }
}
