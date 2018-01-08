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

/*global jQuery: false, AssetShare: false*/

jQuery((function ($, ns, search) {
    "use strict";

    var formId = search.form().id();

    $("body").on("submit", ns.Elements.selector("form"), search.search);

    $("body").on("click", ns.Elements.selector("load-more"), search.loadMore);

    $("body").on("change", ns.Elements.selector("sort"), search.sortResults);

    $("body").on("click", ns.Elements.selector("switch-layout"), search.switchLayout);

    /*  The following code is required for IE */

    /* Note that search.search(..) has its tracker ot ensure parallel searches do not occurr */

    $("button[form='" + formId + "']").on("click", search.search);

    $("input[form='" + formId + "']").keypress(function(e){
        var code = e.keyCode || e.which;
        if (code === 13) {
            e.preventDefault();
            search.search(e);
        }
    });

}(jQuery,
    AssetShare,
    AssetShare.Search)));