---
layout: component-page
title: Download Modal
component-group: modals
---

![Download modal component](./images/main.png)

Displays the modal used to download one or more assets.

* The left portion of the modal displays the list (one or more) assets that will be downloaded as part of this operation.
    * Multiple assets can be downloaded via the [Cart](../cart/). 
* The original assets is always included in the download zip.
* Users can select to include all renditions.
* Users can select to include all (any) sub-assets.

The resulting download is a zip file (the zip file name can be authored via the dialog).

## Authoring

The Download Modal is authored by opening up the download action page (of Action Template type) via AEM's Site admin. 

*Each download page should have exactly one Download Modal component.*

This Download Modal action page must referenced from the [Search page's Page Properties](../search/#page-properties). 

![Authoring view of the download modal - center](./images/authoring.png)

The modal displays the placeholder image when being authored.

### Dialog / Labels

![Labels dialog](./images/dialog-labels.png)

#### Modal Title

The modal's title.

#### Asset List Title

The title text to display above the list of assets to download.

#### Download Form Title

The title text to display above the download option check-boxes.

#### Cancel Button Label

The text for the button that closes the modal.

#### Download Button Label

The text for the button that lets users download the assets.

### Dialog / File

![File dialog](./images/dialog-download-options.png)

#### Exclude Original Assets from ZIP File (since v1.2.0)
 
Check to exclude original assets from the downloaded zip file. This applies for all uses of this Download modal.

#### ZIP File Name
 
The name of the zip file to download.

## Technical details

* **Component**: `/apps/asset-share-commons/components/modals/download`
* **Sling Model**: `N/A`

The download functionality leverages AEM Assets `assetdownload.zip` servlet. To ensure this servlet is
available, the following request path must be open (ie. not blocked via AEM Dispatcher filters).

    HTTP POST /content/dam.assetdownload.zip/<ZIP File Name>.zip?licenseCheck=true&flatStructure=true&downloadSubassets=<true|false>&downloadRenditions=<true|false>