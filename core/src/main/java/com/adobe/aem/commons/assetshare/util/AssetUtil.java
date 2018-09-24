package com.adobe.aem.commons.assetshare.util;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.commons.util.DamUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.core.fs.FileSystem;
import org.apache.sling.api.SlingHttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;

/**
 * Utility class for Asset Related functionalities
 */
public class AssetUtil {

    private static final Logger log = LoggerFactory.getLogger(AssetUtil.class);

    /**
     * Utility method to get asset using Asset ID
     * @param request
     * @param suffix
     * @return
     */
    public static Asset getAssetById(SlingHttpServletRequest request, String suffix) {
        String id = StringUtils.substringBefore(StringUtils.removeStart(suffix, FileSystem.SEPARATOR), ".");
        if (StringUtils.isNotBlank(id)) {
            try {
                return DamUtil.getAssetFromID(request.getResourceResolver(), id);
            } catch (RepositoryException repositoryException) {
                log.error("Error attempting to resolve asset via ID [ " + id + " ]", repositoryException);
            }
        }

        return null;
    }
}
