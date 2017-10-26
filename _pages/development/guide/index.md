---
layout: development-page
title: Development Guide
---

There are many ways that Asset Share Commons could be leveraged on a customer specific project. Detailed below are guidelines for including Asset Share Commons as part of a custom project and instructions on how to extend common areas of an Asset Share implementation.


## Using with Maven

### Add Asset Share Commons as a Dependency

In the `<dependencies>` section of your project's **parent** pom.xml add this:

```
<!-- parent pom.xml -->
<dependencies>
	...
	<dependency>
		<groupId>com.adobe.aem.commons</groupId>
  		<artifactId>assetshare.core</artifactId>
  		<version>1.0.0</version>
   		<scope>provided</scope>
 	</dependency>
 	<dependency>
		<groupId>com.adobe.aem.commons</groupId>
		<artifactId>assetshare.ui.apps</artifactId>
  		<version>1.0.0</version>
   		<scope>provided</scope>
   		<type>content-package</type>
	</dependency>
	...
<dependencies>
```

In the `<dependencies>` section of your project's OSGi bundle module (**core**) pom.xml add this:

```
<!-- core pom.xml -->
<dependencies>
	...
	<dependency>
		<groupId>com.adobe.aem.commons</groupId>
  		<artifactId>assetshare.core</artifactId>
 	</dependency>
	...
<dependencies>
```

In the `<dependencies>` section of your project's content module (**ui.apps**) pom.xml add this:

```
<!-- ui.apps pom.xml -->
<dependencies>
	...
	<dependency>
		<groupId>com.adobe.aem.commons</groupId>
  		<artifactId>assetshare.core</artifactId>
 	</dependency>
 	<dependency>
		<groupId>com.adobe.aem.commons</groupId>
		<artifactId>assetshare.ui.apps</artifactId>
  		<version>1.0.0</version>
	</dependency>
	...
<dependencies>
```

### Add Asset Share Commons as a Sub Package

In the `content-package-maven-plugin` section of your project's content module (**ui.apps**) pom.xml add this:

```
<!-- ui.apps pom.xml -->
<plugins>
	...
     <plugin>
     	<groupId>com.day.jcr.vault</groupId>
       <artifactId>content-package-maven-plugin</artifactId>
       <extensions>true</extensions>
       <configuration>
       	...
          <subPackages>
          	<subPackage>
             	  <groupId>com.adobe.aem.commons</groupId>
  				  <artifactId>assetshare.ui.apps</artifactId>
               <filter>true</filter>
             </subPackage>
         </subPackages>
         ...
     </configuration>
   </plugin>
   ...
</plugins>
```

## Project Theme Client Library

It is recommended to create a project specific theme to be used to style a project's Asset Share. Asset Share Commons includes two themes: Light and Dark. It is easiest to choose one of these themes to start with and customize from there. A copy of the theme should be made and saved beneath the project's `/apps/clientlibs` folder. This client library will need to be included in the Page Design for all the templates used in the project.

* Light: `/apps/asset-share-commons/clientlibs/clientlib-theme/semanticui-light`
* Dark:  `/apps/asset-share-commons/clientlibs/clientlib-theme/semanticui-dark`

See the [Theming](../../theming) page for more details on customizing a theme. 

## Project Asset Placeholder

Throughout the Asset Share Commons project a default Asset Placeholder is used if a valid dam:Asset cannot be resolved. This is quite common when authoring Asset Share Commons pages and is useful to preview how metadata/renditions will be rendered. It is recommended to create a project-specific placeholder asset to be used. The Placeholder asset functionality only occurs in the Author environment. In the Publish environment if an Asset can not be resolved a 404 error code will be thrown.

Example Placeholder asset: `/apps/asset-share-commons/resources/placeholder.jpeg`. 

Since the Placeholder assets only get rendered in the Author environment it is ok to save them beneath the project's `/apps` folder. 

