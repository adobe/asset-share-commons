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

    @ValueMapValue
    @Optional
    private String displayResourceType;

    @OSGiService
    private AssetKitHelper assetKitHelper;

    @OSGiService
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
