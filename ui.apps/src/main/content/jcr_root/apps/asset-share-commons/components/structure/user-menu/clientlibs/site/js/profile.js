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

/*global jQuery: false, AssetShare: false, ContextHub: false*/

jQuery((function($, ns, cart, contextHub, profile) {
    "use strict";

    var ANONYMOUS_SECTION_ID = "cmp-user-menu__profile--anonymous",
        AUTHENTICATED_SECTION_ID = "cmp-user-menu__profile--authenticated",
        AUTHENTICATED_DISPLAY_NAME_ID = "cmp-user-menu__profile-display-name",
        AUTHENTICATED_PROFILE_PIC_ID = "cmp-user-menu__profile-pic--available",
        AUTHENTICATED_PROFILE_PIC_UNAVAILABLE_ID = "cmp-user-menu__profile-pic--unavailable";

    function isAnonymous() {
        var authorizableId = profile.getItem("/authorizableId");
        return !authorizableId || "anonymous" === authorizableId;
    }

    function showAnonymous() {
        var anonymousSection = ns.Elements.element(ANONYMOUS_SECTION_ID),
            authenticatedSection = ns.Elements.element(AUTHENTICATED_SECTION_ID);

        authenticatedSection.hide();
        anonymousSection.show();
    }

    function showAuthenticated() {
        var anonymousSection = ns.Elements.element(ANONYMOUS_SECTION_ID),
            authenticatedSection = ns.Elements.element(AUTHENTICATED_SECTION_ID),
            profilePicAvailable = ns.Elements.element(AUTHENTICATED_PROFILE_PIC_ID),
            profilePicUnavailable = ns.Elements.element(AUTHENTICATED_PROFILE_PIC_UNAVAILABLE_ID),
            profileImagePath = profile.getItem("/path") + "/profile/photos/primary/image.prof.thumbnail.128.128.png";

        // Set display name
        authenticatedSection.find(ns.Elements.selector(AUTHENTICATED_DISPLAY_NAME_ID)).text(profile.getItem("/displayName"));

        // Set profile pic
        $.get(profileImagePath, function() {
            authenticatedSection.find(ns.Elements.selector(AUTHENTICATED_PROFILE_PIC_ID)).attr("src", profileImagePath);
            profilePicUnavailable.hide();
            profilePicAvailable.show();
        }).fail(function() {
            profilePicAvailable.hide();
            profilePicUnavailable.show();
        });

        anonymousSection.hide();
        authenticatedSection.show();
    }

    function init() {
        if (isAnonymous()) {
            showAnonymous();
        } else {
            showAuthenticated();
        }
    }

    // Events
    $("body").on("click", ns.Elements.selector("cmp-user-menu__logout-link"), function() {
        cart.clear();
    });

    profile.eventing.on(contextHub.Constants.EVENT_STORE_READY, init);

}(jQuery,
    AssetShare,
    AssetShare.Cart,
    ContextHub,
    ContextHub.getStore("profile"))));