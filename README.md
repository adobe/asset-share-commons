[![Maven CI](https://github.com/adobe/asset-share-commons/actions/workflows/verify.yaml/badge.svg)](https://github.com/adobe/asset-share-commons/actions/workflows/verify.yaml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.adobe.aem.commons/assetshare/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.adobe.aem.commons/assetshare)
[![codecov](https://codecov.io/gh/adobe/asset-share-commons/branch/develop/graph/badge.svg)](https://codecov.io/gh/Adobe-Marketing-Cloud/asset-share-commons)

# Asset Share Commons

This a content package project generated using the AEM Multimodule Lazybones template.

### Documentation

[https://opensource.adobe.com/asset-share-commons/](https://opensource.adobe.com/asset-share-commons/)

## Building

This project uses Maven for building. Common commands:

From the root directory, run ``mvn -PautoInstallPackage clean install`` to build the bundle and content package and install to a AEM instance.

From the bundle directory, run ``mvn -PautoInstallBundle clean install`` to build *just* the bundle and install to a AEM instance.

## Building All

By default the sample content is not installed. If you want to deploy the sample content to an AEM instance:

From the root directory, run ``mvn -PautoInstallPackage-all clean install`` to build the bundle ui.apps and ui.content content packages and install to a AEM instance.

## Using with AEM Developer Tools for Eclipse

To use this project with the AEM Developer Tools for Eclipse, import the generated Maven projects via the Import:Maven:Existing Maven Projects wizard. Then enable the Content Package facet on the _content_ project by right-clicking on the project, then select Configure, then Convert to Content Package... In the resulting dialog, select _src/main/content_ as the Content Sync Root.

## Specifying AEM Host/Port

The AEM host and port can be specified on the command line with:
mvn -Daem.host=otherhost -Daem.port=5502 <goals>


## Asset Share Commons 2.x Requirements

* AEM as a Cloud Service 2022.5.7493 or newer
* AEM 6.5 SP7 or greater
* AEM WCM Core Components 2.14.0+

## Asset Share Commons 1.x Requirements

* AEM 6.3.1 -> 6.5.x


