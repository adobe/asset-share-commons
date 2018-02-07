---
layout: content-page
title: v1.2.0
---

## v1.2.0 release notes

### Fixed
- 0114: Removed replication status properties from templates and policies.

### Changed
- 0076: Reduced sample video asset file sizes in ui.content project.
- 0101: User Menu's profile information to be driven via ContextHub rather than uncache-able server-side code; Also added a variety of OOTB context hub stores (profile, surfer info, etc.)
- 0108: Updated Download Modal to all for the exclusion of original assets in the download zip.
- 0113: Align cards to left in search results.

## Important upgrade considerations

The user menu has changed to use ContextHub to manage the anonymous vs authenticated state in the user menu component.

* It maybe necessary to clear the browser cache to reload changed JavaScript.
* An AJAX HTTP GET is made to `/libs/granite/security/currentuser.json?nocache=<time-in-ms>` on page loads to retrieve the current user. This URI must be allowed via AEM Dispatcher.  