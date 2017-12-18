# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed

- 0080: Set for Predicate components, update the groupId to be derived from the predicate component resource. 

### Fixed
### Added
### Removed

## [v1.0.2]

### Changed

- 0016: Changed ui.content/pom.xml to remove the core dependency, distribution config, and jslint plug-in.  
- 0011: Added skip deploy directive to ui.content pom.xml (as the ui.content artifact does not get deployed to bintray)
- 0012: Updated AEM package file names to be: 'asset-share-commons.ui.apps-<version>' and 'asset-share-commons.ui.content-<version>'.
- 0016: Changed ui.content/pom.xml to remove the core dependency, distribution config, and jslint plug-in.  
- 0018: Updated components to leverage the ASC modelCache for models: Config, AssetModel and PagePredicate. Added HTL Maven Plugin to prevent typos in the HTL.
- 0021: Reduced file sizes of image in ui.content project.
- 0027: XSS Protect user input for Share emails in EmailShareServiceImpl.java

### Fixed

- 0029: Resolve issue with WARN in logs over missing ACS Commons EmailService dependency. 
- 0053: Fixed issue with broken log in and log out links
- 0056: Updated pom.xml to include ui.content as a module. Updated ui.content/pom.xml so only gets built with profile of 'autoInstallPackage-all' and 'autoInstallPackagePublish-all'
