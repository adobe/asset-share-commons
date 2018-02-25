package com.adobe.aem.commons.assetshare.util;

import org.osgi.annotation.versioning.ProviderType;

/**
 *
 * The Model Cache allows an instance of model to be cached for the life of the Sling HTTP Request.
 *
 * This is convenient when using Sling Models across components when the models are always the same in
 * the context of the Sling HTTP Request (such as the ASC Config or PagePredicate) or when a model should only be
 * instantiated once per request (such as the Search model).
 *
 * The way the ModelCache is typically used is in an HTL script and the ModelCache model is retrieved normally via the
 *     data-sly-use.modelCache
 * call such as
 *     data-sly-use.modelCache="com.adobe.aem.commons.assetshare.util.ModelCache"
 *
 * Cache-able models are then gotten using the Sling Model’s adapter class name, by passing it as a String to the modelCache object’s get method via the [..] operator, such as:
 *     data-sly-test.config="${modelCache['com.adobe.aem.commons.assetshare.configuration.Config']}"
 *
 * If in some circumstance a “fresh” (not from cache) instance of a Sling Model is desired, simply use
 *     data-sly-use.myModel="com...SomeModel"
 * as usual, and simply do not engage the ModelCache.
 *
 * Considerations:
 * - If the requested model does not exist in the ModelCache, it will be adapted from the SlingHttpServletRequest, cached and returned to the requesting code.
 * - Cache-able models MUST be adaptable from SlingHttpServletRequest.
 * - Only ONE instance of a particular Sling Model Class type can exist in the cache (for that request).
 */
@ProviderType
public interface ModelCache {
    /**
     * This method is preferred from use in Java code where classes can explicitly provided.
     *
     *    ModelCache modelCache = request.adaptTo(ModelCache.class);
     *    Search search = modelCache.get(Search.class);`
     *
     * @param clazz The Sling Model class to get from the cache.
     * @param <T> The Sling Model class to get from the cache.
     * @return the cached sling model.
     */
    <T> T get(Class<T> clazz);

    /**
     * This method is preferred from use in HTL code where classes can NOT explicitly provided, but Strings can be easily passed.
     *
     * &lt;sly    data-sly-use.modelCache="com.adobe.aem.commons.assetshare.util.ModelCache"
     *         data-sly-test.config="${modelCache['com.adobe.aem.commons.assetshare.configuration.Config']}"
     *         data-sly-test.search="${modelCache['com.adobe.aem.commons.assetshare.search.Search']}"
     *         data-sly-test.pagePredicate="${modelCache['com.adobe.aem.commons.assetshare.components.predicates.PagePredicate']}"&gt;
     *     ...
     * &lt;/sly&gt;
     *
     * When engaging w/ the ModelCache in HTL, data-sly-test.xxx must be used to set the model object to the HTL variable since it is does not follow the usual adaptation model.
     *
     * @param className the full class name (as a String) of the Sling Model to get from the cache.
     * @return the cached sling model.
     */
    Object get(Object className);
}