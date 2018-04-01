package com.adobe.aem.commons.assetshare.components.details;

/**
 *
 * Interface for Video Component
 *
 */
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
