---
layout: content-page
title: Release Process
---

1. Add a new heading to the CHANGELOG.md file with the release number and today's date.  Commit and push this change.
2. If this is a minor release, create two new Milestones in GitHub -- one for the next minor release and one for the first patch release. For example, if you are releasing 2.2.0, create 2.4.0 and 2.2.2.
1. If this is a patch release, create a new Milestone in GitHub for the next patch release. For example, if you are releasing 3.18.2, create 3.18.4.
1. Close the current milestone in GitHub issues.
1. Make sure that the issues and pull requests are associated with the proper milestone -- anything open for the current release should be moved to the next release, either minor or patch depending on the nature of the issue.
1. Ensure Java 11 is active
1. Run the release: mvn release:prepare followed by git checkout master. You may need to pass -Dgpg.passphrase=**** if your passphrase is not persisted in your settings.xml. If you want to enter your passphrase manually at a prompt, add this to .bashrc or execute prior to mvn release: 
    * `export GPG_TTY=$(tty)` and you can verify it works via `echo "test" | gpg --clearsign`
1. Go to https://github.com/Adobe-Marketing-Cloud/asset-share-commons/releases and edit the release tag, using the CHANGELOG data as the release text and attaching the content package zip files (both min and regular) to the release.
1.  Log into https://oss.sonatype.org/ and close the staging repository. Closing the staging repo will automatically push the artifacts to Maven Central after a small delay (4 hours for all mirrors to catch up)
1.  Add a release announcement (and any other docs) to the documentation site.
