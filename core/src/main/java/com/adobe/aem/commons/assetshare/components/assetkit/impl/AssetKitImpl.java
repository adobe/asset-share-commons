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
package com.adobe.aem.commons.assetshare.components.assetkit.impl;

import com.adobe.aem.commons.assetshare.components.assetkit.AssetKit;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.util.assetkit.AssetKitHelper;
import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ExporterConstants;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.annotation.Nonnull;
import java.util.Collection;

@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {AssetKit.class, ComponentExporter.class},
        resourceType = {com.adobe.aem.commons.assetshare.components.assetkit.impl.AssetKitImpl.RESOURCE_TYPE}
)
@Exporter(name = ExporterConstants.SLING_MODEL_EXPORTER_NAME, extensions = ExporterConstants.SLING_MODEL_EXTENSION)
public class AssetKitImpl implements AssetKit {
    public static final String RESOURCE_TYPE = "asset-share-commons/components/asset-kit";

    @Self
    private SlingHttpServletRequest request;

    @ValueMapValue
    @Optional
    private Collection<String> paths;

    @OSGiService
    private AssetKitHelper assetKitHelper;

    @OSGiService
    @Optional
    private AssetKit.Filter assetsFilter;

    Collection<? extends AssetModel> assets;

    @Override
    public Collection<? extends AssetModel> getAssets() {
        if (assets != null) {
            return assets;
        }

        assets = assetKitHelper.getAssets(request.getResourceResolver(), paths.toArray(new String[0]));
        // Return a list of AssetModels from the paths using isAssetPath, isAssetFolderPath and isAssetCollectionPath

        if (assetsFilter != null) {
            assets = assetsFilter.filter(assets);
        }

        return assets;
    }

    @Override
    public boolean isReady() {
        return getAssets().size() > 0;
    }

    @Nonnull
    @Override
    public String getExportedType() {
        return RESOURCE_TYPE;
    }
}
