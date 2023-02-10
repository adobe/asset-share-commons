# aem-clientlib-generator

A node plugin that creates ClientLib configuration files (repository nodes) for
[AEM Client Libraries](https://helpx.adobe.com/experience-manager/6-3/sites/developing/using/clientlibs.html),
creates _Client Library Folders_ and synchronizes all assets.

It supports both JSON file format (default) and FileVault XML file format (see `serializationFormat` parameter).


## Installation
```bash
npm install aem-clientlib-generator
```


## Usage

### Command Line Interface
The CLI `clientlib` is located in `./bin/clientlib-cli.js`.
The command can be used without parameters, it loads the default configuration file `clientlib.config.js`.
More options are described in help menu:

```text
Options:
  --help, -h     Show help                                             [boolean]
  --version, -v  Show version number                                   [boolean]
  --dry          'Dry run' without write operations.                   [boolean]
  --verbose      Prints more details                                   [boolean]
```

#### clientlib.config.js
A clientlib configuration file is a simple exported module:
```js
module.exports = {
  // default working directory (can be changed per 'cwd' in every asset option)
  context: __dirname,

  // path to the clientlib root folder (output)
  clientLibRoot: "path/to/clientlib-root",

  // define all clientlib options here as array... (multiple clientlibs)
  libs: [
    {
      name: "test.base.apps.mainapp",

      // optional override path to write clientlib files to, by default files
      // are written to lib.name/
      outputPath: "explicit/path/to/lib/or/existing/lib/structure",

      assets: {
        js: [
          "src/frontend/js/app.js"
        ],
        css: [
          "src/frontend/css/styling.css"
        ]
      }
    },
    ...// next clientlibs
  ],

  // or as object (single clientlib)
  libs: {
    name: "test.base.apps.mainapp",
    assets: {
      js: [
        "src/frontend/js/app.js"
      ],
      css: [
        "src/frontend/css/styling.css"
      ]
    }
  }
}
```

#### npm scripts
The CLI can be used in a project as local module via npm scripts (defined in `package.json`).
```js
  // package.json file:

  "scripts": {
    "test": "mocha",
    "build": "clientlib --verbose"
  }
```
In this case `npm run build` tries to load the default clientlib configuration file
`clientlib.config.js` (same directory like package.json) and generates all clientslib
as defined.


### Module: clientlib(arrProps | props, [options], callback)

Import the module into a JavaScript file and run the module as a function:
```js
var clientlib = require("aem-clientlib-generator");
clientlib(arrProps, { verbose: true }, function() {
  console.log("generator has finished");
});
```
**Important:** Due to many write operations, the `clientlib` function is **asynchronous**!

* `arrProps` `{Array<Object>}` Array of Clientlib configuration properties (see below)
* `props` `{Object}` Clientlib configuration properties
  * `path` `{String}` Clientlib root path (optional if `options.clientLibRoot` is set)
  * `outputPath` `{String}` Clientlib destination path (optional, overrides default behavior of writing to the above path or options.clientLibRoot, useful to supply your own directory naming convention or if you are clientlib-ifying an existing directory)
  * `name` `{String}` Clientlib name (required)
  * `serializationFormat` `{String}` Type of the target archive for which the resources must be generated [json|xml] (optional, default=json)
  * `embed` `{Array<String>}` other Clientlib names that should be embedded (optional)
  * `dependencies` `{Array<String>}` other Clientlib names that should be included (optional)
  * `categories` `{Array<String>}` to set a category for the clientLib (optional), ovrrides the default that uses the name as category
  * `cssProcessor` `{Array<String>}` configuration for the clientlib CSS processor, requires AEM 6.2 (optional)
  * `jsProcessor` `{Array<String>}` configuration for the clientlib JS processor, requires AEM 6.2 (optional)
  * `assets` `{Object}` content that should be copied to the clientlib folder, more details below (required)
  * `allowProxy` `{Boolean}` allow for Clientlib creation under `/apps/myapp/clientLibs` but enable proxy to `/etc.clientlibs/myapp/clientlibs/mylib` See [AEM 6.3 Documentation](https://docs.adobe.com/docs/en/aem/6-3/develop/the-basics/clientlibs.html#Locating%20a%20Client%20Library%20Folder%20and%20Using%20the%20Proxy%20Client%20Libraries%20Servlet)
  * `longCacheKey` `{String}` optional string with placeholders to use with URL Fingerprinting, eq. `"${project.version}-${buildNumber}"`. This requires the [build-helper-maven-plugin](http://www.mojohaus.org/build-helper-maven-plugin/usage.html) to be configured, see [wcm-io-samples - Clientlibs](https://github.com/wcm-io/wcm-io-samples/blob/develop/bundles/clientlibs/pom.xml#L56).

* `options` `{Object}` global options to be used for all clientlib definitions (optional)
  * `clientLibRoot` {String} Clientlib root path
  * `context` {String} changes the current working directory (via `process.chdir()`)
  * `cwd` {String} alias for `context`
  * `verbose` {Boolean} prints detailed information during generation
  * `dry` {Boolean} dry run without file write operations (sets automatically verbose to true)


* `callback` `{Function}` to be called if clientlib() has finished

### The `assets` Object

The `assets` object determine the content that should be pushed into the clientlib folder. The key stands for
the content type, `js` for JavaScript files, `css` for styles and `resources` for other content such as
fonts or images.

```javascript
{
  js: {
    // JavaScript files to be copied and used for `js.txt` - a clientlib JS configuration file
  },
  css: {
    // CSS files to be copied and used for `css.txt` - a clientlib CSS configuration file
  },
  resources: {
    // other resources that should be copied
  }
}
```

Each property can be an object of deeper configuration options (`assetConfig`) or an array of files (simple way, see example below).
The following can be configured:

* `assetConfig` `{Object}` Configuration object for an asset type
  * `base` `{String}` path within the clientlib folder where the data should be copied to (optional), default: asset key, e.g. for "js" is the base "js"
    * Hint: Using "." copies the files into the clientlib folder instead of the subfolder
  * `files` `{Array<String|Object>}` array of file paths (sources) or a src-dest key value map (required)
    * Important: The order of JS or CSS files in this property defines the merging/bundling order in AEM clientlib.
    * file object contains:
      * `src` {String} - source file relative to the current working directory or the global `cwd` option, if set
      * `dest` {String} - destination relative to the clientlib folder including base
  * `cwd` {String} - change working directory (relative to the context / global `cwd` option); only available with glob pattern
  * `flatten` {Boolean} - using file's basename instead of folder hierarchy; default true; only available with glob pattern
  * `ignore` `{String|Array<String>}` - glob pattern or array of glob patterns for matches to exclude

For an glob example see example section below.

```javascript
// simple version
js: [
  "pth/to/file.js",
  {src:"pth/to/lib/file.js", dest: "lib/file.js"}
]
// will be transformed to:
js: {
  base: "js"
  files: [
    {src:"pth/to/file.js", dest: "file.js"},
    {src:"pth/to/lib/file.js", dest: "lib/file.js"}
  ]
}
```

### Example
```javascript

var clientlib = require("aem-clientlib-generator");
clientlib([
  {
    name: "test.base.apps.mainapp",
    // the name will be used as subfolder in clientlibs root and for the AEM repository node
    // in this example it creates:
    //   the subfoler: path/to/clientlibs-root/test.base.apps.mainapp/
    //   repository node: path/to/clientlibs-root/test.base.apps.mainapp.json

    // new in AEM 6.2: configure the clientlib processor by yourself:
    // An example to disable minification for CSS:
    cssProcessor: ["default:none", "min:none"],

    // using google closure compiler for minification instead of YUI
    jsProcessor: ["default:none", "min:gcc;compilationLevel=whitespace"],

    // new in AEM 6.3: create clientLibs in /apps/myapp/clientlibs and proxy to /etc.clientlibs/myapp
    allowProxy: true,

    // allow URL Fingerprinting via placeholder
    longCacheKey: "${project.version}-${buildNumber}",

    assets: {

      // creates the JS configuration file:
      //  path/to/clientlibs-root/test.base.apps.mainapp/js.txt
      // which lists all JavaScript files from the ClientLib.
      // and copies all files into a js subfolder (default base):
      //  path/to/clientlibs-root/test.base.apps.mainapp/js/
      js: [

        // file will be copied to:
        //  path/to/clientlibs-root/test.base.apps.mainapp/js/app.js
        {src: "src/frontend/js/app.js", dest: "app.js"},

        // file will be copied to:
        //  path/to/clientlibs-root/test.base.apps.mainapp/js/libs/mylib.min.js
        {src: "src/frontend/js/libs/mylib.min.js", dest: "libs/mylib.min.js"},

        // copy source map files as well
        {src: "src/frontend/js/libs/mylib.min.js.map", dest: "libs/mylib.min.js.map"}
      ],

      // creates the CSS configuration file:
      //  path/to/clientlibs-root/test.base.apps.mainapp/css.txt
      css: [
        "src/frontend/css/styling.css",
        "src/frontend/css/lib.css"
      ]
    }
  },
  {
    name: "test.base.apps.secondapp",
    embed: [
      "test.base.apps.thirdapp"   // this clientlib will be auto embedded in AEM (kind of `merging`)
    ],
    dependencies: [
      "test.base.apps.mainapp"    // define clientlib dependency
    ],
    assets: {
      js: {
        base: "js", // by default the `base` is the asset key property
        files: [
          {src: "src/frontend/secondapp/js/lib.js", dest: "secondapp-lib.js"}
        ]
      },

      // creates the CSS configuration file:
      //  path/to/clientlibs-root/test.base.apps.secondapp/css.txt
      // that lists all CSS files from the ClientLib.
      // All files defined below will be copied into the defined base:
      //  path/to/clientlibs-root/test.base.apps.secondapp/style/
      css: {
        base: "style", // changes the `base` from `css` (default) to `style`
        files: [
          "src/frontend/secondapp/main.css"
        ]
      },
      resources: [
        "src/frontend/resources/template.html"
      ]
    }
  },
  {
    name: "test.base.apps.thirdapp",
    assets: {

      // copy all files into the clientlib subfolder, because `base` is changed:
      //  path/to/clientlibs-root/test.base.apps.thirdapp/
      resources: {
        base: ".", // copy the file into `test.base.apps.thirdapp` (root) instead of `test.base.apps.thirdapp/resources`
        files: [
          "src/frontend/resources/notice.txt"
        ]
      }
    }
  },
  {
    name: "test.base.apps.fourth",
    assets: {
      js: {
        // "flatten" is true by default and using file's basename instead of path for destination
        // set to false to keep the folder hierarchy below "cwd"
        flatten: false, // remove this option if you like a flat list of files in your clientlib
        cwd: "src/frontend/js/", // change working directory (will be stripped from destination)
        files: [
          "**/*.js",  // match all js files recursively
          "**/*.js.map"
        ]
      },
      css: [
        // all css will copied to destination folder "style" (in base folder css)
        {src: "src/frontend/css/*.css", dest: "style/"},

        // all css will copied to destination folder "vendor" (in base folder css)
        {src: "src/frontend/secondapp/*.css", dest: "vendor/"}
      ]
    }
  },
  {
    name: "test.base.apps.myExistingAssetOrganization",
    outputPath: path.join(__dirname, 'libs', 'collectionOne'),
    assets: {

      // uses existing files at ./libs/collectionOne, since base is set to '.'
      js: {
        base: ".", // copy the file into `./libs/collectionOne` (outputPath) instead of `{path}/test.base.apps.myExistingAssetOrganization/js`
        files: [
          "libs/collectionOne/index.js"
        ]
      }
    }
  }
],
{
  cwd: __dirname, // using folder of the file as current working directory
  clientLibRoot: path.join(__dirname, "path/to/clientlibs-root")
},
function() {
  console.log("clientlibs created");
});
```
### Deploying to AEM:

The generated client library can be deployed to AEM via [Sling Content Loading](https://sling.apache.org/documentation/bundles/content-loading-jcr-contentloader.html). Take a look at the [wcm.io Sample Application](https://github.com/wcm-io/wcm-io-samples/tree/develop/bundles/clientlibs).

If you've switched the `serializationFormat` to "xml" you can deploy the client library as part of an AEM content package.
