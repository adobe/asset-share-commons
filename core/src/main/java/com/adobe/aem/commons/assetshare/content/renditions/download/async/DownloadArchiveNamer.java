package com.adobe.aem.commons.assetshare.content.renditions.download.async;

import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRendition;
import com.adobe.cq.dam.download.api.DownloadTarget;
import org.osgi.annotation.versioning.ConsumerType;

import javax.annotation.Nullable;

@ConsumerType
public interface DownloadArchiveNamer {

    /**
     * Generates the name of the file (including any folder paths). This is not to be confused with the name of the actual archive ZIP file.
     *
     * @param assetModel the asset model generating the file
     * @param assetRendition the asset rendition generating the file
     * @param downloadTarget the async download framework DownloadTarget parameters, keys are provided via DownloadTargetParameters.
     * @return the file path this rendition's file should be placed int he archive ZIP file.
     */
    String getArchiveFilePath(AssetModel assetModel, @Nullable AssetRendition assetRendition, DownloadTarget downloadTarget);
}
