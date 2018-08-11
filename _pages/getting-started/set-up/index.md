---
layout: doc-page
title: Set Up
---

## Pre-requisites

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


## Set Up Video

<a href="https://helpx.adobe.com/experience-manager/kt/assets/using/asset-share-commons-article-understand/asset-share-commons-feature-video-setup.html"><img src="./images/video.png" alt="Set up video - center"/></a>

Video walk-through of Asset Share Commons hosted on helpx.adobe.com.

 




