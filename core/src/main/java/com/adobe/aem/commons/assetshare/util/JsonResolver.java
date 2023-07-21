package com.adobe.aem.commons.assetshare.util;

import com.google.gson.JsonElement;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.osgi.annotation.versioning.ProviderType;



@ProviderType
public interface JsonResolver {
    /**
     * Resolves a JSON object from a path.
     *
     * @param request  the SlingHttpServletRequest
     * @param response the SlingHttpServletResponse
     * @param path     the path to resolve, this can be a internal JCR Path to a nt:file/nt:resource, dam:Asset, and internal resource that is requested via an internal sling request, or an external url (starting with http:// or https://).
     * @return the JSON object or null if the path could not be resolved.
     */
    JsonElement resolveJson(SlingHttpServletRequest request, SlingHttpServletResponse response, String path);
}
