package com.adobe.aem.commons.assetshare.util.impl;

import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.util.AssetDeliveryProxy;
import com.adobe.cq.wcm.spi.AssetDelivery;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Component
public class AssetDeliveryProxyImpl implements AssetDeliveryProxy {
    private static final Logger log = LoggerFactory.getLogger(ExpressionEvaluatorImpl.class);

    public static final String[] ALLOWED_FORMATS = new String[] {  "gif", "png", "png8", "jpg", "jpeg", "jpe", "pjpg", "bjpg", "webp", "webpll", "webply" };

    public final String PN_PATH = "path";
    public final String PN_FORMAT = "format";
    public final String PN_SEONAME = "seoname";

    @Reference
    private AssetDelivery assetDelivery;

    @Override
    public String getDeliveryURL(Resource resource, Map<String, Object> params) {
        // The following properties are REQUIRED by the AssetDelivery service, so add them with defaults if they do not exist
        if (StringUtils.isBlank((String) params.get(PN_PATH))) {
            params.put(PN_PATH, resource.getPath());
        }

        if (StringUtils.isBlank((String) params.get(PN_FORMAT)) || !ArrayUtils.contains(ALLOWED_FORMATS, params.get(PN_FORMAT))) {
            params.put(PN_FORMAT, "webp");
        }

        if (StringUtils.isBlank((String) params.get(PN_SEONAME))) {
            AssetModel asset = resource.adaptTo(AssetModel.class);
            if (asset != null) {
                params.put(PN_SEONAME, asset.getTitle().replaceAll("[^a-zA-Z0-9]", "-"));
            } else {
                params.put(PN_SEONAME, resource.getName().replaceAll("[^a-zA-Z0-9]", "-"));
            }
        }

        return assetDelivery.getDeliveryURL(resource, params);
    }
}
