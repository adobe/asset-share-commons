package com.adobe.aem.commons.assetshare.content.renditions;

import com.adobe.aem.commons.assetshare.content.AssetModel;
import org.apache.sling.api.SlingHttpServletRequest;
import org.osgi.annotation.versioning.ConsumerType;

@ConsumerType
/**
 * This interface is used to track the usage of Asset Renditions, and is expected to be implemented by developers using Asset Share Commons.
 * Only a single implementation of this interface (as an OSGi Service) can exist.
 *
 * When implementing the track(..) methods of this interface, ensure they are fast! They are called synchronously and can impact the performance of serving assets.
 * If the call to track is slow, consider emitting an Sling Event instead, and have a separate OSGi Sling Job Listener to listen for the event and perform the tracking.
 * https://sling.apache.org/documentation/bundles/apache-sling-eventing-and-job-handling.html
 *
 * If tracking is not needed for a particular rendition, check for these conditions quickly using the parameters and return immediately.
 */
public interface AssetRenditionTracker {

    /**
     * Track the usage of an Asset Rendition via an HTTP request. This method is NOT used in the context of asynchronous downloads, in this case use the track(..) method below.
     *
     * The user requesting this rendition is available via request.getResourceResolver().getUserID().
     *
     * @param assetRenditionDispatcher the AssetRenditionDispatcher that was used to get the rendition.
     * @param request the SlingHttpServletRequest that was used to get the rendition. The AssetModel can be retrieved via request.adaptTo(AssetModel.class).
     * @param parameters the AssetRenditionParameters that were used to get the rendition. The asset can be retried via parameters.getAsset().
     * @param renditionUri the URI of the rendition that was served. This can be a URL for external renditions, a Sling resolvable relative URL, or an internal JCR resource path.
     */
    void track(AssetRenditionDispatcher assetRenditionDispatcher, SlingHttpServletRequest request, AssetRenditionParameters parameters, String renditionUri);

    /**
     * Track the usage of an Asset Rendition via an asynchronous download. This method is NOT used in the context of HTTP requests (for example Downloads as a zip), in this case use the track(..) method above.
     *
     * @param assetRenditionDispatcher the AssetRenditionDispatcher that was used to get the rendition.
     * @param assetModel the AssetModel that was used to get the rendition.
     * @param parameters the AssetRenditionParameters that were used to get the rendition. The user ID can be retrieved via parameters.getOtherProperties().get("userId", "Unknown").
     * @param renditionUri the URI of the rendition that was served. This can be a URL for external renditions, a Sling resolvable relative URL, or an internal JCR resource path.
     */
    void track(AssetRenditionDispatcher assetRenditionDispatcher, AssetModel assetModel, AssetRenditionParameters parameters, String renditionUri);
}
