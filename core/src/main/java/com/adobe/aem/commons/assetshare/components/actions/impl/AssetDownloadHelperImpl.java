package com.adobe.aem.commons.assetshare.components.actions.impl;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Set;
import com.adobe.aem.commons.assetshare.components.actions.AssetDownloadHelper;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.day.cq.dam.api.jobs.AssetDownloadService;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class AssetDownloadHelperImpl implements AssetDownloadHelper {

    private static final Logger log = LoggerFactory.getLogger(AssetDownloadHelperImpl.class);
    private static final String ASSET_DOWNLOAD_PID = "com.day.cq.dam.core.impl.servlet.AssetDownloadServlet";
    private static final String MAX_SIZE_PROPERTY = "asset.download.prezip.maxcontentsize";

    @Reference
    private AssetDownloadService assetDownloadService;

    @Reference
    private ConfigurationAdmin configAdmin;

    @Override
    public long getMaxContentSizeLimit() {
        return getProperty(ASSET_DOWNLOAD_PID, MAX_SIZE_PROPERTY);
    }

    private long getProperty(final String pid, final String property) {
        try {
            Configuration conf = configAdmin.getConfiguration(pid);

            @SuppressWarnings("unchecked")
            Dictionary<String, Object> props = conf.getProperties();
            
            if (props != null) {
                return PropertiesUtil.toLong(props.get(property), -1L);
            }
        } catch (IOException e) {
            log.error("Could not get property", e);
        }
        return -1L;
    }

    @Override
    public long computeAssetDownloadSize(Collection<AssetModel> assets, Resource requestResource) {
        Set<Resource> assetDownloadRequestPathsSet = new HashSet<Resource>();

        for (AssetModel asset : assets) {
            assetDownloadRequestPathsSet.add(asset.getResource());
        }

        //Use AssetDownloadService to compute the prezip size based on list of assets
        AssetDownloadService.AssetDownloadParams preZipComputationParams = new AssetDownloadService.AssetDownloadParams(
            requestResource,
            assetDownloadRequestPathsSet, 
            true, 
            true, 
            true,
            null, 
            null,
            null,
            "assests.zip", 
            null,
            false, 
            null, 
            null);
        return assetDownloadService.computeAssetDownloadSize(preZipComputationParams);
    }

}