# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [v1.5.2]

- 0177: Removed cache=true on all Sling Model definitions due to memory leaks.(See https://issues.apache.org/jira/browse/SLING-7586)
- 0168: Fixed issue with the rail rendering in Authoring mode.

## [v1.5.0]

### Added
- 0034/0046: Dynamic Media Download modal and image presets datasource
- 0147: Asset Details Video component added to provide in-page video playback on Asset Details pages.

### Fixed

- 0126: Updated the include of AEM Responsive Grid's grid_base.less to a singular file copied into the Asset Share Commons codebase to support AEM 6.3.1 and AEM 6.4 in the same package.   
- 0156: Asset cart does not populate correctly in AEM 6.4.0.
- 0149: The Metadata Properties datasource now includes multi-value text widgets defined on the AEM Assets Metadata Schemas.
- 0152: Fixed issue with leaking resource resolver in QueryBuilder APIs. This was previously thought to be fixed in v1.2.2 #0103. Note this fix is also back-ported to v1.1.4. 

## [v1.4.0]

### Changed

- 0141: Updated Search results to request the main and rail content to allow for more simpler and more robust use of data-asset-share-update-method.

## [v1.3.0]

### Fixed
- 0131: Fixed ContextHub eventing condition that cause the user menu profile to act as "anonymous" on the first page via by an auth'd user.

### Added
- 0128: Path Filter search component.
- 0130: Added auto-search capabilities to search predicate components.
- 0134: Allow multiple ShareServices to be registered and allow each to accept the request.

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
