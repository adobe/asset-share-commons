---
layout: content-page
title: v1.7.0
---

### Fixed
- 0163: User menu disappears when the profile is aleady loaded in previeous requests
- 0340/0358: Corrected URL escaping to handle paths/assets file names using extended and unusual characters.
- 0381: Fixed updating of dynamic service references to multiple share services

### Changed
- 0359: Expanded org.apache.sling.xss to [1.2.0,3) to support AEM 6.5 (uses version 2.0.1) and removed unneeded legacy acom.adobe.acs.commons.email;resolution:=optional import.
- 0374: Added ability to add extra or blacklist Metadata Properties from the Metadata Properties DataSource via OSGi configuration
- 0376: Replaced use of com.adobe.cq.commerce.common.ValueMapDecorator with org.apache.sling.api.wrappers.ValueMapDecorator
- 0378: Date range filter includes the end date (evaluated at 12:59:59PM)

### Added
- 0366: Use sharer email as Reply-To when sharing assets via email
- 0371: Added Horizontal Masonry Card results.
- 0265: Added Freeform-text search component
