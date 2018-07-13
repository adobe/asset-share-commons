---
layout: content-page
title: Computed Properties
description: Computed properties are an extensible Java code abstraction that allows for attributes drive by business logic, to be accessed in HTL as "normal" asset metadata properties.
---

Many important metadata properties of an Asset are stored in a raw form and cannot be displayed "as is". Asset Share Commons introduces the notion of a computed property to solve this issue. A computed property is an abstraction of a raw metadata property that includes a small amount of logic to display in a human friendly format.

For example the size of an asset is stored in the metadata property `jcr:content/metadata/dam:size` and the raw value is stored in bytes and could have a value of **28358905**. The computed property **File Size** displays **28.3KB**.

### List of Computed Properties

**Asset Path** - Returns the path of the asset.

* **Label:** Asset Path
* **Name:** path
* **Return type:** `String`

**Asset Type** - Displays a short string based on the `jcr:content/metadata/dam:MIMEtype`.

* **Label:** Asset Type
* **Name:** type
* **Return type:** `String`

**Is Expired** - Displays true if the current date falls after `jcr:content/metadata/prism:expirationDate`. False otherwise.

* **Label:** Is Expired
* **Name:** expired
* **Return type:** `Boolean`

**File Name** - Returns the file name of the asset.

* **Label:** File Name
* **Name:** fileName
* **Return type:** `String`

**File Size** - Returns the size of the file based on `jcr:content/metadata/dam:size`. Labels for KB, MB, and GB are applied.

* **Label:** File Size
* **Name:** fileSize
* **Return type:** `String`

**Height** - Returns the height of the asset based on `jcr:content/metadata/tiff:ImageLength`. If blank falls back to `jcr:content/metadata/exif:PixelYDimension`.

* **Label:** Height
* **Name:** height
* **Return type:** `Long`

**Width** - Returns the width of the asset based on `jcr:content/metadata/tiff:ImageWidth`. If blank falls back to `jcr:content/metadata/exif:PixelXDimension`.

* **Label:** Width
* **Name:** width
* **Return type:** `Long`

**License** - Returns a path to a license file of an asset based on `jcr:content/metadata/xmpRights:WebStatement`. It is expected that the path is to an HTML page.

* **Label:** License
* **Name:** license
* **Return type:** `String`

**Resolution** - Returns the dimensions of an asset based on the computed properties of Height and Width. The return format is `<width> x <height>`.

* **Label:** Asset Path
* **Name:** path
* **Return type:** `String`

**Tag Titles** - Returns the titles of any tags on the current asset. Based on the `jcr:content/metadata/cq:tags`. A localized tag title will be used if it exists. Locale is derived from the Sling request.

* **Label:** Tag Titles
* **Name:** tagTitles
* **Return type:** `List<String>`

**Smart Tag Titles** - Returns the titles of any tags on the current asset. Based on the `jcr:content/metadata/cq:tags`. A localized tag title will be used if it exists. Locale is derived from the Sling request.

* **Label:** Smart Tag Titles
* **Name:** smartTagTitles
* **Return type:** `List<String>`

**Thumbnail Rendition** - Returns an escaped path to the thumbnail rendition of `cq5dam.thumbnail.319.319.png` for the current asset.

* **Label:** Thumbnail Rendition
* **Name:** thumbnail
* **Return type:** `String`

**Title** - Returns the title of the current asset based on the `jcr:content/metadata/dc:title` property. If the `dc:title` is empty falls back to the asset file name.

* **Label:** Title
* **Name:** title
* **Return type:** `String`

**Web Rendition** - Returns an escaped path to the web rendition for 1280 width. 

* **Label:** Web Rendition
* **Name:** image
* **Return type:** `String`

### Technical details

Computed Property implementation classes can be found in the following package: `com.adobe.aem.commons.assetshare.content.properties.impl`

Additional computed properties can be registered by implementing the `com.adobe.aem.commons.assetshare.content.properties.ComputedProperty` interface.
