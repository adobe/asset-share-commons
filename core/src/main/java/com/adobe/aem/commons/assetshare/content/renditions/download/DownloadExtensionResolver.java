package com.adobe.aem.commons.assetshare.content.renditions.download;

import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRendition;
import org.osgi.annotation.versioning.ConsumerType;

@ConsumerType
public interface DownloadExtensionResolver {
    /**
     * Returns the file extension that should be used for the AssetRendition's download filename. (Do not include the leading .)
     *
     * Return null to use the fallback Apache Sling MimeTypeService to derive the extension for the assetRendition.
     *
     * @param assetModel The AssetModel associated the AssetRendition whose extension is being resolved.
     * @param assetRendition the AssetRendition whose extension is being resolved.
     * @return the extension (without leading dot) or null to use the system fallback extension resolver.
     */
    String resolve(AssetModel assetModel, AssetRendition assetRendition);
}
