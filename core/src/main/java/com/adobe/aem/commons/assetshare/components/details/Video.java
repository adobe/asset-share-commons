package com.adobe.aem.commons.assetshare.components.details;

import org.osgi.annotation.versioning.ProviderType;

/**
 *
 * Interface for Video Component
 *
 */
@ProviderType
public interface Video extends EmptyTextComponent {
    /**
     * @return Returns src
     */
    String getSrc();

    /**
     * @return Returns true if the asset's mime type is video
     */
    boolean isVideoAsset();

}
