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
        ns.Navigation.addressBar(window.top.location.pathname + "?" + queyParams);
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
            trigger(ns.Events.SEARCH_BEGIN, [EVENT_SEARCH_TYPE_FULL]);
            $.when($.get(form.url(), form.serializeFor(ACTION_SEARCH, true))).then(processSearch);
        }
    }

    function loadMore(e) {
        if (e) {
            e.preventDefault();
        }
        if (!running) {
            running = true;
            trigger(ns.Events.SEARCH_BEGIN, [EVENT_SEARCH_TYPE_LOAD_MORE]);
            $.when($.get(form.url(), form.serializeFor(ACTION_LOAD_MORE))).then(processLoadMore);
        }
    }

    function sortResults(e) {
        if (e) {
            e.preventDefault();
        }
        if (!running) {
            running = true;
            trigger(ns.Events.SEARCH_BEGIN, [EVENT_SEARCH_TYPE_FULL]);
            $.when($.get(form.url(), form.serializeFor(ACTION_SORT))).then(processSearch);
        }
    }

    function switchLayout(e) {
        e.preventDefault();
        if (!running) {
            running = true;
            trigger(ns.Events.SEARCH_BEGIN, [EVENT_SEARCH_TYPE_FULL]);
            ns.Data.val("layout", $(this).val());
            $.when($.get(form.url(), form.serializeFor(ACTION_SWITCH_LAYOUT))).then(processSearch);
        }
    }

    (function() {
        // ONLY EXECUTE ON THE SEARCH PAGE
        if (ns.Elements.element("form").length > 0) {
            ns.Navigation.returnUrl(window.location.pathname);
        }
    }());

    (function registerEvents() {
        var formId = getForm().id();

        $("body").on("submit", "#" + formId, search);
        $("body").on("click", ns.Elements.selector("load-more"), loadMore);
        $("body").on("change", ns.Elements.selector("sort"), sortResults);
        $("body").on("click", ns.Elements.selector("switch-layout"), switchLayout);

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
