---
layout: content-page
title: v1.3.0
---

## v1.3.0 release notes

### Fixed
- 0131: Fixed ContextHub eventing condition that cause the user menu profile to act as "anonymous" on the first page via by an auth'd user.

### Added
- 0128: Path Filter search component.
- 0130: Added auto-search capabilities to search predicate components.
- 0134: Allow multiple ShareServices to be registered and allow each to accept the request.

## Important upgrade considerations

* Clear your browser cache (JavaScript).
* To leverage auto-search on existing Asset Shares, you must edit the components (Path, Property, Tags, Date, Sort) and check the "Auto-Search on Change" checkbox, and publish the changes.  