---
layout: doc-page
title: Performance Considerations
---

> **ALWAYS TEST ASSET SHARE COMMONS DEPLOYMENTS AGAINST REAL, REPRESENTATIVE CONTENT BEFORE GOING LIVE!**

* Typically, the initial state content is the most expensive as it contains the least number of restrictions (restrictions tend to increase query speed.)
* AEM 6.3 by default will automatically abort queries that traverse more than 100,000 nodes.
* All the usual AEM performance considerations and sizing also applies to Asset Share Commons deployments.

## Caching

* ACS's CSS and JavaScript assets should be cached with long TTLs (even for authenticated users). 
    * If acceptable, leverage [ACS AEM Commons Versioned ClientLibs](https://adobe-consulting-services.github.io/acs-aem-commons/features/versioned-clientlibs.html) to aid in this.
* Asset Share Commons adheres to idempotent GET requests (they don't change state in AEM), thus assuming authentication requirements allow, GET requests can be cached at various levels (CDN / Disaptcher / In Mem).
- Asset Details pages (assuming authentication requirements permit) are highly cache-able. If components such as the [User Menu]({{site.baseurl}}/pages/structure/user-menu) prevent generally cache-able pages from being cached, use [Sling Dynamic Include](https://helpx.adobe.com/experience-manager/kt/platform-repository/using/sling-dynamic-include-technical-video-setup.html) to not cache only the un-cacheable components on the page (by `sling:resourceType`). 

## Authentication and Caching
* Many (most) asset share deployments require authentication making it difficult to cache pages/results at dispatcher. The good news is, this also generally means the number of users accessing the asset share is dramatically less than a public site.
* If authenticated users' views are defined by their assigned AEM user groups (ACLs by user group, which is the recommended permissions scheme), it is possible to use [ACS AEM Commons HTTP Cache](https://adobe-consulting-services.github.io/acs-aem-commons/features/http-cache/index.html) to cache requests per group (and it also supports caching requests w query params).


## Search Optimization

* Query performance typically degrades as the volume of candidate content increases. 
* Queries should always restrict on at least one, and preferably 2 "optimized" (⚡) properties. Making restrictions mandatory requires use of [Hidden Filters]({{site.baseurl}}/pages/search/hidden), (which unfortunately do not enumerate optimized (⚡) properties in the dialog). If mandatory restrictions are using unoptimized (🐢) properties, consult with your development team to augment the Oak index to optimize them. 
* When customizing the [Search page]({{site.baseurl}}/pages/search/) filter components, prefer optimized (⚡) properties.
* Guess Total is forced by Asset Share Commons. Setting the `guessTotal` lower (ie. <1000) is recommended. This is configured on the [Search Results]({{site.baseurl}}/pages/search/results) component.


## AEM Performance Guides and Resources

Review the following materials for general AEM Performance and Query-related best practices.

* [AEM Performance Optimization](https://docs.adobe.com/docs/en/aem/6-3/deploy/configuring/performance.html)
* [Slow Query Troubleshooting](https://docs.adobe.com/docs/en/aem/6-3/develop/best-practices/troubleshooting-slow-queries.html)
* [Asset Performance Guide](https://docs.adobe.com/docs/en/aem/6-3/deploy/configuring/performance/assets-performance-sizing.html)
