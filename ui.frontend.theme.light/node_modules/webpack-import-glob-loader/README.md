[![Build Status](https://travis-ci.org/fred104/webpack-import-glob-loader.svg)](https://travis-ci.org/fred104/webpack-import-glob-loader.svg)
[![npm version](https://badge.fury.io/js/webpack-import-glob-loader.svg)](https://badge.fury.io/js/webpack-import-glob-loader)
# webpack-import-glob-loader
ES6 import with glob patterns (preloader for Webpack)

(Forked from https://github.com/terpiljenya/import-glob)

Expands globbing patterns for ES6 `import` statements.

---
```js
import modules from "./foo/**/*.js";
```
Expands into
```js
import * as module0 from "./foo/1.js";
import * as module1 from "./foo/bar/2.js";
import * as module2 from "./foo/bar/3.js";

var modules = [module0, module1, module2]
```
---
For importing from node module
```js
import modules from "a-node-module/**/*js";
```
Expands into
```js
import * as module0 from "a-node-module/foo/1.js";
import * as module1 from "a-node-module/foo/bar/2.js";
import * as module2 from "a-node-module/foo/bar/3.js";

var modules = [module0, module1, module2]
```
---
__For side effects:__

```js
import "./foo/**/*.scss";
```
Expands into
```js
import "./foo/1.scss";
import "./foo/bar/2.scss";
```
---
__For sass:__

```scss
@import "./foo/**/*.scss";
```
Expands into
```scss
@import "./foo/1.scss";
@import "./foo/bar/2.scss";
```

---

## Install
```sh
npm install import-glob-loader --save-dev
```

## Usage
You can use it one of two ways, the recommended way is to use it as a preloader

```js
// ./webpack.config.js

module.exports = {
  ...
  module: {
    rules: [
      {
          test: /\.js$/,
          use: 'webpack-import-glob-loader'
      },
      {
          test: /\.scss$/,
          use: 'webpack-import-glob-loader'
      },
    ]
  }
};
```

Alternatively you can use it as a chained loader
```js
// foo/bar.js
import "./**/*.js";

// index.js
import 'webpack-import-glob-loader!foo/bar.js';
```
