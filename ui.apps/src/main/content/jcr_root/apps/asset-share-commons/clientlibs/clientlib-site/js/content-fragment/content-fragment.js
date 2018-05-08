    AssetShare.ContentFragment = (function ($, window, ns) {

    function setHtml(elem, htmlStr, loader) {
        if(loader)
        	loader.hide();
        if(elem)
        	elem.html(htmlStr);
    }

    function setCFVariation(url, contentFragmentContainer, fallbackHeader, placeholderImgContainer, loader) {
        $.get(url, function(result) {
            if (result.length === 0) {
                setHtml(fallbackHeader, fallbackMsg, loader);
            } else {
                setHtml(contentFragmentContainer, result, loader);
            }
        }).fail(function() {
            if (loader)
            	loader.hide();
			if(fallbackHeader)
            	fallbackHeader.hide();
            if(placeholderImgContainer)
            	placeholderImgContainer.css("display", "block");
        });
    }

    function getCFSelectorBasedPath() {
        var originalVarSelPath = "",
            cfVarSelPath = "",
            regex = /\.html/,
            pathList = window.location.pathname.split(regex),
            queryString = window.location.search.replace("?", ''),
            queryParams = queryString.split("&"),
            variationParam = queryParams.filter(function(param) { return param.includes("variation="); });

        if (pathList.length === 3) {
            originalVarSelPath = pathList[2] + ".cfm.content.html";
            if (pathList[2] === "") {
                originalVarSelPath = "";
            }
        } else if (pathList.length === 2) {
            originalVarSelPath = pathList[1] + ".cfm.content.html";
            if (pathList[1] === "") {
                originalVarSelPath = "";
            }
        }


        if (variationParam.length === 0) {
            cfVarSelPath = originalVarSelPath;
        } else if (variationParam.length === 1) {
            cfVarSelPath = originalVarSelPath + "?" + variationParam.toString();
        }
        return cfVarSelPath;
    }

    return {
        getCFSelectorBasedPath: getCFSelectorBasedPath,
        setCFVariation: setCFVariation
    };

    }(jQuery, window, AssetShare));


