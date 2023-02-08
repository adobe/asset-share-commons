package com.adobe.aem.commons.assetshare.util.assetkit;

import org.apache.sling.api.resource.Resource;
import org.osgi.annotation.versioning.ConsumerType;

@ConsumerType
public interface PagePathGenerator {
    String getName();

    default String getId() { return this.getClass().getName(); }
    String generatePagePath(String prefix, Resource assetKit);
}
