var glob = require("glob");
var path = require("path");
var fs = require('fs');

function walkUpToFindNodeModulesPath(context) {
  var tempPath = path.resolve(context, 'node_modules');
  var upDirPath = path.resolve(context, '../');

  if (fs.existsSync(tempPath) && fs.lstatSync(tempPath).isDirectory()) {
    return tempPath;
  } else if (upDirPath === context) {
    return undefined;
  } else {
    return walkUpToFindNodeModulesPath(upDirPath);
  }
}

function isNodeModule(str) {
  return !str.match(/^\./);
}

module.exports = function(source) {
  this.cacheable && this.cacheable(true);

  var self = this;
  var regex = /.?import + ?((\w+) +from )?([\'\"])(.*?);?\3/gm;
  var importModules = /import +(\w+) +from +([\'\"])(.*?)\2/gm;
  var importFiles = /import +([\'\"])(.*?)\1/gm;
  var importSass = /@import +([\'\"])(.*?)\1/gm;
  var resourceDir = path.dirname(this.resourcePath);

  var nodeModulesPath = walkUpToFindNodeModulesPath(resourceDir);

  function replacer(match, fromStatement, obj, quote, filename) {
    var modules = [];
    var withModules = false;

    if (!filename.match(/\*/)) return match;

    var globRelativePath = filename.match(/!?([^!]*)$/)[1];
    var prefix = filename.replace(globRelativePath, '');
    var cwdPath;

    if (isNodeModule(globRelativePath)) {
      if (!nodeModulesPath) {
        self.emitError(new Error("Cannot find node_modules directory."));
        return match;
      }

      cwdPath = nodeModulesPath;
    } else {
      cwdPath = resourceDir;
    }

    var result = glob
      .sync(globRelativePath, {
        cwd: cwdPath
      })
      .map((file, index) => {
        var fileName = quote + prefix + file + quote;

        if (match.match(importSass)) {
          return '@import ' + fileName;

        } else if (match.match(importModules)) {
          var moduleName = obj + index;
          modules.push(moduleName);
          withModules = true;
          return 'import * as ' + moduleName + ' from ' + fileName;

        } else if (match.match(importFiles)) {
          return 'import ' + fileName;

        } else {
          self.emitWarning('Unknown import: "' + match + '"');
        }
      })
      .join('; ');

    if (result && withModules) {
      result += '; var ' + obj + ' = [' + modules.join(', ') + ']';
    }

    if (!result) {
      self.emitWarning('Empty results for "' + match + '"');
    }

    return result;
  }

  var res = source.replace(regex, replacer);
  return res;
};
