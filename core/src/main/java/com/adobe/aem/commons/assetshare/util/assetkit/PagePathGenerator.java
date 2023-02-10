package com.adobe.aem.commons.assetshare.util.assetkit;

import org.apache.sling.api.resource.Resource;
import org.osgi.annotation.versioning.ConsumerType;

@ConsumerType
public interface PagePathGenerator {
    /**
     * The name of the page path generator. Displays in the Asset kit creator workflow dialog's dropdown.
     * @return the name of the page path generator. Ideally is unique across all page path generator, so you can tell which is which.
     */    
    String getName();

    /**
     * The id of the component updater. Used to identify the component updater in the workflow dialog.
     * Must be unique across all component updaters.
     * Defaults to the component's full class name. No need to change the default implementation.
     * @return the unique id of the page path generator.
     */
    default String getId() { return this.getClass().getName(); }

    /**
     * Generates a page path based on the @{code prefix} and @{code assetKit} resource.
     * @param prefix the JCR path prefix all asset kit pages will be created under. This is defined in the Asset Kit creator workflow dialog.
     * @param assetKit the resource (folder, collection) that represents the asset kit's contents.
     * @return the full JCR path of the asset kit page.
     */
    String generatePagePath(String prefix, Resource assetKit);
}
