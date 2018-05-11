jQuery((function ($, window, ns) {
    (function () {

        var variationButton =$(".cmp-details-cf-variations .cf-variation-label"),
            originalVariationLabel = $(".cmp-details-cf-variations .original").data("variant"),
            contentFragmentContainer = $(".cmp-details-cf-renderer div");

        function setAllVariationButtonStyle(variant) {
            variationButton.each(function () {
                if (variant === $(this).data("variant")) {
                    $(this).css("background-color", "#2fcc9c");
                    $(this).css("color", "white");
                }
                else {
                    $(this).css("background-color", "");
                    $(this).css("color", "");
                }
            });
        }

        function showVariant() {
            var url = ns.getCFSelectorBasedPath();
            if (url.length > 0) {
                ns.setCFVariation(url,contentFragmentContainer)
            }
        }

        function setAddressBar(variant) {
            var paramsString = window.location.search.replace("?", ''),
                searchParams = new window.URLSearchParams(paramsString),
                newUrl = "";
            if (variant === originalVariationLabel) {
                /*jslint es5: true */
                searchParams.delete("variation");
                /*jslint es5: false */
                newUrl = (searchParams.entries()) ? window.location.pathname : window.location.pathname + "?" + searchParams.toString();
                window.history.pushState({}, 'Original', newUrl);
            } else {
                if (paramsString !== "") {
                    if (searchParams.has("variation")) {
                        searchParams.set("variation", variant);
                        newUrl = window.location.pathname + "?" + searchParams.toString();
                    } else {
                        newUrl = window.location.pathname + window.location.search + "&variation=" + variant;
                    }

                } else {
                    newUrl = window.location.pathname + '?variation=' + variant;
                }
                window.history.pushState({}, 'Variation', newUrl);
            }
        }

        if (contentFragmentContainer.length){
            variationButton.on("click", function (target) {
                var buttonVariant = target.currentTarget.dataset.variant;
                setAllVariationButtonStyle(buttonVariant);
                setAddressBar(buttonVariant);
                showVariant();
            });
        }

    }());
}(jQuery, window, AssetShare.ContentFragment)));

