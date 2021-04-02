---
layout: development-page
title: Templates
---

Creating project-specific editable templates is the recommended way of integrating Asset Share Commons into a project. The [ui.content](https://github.com/Adobe-Marketing-Cloud/asset-share-commons/releases/tag/asset-share-commons.ui.content-1.0.0) package includes examples of the templates and template-types needed under `/conf/asset-share-commons/settings/wcm/`. *Note that two sets of templates are included in ui.content to support both the Light and Dark theme examples. A project implementation most likely only needs a single set. [See for project-specific templates and template types](https://github.com/godanny86/sample-assetshare/tree/master/ui.apps/src/main/content/jcr_root/conf/sample-assetshare/settings/wcm)

## Template Types

When setting up a new project it is easiest to copy the template types from `/conf/asset-share-commons/settings/wcm/template-types` in to the project's `/conf` folder.

**Search Template Type**

* Required
* `sling:resourceType` : `asset-share-commons/components/structure/search-page`
* Example in `ui.content` at  `/conf/asset-share-commons/settings/wcm/template-types/search-page`
* For Search Pages, includes page properties tab for search configuration.

**Asset Details Template Type**

* Required
* `sling:resourceType` : `asset-share-commons/components/structure/details-page`
* Example in `ui.content` at `/conf/asset-share-commons/settings/wcm/template-types/details-page`
* For Asset Detail pages, includes page properties tab for detail page configuration.

**Empty Template Type**

* Required
* `sling:resourceType` : `asset-share-commons/components/structure/page`
* Example in `ui.content` at `/conf/asset-share-commons/settings/wcm/template-types/empty-page`
* For action modal pages, license agreement terms and conditions and any other blank content pages. 

**Rail Template Type**

* (Optional)
* `sling:resourceType` : `asset-share-commons/components/structure/page`
* Example in `ui.content` at `/conf/asset-share-commons/settings/wcm/template-types/rail-page`
* For content pages with a rail that add supporting content to the Asset Share portal. 

## Templates

Due to the complex structure of Editable Templates it is recommended to create each project specific Template using the Template Editor in the AEM UI. 

Templates found beneath `/conf/asset-share-commons/settings/wcm/templates` should be used as a reference but should NOT be copied directly into a project's `/conf` directory. 

A Structure policy should be created to include *Asset Share Commons - Structure* components (Header, Footer, User menu...) on each Template. Allowed Components policies should also be configured based on the template. Lastly the Page Design for each Template needs to be configured to point to a Semantic UI theme client library (preferably a project specific one). [

**Search Template**

* `cq:templateType` : `	
/conf/<project>/settings/wcm/template-types/search-page`
* example: `/conf/asset-share-commons/settings/wcm/templates/search-template`
* Structure Policy: *Asset Share Commons - Structure*, *Layout Container*
* Allowed Components: *Asset Share Commons - Content*, *Asset Share Commons - Search*

**Details Template**

* `cq:templateType` : `	
/conf/<project>/settings/wcm/template-types/details-page`
* example: `/conf/asset-share-commons/settings/wcm/templates/details-template`
* Structure Policy: *Asset Share Commons - Structure*, *Layout Container*
* Allowed Components: *Asset Share Commons - Content*, *Asset Share Commons - Details*

**Action Template**

* `cq:templateType` : `	
/conf/<project>/settings/wcm/template-types/empty-page`
* example: `/conf/asset-share-commons/settings/wcm/templates/action-template`
* Structure Policy: *Asset Share Commons - Structure*, *Layout Container*
* Allowed Components: *Asset Share Commons - Modals*

**Content Rail Template**

* `cq:templateType` : `	
/conf/<project>/settings/wcm/template-types/rail-page`
* example: `/conf/asset-share-commons/settings/wcm/templates/content-rail-template`
* Structure Policy: *Asset Share Commons - Structure*, *Layout Container*
* Allowed Components: *Asset Share Commons - Content*

**Empty Template**

* `cq:templateType` : `	
/conf/<project>/settings/wcm/template-types/empty-page`
* example: `/conf/asset-share-commons/settings/wcm/templates/empty-template`
* Structure Policy: *Asset Share Commons - Structure*, *Layout Container*
* Allowed Components: *Asset Share Commons - Content*
