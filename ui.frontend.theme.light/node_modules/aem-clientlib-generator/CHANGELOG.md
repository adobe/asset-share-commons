# Changelog

## 1.4.2

### Fixed

- Fix security alerts by updating the dependencies.

## 1.4.1

### Added

- support for _serializationFormat_ in clientLibs configuration to enable FileVault XML or JSON (default) output format

## 1.3.0

### Added

- `categories` can be set in config to override name as category value

## 1.2.4

### Fixed

- added missing dependency `yargs`

## 1.2.3

### Fixed

- issue with clientlibs using relative path delimiters under windows

## 1.2.2

### Added

- support for _allowProxy_ in clientLibs configuration (optional in AEM 6.3)
- support for _longCacheKey_ in clientLibs configuration (optional)

## 1.2.1

### Fixed

- missing glob version

## 1.2.0

### Added

- clientlib CLI with new configuration file `clientlib.config.js`
- options verbose and dry
- add glob feature

## 1.1.0

### Added

- properties _cssProcessor_ and _jsProcessor_ to configure the minification tool to be used for the ClientLib
  (needs AEM 6.2)

## 1.0.1

### Fixed

- file entries in clientlib configuration `js.txt` and `css.txt` will only be added with file extension ".js" or ".css"
  (source maps can be included now)
- fix example

## 1.0.0

- Initial release
