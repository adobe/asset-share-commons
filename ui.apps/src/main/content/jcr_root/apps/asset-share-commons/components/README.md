# Component Style Guide

## cq:Component
* Node names are lowercase and hyphenated
* *Hyphens represent spaces; The English language should be used to determine where spaces occur (ie. search-bar is correct; searchbar is not.)*


### Component Title
* `jcr:title` - same as Dialog Title
* *See rules defined below in Dialog Title*

### Component Description
* `jcr:description` - a succinct description of the component should always be included.
* for components that render content the description should start with *Displays xyz*
* for components that allow a user interaction (button, filter...) should start with *Allows a user to..xyz*

### Component Icon
* Provide a CoralUI Component icon unique to the components group
  * `cq:icon=corualUiIconName`
  * [https://docs.adobe.com/docs/en/aem/6-3/develop/ref/coral-ui/coralui3/Coral.Icon.html#availableIcons](https://docs.adobe.com/docs/en/aem/6-3/develop/ref/coral-ui/coralui3/Coral.Icon.html#availableIcons)
  


### Component Groups
* All components must be in a component group prefixed with "Asset Share Commons - XXXX"
  * Asset Share Commons - Structure
  * Asset Share Commons - Search
  * Asset Share Commons - Details
  * Asset Share Commons - Content
  * Asset Share Commons - Modals
* Non-D&D components are set to `.hidden`

## cq:htmlTag
* Set classes: `cmp cmp-<component-parent-folder>-<component-node-name>`
  * Ex: `cmp-modal-cart`, `cmp-content-image`, `cmp-details-image`

## cq:dialog

### Node Names
* Node names should be lowercase and and hyphenated (NOT camelCase) (JR Standards)

### Dialog Title

* Title is UNqualified by Component Group
* Titles should be suscinct but descriptive
  * Prefer `Download Modal` to `Download` and `Hidden Filter` to `Hidden`
* Title is capitalized ()except for articles)
  * Examples: `Date Range Filter`, `Sort`, `Search Results`, `Download Modal`

### Dialog Description
* Optional

### Tab Nodes
* Tab titles describe the contents of the tab; avoid ambiguous terms like `Main` or `General`
* Tab node names `tab-#`


### Fields

#### Required Fields
* Required fields should have default values defined in `cq:template`
  * If a valid default value cannot be predicted then it cannot be in `cq:template` and the placeholder template will render on initial load. 

#### Field Labels/Text
* Fields must have labels (text)
* Text is capitalized (except for articles)

#### Field Description
* Optional
* When present Capitalize first letter and proper nouns.
* Avoid passive voice (as it uses more words)
* Add punctuation (ex. ending '.')

#### Name Property
* `name` property saves to a `camelCase` property name
* camelCase based on where spaces occur in the English language (Ex. prefer `orderBySort` to `orderbySort`; `orderby` is not a word)
** Properties for UI labels should be post-fixed with "Label" (ie. `descendingLabel`, `shareButtonLabel`)
** Properties for Text should be post-fixed with "Text" (`instructionsText`, `messageText`)


#### Options (special case)
* When create label -> value pairs... save to `text` / `value` property combinations.
  * *Hidden predicate is example where this diverges as these appear as options, but are just key/value pairs and are semantically named: predicate / value*

#### Empty Text and Default Values
* Default values should be provided via `cq:template` (see below)
* `emptyText` should be provided to suggested values; This may match the value in cq:template when appropriate.
   * It is rare `value` is to be used as this value will only persist if the component is editted once. Prefer `cq:template`

## cq:editConfig

* If the dialog contains a multi-field it MUST be Fullscreen
    ```
<?xml version="1.0" encoding="UTF-8"?>
<jcr:root xmlns:cq="http://www.day.com/jcr/cq/1.0" xmlns:jcr="http://www.jcp.org/jcr/1.0"
    jcr:primaryType="cq:EditConfig"
    dialogLayout="fullscreen"/>
    ```
* Else, MUST be not-fullscreen (leave off the `cq:editConfig`)


## cq:template
* A `cq:template` provides default values. 
* Preferred to adding default values to the `cq:dialog` field definition
* `emptyText` in field can be used to suggest default terms


-----


## HTL

