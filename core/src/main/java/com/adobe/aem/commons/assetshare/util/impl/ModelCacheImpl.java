package com.adobe.aem.commons.assetshare.util.impl;

import com.adobe.aem.commons.assetshare.util.ModelCache;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.commons.classloader.DynamicClassLoaderManager;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.factory.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Model(
    adaptables = { SlingHttpServletRequest.class },
    adapters = { ModelCache.class }
)
public class ModelCacheImpl extends AbstractHTLMap implements ModelCache {
    private static final Logger log = LoggerFactory.getLogger(ModelCacheImpl.class);

    private static String REQUEST_ATTRIBUTE_KEY_PREFIX = "asset-share-commons__request_models_cache__";

    @Self
    private SlingHttpServletRequest request;

    @OSGiService
    private ModelFactory modelFactory;

    @OSGiService
    private DynamicClassLoaderManager dynamicClassLoaderManager;

    @Override
    public final Object get(Object key) {
        if (!(key instanceof String)) {
            throw new IllegalArgumentException("The argument must be a String object");
        }

        Class clazz = null;

        try {
            clazz = Class.forName((String) key, true, dynamicClassLoaderManager.getDynamicClassLoader());
            return get(clazz);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Unable to derive a class from " + (String)key);
        }
    }

    @Override
    public final <T> T get(Class<T> clazz) {
        final String requestAttributeKey = getRequestAttributeKey(clazz);
        final Object cachedModel = request.getAttribute(requestAttributeKey);

        if (cachedModel == null) {
            final T model = request.adaptTo(clazz);
            if (model != null) {
                request.setAttribute(requestAttributeKey, model);
                log.debug("Initial caching of model [ {} ]", clazz.getName());
                return model;
            } else {
                log.debug("Could not create a model to cache for [ {} ] from the SlingHttpServletRequest", clazz.getName());
                return null;
            }
        } else {
            log.debug("Served model for [ {} ] from cache", clazz.getName());
            return (T) cachedModel;
        }
    }

    private String getRequestAttributeKey(final Class clazz) {
        return REQUEST_ATTRIBUTE_KEY_PREFIX + clazz.getName();
    }
}
