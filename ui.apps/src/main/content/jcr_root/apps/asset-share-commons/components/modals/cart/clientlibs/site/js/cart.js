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

jQuery((function($, ns, cart, semanticModal, downloadModal, licenseModal, shareModal) {
    "use strict";
    AssetShare.SemanticUI.Modals.CartModal = (function () {
        var CART_URL = ns.Data.val("cart-url"),
            CART_MODAL_ID = "cart-modal",
            WHEN_CART_UPDATED = "cart-updated";

        function asFormData() {
            var formData = new ns.FormData();

            cart.paths().forEach(function (path) {
                formData.add("path", encodeURIComponent(path));
            });

            // Set this to prevent odd placeholder injection when running on AEM Author; This will be a NOOP
            formData.add("wcmmode", "disabled");

            return formData;
        }

        function serialize() {
            return asFormData().serialize();
        }

        function getId() {
            return CART_MODAL_ID;
        }

        function getModal() {
            return {
                id: CART_MODAL_ID,
                url: CART_URL,
                data: serialize(),
                options: {}
            };
        }

        function updateCartModal() {
            $.get(CART_URL, serialize()).then(function (htmlResponse) {
                ns.Elements.update(htmlResponse, WHEN_CART_UPDATED);
            });
        }

        function show(e) {
            e.preventDefault();
            e.stopPropagation();

            semanticModal.show([getModal()]);
        }

        function download(e) {
            e.preventDefault();
            e.stopPropagation();

            semanticModal.show([downloadModal.modal(asFormData())]);
        }

        function share(e) {
            e.preventDefault();
            e.stopPropagation();

            semanticModal.show([shareModal.modal(asFormData())]);
        }

        function remove(e) {
            var assetPath = ns.Data.attr(this, "asset");

            e.preventDefault();
            e.stopPropagation();

            cart.remove(assetPath);
            updateCartModal();
        }

        function clear(e) {
            e.preventDefault();
            e.stopPropagation();

            cart.clear();
            updateCartModal();
        }

        /** REGISTER EVENTS **/

        $((function registerEvents() {
            $("body").on("click", ns.Elements.selector(["show-cart"]), show);

            $("body").on("click", ns.Elements.selector(["download-all"]), download);

            $("body").on("click", ns.Elements.selector(["share-all"]), share);

            $("body").on("click", ns.Elements.selector(["remove-from-cart"]), remove);

            $("body").on("click", ns.Elements.selector(["clear-cart"]), clear);
        }()));

        return {
            show: show
        };
    }());
}(jQuery,
    AssetShare,
    AssetShare.Cart,
    AssetShare.SemanticUI.Modal,
    AssetShare.SemanticUI.Modals.DownloadModal,
    AssetShare.SemanticUI.Modals.LicenseModal,
    AssetShare.SemanticUI.Modals.ShareModal)));