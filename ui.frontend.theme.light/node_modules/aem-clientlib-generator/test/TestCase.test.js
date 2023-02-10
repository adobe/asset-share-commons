/*
 *  Copyright (c) 2016 pro!vision GmbH and Contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

"use strict";

var fs = require("fs");
var walk = require("klaw");
var fse = require("fs-extra");
var clientlib = require("../lib/clientlib");
var fileExists = require("../lib/clientlib").fileExists;
var path = require("path");
var assert = require('assert');

var clientLibConf = require("./clientlib.config");

var resultDir = path.join(__dirname, "result");
var expectedDir = path.join(__dirname, "expected");


describe("Test output", function() {

  // cleanup result folder
  beforeEach(function(){
    fse.removeSync(resultDir);
    fs.mkdirSync(resultDir);
  });

  it("should create files correctly", function(done) {

    var libs = clientLibConf.libs;
    delete clientLibConf.libs;

    clientlib(libs, clientLibConf, function() {

      var items = []; // files, directories, symlinks, etc
      walk(expectedDir)
        .on("data", function (item) {
          items.push(item.path)
        })
        .on("end", function () {

          items.forEach(function(expectedFile) {
            var subFilePath = path.relative(expectedDir, expectedFile);
            if (!subFilePath) {
              return;
            }

            var resultFile = path.join(resultDir, subFilePath);

            assert.ok(fileExists(resultFile), "file does not exist in result: " + subFilePath);

            if (!fs.lstatSync(expectedFile).isDirectory()) {
              var result = fs.readFileSync(resultFile, "utf-8").replace(/\r\n/g, "\n");
              var expected = fs.readFileSync(expectedFile, "utf-8").replace(/\r\n/g, "\n");

              assert.equal(result, expected, "content of " + subFilePath + " is not expected");
            }

          });

          done();
        });
    });
  });
});
