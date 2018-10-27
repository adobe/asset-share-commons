package com.adobe.aem.commons.assetshare.util;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface MimeTypeHelper {
    /**
     * @param mimeType the mime type to check
     * @return true if the mime type is to be considered supported by the browser.
     */
    boolean isBrowserSupportedImage(String mimeType);

}
