package com.adobe.aem.commons.assetshare.util;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface ModelCache {
    Object get(Object className);
}