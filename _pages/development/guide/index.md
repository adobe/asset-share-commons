---
layout: development-page
title: Development Guide
---

There are many ways that Asset Share Commons could be leveraged on a customer specific project. Detailed below are guidelines for including Asset Share Commons as part of a custom project and instructions on how to extend common areas of an Asset Share implementation.


## Using with Maven

### Use Maven Bundle Plugin v 3.3.0+

Asset Share Commons uses the latest OSGi annotations. In the `<pluginManagement>` section of your project's **parent** pom.xml ensure that the `maven-bundle-plugin` is using a version 3.3.0 or higher.

```
<!-- parent pom.xml -->
...
<build>
	<pluginManagement>
    	<plugins>
    	...
       <plugin>
       	<groupId>org.apache.felix</groupId>
          <artifactId>maven-bundle-plugin</artifactId>
          <version>3.3.0</version>
       </plugin>
       ...
   </pluginManagement>
</build>
```

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

### Asset Status - Computed Property

To illustrate the concept of Computed Properties we will be implementing a requirement to show a "New" or "Updated" status indicator if an Asset has been created/modified in the last 7 days.

#### 1. Create AssetStatusImpl.java

In your project's core bundle add a new implementation class named `AssetStatusImpl.java`. Our new class will extend `AbstractComputedProperty.class`, an abstract class exposed by Asset Share Commons to make use of several common methods:

```
@Component(service = ComputedProperty.class)
@Designate(ocd = AssetStatusImpl.Cfg.class)
public class AssetStatusImpl extends AbstractComputedProperty<String> {

...

```

By extending the AbstractComputedProperty class our new class will implement the `com.adobe.aem.commons.assetshare.content.properties.ComputedProperty` interface.

#### 2. Add ObjectClassDefinition

Each implementation of a Computed Property needs to provide:

1. **Name** - identifies the Computed Property via a ValueMap 
2. **Label** - a human friendly label, used for data source drop downs
3. **Type** - a classification of the computed property. Valid types are `metadata`, `rendition`, `url`. This is used by data sources to filter which computed properties are shown to a user. A computed property can have multiple types.

Using the new OSGi annotations we can add an ObjectClassDefinition which will expose the Label and Types as an OSGi Configuration. We will also add a configuration for Days which will determine the period in which an asset is considered "New" or "Updated".

```
 
 public static final String LABEL = "Asset Status";
 public static final String NAME = "assetStatus";
 
 @ObjectClassDefinition(name = "Sample Asset Share - Computed Property - Asset Status")
    public @interface Cfg {
        @AttributeDefinition(
                name = "Label",
                description = "Human read-able label."
        )
        String label() default LABEL;

        @AttributeDefinition(
                name = "Types",
                description = "Defines the type of data this exposes. This classification allows for intelligent exposure of Computed Properties in DataSources, etc."
        )
        String[] types() default {Types.METADATA};
        
        @AttributeDefinition(
                name = "Days",
                description = "Defines the number of days in which an asset is considered 'New' or 'Updated'. Expected to be a negative number."
        )
        int days() default DEFAULT_DAYS;
    }
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getLabel() {
        return cfg.label();
    }

    @Override
    public String[] getTypes() {
       return cfg.types();
    }
    
    @Activate
    protected void activate(Cfg cfg) {
        this.cfg = cfg;
    }
    
```
 
The `@Activate` method ensures that if a change is made via the OSGi configuration the component is updated.

#### 3. Populate getter method

Finally we can populate the `get(Asset, SlingHttpServletRequest)` method. This is the entry point in which the real "work" of the computed property takes place. The `com.day.cq.dam.api.Asset` parameter represents the current Asset. `SlingHttpServletRequest` parameters is the current request (useful for internationalization). We will simply get the `jcr:created` and `jcr:content/jcr:lastModified` properties from the current asset and compare them to a date 7 days ago. This will determine the status label returned.

```
...
    @Override
    public String get(Asset asset, SlingHttpServletRequest request) {
        
        final ValueMap assetProperties = getAssetProperties(asset);
        Calendar assetCreated = assetProperties.get(JcrConstants.JCR_CREATED, Calendar.class);
        Calendar assetModified = assetProperties.get(JcrConstants.JCR_CONTENT + "/" + JcrConstants.JCR_LASTMODIFIED, Calendar.class);
        
        //Get a calendar to compare to
        Calendar weekOld = getCompareCalendar(cfg.days());
       
        if(assetCreated.after(weekOld)) {
            //if asset created < one week ago
            return NEW_STATUS;
        } else if (assetModified.after(weekOld)) {
            //if asset modified < one week ago
            return UPDATED_STATUS;
        }
        return null;
    }
    
    /***
     * 
     * @return a Calendar object to compare asset dates to
     */
    private Calendar getCompareCalendar(int daysOld) {
        Calendar compareCal = Calendar.getInstance();
        // reset hour, minutes, seconds and millis
        compareCal.set(Calendar.HOUR_OF_DAY, 0);
        compareCal.set(Calendar.MINUTE, 0);
        compareCal.set(Calendar.SECOND, 0);
        compareCal.set(Calendar.MILLISECOND, 0);
        compareCal.add(Calendar.DAY_OF_MONTH, daysOld);
        
        return compareCal;
    }
...
```

