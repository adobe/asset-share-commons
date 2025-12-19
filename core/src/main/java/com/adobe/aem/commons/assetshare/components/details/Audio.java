package com.adobe.aem.commons.assetshare.components.details;

import org.osgi.annotation.versioning.ConsumerType;

/**
 * Interface for Audio Component
 */
@ConsumerType
public interface Audio extends EmptyTextComponent {

    /**
     * @return Returns src
     */
    String getSrc();

    /**
     * @return Returns true if the asset's mime type is audio
     */
    boolean isAudioAsset();

    /**
     * @return Returns the (mime))type of the audio asset
     */
    default String getType() { return null; }

}