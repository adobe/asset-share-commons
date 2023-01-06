package com.adobe.aem.commons.assetshare.util.assetkit.impl.componentupdaters;

import com.adobe.aem.commons.assetshare.components.assetkit.impl.AssetKitImpl;
import com.adobe.aem.commons.assetshare.util.assetkit.AssetKitHelper;
import com.adobe.aem.commons.assetshare.util.assetkit.ComponentUpdater;
import com.day.cq.wcm.api.Page;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.RepositoryException;

@Component
public class AssetKitComponentUpdaterImpl implements ComponentUpdater {
    private static String RESOURCE_TYPE = AssetKitImpl.RESOURCE_TYPE;
    private static String PROPERTY_NAME = "paths";

    @Reference
    private transient AssetKitHelper assetKitHelper;

    @Override
    public String getName() {
        return "Asset kit component (Asset Share Commons)";
    }

    @Override
    public void updateComponent(Page assetKitPage, Resource assetKit) throws PersistenceException, RepositoryException {
        assetKitHelper.updateComponentOnPage(assetKitPage, RESOURCE_TYPE, PROPERTY_NAME, assetKit.getPath());
    }
}
