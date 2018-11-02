# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [v1.6.10]

### Fixed
- 0259: Fixed issue with the Statistics component misreporting how many more results are available.
- 0301: Files having special character in the filename. Download, Share and Add to Cart do not work.
- 0307: Fixed issue with Details page Image component's fallback is not used for non-image assets. 
- 0308: Fixed issue where unsupported (by the browser) image asset types (ex. DFX) are used for image display in browser (thumbs/preview) instead of placeholder.
- 0311: Empty metadata fields fail to hide or display emptyText

### Changes
- 0294: Changes (JS) AssetShare.Navigation.goToTop(..) to avoid using window.location.hash which pushes state to browser's history and prevents use of the browser's back button.

## [v1.6.8]

### Fixed
- 0283: AssetDetails404Servlet uses sendError(..) to set 404 status, allowing it to work with Sling Error Handlers.
- 0285: Modal DOM elements are now removed when then modal is hidden.
- 0288: AssetDetails404Servlet handles UUID based asset details pages.

### Changed
- 0290: Modals emit JS event when they are shown.

## [v1.6.6]

### Fixed
- 0275: Revert inclusion of Core Components 2.1.0 as it breaks compatability with AEM 6.3 SP1
- 0276: Corrected resource injection strategy in SearchConfigImpl that resulted in the model being instantiatable on 6.3.x

### Fixed
- 0275: Resolves issue with offset not being set.
- 0255: Removed need for "generic" Sort By and Sort Direction labels; added intelligence to get default values from Search Results 
## [v1.6.4]

### Fixed
- 0260: Resolves issue with offset not being set.
- 0255: Removed need for "generic" Sort By and Sort Direction labels; added intelligence to get default values from Search Results component.
- 0254: HTTP query param sort parameters are not reflected in Sort component
- 0249: Fixed issue when Search Statistics (or an other component that uses Search model) is placed before the Search components, resutling in 0 results.
- 0248: Issue with HTTP parameter QB groups and server-side provided (group_3 would mix in with server-side paths)
- 0227: Sites editor is missing workflow status information
- 0237: Fixes issue with ContextHub being unloaded after a Form submissions via modals.
- 0240: Fixed issue with submitted date-range search values lagging behind actual value by one submission.
- 0231: metadataFieldTypes does not filter data source for Date Range and Tag Filters
- 0192: Updated included Core Components to v2.1.0

## [v1.6.2]

### Changed
- 0069: Fixes issue with request URI being too long for modals by switching from GET to POST

### Fixed
- 0218: Fixes issue with Action Buttons' Download not working for Licensed assets when Licensing is disabled.
- 0221: Fixes logic for Dynamic Media download modal to display a dropdown when image presets are set 

## [v1.6.0]

### Added
- 0208: Addition of Smart Tags Computed Property and support of Smart Tags in the Tags Asset Details Component.
- 0184: Added Search Predicates framework and provided OOTB implementations for: Exclude Content Fragments, Exclude Expired Assets, Exclude Sub-assets.
- 0182: Added resource providers for Search and Asset Details pages that warn about mis-configurations of Asset Share Commons in AEM Author.
- 0191: Support alphabetical or natural ordering of Tags in in the Tags search predicate. Fixed issues with Source options in dialog as well.

### Fixed
- 0204: For mobile and tablet view, the filter rail should slide from left.
- 0195: Search does not work in IE11 - Missing findIndex() & find() methods.

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
