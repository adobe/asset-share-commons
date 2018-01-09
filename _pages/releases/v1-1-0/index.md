---
layout: content-page
title: v1.1.0
---

## v1.1.0 release notes

### Fixed
- 0074: Removed unused configurations (originally added for release purposes) from ui.content pom.xml
- 0080: Handle the numbering of predicate search components after insert, and made group numbering mode logical.
- 0086: Moved ACS AEM Commons E-mail Service dependency into the Asset Share Commons project to reduce the service resolution issues as reported by #86. This now removes the dependency on ACS AEM Commons.
- 0096: Corrected wording on Share Action checkbox to reflect new behavior. Only generate groupIds for "ready" components.

### Added
- 0070: Added button to 'Remove from Cart' once an asset has been added
- 0090: Added asset-share-commons.cart.clear JavaScript event when cart is cleared


## Important upgrade considerations

### Add and remove from cart

v1.1.0 brings the ability to remove assets from the cart from either the Search Results or from the Action Buttons component on the Asset Details page.

To align to this pattern, you must:

* Open any/all Search Page Page Properties, and set the `Removed from Cart` message, and message style.
* Edit any Action Buttons components on Asset Details pages, and provide labels for `Add to Cart` as well as `Remove from Cart`.

### E-mail share functionality

Due to OSGi Service resolution issues of the ACS AEM Commons E-mail Service, a copy of the ACS AEM Commons E-mail Service has been copied into Asset Share Commons.

Because of this, Asset Share Commons is no longer dependent on ACS AEM Commons. 

### Search page predicate group ids

v1.1.0 enhances how the Predicate Group Id's are generated. This may change the QueryBuilder group id's of existing search page URLs. 

Starting in v1.1.0 is recommended that any custom search predicate components that require the submission of a QueryBuilder group id, set the following property on the `cq:Component` node.

```
[cq:Component]@genereatePredicateId="true"
```

If this is not set the group id for this component will start at 10000 (which will work, but looks odd).