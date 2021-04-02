---
layout: doc-page
title: Upgrade to Asset Share Commons 2.0
---

This is an abridged guide to upgrading from Asset Share Commons 1.x to Asset Share Commons 2.0.

Details of this can be found through the rest of the Getting Started section.

## Project-level updates

The following should be updated at a project level by the development team.

1. Ensure you are on __AEM as a Cloud Service__ or __AEM 6.5 SP7+__.
1. Ensure your custom project that includes Asset Share Commons is updated to the latest [AEM project structure](https://experienceleague.adobe.com/docs/experience-manager-cloud-service/implementing/developing/aem-project-content-package-structure.html).
    * If you were deploying Asset Share Commons source code directly, you'll want to re-think that now, and move to a custom, wrapping project.
1. [Embed Asset Share Commons 2+ `all` artifact](../../development/deploying) in your AEM Maven Project's `all` project.
1. [Add a maven dependency on Asset Share Commons 2+ `core` artifact](../../development/deploying) in your AEM Maven Project's `core`  project.
1. Define [Asset Rendition](../../development/asset-renditions) OSGi configurations for the asset renditions you use in your your AEM Maven Project's `ui.config` project's `config` folder.
1. Update your AEM Maven Project's `dispatcher` project with the required [filters](https://github.com/Adobe-Marketing-Cloud/asset-share-commons/blob/develop/dispatcher/src/conf.dispatcher.d/filters/filters.any) and [cache-able HTTP request headers](https://github.com/Adobe-Marketing-Cloud/asset-share-commons/blob/develop/dispatcher/src/conf.dispatcher.d/available_farms/asset-share-commons.farm#L92-L95).

## Content authoring updates

The following should be updated in AEM as apart of Asset Share Commons configuration, after the above project-level updates have been deployed.

1. Update the Search Page's > Page Properties > [Messages](../../structure/messages) and ensure all desired message text is filled out.
1. Update the Actions > Download > [Download Modal](../../actions/download) component as desired, selecting the [Asset Renditions](../development/asset-renditions) defined by the development team (via OSGi configurations).
1. Create and author the Actions > Downloads > [Downloads Modal](../../actions/downloads) component as desired.
    * _Only applies to AEM as a Cloud Service_
1. Update the Asset Details pages' [Renditions](../details/renditions), [Image](../details/image), and [Video](../details/video) components to use [Asset Renditions](../development/asset-renditions).
1. Don't forget to Publish all your changes!
