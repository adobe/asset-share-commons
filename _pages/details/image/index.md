---
layout: component-page
title: Image
component-group: "asset details"
---

![Image component - Center](./images/main.png)

The Image component displays an image rendition for a given Asset.

## Authoring

Authors have several configurations available to choose which asset rendition is displayed.

### Dialog / Configuration

![Image dialog](./images/dialog.png)

#### Rendition

An Image rendition that can be picked from a dropdown that lists all Asset Rendition Names that are registered to Asset Rendition Dispatcher OSGi configurations with `type = image`. These values are exposed via [Asset Share Commons' Asset Rendition framework](/asset-share-commons/pages/development/asset-renditions/).

OOTB the available image renditions are available:

* Original (maps to static original rendition)
* Web (maps to the static cq5dam.web.x.x.x)

#### Fallback Image Src

An image can be selected to be used if no rendition for a given asset is found. The fallback image src will be used directly to populate the `src` attribute. Even if the fallback image is an asset in the DAM that has a rendition, only the path will be used.

#### Maximum Image Height (in pixels)

If set, an inline style attribute `max-height` will be set to explicitly limit the height the image can grow. If left blank the image will render as is.


### Legacy support

In Asset Share Commons v1.8.0 the dialog was updated to use the Renditions provided by the [Asset Share Commons' Asset Rendition framework](/asset-share-commons/pages/development/asset-renditions/).

Previous instances of Image components will continue to work and allow configurations in both modes using a Legacy Mode toggle switch.

Any NEW instance of the Image component ONLY supports the Asset Renditions.

#### Migrating off legacy

It is highly recommended to move to the new Asset Rendition based approached at your earliest convenience. The easiest way to migrate is to:

1. Define and configure any required AssetRenditionDispatcher configurations, and promote through QA -> Stage as needed.
2. Update AEM Dispatcher configuration to support Asset Renditions.
3. Deploy Asset Share Commons v1.8.0+ and any dependency configurations to production environment.
3. On AEM Author, navigate to the page with the Image component.
4. Enter Edit mode
5. Add a NEW Image component above the Image component using the legacy configuration.
6. Configure the NEW Image component as needed, and verify it is surfacing the expected rendition.
7. Delete the previous Image component using the legacy configuration.
8. Double-check everything works as expected on AEM Author.
9. Publish the page.
10. Double-check the page renders properly on AEM Publish
11. Rinse, repeat for all instances of the Image component.

## Technical details

* **Component**: `/apps/asset-share-commons/components/details/image`
* **Sling Model**: `com.adobe.aem.commons.assetshare.components.details.impl.ImageImpl`

**Dialog Data Sources**

* **Renditions**: [`com.adobe.aem.commons.assetshare.content.impl.datasources.AssetRenditionsDataSource`](https://github.com/Adobe-Marketing-Cloud/asset-share-commons/blob/develop/core/src/main/java/com/adobe/aem/commons/assetshare/content/impl/datasources/AssetRenditionsDataSource.java) with a filter of `allowedAssetRenditionTypes="[image]"`
