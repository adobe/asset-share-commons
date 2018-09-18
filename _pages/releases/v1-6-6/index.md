---
layout: content-page
title: v1.6.6
---

**Please use this instead of Asset Share Commons 1.6.4, as v1.6.4 broke backwards compatibility and may cause issues upgrading Asset Share Commons moving forward**

### Fixed
- 0275: Revert inclusion of Core Components 2.1.0 as it breaks compatability with AEM 6.3 SP1
- 0276: Corrected resource injection strategy in SearchConfigImpl that resulted in the model being instantiatable on 6.3.x


## Important upgrade considerations

- Rolls back dependency on Core Components v2.1.0 to the AEM 6.3 SP1 compatible Core Components 1.1.0.
