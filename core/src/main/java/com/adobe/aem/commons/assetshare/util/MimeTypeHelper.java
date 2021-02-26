package com.adobe.aem.commons.assetshare.util;

import java.util.List;
import java.util.Map;

import org.osgi.annotation.versioning.ProviderType;

import com.adobe.aem.commons.assetshare.content.AssetModel;

@ProviderType
public interface MimeTypeHelper {
    /**
     * @param mimeType the mime type to check
     * @return true if the mime type is to be considered supported by the browser.
     */
    boolean isBrowserSupportedImage(String mimeType);
    /**
     * @param mimeType the mime type to check
     * @return true if the mime type is configured to download.
     */
    boolean isDownloadSupportedImage(String mimeType);
    /**
     * @param mimeType the mime type to check
     * @returntrue if the mime type is configured to download.
     */
    boolean isDownloadSupportedVideo(String mimeType);
    /**
     * @param mimeType the mime type to check
     * @return true if the mime type is configured to download.
     */
    boolean isDownloadSupportedOther(String mimeType);
    
    
    /**
     * Get MimeType of an asset based on the Asset Model
     * 
     *
     * @param assetModel Absolute path of the template used to send the email.
     * @param emailParams Replacement variable map to be injected in the template
     * @param recipients recipient email addresses
     *
     * @return failureList containing list recipient's InternetAddresses for which email sent failed
     */
    String getMimeType(AssetModel asset);
    

}
