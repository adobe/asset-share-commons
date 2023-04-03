package com.adobe.aem.commons.assetshare.util;

import org.apache.sling.api.resource.Resource;
import org.osgi.annotation.versioning.ProviderType;

import java.util.Map;

/**
 * This interface is used to proxy the AssetDelivery service, which is only available on AEM as a Cloud Service environments running in the Adobe Cloud (not the AEM SDK).
 */
@ProviderType
public interface AssetDeliveryProxy {
    String getDeliveryURL(Resource resource, Map<String, Object> params);
}
