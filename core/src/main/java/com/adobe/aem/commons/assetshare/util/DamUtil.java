package com.adobe.aem.commons.assetshare.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ResourceResolver;

import static org.apache.sling.jcr.resource.api.JcrResourceConstants.NT_SLING_FOLDER;
import static org.apache.sling.jcr.resource.api.JcrResourceConstants.NT_SLING_ORDERED_FOLDER;

public class DamUtil {

    public static boolean isAssetFolder(ResourceResolver resourceResolver, String path) {
        if (!StringUtils.startsWith(path, "/content/dam/")) {
            return false;
        } else if (resourceResolver.getResource(path) == null) {
            return false;
        } else if (resourceResolver.getResource(path).isResourceType("nt:folder")  || resourceResolver.getResource(path).isResourceType(NT_SLING_FOLDER) || resourceResolver.getResource(path).isResourceType(NT_SLING_ORDERED_FOLDER)) {
            return true;
        }

        return false;
    }
}
