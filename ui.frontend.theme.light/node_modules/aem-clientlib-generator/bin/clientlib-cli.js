#!/usr/bin/env node

var DEFAULT_FILE = "clientlib.config.js";
var clientlib = require("../lib/clientlib");

var path = require("path");
var fs = require("fs");
var yargs = require("yargs")
  .usage("aem-clientlib-generator " + require("../package.json").version + "\n" +
    "Usage with config file: clientlib [path] [options]" + "\n\n" +
    "Default config path: " + DEFAULT_FILE);

yargs
  .help("help")
  .alias("help", "h")
  .version()
  .alias("version", "v")
  .options({
    "dry": {
      type: "boolean",
      describe: "'Dry run' without write operations."
    },
    "verbose": {
      type: "boolean",
      describe: "Prints more details"
    }
  }).strict();

var argv = yargs.argv;
var configPath = path.resolve(process.cwd(), DEFAULT_FILE);

if (argv._ && argv._.length > 0) {
  configPath = argv._[0];
  if (!path.isAbsolute(configPath)) {
    configPath = path.resolve(process.cwd(), configPath);
  }
}

if (!fs.existsSync(configPath)) {
  console.error("Could not find config file: " + configPath);
  process.exit(1);
}

var clientLibConf = require(configPath);
var libs = clientLibConf.libs;
delete clientLibConf.libs;

clientLibConf.dry = argv.dry;
clientLibConf.verbose = argv.verbose || argv.dry;

clientlib(libs, clientLibConf);
