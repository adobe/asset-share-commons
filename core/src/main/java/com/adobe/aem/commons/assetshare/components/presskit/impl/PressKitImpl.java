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

@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {PressKit.class},
        resourceType = {PressKitImpl.RESOURCE_TYPE}
)
public class PressKitImpl implements PressKit {
    public static final String RESOURCE_TYPE = "asset-share-commons/components/presskit";

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
                /*
                if (isAssetPath(resource)) {
                    assets.addAll(getAssetsFromAssetPath(resource));
                } else if (isAssetFolderPath(resource)) {

                } else if (isAssetCollectionPath(resource)) {
                    assets.addAll(getAssetsFromAssetCollectionPath(resource));
                }*/
                assets.addAll(getAssetsFromAssetFolderPath(resource));
            }
        }

        return assets;
    }

    @Override
    public boolean isReady() {
        return getAssets().size() > 0;
    }

    private Collection<? extends AssetModel> getAssetsFromAssetCollectionPath(Resource resource) {
        final List<AssetModel> results = new ArrayList<>();
        final ResourceCollection collection = resource.adaptTo(ResourceCollection.class);
        if (collection != null) {
            collection.getResources().forEachRemaining(r -> {
                if (isAssetPath(r)) {
                    results.addAll(getAssetsFromAssetPath(r));
                }
            });
        }
        return results;
    }

    private Collection<? extends AssetModel> getAssetsFromAssetFolderPath(Resource resource) {
        final List<AssetModel> results = new ArrayList<>();
        resource.getChildren().forEach(child -> {
            if (!"jcr:content".equals(child.getName()) && (isAssetPath(child))) {
                results.addAll(getAssetsFromAssetPath(child));
            }
        });

        return results;
    }

    private Collection<? extends AssetModel> getAssetsFromAssetPath(Resource resource) {
        final List<AssetModel> results = new ArrayList<>();
        results.add(modelFactory.createModel(resource, AssetModel.class));
        return results;
    }

    private boolean isAssetPath(Resource resource) {
        return resource != null && DamUtil.isAsset(resource);
    }

    private boolean isAssetFolderPath(Resource resource) {
        return resource != null && (resource.isResourceType("sling:Folder") || resource.isResourceType("sling:OrderedFolder") || resource.isResourceType("nt:folder"));
    }

    private boolean isAssetCollectionPath(Resource resource) {
        return resource != null && resource.adaptTo(ResourceCollection.class) != null;
    }
}
