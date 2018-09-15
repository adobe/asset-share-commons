---
layout: content-page
title: Release Process
---

> The release process can only be executed by core contributors with access to dependency systems.

1. On the `/develop`, verify the `CHANGELOG.md` is prepared for release ([Unrelease] -> [vX.X.X])
2. Create a Pull Request from `/develop` -> `/master` with title `vX.X.X Release`
3. Ensure all checks pass (CodeClimate and TravisCI)
2. Keep all commits (do NOT Squash and Merge) when merging `/develop` -> `/master`
4. Checkout `/master` to your local machine; ensure it is up-to-date with `origin/master`
5. Ensure that there are no empty directories using `git clean -i`
5. In the same folder as the reactor pom, execute the command:
	* `mvn -Pbintray-asset-share-commons release:prepare`
	* Set the release tag as: `asset-share-commons-X.X.X`
6. When complete, copy the resulting AEM packages from the `ui.apps` and `ui.content` target folders to a safe place (will be used in Step 10)
	* `asset-share-commons.ui.apps-X.X.X.zip`
	* `asset-share-commons.ui.content-X.X.X.zip`
7. Also copy the generated `apidocs` folder from the `ui.core` project's target/site to a safe place.
8. In the same folder as the reactor pom, execute the command:
	* `mvn -Pbintray-asset-share-commons release:perform`
9. [Publish the 16 artifacts on bintray.com](https://bintray.com/asc/releases/asset-share-commons)
10. Contact the Adobe release contact (Simo!), and request an artifact deployment to repo.adobe.com (with key INFRA-5605)
11. [Create a release on GitHub](https://github.com/Adobe-Marketing-Cloud/asset-share-commons/releases) for `asset-share-commons-X.X.X`
	* Upload the 2 artifacts from step 6 to the release.
	* Link to the changelog in the description for the matching release tag commit.
		* https://github.com/Adobe-Marketing-Cloud/asset-share-commons/blob/asset-share-commons-X.X.X/CHANGELOG.md
12. Merge (do NOT squash and merge) `/master` back into `/develop` to update the the pom versions.
13. [Close the GitHub milestone](https://github.com/Adobe-Marketing-Cloud/asset-share-commons/milestones) `X.X.X` and create the next Milestone.
14. Copy the `apidocs` from step 7 into the Asset Share Commons GitHub pages site (asset-share-commons/gh-pages branch) at `/apidocs`.

> It may take several days for the artifacts to be promoted to [repo.adobe.com](https://repo.adobe.com/nexus/content/groups/public/com/adobe/aem/commons/assetshare/), however the artifacts are immediately available on [bintray.com > Set Up](https://bintray.com/asc/releases/asset-share-commons).
