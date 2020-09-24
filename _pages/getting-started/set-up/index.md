---
layout: doc-page
title: Set Up
---

## Pre-requisites

* [AEM 6.5](https://helpx.adobe.com/experience-manager/6-5/release-notes.html)
* [AEM 6.4](https://helpx.adobe.com/experience-manager/6-4/release-notes.html)
* [AEM 6.3 Service Pack 1 (6.3.1.0)](https://docs.adobe.com/docs/en/aem/6-3/release-notes/sp1.html)
* [ACS AEM Commons 3.11.0+](https://github.com/Adobe-Consulting-Services/acs-aem-commons/releases)
    * Starting with v1.1.0, ACS AEM Commons is no longer a dependency.
    * Prior to v1.1.0 generally optional, but required for E-mail Sharing. 

## Asset Share Commons Downloads

* [https://github.com/Adobe-Marketing-Cloud/asset-share-commons/releases](https://github.com/Adobe-Marketing-Cloud/asset-share-commons/releases)

Download the latest available versions of the **ui.apps** and **ui.content** packages.

## Dispatcher

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

  /0201 { /type "allow" /method "GET" /url "/home/users/*/profile/*" } # enable retrieving user profile
  /0202 { /type "allow" /method "GET" /url "/home/users/*.infinity.json" } # enable retrieving user details
  /0203 { /type "allow" /method "GET" /path "/content/dam" /extension "renditions" } # enable asset rendition serving
}

/headers {
    ...
    "Content-Disposition"
    "Content-Type"
    "Content-Length"
    ...
}
```

## Production Setup

Best practices of deploying an AEM Sites project in production should be followed when deploying Asset Share Commons. This includes:

1. Installing AEM with **nosamplecontent** runmode to use [Production Ready Mode](https://helpx.adobe.com/experience-manager/6-5/sites/administering/using/production-ready.html).
2. Review and implement the [Security Checklist](https://helpx.adobe.com/experience-manager/6-5/sites/administering/using/security-checklist.html) for deployments.
3. Review the Asset Download Servlet settings as part of the [Download component](../../actions/download).

## Set Up Video

<a href="https://helpx.adobe.com/experience-manager/kt/assets/using/asset-share-commons-article-understand/asset-share-commons-feature-video-setup.html"><img src="./images/video.png" alt="Set up video - center"/></a>

Video walk-through of Asset Share Commons hosted on helpx.adobe.com.

 