#### 4. Verify deployment of Computed Property

After building and deploying the project, navigate to the [OSGi Config manager](http://localhost:4502/system/console/configMgr/com.sample.assetshare.content.properties.impl.AssetStatusImpl). The new computed property and OSGi config should be there.

![OSGi configuration for Asset Status](./images/osgi-config-asset-status.png)

Lastly open up the dialog of a Metadata component on one of the Asset Details pages. The Asset Status should now appear in the Computed Property drop down in the dialog:

![Metadata Component dialog with Asset Status](./images/asset-status-computed-dialog.png)

The java class in full can be viewed here:

## Custom Search Results Example

Now that we have created a new computed property we want do display the Asset Status in the search results.

#### 1. Create Card and List Result Components

In your project's `/apps/components` directory in ui.apps add two new components named `card` and `list`.

**Card Component**

```
<?xml version="1.0" encoding="UTF-8"?>
<jcr:root xmlns:sling="http://sling.apache.org/jcr/sling/1.0" xmlns:cq="http://www.day.com/jcr/cq/1.0" xmlns:jcr="http://www.jcp.org/jcr/1.0"
    jcr:primaryType="cq:Component"
    jcr:title="Sample Assetshare Cards"
    sling:resourceSuperType="asset-share-commons/components/search/results/result/card"
    componentGroup=".hidden"
    extensionType="asset-share-commons/search/results/result/card"/>

```

**List Component**

```
<?xml version="1.0" encoding="UTF-8"?>
<jcr:root xmlns:sling="http://sling.apache.org/jcr/sling/1.0" xmlns:cq="http://www.day.com/jcr/cq/1.0" xmlns:jcr="http://www.jcp.org/jcr/1.0"
    jcr:primaryType="cq:Component"
    jcr:title="Sample Assetshare List"
    sling:resourceSuperType="asset-share-commons/components/search/results/result/list"
    componentGroup=".hidden"
    extensionType="asset-share-commons/search/results/result/list"/>
```

The `sling:resourceSuperType` inherits from the Default Asset Share Commons card and list components. The `extensionType` ensures the components will appear in the Search Results component dialog dropdown.

#### 2. Copy Card and List Template HTL files

Create a folder named `templates` beneath the card and list components. Copy the HTL template files from the Default Asset Share components beneath the respective components. The structure should look like this:

```
/apps/sample-assetshare/components/search
			/card
				/templates
					card.html (copied from /apps/asset-share-commons/components/search/results/result/card/templates/card.html)
			/list
				/templates
					list.html (copied from /apps/asset-share-commons/components/search/results/result/list/templates/list.html)
```

#### 3. Update list.html

Update the header template to add a new column heading for Status after the Preview thumbnail.

```
<template data-sly-template.header="${@ search}">
	<thead>
		<tr><th class="left aligned">Preview</th>
       <th>${'Status' @i18n}</th>
   ...
```

Update the row template to add a column to display the Asset Status computed property. Add the column after the image column (second column).

```
<template data-sly-template.row="${@ asset = result, config = config }">
	...
	<td class="image">
			<a href="${assetDetails.url @ suffix = asset.path}"><img src="${asset.properties['thumbnail'] || properties['missingImage'] @ context = 'attribute'}" alt="${asset.properties['title']}"/></a>
	</td>
	<!--/* Asset Status Computed Property */-->
	<td><div class="ui status label">${asset.properties['assetStatus'] @ i18n}</div></td>
	...
```

#### 4. Update card.html

Add a status label on in the card template directly inside the `article` tag.

```
<template data-sly-template.card="${@ asset = result, config = config }">
	...
    <article
            data-asset-share-id="asset"
            data-asset-share-asset="${asset.path}"
            id="${asset.path}"
            class="ui card cmp-card">
       <!--/* Asset Status Computed Property */-->
		<div data-sly-test.status="${asset.properties['assetStatus']}" 
		     class="floating ui status label">${asset.properties['assetStatus'] @ i18n}</div>
	...
		     
```
#### 5. Update Search Results Card and List Renderer

Deploy the new components to AEM. On a Search Results page update the Search Results Component dialog to use the custom Card and List renderers.

![Search Results dialog custom renderers for Card and List](./images/search-results-custom-renderer-dialog.png)

You should now see the Asset Status indicator in the search results (for new and updated assets within the last 7 days).

![Card results with status](./images/search-results-card-status.png)

![List results with status](./images/search-results-list-status.png)

Of course some style changes could be used (especially on Card view).

## Custom Component Example

New Status Indicator. OSGi Config for days defaults to 7. Adds to Search Results, Image Component. Image Asset Details component to show a video -> 
