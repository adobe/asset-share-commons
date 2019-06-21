---
layout: content-page
title: Release Process
---


## Executing the Process

> The release process can only be executed by Adobe Employees with access to ACS Jenkins Instance.


The release process is performed entirely by the Jenkins pipeline. To perform a release:

1. Access the [AEM Modernize Tools pipeline](https://acs.ci.corp.adobe.com/blue/organizations/jenkins/AEM%20Modernize%20Tools/branches/) on the ACS Jenkins instance.
1. Trigger the pipeline (do *not* replay the last run.)
1. Answer the question on the type of release being performed.
1. Verify the release version.
1. Allow the pipeline to finish.
1. [Close the GitHub milestone](https://github.com/Adobe-Marketing-Cloud/asset-share-commons/milestones) `X.X.X` and create the next Milestone. Ensure all issues associated with the completed milestone are closed.

> It may take several days for the artifacts to be promoted to [repo.adobe.com](https://repo.adobe.com/nexus/content/groups/public/com/adobe/aem/commons/assetshare/), however the artifacts are immediately available on [bintray.com > Set Up](https://bintray.com/asc/releases/asset-share-commons).



## Setting it up

The pipeline setup has already been performed. But as a mechanism for documentation in the event it needs to be recreated:

### Github Access

First thing's first, Jenkins will be accessing the Github repo. This pipeline uses a system account `adobe-acsbuild`.

#### API Token
1. On Github create a new [Access Token](https://help.github.com/en/articles/creating-a-personal-access-token-for-the-command-line), the only access scope required is *repo*.
1. Create a new *Credential* entry with the access token for the password. The **ID** value must match the pipeline reference.

<p class="image">
    <img src="{{ site.baseurl }}/pages/releases/process/images/github-token-credential.png" alt="ACSBuild Github Token Credential"/>
</p>


### BinTray Access

We deploy the artifacts to Bintray so we need to setup those credentials.  

1. Get the API Key from the Abode ACSBuild Bintray account.

1. Create a new *Credential* entry with the access token for the password.

<p class="image">
    <img src="{{ site.baseurl }}/pages/releases/process/images/bintray-token-credential.png" alt="ACSBuild Bintray Token Credential"/>
</p>


### Maven Settings


The project's POM is already configured for the Bintray Adobe repository & package, we just need to supply the credentials. This is done through a custom build *settings.xml*. The pipeline is also pre-configured to reference this file - we need to a) make sure it exists, and b) has the expected credentials.

1. Check to see if an existing *Maven Settings* exists with an **ID** of `acsbuild-settings`. If so edit the existing one, otherwise create a new one with this **ID**.
1. Give it an appropriate name and description if it did not previously exist.
1. Add two credentials to the *Server Credentials* section:
  * ACSBuild Github Token credentials, the *ServerId* is `adobe-acsbuild-github-token` (this is the value from the pom's `project.scm.id`).
   * ACSBuild Bintray credentials, the *ServerId* is `bintray-asc-releases` (this is the value from the pom's `distributionManagement`).

<p class="image">
    <img src="{{ site.baseurl }}/pages/releases/process/images/acsbuild-settings.png" alt="ACSBuild Maven Settings"/>
</p>

### Tools

Install the JKD 8 and Maven 3.5 tools on the Jenkins server. Name them `JKD8` and `Maven 3.5` respectively. (You can install newer versions, or name them differently, just make sure to update the Jenkinsfile references.) 

### Plugins

The pipeline makes use of a few plugins, not all of which are likely to be installed by default. Install the following Jenkins Plugins:

* HTTP Request


### Pipeline Job

The pipeline is self-contained - so we just need to configure the actual Jenkins job definition.

1. Create a new *Multibranch Pipeline* job, title `Asset Share Commons`
1. Fill out the configuration, anything not shown in this image can be left with the default value.

<p class="image">
    <img style="max-width: 800px" src="{{ site.baseurl }}/pages/releases/process/images/jenkins-job-configuration.png" alt="Asset Share Commons Jenkins configuration"/>
</p>


#### Script Approvals

The first run of a release may error at the Metadata or Github Release stage, due to a security check. Scripted pipelines are not permitted access the SCM User Config or to instantiate JsonSlurperClassic used to parse Github API responses. Approving the following signatures will resolve this and allow the pipeline to complete successfully.

Also, to parse a response from the Github API we need to approve the JsonSlurper API as well.

This may be resolved at a future date if we decide to move these steps to a Shared Library, instead of in the pipeline itself. For now we are trying to keep everything self-contained.

<p class="image">
    <img src="{{ site.baseurl }}/pages/releases/process/images/script-approvals.png" alt="Jenkins Script Approvals"/>
</p>



