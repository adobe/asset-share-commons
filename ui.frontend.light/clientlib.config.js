module.exports = {
    // default working directory (can be changed per 'cwd' in every asset option)
    context: __dirname,

    // path to the clientlib root folder (output)
    clientLibRoot: "./../ui.apps/src/main/content/jcr_root/apps/asset-share-commons/clientlibs/clientlib-theme",

    libs: {
        name: "semanticui-light",
        allowProxy: true,
        categories: ["asset-share-commons.semantic-ui-light"],
        embed: ["asset-share-commons.site.semantic-ui","asset-share-commons.site.semantic-ui.components"],
        dependencies: ["asset-share-commons.base"],
        serializationFormat: "xml",
        cssProcessor : ["default:none", "min:none"],
        jsProcessor: ["default:none", "min:none"],
        assets: {
            js: [
                "dist/semanticui-light/semantic.min.js"
            ],
            css: [
                "dist/semanticui-light/semantic.min.css"
            ],
            resources: [
                {
                    src: "dist/resources/default/assets/fonts/*",
                    dest: "assets/fonts"
                },
                {
                    src: "dist/resources/light/assets/fonts/*",
                    dest: "assets/fonts"
                }
            ]
        }
    }
};
