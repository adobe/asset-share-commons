---
layout: content-page
title: v1.8.0
---

### Added
- 0345: Asset Rendition Dispatcher framework allowing for named, cache-able renditions.
- 0392: Dialog support to allow selection of Rendition via Rendition Servlet
- 0393: Added max size configuration for AssetDownloadServlet and UI check for end user downloads.
- 0395: Support for parameterized Computed Properties.
- 0398: Added JDK11 support and updated travis to handle JDK8 and JDK11.
- 0418: Added codecov support for travis builds.

### Fixed
- 0388: Corrected spelling of Boolean in dialog value.
- 0390: Fixed issue where initial values from query parameters were not respected in Search / Sort component's HTL.
- 0400: InternalRedirectRenditionDispatcherImpl now supports asset paths with spaces.
- 0412: Search Results dialog not opening due to MetadataSchemaPropertiesImpl throws NPE when OSGi properties not configured
- 0421: AssetRenditions thumbnail sizes on AEM 6.3.x, and null input handling in UrlUtil.

## Important upgrade considerations

#0345 and #0392 leverage the newly added Asset Renditions framework. This results in cache-able renditions HTTP requests.
The new HTTP Request URL pattern looks like `<asset-path>.renditions/<rendition-name>/asset.rendition`, or using a real example:
`/content/dam/images/dog.png.renditions/web/asset.rendition`. Note that the extension of this URL as understood by Web servers or CDN is `.rendition` and not a well known extension type such as `.jpeg` or `.png`.
To ensure the client understands how to treat these requests the Asset Renditions framework sets the appropriate Content-Type HTTP Response header, however this can be lost of the response is cached.

Ensure your [AEM Dispatcher configuration](asset-share-commons/pages/development/asset-renditions/index.html#dispatcher-confi) has been updated per the recommendations.

Also, note that the Details Image and Video components have been updated to use the [Asset Renditions framework](/asset-share-commons/pages/development/asset-renditions/).
Existing Image and Video components can continue operating in the legacy mode (using the dialog switch) however it is recommended to move to the new framework ASAP.


This may require [new Asset Rendition configurations](/asset-share-commons/pages/development/asset-renditions/) which can be deployed via OSGi configuration.