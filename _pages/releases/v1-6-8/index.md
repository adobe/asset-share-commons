---
layout: content-page
title: v1.6.8
---

### Fixed
- 0283: AssetDetails404Servlet uses sendError(..) to set 404 status, allowing it to work with Sling Error Handlers.
- 0285: Modal DOM elements are now removed when then modal is hidden.
- 0288: AssetDetails404Servlet handles UUID based asset details pages.

### Changed
- 0290: Modals emit JS event when they are shown.
