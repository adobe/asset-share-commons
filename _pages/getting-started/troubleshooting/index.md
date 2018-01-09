---
layout: doc-page
title: Troubleshooting
---

## Lack of read permission result in incorrect rendering of pages on AEM Publish 

This typically occurs due to a lack of `read` permissions (to the `everyone` user for the anonymous use case) on:

* `/conf/xxx/wcm/settings/templates`
* `/conf/xxx/wcm/settings/policies`
* `/etc/cloudsettings` (contains the ContextHub Cart)

A common symptom is JavaScript and CSS does not load properly.