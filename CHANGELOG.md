# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [v1.2.2]

### Fixed
- 0123: Fixed issued with OOTB ContextHub store type nodes not installing.

## [v1.2.0]

### Fixed
- 0114: Removed replication status properties from templates and policies.

### Changed
- 0076: Reduced sample video asset file sizes in ui.content project.
- 0101: User Menu's profile information to be driven via ContextHub rather than uncache-able server-side code; Also added a variety of OOTB context hub stores (profile, surfer info, etc.) 
- 0108: Updated Download Modal to all for the exclusion of original assets in the download zip.
- 0113: Align cards to left in search results.

## [v1.1.2]

### Fixed
- 0102: Fixed the PID for the Asset Share Commons - E-mail Service to the fully qualified class name.
- 0103: Resolve the search results hits using the request's resource resolver to prevent resource leakage.

## [v1.1.0]

### Fixed
- 0074: Removed unused configurations (originally added for release purposes) from ui.content pom.xml
- 0080: Handle the numbering of predicate search components after insert, and made group numbering mode logical.
- 0086: Moved ACS AEM Commons E-mail Service dependency into the Asset Share Commons project to reduce the service resolution issues as reported by #86. This now removes the dependency on ACS AEM Commons.
- 0096: Corrected wording on Share Action checkbox to reflect new behavior. Only generate groupIds for "ready" components.

### Added
- 0070: Added button to 'Remove from Cart' once an asset has been added
- 0090: Added asset-share-commons.cart.clear JavaScript event when cart is cleared

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
