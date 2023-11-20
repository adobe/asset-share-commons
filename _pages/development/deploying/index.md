---
layout: development-page
title: Deploying
---

Asset Share Commons should always be included as a package *dependency* in your AEM Maven project. It is *not* recommended to ever deploy Asset Share Commons source code directly, unless you plan to fork the project entirely - however this likely means you will not be able to enjoy later enhancements or bug fixes.

## Prerequisites

Ensure your AEM Maven project follows the latest [Maven project structure best practices](https://experienceleague.adobe.com/docs/experience-manager-cloud-service/implementing/developing/aem-project-content-package-structure.html). This can be create using the latest [AEM Project Archetype](https://github.com/adobe/aem-project-archetype).


## Including Asset Share Commons application

Include Asset Share Commons 2.0.0+ `all` project as an `embedded` in your AEM Maven project's `all/pom.xml`.

1. Add the Asset Share Commons `all` project as `<dependency>`.

    ```
    <dependencies>
        ...
        <dependency>
            <groupId>com.adobe.aem.commons</groupId>
            <artifactId>assetshare.all</artifactId>
            <version>3.x.x</version>
            <!-- Add the cloud classifier when deploying to AEM as a Cloud Service; omit if deploying to AEM 6.5 -->
            <classifier>cloud</classifier>  
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


## Custom development using Asset Share Commons APIs

1. Optionally, include the `assetshare.core` as a dependency in your AEM project's `core/pom.xml` if you plan developing Java code against Asset Share Commons' APIs.

    ```
    <dependencies>
        ...
        <dependency>
            <groupId>com.adobe.aem.commons</groupId>
            <artifactId>assetshare.core</artifactId>
            <version>3.x.x</version>
            <type>jar</type>
        </dependency>
        ...
    </dependency>    
    ```
