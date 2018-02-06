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

/*global jQuery: false, AssetShare: false, ContextHub: false */

AssetShare.ContextHub.Profile = (function ($, ns, contextHub) {
    "use strict";

    $(function() {
        // https://github.com/Adobe-Marketing-Cloud/aem-sample-we-retail/blob/master/ui.apps/src/main/content/jcr_root/apps/weretail/components/structure/header/clientlib/js/utilities.js
        $.ajax({
            type   : "GET",
            url    : "/libs/granite/security/currentuser.json?nocache=" + new Date().getTime(),
            async  : true,
            success: function (json) {

                // On publish: load the request user into ContextHub
                if (typeof contextHub !== "undefined") {
                    var profileStore = contextHub.getStore("profile"),
                        requestUser = json.home,
                        contextHubUser = profileStore.getTree().path;

                    if (!contextHubUser || contextHubUser !== requestUser) {
                        profileStore.loadProfile(requestUser);
                    } else {
                        profileStore.announceReadiness();
                    }
                }
            }
        });
    });

    return {};
}(jQuery,
    AssetShare,
    ContextHub));