***NOTE** the Placeholder Asset should not be confused with a **Not Found** thumbnail. Several components like the [Search Results component](#) and the [Image component](#) allow an author to set a Not Found thumbnail to be used if the current asset does not have a valid thumbnail. The Not Found asset **must** be in the DAM or publicly accessible (should never be beneath `/apps`).




## Editable Templates

Creating project-specific editable templates is the recommended way of integrating Asset Share Commons into a project. The [ui.content](https://github.com/Adobe-Marketing-Cloud/asset-share-commons/releases/tag/asset-share-commons.ui.content-1.0.0) package includes examples of the templates and template-types needed under `/conf/asset-share-commons/settings/wcm/`. *Note that two sets of templates are included in ui.content to support both the Light and Dark theme examples. A project implementation most likely only needs a single set.

### Template Types

When setting up a new project it is easiest to copy the template types from `/conf/asset-share-commons/settings/wcm/template-types` in to the project's `/conf` folder.

**Search Template Type**

* Required
* `sling:resourceType` : `asset-share-commons/components/structure/search-page`
* example: `/conf/asset-share-commons/settings/wcm/template-types/search-page`
* For Search Pages, includes page properties tab for search configuration.

**Asset Details Template Type**

* Required
* `sling:resourceType` : `asset-share-commons/components/structure/details-page`
* example: `/conf/asset-share-commons/settings/wcm/template-types/details-page`
* For Asset Detail pages, includes page properties tab for detail page configuration.

**Empty Template Type**

* Required
* `sling:resourceType` : `asset-share-commons/components/structure/page`
* example: `/conf/asset-share-commons/settings/wcm/template-types/empty-page`
* For action modal pages, license agreement terms and conditions and any other blank content pages. 

**Rail Template Type**

* (Optional)
* `sling:resourceType` : `asset-share-commons/components/structure/page`
* example: `/conf/asset-share-commons/settings/wcm/template-types/rail-page`
* For content pages with a rail that add supporting content to the Asset Share portal. 

### Templates

Due to the complex structure of Editable Templates it is recommended to create each project specific Template using the Template Editor in the AEM UI. Templates found beneath `/conf/asset-share-commons/settings/wcm/templates` should be used as a reference but should NOT be copied directly into a project's `/conf` directory. A Structure policy should be created to include *Asset Share Commons - Structure* components (Header, Footer, User menu...) on each Template. Allowed Components policies should also be configured based on the template. Lastly the Page Design for each Template needs to be configured to point to a Semantic UI theme client library (preferrably a project specific one).

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

#### Search Template Type

A Search Template Type is needed for any Search Templates. The `sling:resourceType` should point to `asset-share-commons/components/structure/search-page` or to a component in which the `sling:resourceSuperType` points to `asset-share-commons/components/structure/search-page`. This will ensure the proper dialog for search configurations is included.

## Content

A project specific content structure should be created. At the root of the site, the allowed templates should be configured to allow pages to be created from Project templates.

### Recommended Content Architecture

```
/content
     /site-root
           /search-page
                /details (default details page)
                     /image (details for images)
                     /video (details for videos)
                     /document  (details for word documents)
                     /presentation   (details for power point)
                /actions
                     /cart
                     /download
                     /share
                     /license
                /specific-search-page-1        
                /specific-search-page-2  
```

More details around the recommended content hierarchy can be found on the [Search Page](#) documentation.

## Custom Computed Properties

[Computed Properties](#) are used throughout Asset Share Commons to display metadata about an individual asset. Implementing a new computed property is one of the easiest ways to extend Asset Share Commons to meet business requirements.

### New Status - Computed Property

To illustrate the concept of Computed Properties we will be implementing a requirement to show a "New" status indicator if an Asset has been created in the last 7 days.

## Custom Search Results Example



## Custom Component Example

New Status Indicator. OSGi Config for days defaults to 7. Adds to Search Results, Image Component. Image Asset Details component to show a video -> 