Unless specified otherwise, follow the standards set forth in the []Netcentric HTL Style guide](https://github.com/Netcentric/aem-htl-style-guide).

### General Structure

* All Components must define all `data-sly-use` including templates in an opening `<sly...>` tag.
* The opening sly tag must have an `data-sly-test.empty=${xxxx.empty}` to determine if the HTL should be rendered.
  * The exception is if there is no backing Sling Model; however the valid check should still occur in the top sly tag.
* If xxx.empty returns false, the `placeholderTemplate.placeholder` is included for display.

```
<sly data-sly-use.predicate="com.adobe.aem.commons.assetshare.components.predicates.SortPredicate"
	 data-sly-use.placeholderTemplate="core/wcm/components/commons/v1/templates.html"
	 data-sly-test.ready="${predicate.ready}">
	 
...

</sly>
<sly data-sly-call="${placeholderTemplate.placeholder @ isEmpty=!ready}"></sly>
```

### Re-use Test Variables

Whenever possible re-use test variables instead of calling the same conditional twice

```
//Bad
<sly data-sly-test="${predicate.ready}"></sly>
<sly data-sly-call="${placeholderTemplate.placeholder @ isEmpty=predicate.ready}"></sly>

//Good
<sly data-sly-test.ready="${predicate.ready}"></sly>
<sly data-sly-call="${placeholderTemplate.placeholder @ isEmpty=!ready}"></sly>
```
### Variable use

When using variables that follow Java Bean naming conventions, use the abbreviated format:
* `myModel.getName()` -> `${model.name}`
* `myModel.isReady()` -> `${model.ready}`

### Styles

* Prefer HTML attributes to be on their own lines
  * If attributes are minimal and values short, break this rule.
* Prefer `data-asset-share` attributes below `HTML attributes` above `data-sly` attributes 
* Prefer `HTML` and `data-asset-share` attributes above `ata-sly` attributes
    
    ```
    <div class="ui section"
         id="${some.id}
         data-asset-share-id="${my.assetId}"
         data-sly-test=${my.test}">...
    ```
* Prefer `'` to `"` 
  * Ex.`${'Hardcoded text' @ i18n}`
* Prefer `[..]` notation for accessing maps
  * Ex. `${properties['myProp']`
* HTML must be formatted (proper indentations, etc.)

### Templates

* Templates are stored in a `templates` folder in the component.
* Template files are named semantically based on their contents.
* As a general rule there should be a single file of templates per component that can include multiple templates.
* The data-sly-use.XXX for templates should be a combination of the (camelCased) template HTML name and "Template".
  * Ex. `data-sly.use.checkboxTemplate=templates/checkbox.html`
* `<template...` names are camelCase

 
 
### Localization
 
 * ~Never use `${ properites['someVal'] || 'default' @ i18n }` in components.
   * Required fields always source their "default" value from `cq:template`, and optional values should not force default values.
 * Any visible NON-authorable text in a component MUST be processed by i18n: `${'Hardcoded Text' @ i18n}`
 

## Client Libraries

### Site

Any global styles and javascript that applies to the site should be added to this client library. This clientlib includes javascript for search, actions, and other behavior not tied directly to a single component.

* Category: *asset-share-commons.site*
* Path: */apps/asset-share-commons/clientlibs/clientlib-site*
* Embeds: none
* Type: standard clientlib

### Author

Sometimes there is a need to add css or javascript to allow for a rich authoring experience. Any styles or javascript that is required only for the author instance should be placed here.

* Category: *asset-share-commons.author*
* Path: */apps/asset-share-commons/clientlibs/clientlib-author*
* Embeds: none
* Type: standard clientlib

### Dependencies

Any 3rd party dependencies (jQuery, moment) or AEM libraries (granite.shared, cq.shared) should be embedded in this client library. These dependencies will be loaded in the header of the page (both CSS and Javascript) to ensure they are loaded for client libraries depending on them. This is a shell clientlibrary and only embeds should be used, no javascript or CSS should be placed directly in this client library.

* Category: *asset-share-commons.dependencies*
* Path: */apps/asset-share-commons/clientlibs/clientlib-dependencies*
* Embeds: *jquery,granite.utils,granite.jquery,cq.jquery,granite.shared,cq.shared,underscore,asset-share-commons.vendor*
* Type: shell clientlib

### Base

The base clientlib includes the global site client library as well as any component specific client libraries. This is a shell clientlibrary and only embeds should be used, no javascript/CSS should be placed directly in this client lib.

* Category: *asset-share-commons.base*
* Path: */apps/asset-share-commons/clientlibs/clientlib-base*
* Embeds: *core.wcm.components.image.v1,asset-share-commons.site,asset-share-commons.site.components*

### Component

Any time a component requires some specific styles or javascript to function a client library should be created. The following is the expected structure:

+ clientlib
  + editor // if css/javascript only for the author instance i.e dialog javascript
    - category : asset-share-commons.author
  + site // css/javascript meant for the site.
    - category : ['asset-share-commons.site.components', 'asset-share-commons.site.components.<folder-level>.<component-name>']
    

### Themes

Semantic UI themes are used for much of the display and functionality on the site. To allow flexibility for different themes these are expected to be included in the Template's Page design. A re-usable policy can be created to ensure consistent theming across templates. Asset Share Commons includes 2 themes: light and dark. These are placed beneath */apps/asset-share-commons/clientlibs/clientlib-theme*.

Structure of Light Theme:

+ semanticui-light
  + assets //fonts and images
  + definitions // Semantic UI Less files
  + site //optional overrides
  + themes // includes default Semantic UI and Light theme
  - theme.config //file to 


  

### Vendor

Any 3rd party dependencies not included in the AEM quickstart but needed for the application should be placed in this folder. Each 3rd party library should have its own clientlib and have a category of *asset-share-commons.vendor* to ensure it is embedded in the Dependencies clientlib. An additional category uniquely identifying the library should also be added.

An example is the client library for modernizr:

* Categories: *asset-share-commons.vendor,asset-share-commons.modernizr*
* Path: */apps/asset-share-commons/clientlibs/vendor/modernizr*
* Embeds: none



 
 
