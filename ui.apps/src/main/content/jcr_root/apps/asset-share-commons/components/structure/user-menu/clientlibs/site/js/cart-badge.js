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

jQuery((function($, ns, cart, contextHub) {
    "use strict";

    function updateCartCountBadge(size) {
        ns.Elements.element("cart-count").text(size);
    }

    // On cart change
    $("body").on("asset-share-commons.cart.update", function(e, size, paths) {
        updateCartCountBadge(size);
    });

    // Page init
    function init() {
        updateCartCountBadge(cart.size);
    }

    init();

    cart.store().eventing.on(contextHub.Constants.EVENT_STORE_READY, init);

}(jQuery,
    AssetShare,
    AssetShare.Cart,
    ContextHub)));