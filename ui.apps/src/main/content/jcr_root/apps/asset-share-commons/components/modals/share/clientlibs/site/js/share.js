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
jQuery((function($, ns, semanticModal) {
    "use strict";
    AssetShare.SemanticUI.Modals.ShareModal = (function () {
        var SHARE_URL = ns.Data.val("share-url"),

            ASSETS_ID = "asset-share-asset",

            SHARE_MODAL_ID = "share-modal",
            SHARE_BUTTON_ID = "share-asset",

            CSS_INITIAL = "cmp-modal-share__wrapper--initial",
            CSS_LOADING = "cmp-modal-share__wrapper--loading",
            CSS_SHARE_SUCCESS = "cmp-modal-share__wrapper--success",
            CSS_SHARE_ERROR = "cmp-modal-share__wrapper--error";

        function getId() {
            return SHARE_MODAL_ID;
        }

        function getUrl() {
            return SHARE_URL;
        }

        function getModal(formDataOrAssetPath) {
            var formData = formDataOrAssetPath;

            if (typeof formDataOrAssetPath === 'string') {
                formData = new ns.FormData();
                formData.add("path", encodeURIComponent(formDataOrAssetPath));
            }

            return {
                id: SHARE_MODAL_ID,
                url: SHARE_URL,
                data: formData.serialize(),
                options: {
                    show: function (modal) {
                        modal.modal("show");
                    }
                }
            };
        }

        function share(e) {
            var shareModal = getModal($(this).data(ASSETS_ID));

            e.preventDefault();
            e.stopPropagation();

            semanticModal.show(shareModal);
        }

        function submit(form) {
            form = $(form);

            //show loader
            form.removeClass(CSS_INITIAL).addClass(CSS_LOADING);

            $.ajax({
                type: "POST",
                url: form.attr("action"),
                data: form.serialize(),
                success: function () {
                    form.delay(500).removeClass(CSS_LOADING).addClass(CSS_SHARE_SUCCESS);
                },
                error: function () {
                    form.delay(500).removeClass(CSS_LOADING).addClass(CSS_SHARE_ERROR);
                }
            });
        }

        /** Register Events **/
        $((function registerEvents() {
            $("body").on("click", ns.Elements.selector([SHARE_BUTTON_ID]), share);

            $("body").on("submit", ns.Elements.selector([SHARE_MODAL_ID]), function (e) {
                var formEl = $(this);

                e.preventDefault();
                e.stopPropagation();

                if (formEl.form('is valid')) {
                    submit(formEl);
                }
            });
        }()));

        return {
            id: getId,
            url: getUrl,
            modal: getModal,
            share: share,
            submit: submit
        };
    }());
}(jQuery,
    AssetShare,
    AssetShare.SemanticUI.Modal)));