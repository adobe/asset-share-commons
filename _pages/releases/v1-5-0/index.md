---
layout: content-page
title: v1.5.0
---

## v1.5.0 release notes


## [v1.5.0]

### Added
- 0034/0046: Dynamic Media Download modal and image presets datasource
- 0147: Asset Details Video component added to provide in-page video playback on Asset Details pages.

### Fixed

- 0126: Updated the include of AEM Responsive Grid's grid_base.less to a singular file copied into the Asset Share Commons codebase to support AEM 6.3.1 and AEM 6.4 in the same package.   
- 0156: Asset cart does not populate correctly in AEM 6.4.0.
- 0149: The Metadata Properties datasource now includes multi-value text widgets defined on the AEM Assets Metadata Schemas.
- 0152: Fixed issue with leaking resource resolver in QueryBuilder APIs. This was previously thought to be fixed in v1.2.2 #0103. Note this fix is also back-ported to v1.1.4. 


## Important upgrade considerations

* This is the **first** Asset Share Commons release that supports AEM 6.4.
* This release continues to support AEM 6.3 SP1+.
         