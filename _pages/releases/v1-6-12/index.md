---
layout: content-page
title: v1.6.12
---

### Fixed
- 0326: Removed the sample (non-working) FolderSearchProvider and FolderResult/sImpl from the code base.
- 0333: Fixed issue with the highest Computed Property, by name, not being selected for use (the first to bind was always being used).
- 0335: Fixed a content issue that could result in "Remove From Cart" notification no longer working after saving page properties.
- 0336: Fixed the ASC computed properties to have a default service ranking lower zero (set all to -1).
- 0344: Fixed a property field mapping in the "Share" component dialog where the property name was `./errorText` instead of `./errorTitle`.
- 0337: Fixed intermittent race condition when multiple modals are loaded (License > Download)

### Changed
- 0297: Allow authoring of an the Sort By label value when the sorting property is not present in the Sort component's Sort By options.
- 0313: Reformatted core/pom.xml
- 0322: Email Sharing Externalizer extension to allow custom externalizer domain to be used for publish links.
- 0327: Updated SearchPredicateDataSource and AssetDetailsResolver to GREEDY'ily acquire @References to allow 3rd party service impls to register properly.

### Added
- 0303: Added ability to hide the Apply Filter Toggle control completely (useful for when auto-search on change is enabled everywhere)