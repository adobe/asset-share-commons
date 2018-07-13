---
layout: content-page
title: v1.6.0
---

### Added
- 0208: Addition of Smart Tags Computed Property and support of Smart Tags in the Tags Asset Details Component.
- 0184: Added Search Predicates framework and provided OOTB implementations for: Exclude Content Fragments, Exclude Expired Assets, Exclude Sub-assets.
- 0182: Added resource providers for Search and Asset Details pages that warn about mis-configurations of Asset Share Commons in AEM Author.
- 0191: Support alphabetical or natural ordering of Tags in in the Tags search predicate. Fixed issues with Source options in dialog as well.

### Fixed
- 0204: For mobile and tablet view, the filter rail should slide from left.
- 0195: Search does not work in IE11 - Missing findIndex() & find() methods.

## Important upgrade considerations

- If Hidden Predicates are being used to limit search queries from returning: Sub-assets, Content fragments and Expired assets, these can now be restricted using the Search Predicate Framework introduced in #184.

