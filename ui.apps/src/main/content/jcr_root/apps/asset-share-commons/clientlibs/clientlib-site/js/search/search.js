/*
 * Asset Share Commons
 *
 * Copyright [2017]  Adobe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*global jQuery: false, AssetShare: false, window: false */

AssetShare.Search = (function (window, $, ns, ajax) {
    "use strict";

    var EVENT_SEARCH_TYPE_FULL = "search",
        EVENT_SEARCH_TYPE_LOAD_MORE = "load-more",

        ACTION_SEARCH = "search",
        ACTION_DEEP_LINK = "deep-link",
        ACTION_LOAD_MORE = "load-more",
        ACTION_SORT = "sort",
        ACTION_SWITCH_LAYOUT = "switch-layout",
        DISCOVERY_COMMAND = "/discovery",

        running = false,
        queryParamsOverride = null,

        form = ns.Search.Form(ns);

    function getForm() {
        return form;
    }

    function trigger(eventType, params) {
        $("body").trigger(eventType, params);
    }

    function setAddressBar(queyParams) {
        if (ns.Util.isSameOrigin()) {
            ns.Navigation.addressBar(window.top.location.pathname + "?" + queyParams);
        } else {
            ns.Navigation.addressBar(window.location.pathname + "?" + queyParams);
        }

        ns.Navigation.returnUrl(window.location.pathname + "?" + queyParams);
    }

    function processSearch(fragmentHtml) {
        ns.Elements.update(fragmentHtml, ACTION_SEARCH);

        ns.Navigation.gotoTop();
        setAddressBar(queryParamsOverride || form.serializeFor(ACTION_DEEP_LINK));
        queryParamsOverride = null;

        trigger(ns.Events.SEARCH_END, [EVENT_SEARCH_TYPE_FULL]);
        running = false;
    }

    function getDiscoveryQueryElement() {
        return ns.Elements.element("discovery-query");
    }

    function getDiscoveryQueryOutputElement() {
        return ns.Elements.element("discovery-query-output");
    }

    function getDiscoveryEndpoint() {
        return ns.Data.attr(ns.Elements.element("discovery-search"), "discovery-agent-endpoint") || "/discovery";
    }

    function isDiscoveryCommandValue(value) {
        return value && new RegExp("^" + DISCOVERY_COMMAND + "(\\s|$)").test($.trim(value));
    }

    function getSearchPrompt() {
        var searchInput = $("#" + form.id()).find(":input").add($("[form='" + form.id() + "']")).filter(function() {
            return isDiscoveryCommandValue($(this).val());
        }).first();

        return $.trim(searchInput.val()).substring(DISCOVERY_COMMAND.length).replace(/^\s+/, "");
    }

    function getDiscoverySearchFieldNames() {
        return $("#" + form.id()).find(":input").add($("[form='" + form.id() + "']")).filter(function() {
            return isDiscoveryCommandValue($(this).val());
        }).map(function() {
            return $(this).attr("name");
        }).get();
    }

    function isDiscoverySearch() {
        return getSearchPrompt().length > 0;
    }

    function objectToQueryString(queryObject) {
        var params = [];

        if (!queryObject) {
            return "";
        }

        $.each(queryObject, function(key, value) {
            if (Array.isArray(value)) {
                value.forEach(function(arrayValue) {
                    params.push({name: key, value: arrayValue});
                });
            } else {
                params.push({name: key, value: value});
            }
        });

        return $.param(params);
    }

    function parseDiscoveryResponse(response) {
        var query = response;

        if (typeof query === "string") {
            try {
                query = JSON.parse(query);
            } catch (e) {
                return query;
            }
        }

        if (query.query) {
            query = query.query;
        } else if (query.queryBuilderQuery) {
            query = query.queryBuilderQuery;
        } else if (query.querybuilder) {
            query = query.querybuilder;
        } else if (query.queryBuilder) {
            query = query.queryBuilder;
        } else if (query.queryParameters) {
            query = query.queryParameters;
        } else if (query.params) {
            query = query.params;
        }

        if (typeof query === "string") {
            return query;
        }

        return objectToQueryString(query);
    }

    function showDiscoveryQuery(query) {
        var queryElement = getDiscoveryQueryElement(),
            outputElement = getDiscoveryQueryOutputElement(),
            decodedQuery;

        try {
            decodedQuery = decodeURIComponent(query.replace(/\+/g, " "));
        } catch (e) {
            decodedQuery = query;
        }

        outputElement.text(decodedQuery);
        queryElement.removeClass("hidden");
    }

    function discoverySearch() {
        var prompt = getSearchPrompt(),
            context = form.serializeJsonFor(ACTION_SEARCH, true, getDiscoverySearchFieldNames());

        $.when($.post(getDiscoveryEndpoint(), {
            prompt: prompt,
            context: context
        })).then(function(response) {
            var query = parseDiscoveryResponse(response);

            showDiscoveryQuery(query);
            queryParamsOverride = query;
            form.submitQuery(query, processSearch).fail(function() {
                queryParamsOverride = null;
                trigger(ns.Events.SEARCH_INVALID, [EVENT_SEARCH_TYPE_FULL]);
                running = false;
            });
        }).fail(function() {
            queryParamsOverride = null;
            trigger(ns.Events.SEARCH_INVALID, [EVENT_SEARCH_TYPE_FULL]);
            running = false;
        });
    }

    function processLoadMore(fragmentHtml) {
        ns.Elements.update(fragmentHtml, ACTION_LOAD_MORE);

        setAddressBar(form.serializeFor(ACTION_DEEP_LINK));

        trigger(ns.Events.SEARCH_END, [EVENT_SEARCH_TYPE_LOAD_MORE]);
        running = false;
    }

    function search(e) {
        if (e) {
            e.preventDefault();
        }
        if (!running) {
            running = true;

            if (isDiscoverySearch()) {
                trigger(ns.Events.SEARCH_BEGIN, [EVENT_SEARCH_TYPE_FULL]);
                discoverySearch();
            } else if (form.submit(ACTION_SEARCH, true, processSearch)) {
                trigger(ns.Events.SEARCH_BEGIN, [EVENT_SEARCH_TYPE_FULL]);
            } else {
                trigger(ns.Events.SEARCH_INVALID, [EVENT_SEARCH_TYPE_FULL]);
                running = false;
            }
        }
    }

    function loadMore(e) {
        if (e) {
            e.preventDefault();
        }
        if (!running) {
            running = true;
            if (form.submit(ACTION_LOAD_MORE, false, processLoadMore)) {
                trigger(ns.Events.SEARCH_BEGIN, [EVENT_SEARCH_TYPE_LOAD_MORE]);
            } else {
                trigger(ns.Events.SEARCH_INVALID, [EVENT_SEARCH_TYPE_LOAD_MORE]);
                running = false;
            }
        }
    }

    function sortResults(e) {
        if (e) {
            e.preventDefault();
        }
        if (!running) {
            running = true;
            if (form.submit(ACTION_SORT, false, processSearch)) {
                trigger(ns.Events.SEARCH_BEGIN, [EVENT_SEARCH_TYPE_FULL]);
            } else {
                trigger(ns.Events.SEARCH_INVALID, [EVENT_SEARCH_TYPE_FULL]);
                running = false;
            }
        }
    }

    function switchLayout(e) {
        e.preventDefault();
        if (!running) {
            running = true;

            ns.Data.val("layout", $(this).val());
            if (form.submit(ACTION_SWITCH_LAYOUT, false, processSearch)) {
                trigger(ns.Events.SEARCH_BEGIN, [EVENT_SEARCH_TYPE_FULL]);
            } else {
                trigger(ns.Events.SEARCH_INVALID, [EVENT_SEARCH_TYPE_FULL]);
                running = false;
            }
        }
    }

    (function() {
        // ONLY EXECUTE ON THE SEARCH PAGE
        if (ns.Elements.element("form").length > 0) {
            ns.Navigation.returnUrl(window.location.pathname + window.location.search);
        }
    }());

    (function registerEvents() {
        var formId = getForm().id();

        $("body").on("submit", "#" + formId, search);
        $("body").on("click", ns.Elements.selector("load-more"), loadMore);
        $("body").on("change", ns.Elements.selector("sort"), sortResults);
        $("body").on("click", ns.Elements.selector("switch-layout"), switchLayout);

        $("body").on("change", "[data-asset-share-search-on='change']", search);
        $("body").on("click", "[data-asset-share-search-on='click']", search);

        /* Required for IE */
        $("button[form='" + formId + "']").on("click", search);
        $("input[form='" + formId + "']").keypress(function(e) {
            if ((e.keyCode || e.which) === 13) {
                search(e);
            }
        });

        // Handle navigation back/forward on search page
        window.addEventListener('popstate', function(event) {
            if (getForm()) { window.location.reload(); }
        });
    }());

    return {
        loadMore: loadMore,
        search: search,
        sortResults: sortResults,
        switchLayout: switchLayout,
        form: getForm
    };

}(window,
    jQuery,
    AssetShare,
    AssetShare.Ajax));
