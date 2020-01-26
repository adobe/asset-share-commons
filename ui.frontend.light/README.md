# Semantic UI Theme - Light

Includes the Semantic UI as a dedicated front-end module. The module is based on [Semantic UI's build tools](https://semantic-ui.com/introduction/build-tools.html) which relies on [gulp](https://gulpjs.com/). During a maven build, this module triggers the command `gulp build` which will compile the Semantic UI. [aem-clientlib-generator](https://www.npmjs.com/package/aem-clientlib-generator) is used to transform the compiled CSS and JS into a client-side library that is pushed in to the **ui.apps** module.


## Local Development tools

* `npm run prod` - triggers a full build of the Semantic UI library and pushes a compiled client-side library into the ui.apps project
* `npm run dev` - triggers a build of the Semantic UI and then uses [aem-sync](https://www.npmjs.com/package/aemsync) to push just the compiled client-side library to a local instance of AEM
* `npm run start` - Starts a watch for changes made to semantic ui src. Browser-sync tool is used to inject partial css files (the ones that changed) into an HTML document

Full documentation - TBD

