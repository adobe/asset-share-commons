---
layout: content-page
title: v1.6.10
---

### Fixed
- 0259: Fixed issue with the Statistics component misreporting how many more results are available.
- 0301: Files having special character in the filename. Download, Share and Add to Cart do not work.
- 0307: Fixed issue with Details page Image component's fallback is not used for non-image assets.
- 0308: Fixed issue where unsupported (by the browser) image asset types (ex. DFX) are used for image display in browser (thumbs/preview) instead of placeholder.
- 0311: Empty metadata fields fail to hide or display emptyText.

### Changes
- 0294: Changes (JS) `AssetShare.Navigation.goToTop(..)` to avoid using window.location.hash which pushes state to browser's history and prevents use of the browser's back button.