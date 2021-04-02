---
layout: development-page
title: Miscellaneous 
---

## Customizing the Asset Placeholder

Throughout the Asset Share Commons project a default Asset Placeholder is used if a valid dam:Asset cannot be resolved. This is quite common when authoring Asset Share Commons pages and is useful to preview how metadata/renditions will be rendered. It is recommended to create a project-specific placeholder asset to be used. The Placeholder asset functionality only occurs in the Author environment. In the Publish environment if an Asset can not be resolved a 404 error code will be thrown.

* Example Placeholder asset: `/apps/asset-share-commons/resources/placeholder.jpeg`. 

Since the Placeholder assets only get rendered in the Author environment it is ok to save them beneath the project's `/apps` folder. 

***NOTE** the Placeholder Asset should not be confused with a **Not Found** thumbnail. Several components like the [Search Results component](../../search/results) and the [Image component](../../details/image) allow an author to set a Not Found thumbnail to be used if the current asset does not have a valid thumbnail. The Not Found asset **must** be in the DAM or publicly accessible (should never be beneath `/apps`).
