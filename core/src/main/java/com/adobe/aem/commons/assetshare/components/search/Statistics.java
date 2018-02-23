package com.adobe.aem.commons.assetshare.components.search;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface Statistics {
    /**
     * @return the component id; unique to this instance of the component.
     */
    String getId();
}
