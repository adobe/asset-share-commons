package com.adobe.aem.commons.assetshare.components.actions.impl;

import com.adobe.aem.commons.assetshare.components.actions.AssetDownloadHelper;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.day.cq.dam.api.jobs.AssetDownloadService;
import org.apache.sling.api.resource.Resource;
import org.osgi.util.converter.Converters;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Set;

@Component
public class AssetDownloadHelperImpl implements AssetDownloadHelper {
    private static final Logger log = LoggerFactory.getLogger(AssetDownloadHelperImpl.class);

    private static final String ASSET_DOWNLOAD_SERVLET_PID = "com.day.cq.dam.core.impl.servlet.AssetDownloadServlet";
    private static final String MAX_SIZE_PROPERTY = "asset.download.prezip.maxcontentsize";
    private static final long DEFAULT_SIZE_LIMIT = -1L;

    @Reference
    private AssetDownloadService assetDownloadService;

    @Reference
    private ConfigurationAdmin configAdmin;

    @Override
    public long getMaxContentSizeLimit() {
        try {
            final Dictionary<String, Object> osgiConfigurationProperties = getAssetDownloadServletProperties();
           
            if (osgiConfigurationProperties != null) {
                return Converters.standardConverter().convert(osgiConfigurationProperties.get(MAX_SIZE_PROPERTY)).defaultValue(DEFAULT_SIZE_LIMIT).to(Long.class);
            } else{
                log.debug("No OSGi configuration properties could be found for service.pid [ {} ]", ASSET_DOWNLOAD_SERVLET_PID);
            }
        } catch (IOException | InvalidSyntaxException e) {
            log.error("Could not get max content size property for AEM's Asset Download Servlet", e);
        }

        return DEFAULT_SIZE_LIMIT;
    }

    @SuppressWarnings("unchecked")
    private Dictionary<String, Object> getAssetDownloadServletProperties () throws IOException, InvalidSyntaxException {
         Configuration[] configurations = configAdmin.listConfigurations(
                    "(service.pid=" + ASSET_DOWNLOAD_SERVLET_PID + ")");

            if (configurations != null && configurations.length == 1) {
                return configurations[0].getProperties();
            } else {
                log.debug("A non-unary number of OSGi configuration could be found for service.pid [ {} ]", ASSET_DOWNLOAD_SERVLET_PID);
            }
            return null;

    }

    @Override
    public long getAssetDownloadSize(Collection<AssetModel> assets, Resource configResource) {
        final Set<Resource> assetDownloadRequestPathsSet = new HashSet<Resource>();

        for (final AssetModel asset : assets) {
            assetDownloadRequestPathsSet.add(asset.getResource());
        }

        // Use AssetDownloadService to compute the pre-zip size based on list of assets
        final AssetDownloadService.AssetDownloadParams preZipComputationParams = new AssetDownloadService.AssetDownloadParams(
                configResource,
                assetDownloadRequestPathsSet,
                true,
                true,
                true,
                null,
                null,
                "assets.zip", // downloadName doesn't matter
                null,
                false,
                null,
                null);

        return assetDownloadService.computeAssetDownloadSize(preZipComputationParams);
    }
}