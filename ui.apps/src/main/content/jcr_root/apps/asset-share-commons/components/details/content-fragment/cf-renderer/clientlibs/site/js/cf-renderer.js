jQuery((function($, ns) {
    (function () {

    var fallbackHeader = $(".cmp-details-cf-renderer div h1"),
        contentFragmentContainer = $(".cmp-details-cf-renderer div"),
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

