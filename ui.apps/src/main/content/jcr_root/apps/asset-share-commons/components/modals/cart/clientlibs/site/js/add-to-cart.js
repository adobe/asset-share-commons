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

    $("body").on("click", ns.Elements.selector("add-to-cart"), function (e) {
        var assetPath = ns.Data.attr(this, "asset"),
            license = ns.Data.attr(this, "license"),
            modal;

        e.preventDefault();
        e.stopPropagation();

        if (cart.contains(assetPath)) {
            messages.show("cart-exists");
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

    $("body").on("asset-share-commons.cart.add", function(event, size, paths) {
        messages.show("cart-add");
    });
}(jQuery,
    AssetShare,
    AssetShare.Messages,
    AssetShare.Cart,
    AssetShare.SemanticUI.Modal,
    AssetShare.SemanticUI.Modals.LicenseModal)));