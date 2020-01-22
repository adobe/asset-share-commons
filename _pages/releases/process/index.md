---
layout: content-page
title: Release Process
---

## Executing the Automated Process

> The release process can only be executed by Adobe Employees with access to ACS Jenkins Instance.

The release process is performed entirely by the Jenkins pipeline. To perform a release:

1. Log in to the Adobe VPN.
1. Access the [Asset Share Commons pipeline](https://acs.ci.corp.adobe.com/blue/organizations/jenkins/Asset%20Share%20Commons/branches/) on the ACS Jenkins instance.
1. Trigger the pipeline (do *not* replay the last run.)
1. Answer the question on the type of release being performed.
1. Verify the release version.
1. Allow the pipeline to finish.
1. [Close the GitHub milestone](https://github.com/Adobe-Marketing-Cloud/asset-share-commons/milestones) `X.X.X` and create the next Milestone. Ensure all issues associated with the completed milestone are closed.

> It may take several days for the artifacts to be promoted to [repo.adobe.com](https://repo.adobe.com/nexus/content/groups/public/com/adobe/aem/commons/assetshare/), however the artifacts are immediately available on [bintray.com > Set Up](https://bintray.com/asc/releases/asset-share-commons).


## Setting up the Automated Process

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


## Executing the Manual Process (Legacy)

> The release process can only be executed by core contributors with access to dependency systems.

1. On the `/develop`, verify the `CHANGELOG.md` is prepared for release ([Unrelease] -> [vX.X.X])
2. Create a Pull Request from `/develop` -> `/master` with title `vX.X.X Release`
3. Ensure all checks pass (CodeClimate and TravisCI)
2. Keep all commits (do NOT Squash and Merge) when merging `/develop` -> `/master`
4. Checkout `/master` to your local machine; ensure it is up-to-date with `origin/master` via `git pull` and `git status`
5. Ensure that there are no empty directories using `git clean -i`
5. In the same folder as the reactor pom, execute the command:
	* `mvn -Pbintray-release release:prepare`
	* Set the release tag as: `asset-share-commons-X.X.X`
6. When complete, copy the resulting AEM packages from the `ui.apps` and `ui.content` target folders to a safe place (will be used in Step 10)
	* `asset-share-commons.ui.apps-X.X.X.zip`
	* `asset-share-commons.ui.content-X.X.X.zip`
7. Also copy the generated `apidocs` folder from the `ui.core` project's target/site to a safe place.
8. In the same folder as the reactor pom, execute the command:
	* `mvn -Pbintray-release release:perform`
9. [Publish the 16 artifacts on bintray.com](https://bintray.com/asc/releases/asset-share-commons)
10. Contact the Adobe release contact (Simo!), and request an artifact deployment to repo.adobe.com (with key INFRA-5605)
11. [Create a release on GitHub](https://github.com/Adobe-Marketing-Cloud/asset-share-commons/releases) for `asset-share-commons-X.X.X`
	* Upload the 2 artifacts from step 6 to the release.
	* Link to the changelog in the description for the matching release tag commit.
		* https://github.com/Adobe-Marketing-Cloud/asset-share-commons/blob/asset-share-commons-X.X.X/CHANGELOG.md
12. Merge (do NOT squash and merge) `/master` back into `/develop` to update the the pom versions w/ commit message: `vX.X.X-SNAPSHOT`.
13. [Close the GitHub milestone](https://github.com/Adobe-Marketing-Cloud/asset-share-commons/milestones) `X.X.X` and create the next Milestone. Ensure all issues associated with the completed milestone are closed.
14. Copy the `apidocs` from step 7 into the Asset Share Commons GitHub pages site (asset-share-commons/gh-pages branch) at `/apidocs`.

> It may take several days for the artifacts to be promoted to [repo.adobe.com](https://repo.adobe.com/nexus/content/groups/public/com/adobe/aem/commons/assetshare/), however the artifacts are immediately available on [bintray.com > Set Up](https://bintray.com/asc/releases/asset-share-commons).


