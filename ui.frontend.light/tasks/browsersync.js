var gulp        = require('gulp');
var browserSync = require('browser-sync').create();
var cssFiles = ["reset", "site", "button", "container", "divider", "header", "icon", "image", "input", "label", "list", "loader", "placeholder", "rail", "reveal", "segment", "step", "breadcrumb", "form", "grid", "menu", "message", "table", "card", "item", "statistic", "accordion", "checkbox", "dimmer", "dropdown", "embed", "modal", "nag", "popup", "progress", "rating", "search", "shape", "sidebar", "sticky", "tab", "transition", "form", "calendar"];



/*----------------
    Serve
  ------------------*/


module.exports = function(callback) {

    var injectionHTML = cssLinks(cssFiles);
    console.log("Injection: " + injectionHTML);

    browserSync.init({
        proxy: "http://localhost:4502",
        files: ["dist/components/*.css", "dist/components/*.js"],
        serveStatic: ['dist/components'],
        snippetOptions: {
          rule: {
              match: /<\/head>/i,
              fn: function (snippet, match) {
                  return injectionHTML + snippet + match;
              }
          }
      }
    });

}

function cssLinks(fileArray) {
    var linkHTML = '';
    fileArray.forEach(function(file) {
        console.log('file: ' + file);
        linkHTML += '<link rel="stylesheet" type="text/css" href="/' + file + '.css"/>';
    });
    return linkHTML;
}

