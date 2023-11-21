---
layout: doc-page
title: Upgrading to Asset Share Commons
---

This is an abridged guide to upgrading from Asset Share Commons 1.x to Asset Share Commons 2.0.0+ (please upgrade to latest release, {{ site.data.asc.version }}).

Details of this can be found throughout the rest of the Getting Started section.

## Project-level updates

The following should be updated at a project level by the development team.

1. Ensure you are on __AEM as a Cloud Service__ or __AEM 6.5 SP7+__.
1. Ensure your custom project that includes Asset Share Commons is updated to the latest [AEM project structure](https://experienceleague.adobe.com/docs/experience-manager-cloud-service/implementing/developing/aem-project-content-package-structure.html).
    * If you were deploying Asset Share Commons source code directly, you'll want to re-think that now, and move to a custom, wrapping project.
1. [Embed Asset Share Commons {{ site.data.asc.version }} `all` artifact](../../development/deploying) in your AEM Maven Project's `all` project.
    * Make sure when deploying to __AEM as a Cloud Service__ make sure the `<classifier>cloud</classifier>` is used in the Maven dependency.
1. [Add a maven dependency on Asset Share Commons {{ site.data.asc.version }} `core` artifact](../../development/deploying) in your AEM Maven Project's `core`  project.
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

## More upgrade details 

When deploying Asset Share Commons (ASC) to AEM as a Cloud Service, especially transitioning from an existing AEM 6.5 ASC setup, it's crucial to understand how ASC is designed to work with both environments. 

1. **ASC Version Compatibility:**
   - Asset Share Commons 2.0.0 was the first version compatible with AEM as a Cloud Service. However, all subsequent versions, including the latest ({{ site.data.asc.version }}), are also compatible and offer more bug fixes and enhancements. Prefer using the latest version ({{ site.data.asc.version }}) as it includes the most bug fixes.

2. **Unified ASC Codebase with Dual Compatibility:**
   - ASC employs a single codebase with distinct branches for AEM 6.5 ("classic") and AEM as a Cloud Service ("cloud"). The functionalities toggle based on the AEM version in use. Because of this, it is important to specify the correct package flavor when deploying ASC to AEM as a Cloud Service.

3. **Selecting the Right Package Flavor:**
   - From version 2.0.0 onwards, ASC provides two package flavors with each release: "Cloud" and "Classic". 
   - For AEM as a Cloud Service, use the "Cloud" version (e.g., `asset-share-commons.all-{{ site.data.asc.version }}-cloud.zip`).
   - For AEM 6.5, use the "Classic" version (e.g., `asset-share-commons.all-{{ site.data.asc.version }}.zip`).

4. **Deployment via Maven Dependencies:**
   - Ensure the use of the `cloud` classifier in your `all` package Maven dependency for AEM as a Cloud Service deployment:
     ```xml
     <dependencies>
         ...
         <dependency>
             <groupId>com.adobe.aem.commons</groupId>
             <artifactId>assetshare.all</artifactId>
             <version>{{ site.data.asc.version }}</version>
             <classifier>cloud</classifier> <!-- Critical for Cloud Service -->
             <type>zip</type>
         </dependency>
         ...
     </dependency>
     ```
   - Omitting the `cloud` classifier defaults to the "Classic" version, which lacks certain Cloud Service-safe features (e.g., the AEM as a Cloud Service-specific download functionality).

5. **Expect Changes in Functionality:**
   - Transitioning to the "Cloud" package means some features will change. Some functionalities will be replaced (like download functionality), while others might require reconfiguration.
   - It is recommended the initial deployment of the "Cloud" package be done in a non-production environment to ensure the expected behavior, and understand what configurations/reconfigurations are required.
   - That said, the amount of reconfiguration required should be minimal.

6. **Reviewing Code Differences:**
   - Understand the effectged areas by reviewing the "classic" and "cloud" toggles in the ASC repository:
     - [Classic toggles](https://github.com/search?q=repo%3Aadobe%2Fasset-share-commons%20classic&type=code)
     - [Cloud toggles](https://github.com/search?q=repo%3Aadobe%2Fasset-share-commons+%22cloud-ready%22&type=code)

Hopefully this helps you understand the transition process better, and the reasoning behind it.