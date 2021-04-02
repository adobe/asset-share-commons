---
layout: doc-page
title: Set Up
---

## Asset Share Commons 2.x

* [AEM as a Cloud Service](https://experienceleague.adobe.com/docs/experience-manager-learn/cloud-service/overview.html) or [AEM 6.5 SP7](https://helpx.adobe.com/experience-manager/6-5/release-notes.html) or greater
* [AEM WCM Core Components 2.14.0](https://github.com/adobe/aem-core-wcm-components/releases) or greater

### Dispatcher 

In order to user Asset Share Commons 2.x, you must create your own AEM Dispatcher project, and copy the rules ([filters](https://github.com/Adobe-Marketing-Cloud/asset-share-commons/blob/develop/dispatcher/src/conf.dispatcher.d/filters/filters.any) and [cache-able HTTP request headers](https://github.com/Adobe-Marketing-Cloud/asset-share-commons/blob/develop/dispatcher/src/conf.dispatcher.d/available_farms/asset-share-commons.farm#L92-L95)) defined in the project's [dispatcher sub-project](https://github.com/Adobe-Marketing-Cloud/asset-share-commons/tree/master/dispatcher).

### Other setup considerations

* AEM as a Cloud Services requires an Adobe Support ticket to open up SMTP/E-mail. In order to use emailing Sharing in Asset Share Commons, you must make this request before configuring your CQ Mailer OSGi configuration. 
* Only [Asset Renditions](../../development/asset-renditions) are supported. 
  * This requires the [Asset Renditions](../../development/asset-renditions) to be deployed as OSGi configurations (mapping the "name" to the actual rendition).
  * Download of Dynamic Media renditions is supported, via ExternalRedirectAssetRenditionDispatcher OSGi configurations.
* AEM as a Cloud Services uses an async download mechanism. Please ensure the following are configured accordingly:
  * Modals > [Download](../../actions/download)
  * Modals > [Downloads](../../actions/downloads) (lists the async downloads)
  * Search Page > Page Properties > Asset Share > [Messages Configuration > Archive Added To Downloads](../../structure/messages)
* ContextHub is no longer used for managing the Asset Share Commons cart. ContentHub still works and can be loaded if a valid ContextHub configuration path is provided in the usual Page Properties (`Personalization > ContextHub Configurations > ContextHub Path`).

### How to use

Asset Share Commons should be included as a package dependency in *YOUR* project. Asset Share Commons should not be directly installed to AEM, with the expectation of trying it out.

A `ui.content.sample` AEM content package is provided with a "build out" of an asset share experience, however this package is *ONLY* a sample, and should not be installed on production, but rather used to make it easy to try Asset Share Commons out.  You can copy structures from this project to your project's `ui.content` packages as it makes sense for your use cases, however never depend on anything in `ui.content.sample` directly.

## Asset Share Commons 1.x

* [AEM 6.5](https://helpx.adobe.com/experience-manager/6-5/release-notes.html)
* [AEM 6.4](https://helpx.adobe.com/experience-manager/6-4/release-notes.html)
* [AEM 6.3 Service Pack 1 (6.3.1.0)](https://docs.adobe.com/docs/en/aem/6-3/release-notes/sp1.html)
* [ACS AEM Commons 3.11.0+](https://github.com/Adobe-Consulting-Services/acs-aem-commons/releases)
    * Starting with v1.1.0, ACS AEM Commons is no longer a dependency.
    * Prior to v1.1.0 generally optional, but required for E-mail Sharing. 

### Asset Share Commons Downloads

* [https://github.com/Adobe-Marketing-Cloud/asset-share-commons/releases](https://github.com/Adobe-Marketing-Cloud/asset-share-commons/releases)

Download the latest available versions of the **ui.apps** and **ui.content** packages.

### Dispatcher

Ensure AEM Dispatcher allows the following URL paths/patterns:

* `HTTP GET /libs/granite/security/currentuser.json?nocache=<time-in-ms>` on page loads to retrieve the current user. This URI must be allowed via AEM Dispatcher.  
* `HTTP GET /home/users/.../<user>.infinity.json` on page loads to retrieve the current user. This URI must be allowed via AEM Dispatcher. This is a standard request made by the OOTB ContextHub Profile store.
* `HTTP GET /content/dam/...<asset>.renditions/...` which is used to serve asset renditions via the Asset Renditions framework.
* Enable caching of the following HTTP Response Headers
   * `Content-Disposition`
   * `Content-Type`
   * `Content-Length`

Example `dispatcher.any` rules

```
/filter {
  ...
   # ContextHub
   /0201 { /type "allow" /method "GET" /path "/home/users/*" /extension '(json|png)' }

   # Current user
   # No longer needed to be added explicitly as this should be in the OOTB allow rules
   # /0202 { /type "allow" /url "/libs/granite/security/currentuser.json" }

   # ContextHub page data
   /0202 { /type "allow" /method "GET" /path "/content/*" /selectors "pagedata" /extension "json" }

   # Asset Renditions requests
   /0203 { /type "allow" /method "GET" /path "/content/dam/*" /extension "renditions" }

   # Asset Rendition downloads
   /0204 { /type "allow" /method "POST" /path "/content/*" /extension "zip" }
}

/headers {
    ...
    "Content-Disposition"
    "Content-Type"
    "Content-Length"
    ...
}
```

### Production Setup

Best practices of deploying an AEM Sites project in production should be followed when deploying Asset Share Commons. This includes:

1. Installing AEM with **nosamplecontent** runmode to use [Production Ready Mode](https://helpx.adobe.com/experience-manager/6-5/sites/administering/using/production-ready.html).
2. Review and implement the [Security Checklist](https://helpx.adobe.com/experience-manager/6-5/sites/administering/using/security-checklist.html) for deployments.
3. Review the Asset Download Servlet settings as part of the [Download component](../../actions/download).

## Set Up Video

<a href="https://helpx.adobe.com/experience-manager/kt/assets/using/asset-share-commons-article-understand/asset-share-commons-feature-video-setup.html"><img src="./images/video.png" alt="Set up video - center"/></a>

Video walk-through of Asset Share Commons hosted on helpx.adobe.com.

 




