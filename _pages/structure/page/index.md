---
layout: component-page
title: Base Page
component-group: ".hidden"
---

This is the base page component for all pages/templates in Asset Share Commons. It inherits from the [Core Component Page](https://github.com/Adobe-Marketing-Cloud/aem-core-wcm-components/tree/master/content/src/content/jcr_root/apps/core/wcm/components/page/v1/page). It is designed to be used with AEM editable templates.

## Technical Details

* **Sling Resource Type**: `asset-share-commons/components/structure/page`
* **Sling Resource Super Type**: `core/wcm/components/page/v1/page`


### Client Library Includes

**customheaderlibs.html**

* `asset-share-commons.dependencies`
    * JavaScript and CSS
* `asset-share-commons.base`
    * JavaScript and CSS
* `asset-share-commons.author`
     * JavaScript and CSS only included in the author environment

**customfooterlibs.html**

* `asset-share-commons.base`
    * JavaScript only

### Other details

* **messages.html** - includes messages component on all pages. Used for displaying add to cart behavior.
* **partial.html** - used to render modals for Cart, Download, Share.