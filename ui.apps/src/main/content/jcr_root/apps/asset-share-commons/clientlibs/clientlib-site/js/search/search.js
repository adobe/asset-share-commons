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

        running = false,

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
        setAddressBar(form.serializeFor(ACTION_DEEP_LINK));

        trigger(ns.Events.SEARCH_END, [EVENT_SEARCH_TYPE_FULL]);
        running = false;
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
            if (form.submit(ACTION_SEARCH, true, processSearch)) {
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
