---
layout: content-page
title: v1.6.4
---

**Please use  Asset Share Commons 1.6.,6 instead of this version, as this version broke backwards compatibility and may cause issues upgrading Asset Share Commons moving forward**


### Fixed
- 0260: Resolves issue with offset not being set.
- 0255: Removed need for "generic" Sort By and Sort Direction labels; added intelligence to get default values from Search Results component.
- 0254: HTTP query param sort parameters are not reflected in Sort component
- 0249: Fixed issue when Search Statistics (or an other component that uses Search model) is placed before the Search components, resutling in 0 results.
- 0248: Issue with HTTP parameter QB groups and server-side provided (group_3 would mix in with server-side paths)
- 0227: Sites editor is missing workflow status information
- 0237: Fixes issue with ContextHub being unloaded after a Form submissions via modals.
- 0240: Fixed issue with submitted date-range search values lagging behind actual value by one submission.
- 0231: metadataFieldTypes does not filter data source for Date Range and Tag Filters
- ????: Updated included Core Components to v2.1.0

## Important upgrade considerations

- The Sort Predicate labels are no longer used. You should ensure that the default sort order specified in the Search Results component has a matching entry in the Sort component (assuming the Sort component is used).
- Core Components v2.1.0 are now included in Asset Share Commons 1.6.4.
