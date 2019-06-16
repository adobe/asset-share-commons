---
layout: content-page
title: v1.8.0
---

### Added
- 0345: Asset Rendition Dispatcher framework allowing for named, cacheable renditions.
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