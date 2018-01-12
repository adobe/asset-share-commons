---
layout: content-page
title: v1.1.2
---

## v1.1.2 release notes

## [v1.1.2]

### Fixed
- 0102: Fixed the PID for the Asset Share Commons - E-mail Service to the fully qualified class name.
- 0103: Resolve the search results hits using the request's resource resolver to prevent resource leakage.

## Important upgrade considerations

This is a **CRITICAL** bug fix release as it remedies resource resolver leakage via QueryBuilder's hit.getResource() API call.
