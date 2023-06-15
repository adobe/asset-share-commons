---
layout: content-page
title: Asset Renditions Tracker hook
description: Customizable hook to track Asset Renditions access.
---

Asset Share Commons v3.2.0+ includes a [hook](https://github.com/adobe/asset-share-commons/blob/develop/core/src/main/java/com/adobe/aem/commons/assetshare/content/renditions/AssetRenditionTracker.java) that allows a custom Java implementation to be invoked whenever [Asset Renditions](../asset-renditions/index.md) are served. There are two hook locations:

+ In the AssetRenditionDispatcher's `dispatch(..)` used to deliver assets via HTTP request (ex. direct downloads, details preview, search results, etc.).
+ In the AssetRenditionDispatcher's `getRendition(..)` used to collect asset renditions for download (via the download zips).

It is up to the implementor to decide which asset rendition requests to track in their custom tracker implementation.

## Limitations of Asset Rendition tracking

+ Request must go to AEM since as this traacking is invoked in Java code. 
    + This is often the case in asset shares, as most asset shares require authentication which circumvents caching. If more specialized caching is involved, such as in-memory caching in AEM runtime, then this approach may not work.
+ You must use [Asset Share Commons Asset Renditions](../asset-renditions/index.md), as Asset Renditions are the only point Asset Share Commons can hook into.
+ You must implement the tracking OSGi service yourself, and make sure it is performant, and does not slow down the request processing (especially in the context of HTTP requests). This may require emitting an event about the access request, and processing the recording in an out-of-band job.
+ You may use this in tandem with custom web analytics implementations. Web analytics could be instrumented to track web requests to asset renditions, and this approach could implement tracking only for the download hook.

## Example AssetRenditionTracker implementation

The following example simply logs information about each Asset Rendition `dispatch(..)` or `getRendition(..)` call. The custom implementation must implement the [AssetRenditionsTracker](https://github.com/adobe/asset-share-commons/blob/develop/core/src/main/java/com/adobe/aem/commons/assetshare/content/renditions/AssetRenditionTracker.java) interface, and register as an OSGi service. Typically these implementations only need to be active on AEM Publish service, so a `configurationPolicy` can control this.

```java
package com.examples.assetshare.tracking.impl;

import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionDispatcher;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionParameters;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionTracker;
import org.apache.sling.api.SlingHttpServletRequest;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component(
        service = AssetRenditionTracker.class,
        configurationPolicy = ConfigurationPolicy.REQUIRE // Since we probably only want to enable this on config.publish services.
)
public class AssetRenditionTrackerImpl implements AssetRenditionTracker {
    private static final Logger log = LoggerFactory.getLogger(AssetRenditionTrackerImpl.class);

    @Override
    public void track(AssetRenditionDispatcher assetRenditionDispatcher, SlingHttpServletRequest request, AssetRenditionParameters parameters, String renditionUri) {

        log.debug("Tracking REQUEST asset [ {} ] w/ rendition name [ {} ] at URI [ {} ] using rendition Dispatcher [ {} ] by user [ {} ]", new String[] {
                parameters.getAsset().getPath(),
                parameters.getRenditionName(),
                renditionUri,
                assetRenditionDispatcher.getLabel(),
                request.getResourceResolver().getUserID()
        });

    }

    @Override
    public void track(AssetRenditionDispatcher assetRenditionDispatcher, AssetModel assetModel, AssetRenditionParameters parameters, String renditionUri) {
        log.debug("Tracking DOWNLOAD asset [ {} ] w/ rendition name [ {} ] at URI [ {} ] using rendition Dispatcher [ {} ] by user [ {} ]", new String[] {
                parameters.getAsset().getPath(),
                parameters.getRenditionName(),
                renditionUri,
                assetRenditionDispatcher.getLabel(),
                parameters.getOtherProperties().get("userId", "Unknown")
        });
    }
}
```

Enable the custom `AssetRenditionTracker` implementation by creating an empty OSGi configuration in the desired runmode (usually `config.publish`).

`/ui.config//config.publish/com.examples.assetshare.tracking.impl.AssetRenditionTrackerImpl.cfg.json`

```json
{
    "README": "This OSGi configuration is required to exist for the custom AsAssetRenditionTrackerImpl OSGi component to be enabled on AEM Publish."
}

```


## Adding tracking to a custom Asset Rendition Dispatcher.

The [AssetRenditionTracker](https://github.com/adobe/asset-share-commons/blob/develop/core/src/main/java/com/adobe/aem/commons/assetshare/content/renditions/AssetRenditionTracker.java) hook can be added to custom `AssetRenditionDispatchers`. To do this, simply:

1. Add an optional `@Reference` to the `AssetRenditionTracker` OSGi service.

    ```java
    import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionTracker;
    ...
     @Reference(
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            policyOption = ReferencePolicyOption.GREEDY
    )
    private volatile AssetRenditionTracker assetRenditionTracker;
    ```

    For example, see [ExternalRedirectRenditionDispatcherImpl.java](https://github.com/adobe/asset-share-commons/blob/develop/core/src/main/java/com/adobe/aem/commons/assetshare/content/renditions/impl/dispatchers/ExternalRedirectRenditionDispatcherImpl.java#L76-L81)

2. In the custom AssetRenditionDispatcher's `dispatch(..)`, call the `track(..)` that accepts a `SlingHttpServletRequest` object as a parameter, immediately before dispatching the rendition.

    ```java
    if (assetRenditionTracker != null) {
        assetRenditionTracker.track(this, request, parameters, renditionRedirect);
    }
    ```

    For example, see [ExternalRedirectRenditionDispatcherImpl.java](https://github.com/adobe/asset-share-commons/blob/develop/core/src/main/java/com/adobe/aem/commons/assetshare/content/renditions/impl/dispatchers/ExternalRedirectRenditionDispatcherImpl.java#L76-L81)

3. In the custom AssetRenditionDispatcher's `getRendition(..)`, call the `track(..)` that accepts an `AssetModel` object as a parameter, immediately before dispatching the rendition.

    ```java
    if (assetRenditionTracker != null) {
        assetRenditionTracker.track(this, assetModel, parameters, renditionRedirect);
    }
    ```

    For example, see [ExternalRedirectRenditionDispatcherImpl.java](https://github.com/adobe/asset-share-commons/blob/develop/core/src/main/java/com/adobe/aem/commons/assetshare/content/renditions/impl/dispatchers/ExternalRedirectRenditionDispatcherImpl.java#L76-L81)

