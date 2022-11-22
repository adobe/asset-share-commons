package com.adobe.aem.commons.assetshare.components.presskit.impl;

import com.adobe.aem.commons.assetshare.components.presskit.PressKit;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.day.cq.dam.commons.util.DamUtil;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.apache.sling.models.factory.ModelFactory;
import org.apache.sling.resource.collection.ResourceCollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.day.cq.commons.jcr.JcrConstants.JCR_CONTENT;

@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {PressKit.class},
        resourceType = {PressKitImpl.RESOURCE_TYPE}
)
public class PressKitImpl implements PressKit {
    public static final String RESOURCE_TYPE = "asset-share-commons/components/press-kit";

    @Self
    private SlingHttpServletRequest request;

    @ValueMapValue
    @Optional
    private List<String> paths;

    @ValueMapValue
    @Optional
    private String displayResourceType;

    @OSGiService
    private ModelFactory modelFactory;

    List<AssetModel> assets;

    @Override
    public List<AssetModel> getAssets() {
        if (assets != null) {
            return assets;
        }

        assets = new ArrayList<>();
        // Return a list of AssetModels from the paths using isAssetPath, isAssetFolderPath and isAssetCollectionPath

        for (final String path : paths) {
            final Resource resource = request.getResourceResolver().getResource(path);
            if (resource != null) {
                if (isAssetPath(resource)) {
                    assets.addAll(getAssetsFromAssetPath(resource));
                } else if (com.adobe.aem.commons.assetshare.util.DamUtil.isAssetFolder(resource.getResourceResolver(), resource.getPath())) {
                    assets.addAll(getAssetsFromAssetFolderPath(resource));
                }
            }
        }

        return assets;
    }

    @Override
    public boolean isReady() {
        return getAssets().size() > 0;
    }

    private Collection<? extends AssetModel> getAssetsFromAssetFolderPath(Resource resource) {
        final List<AssetModel> results = new ArrayList<>();
        resource.getChildren().forEach(child -> {
            if (!JCR_CONTENT.equals(child.getName()) && (isAssetPath(child))) {
                results.addAll(getAssetsFromAssetPath(child));
            }
        });

        String pressKitBannerImage = resource.getValueMap().get("jcr:content/metadata/pressKitBannerImage", String.class);

        return filterAssets(results, pressKitBannerImage);
    }

    private Collection<? extends AssetModel> getAssetsFromAssetPath(Resource resource) {
        final List<AssetModel> results = new ArrayList<>();
        results.add(modelFactory.createModel(resource, AssetModel.class));
        return results;
    }

    private boolean isAssetPath(Resource resource) {
        return resource != null && DamUtil.isAsset(resource);
    }

    // Filter assets to remove bannerImage
    private Collection<? extends AssetModel> filterAssets(Collection<? extends AssetModel> assets, String bannerImage) {
        if (bannerImage == null) {
            return assets;
        }

        String bannerImageRegex = bannerImage.replaceAll("\\.", "\\\\.").replaceAll("\\*", ".*");

        final List<AssetModel> results = new ArrayList<>();
        assets.forEach(asset -> {
            // Regex match asset.getName() with bannerImage
            if (!asset.getName().matches(bannerImageRegex)) {
                results.add(asset);
            }
        });
        return results;
    }

}
