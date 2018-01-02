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

jQuery((function ($, ns, messages, cart, semanticModal, licenseModal) {
    "use strict";

    var ADD_TO_CART = "add-to-cart",
        REMOVE_FROM_CART = "remove-from-cart",

        MESSAGE_CART_ADD = "cart-add",
        MESSAGE_CART_EXISTS = "cart-exists",
        MESSAGE_CART_REMOVE = "cart-remove";

    /** Add to Cart **/

    $("body").on("click", ns.Elements.selector(ADD_TO_CART), function (e) {
        var assetPath = ns.Data.attr(this, "asset"),
            license = ns.Data.attr(this, "license"),
            modal;

        e.preventDefault();
        e.stopPropagation();

        if (cart.contains(assetPath)) {
            messages.show(MESSAGE_CART_EXISTS);
            return false;
        } else {
            if (license) {
                modal = licenseModal.modal(assetPath);
                modal.options.onApprove = function () {
                    cart.add(assetPath);
                    return true;
                };
                semanticModal.show(modal);
            } else {
                cart.add(assetPath);
                return true;
            }
        }
    });


    /** Toggle Add/Remove Buttons for Cart **/

    function toggleCartButtons(addOperation, paths) {
        var showState = REMOVE_FROM_CART,
            hideState = ADD_TO_CART,
            assetSelector = '[data-asset-share-asset="' + paths + '"]',
            showSelector,
            hideSelector;

        // Is a Remove from Cart operation (aka Not an Add to Cart Operation)
        if (!addOperation) {
            showState = ADD_TO_CART;
            hideState = REMOVE_FROM_CART;
        }

        showSelector = assetSelector + ns.Elements.selector(showState);
        hideSelector = assetSelector + ns.Elements.selector(hideState);

        $(hideSelector).addClass("hidden").hide();
        $(showSelector).removeClass("hidden").show();
    }

    function flipAction(assetPath) {
        if (cart.contains(assetPath)) {
            toggleCartButtons(true, assetPath);
        } else {
            toggleCartButtons(false, assetPath);
        }
    }

    function handleCartButtonsUpdate() {
        var assetPath;
        ns.Elements.element(ADD_TO_CART).each(function() {
            assetPath = ns.Data.attr(this, "asset");
            flipAction(assetPath);
        });
    }

    $("body").on(ns.Events.PAGE_LOAD,function() {
        handleCartButtonsUpdate();
    });

    $("body").on(ns.Events.SEARCH_END, function() {
        handleCartButtonsUpdate();
    });

    $("body").on(ns.Events.CART_CLEAR, function() {
        handleCartButtonsUpdate();
    });

    $("body").on(ns.Events.CART_ADD, function(event, size, paths) {
        messages.show(MESSAGE_CART_ADD);

        toggleCartButtons(true, paths);
    });

    $("body").on(ns.Events.CART_REMOVE,function(event, size, paths) {
        messages.show(MESSAGE_CART_REMOVE);

        toggleCartButtons(false, paths);
    });

}(jQuery,
    AssetShare,
    AssetShare.Messages,
    AssetShare.Cart,
    AssetShare.SemanticUI.Modal,
    AssetShare.SemanticUI.Modals.LicenseModal)));