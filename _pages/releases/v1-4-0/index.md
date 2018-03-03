---
layout: content-page
title: v1.4.0
---

## v1.4.0 release notes

## [v1.4.0]

### Changed

- 0141: Updated Search results to request the main and rail content to allow for more simpler and more robust use of data-asset-share-update-method.

## Important upgrade considerations

* The change in 0141 has the following impacts:
  * AJAX search results are now requested via an `XHR HTTP GET /content/the/search-page.results.html`
    * Previously a request to the search-results component resource under the page's jcr:content node was used.
  * By default the results include the search page's Main Layout Container and the Rail Layout Container's contents. 
  * Which resources included into the search's AJAX response can be customized by overlaying the `search-page`'s `results.html`.
  * Search components now have a "Search Behavior" tab that allows search components to be re-rendered after each search.
     * This is generally unneeded for the OOTB search components at this time, however is handy for custom dynamic facet filtering components, and the like.
         