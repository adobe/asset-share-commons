---
layout: doc-page
title: Troubleshooting
---


## Issue: Pages do not render with styles and functionality on AEM Publish 

### Problem:

Pages render but CSS and JavaScript are not being loaded.

### Cause: 

Lack of read permissions on Editable Templates causing JavaScript and CSS does not load properly.

### Fix: 

This typically occurs due to a lack of `read` permissions (to the `everyone` user for the anonymous use case) on:

* `/conf/xxx/wcm/settings/templates`
* `/conf/xxx/wcm/settings/policies`

Use Sling Repository Initializer to set read permissions on these paths for everyone / anonymous.


### Issue: Unable to Download Assets Larger than 5GB in an Asset Share Commons-based Project

#### Problem:

When using Asset Share Commons (ASC) project on AEM as a Cloud Service, with anonymous access enabled, users may encounter a `503 Service Unavailable` error when downloading assets larger than 5GB. Downloads smaller than 5GB work as expected, but anything above this limit returns an error.

#### Cause:

The issue is related to segmented caching on the Fastly CDN. When an asset download request is made anonymously (i.e., without being logged in via AEM's Sling Authenticator), Fastly’s caching mechanisms prevent the download of assets larger than 5GB.

#### Fix:

As a workaround, you can modify the Vhost file to force the download requests to be non-cacheable by setting the appropriate headers.

```xml
<LocationMatch "^/content/dam.downloadbinaries.json?.*$">
    Header unset Cache-Control
    Header unset Expires
    Header always set Cache-Control "private"
</LocationMatch>
```
