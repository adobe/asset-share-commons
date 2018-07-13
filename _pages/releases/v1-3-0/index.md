---
layout: content-page
title: v1.3.0
---

### Fixed
- 0131: Fixed ContextHub eventing condition that cause the user menu profile to act as "anonymous" on the first page via by an authenticated user.

### Added
- 0128: Introduction of the <a href="{{ site.baseurl }}/pages/search/path">Path Filter Search component</a> that allows for pre-authored path restrictions to be selected by end-users to refine searches.
- 0130: Added auto-search capabilities to Search Components: 
    - <a href="{{ site.baseurl }}/pages/search/property">Property Filter</a>
    - <a href="{{ site.baseurl }}/pages/search/date-range">Date Range Filter</a>
    - <a href="{{ site.baseurl }}/pages/search/tags">Tags Filter</a>
    - <a href="{{ site.baseurl }}/pages/search/path">Path Filter</a>
    - <a href="{{ site.baseurl }}/pages/search/sort">Sort</a>
- 0134: Allow multiple ShareServices to be registered and allow each to accept the request.

## Important upgrade considerations

* Clear your browser cache to ensure new JavaScript is loaded; as 0130 and 0131 introduced new JavaScript.
* To leverage Auto-Search (0130) on existing Asset Share deployments, you must edit (autho) the components (Path, Property, Tags, Date, Sort) and check the "Auto-Search on Change" checkbox, and publish the changes. Dragging NEW components on will default the Auto-Search on Change state to true.  