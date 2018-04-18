---
layout: content-page
title: v1.5.2
---

## v1.5.2 release notes


## [v1.5.2]

- 0177: Removed cache=true on all Sling Model definitions due to memory leaks.(See [https://issues.apache.org/jira/browse/SLING-7586](https://issues.apache.org/jira/browse/SLING-7586))
- 0168: Fixed issue with the rail rendering in Authoring mode.


## Important upgrade considerations

* 0177 resolves a critical memory leak in Sling Models.
         