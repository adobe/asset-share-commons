---
layout: doc-page
title: New Project Guide
---

## AEM Project Archetype

Start by generating a vanilla project using the [AEM Project archetype](https://github.com/adobe/aem-project-archetype). 

For example:

```
mvn -B archetype:generate \
 -D archetypeGroupId=com.adobe.aem \
 -D archetypeArtifactId=aem-project-archetype \
 -D archetypeVersion=30 \
 -D appTitle="My Asset Share" \
 -D appId="my-asset-share" \
 -D groupId="com.myassetshare"
```

## Add Asset Share Commons as a dependency

In the `<dependencies>` section of your project's **parent** pom.xml (root of the project) add the following dependencies:

```
<!-- parent pom.xml -->
<dependencies>
	...
	<dependency>
    <groupId>com.adobe.aem.commons</groupId>
    <artifactId>assetshare.all</artifactId>
    <version>2.x.x</version>
    <type>zip</type>
  </dependency>
  <!-- optional dependency to code against Asset Share Commons APIs -->
 	<dependency>
    <groupId>com.adobe.aem.commons</groupId>
    <artifactId>assetshare.core</artifactId>
    <version>2.x.x</version>
    <type>jar</type>
  </dependency>
	...
<dependencies>
```

## Embed Asset Share Commons in All module

Include Asset Share Commons 2.x's `all` project as an `embedded` in your AEM Maven project's `all/pom.xml`.

1. Add the Asset Share Commons `all` project as `<dependency>`.

    ```
    <dependencies>
        ...
        <dependency>
            <groupId>com.adobe.aem.commons</groupId>
            <artifactId>assetshare.all</artifactId>
            <type>zip</type>
        </dependency>
        ...
    </dependency>    
    ```

2. Add the `assetshare.all` dependency to your `all/pom.xml`'s `<embeddeds>` list as a `container`.

    ```
    <plugins>
        <plugin>
            <groupId>org.apache.jackrabbit</groupId>
            <artifactId>filevault-package-maven-plugin</artifactId>
            ...
            <configuration>
                <allowIndexDefinitions>true</allowIndexDefinitions>
                ...
                <embeddeds>
                    <embedded>
                        <groupId>com.adobe.aem.commons</groupId>
                        <artifactId>assetshare.all</artifactId>
                        <type>zip</type>
                        <target>/apps/<my-app>-packages/container/install</target>
                    </embedded>
                    ...
    ```

### (Optional) Custom development using Asset Share Commons APIs

1. Optionally, include the `assetshare.core` as a dependency in your AEM project's `core/pom.xml` if you plan developing Java code against Asset Share Commons' APIs.

    ```
    <dependencies>
        ...
        <dependency>
            <groupId>com.adobe.aem.commons</groupId>
            <artifactId>assetshare.core</artifactId>
            <type>jar</type>
        </dependency>
        ...
    </dependency>    
    ```


## Update Dispatcher module

When generating a project via the archetype a Dispatcher module is created. 

1. Update the `filter.any` file to include the following [filters](https://github.com/adobe/asset-share-commons/blob/develop/dispatcher/src/conf.dispatcher.d/filters/filters.any#L16-L31).

2. Make a copy of the `default.farm`, i.e `asset-share-commons.farm` at `src/conf.dispatcher.d/available_farms/default.farm`.
3. Update the new farm to include the following headers to enable [cache-able HTTP request headers](https://github.com/Adobe-Marketing-Cloud/asset-share-commons/blob/develop/dispatcher/src/conf.dispatcher.d/available_farms/asset-share-commons.farm#L92-L95))
4. Update the symbolic link at `dispatcher/src/conf.dispatcher.d/enabled_farms` to point to the new farm.

## Update the Frontend module

Asset Share Commons ships with two themes **Light** and **Dark**. These can be used directly. If you would like to customize the theme to match your brand's colors perform the following steps:

1. In your project remove the contents of the directory `ui.frontend` except for the `ui.frontend/pom.xml` file.
2. Download the contents of either the [Light](https://github.com/adobe/asset-share-commons/tree/develop/ui.frontend.theme.light) or [Dark](https://github.com/adobe/asset-share-commons/tree/develop/ui.frontend.theme.dark). It is far easier to start from one of the sample themes.
3. Copy the entire contents of the previous step into your project's `ui.frontend` folder. Do **not** copy the `pom.xml` file from the Asset Share Commons repo.
4. Open the file `ui.frontend/clientlib.config.js`. Make the following changes:

    1. Update the `clientLibRoot` to match your project:

        ```
        clientLibRoot: "./../ui.apps/src/main/content/jcr_root/apps/<my-app>/clientlibs"
        ```
  
    2. Update the `name` from `semanticui-light` to `clientlib-site`.
    3. Update the `categories` from `asset-share-commons.semantic-ui-light` to `<my-app>-asset-share.theme`.

5. Make a change to the file `ui.frontend/semanticui/site/globals/site.variables` such as updating `@primaryColor`.


## Update sample content

Asset Share Commons includes templates and a series of pages pre-configured. A **Light** and **Dark** set of content is included. These can be used as is. For more granular control, copy these templates and pages into your projects `ui.content` folder and modify to match your projects needs.

1. In your project's `ui.content` module remove the folder at: `ui.content/src/main/content/jcr_root/conf/<your-app>/settings/wcm`.
1. Replace the `wcm` folder with Asset Share Common's [wcm](https://github.com/adobe/asset-share-commons/tree/develop/ui.content/src/main/content/jcr_root/conf/asset-share-commons/settings/wcm) folder. 
1. If using the **Light** theme as a base, under `wcm/templates` remove the **Dark** theme templates ( or vice-versa).
1. Remove the content under: `ui.content/src/main/content/jcr_root/content/<your-app>/us/en` and replace with either the [Light](https://github.com/adobe/asset-share-commons/tree/develop/ui.content.sample/src/main/content/jcr_root/content/asset-share-commons/en/light) set of pages or [Dark](https://github.com/adobe/asset-share-commons/tree/develop/ui.content.sample/src/main/content/jcr_root/content/asset-share-commons/en/dark) set. 
1. Perform a find+replace across files, to modify any references to `/conf/asset-share-commons/settings/wcm` with `/conf/<your-app>/settings/wcm`. There should be 30 references across 30 files to update.
1. Open the policies file at `ui.content/src/main/content/jcr_root/conf/<your-app>/settings/wcm/policies/.content.xml`. 
1. Find and replace any references to `asset-share-commons.semantic-ui-light` and replace with the category used for your custom clientlib from the `ui.frontend` module i.e `<my-app>-asset-share.theme`. There should be 3 references.
1. Perform a find+replace across files and modify any references to `/content/asset-share-commons/en/light` with `/content/<your-app>/us/en`.

## Example project

An example project has been created for the WKND Brand:

![WKND Asset Share](https://user-images.githubusercontent.com/8974514/134435899-ac6f0b11-da30-40e4-b744-334d0b16758f.png)

You can view the [source code for the project here](https://github.com/godanny86/wknd-asset-share).
