jQuery((function($, ns) {
    (function () {

    var fallbackHeader = $(".cf-render h1"),
        contentFragmentContainer = $(".cf-render"),
        placeholderImgContainer = $(".cf-placeholder"),
        fallbackMsg = contentFragmentContainer.data("fallback-msg"),
        loader = $(".loader"),
        url = "";

    if (contentFragmentContainer.length) {
        url = ns.getCFSelectorBasedPath();
        if (url.length > 0) {
            ns.setCFVariation(url, contentFragmentContainer, fallbackHeader, placeholderImgContainer, loader);
        } else {
            loader.hide();
            fallbackHeader.hide();
            placeholderImgContainer.css("display", "block");
        }
    }


    }());
}(jQuery, AssetShare.ContentFragment)));

