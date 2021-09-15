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

jQuery((function($, ns, semanticModal, download) {
    "use strict";
    AssetShare.SemanticUI.Modals.DownloadsModal = (function() {
        var DOWNLOADS_URL = ns.Data.val("downloads-url"),
            DOWNLOADS_MODAL_ID = "downloads-modal",
            WHEN_DOWNLOADS_UPDATED = "downloads-updated";

        function getId() {
            return DOWNLOAD_PANEL_MODAL_ID;
        }

        function getUrl() {
            return DOWNLOADS_URL;
        }

        function getModalData() {
            return download.getDownloadIds().map((id) => { return { name: 'downloadId', value: id } });
        }

        function getModal() {
            // Returns an object describing how to get load the Downloads modal
            return {
                    id : DOWNLOADS_MODAL_ID,
                    url : DOWNLOADS_URL,
                    data : getModalData(),
                    dataType: 'json',
                    options : {}
            };
        }

        function updateBadge() {
            if (DOWNLOADS_URL) {
                $.post(DOWNLOADS_URL, getModalData()).then(function(htmlResponse) {
                    var count = ns.Elements.element('downloads-modal', htmlResponse).data('asset-share-downloads-count') || 0;
                    ns.Elements.element("downloads-count").text(count);
                });
            }
        }

        function update() {
            if (DOWNLOADS_URL) {
                $.post(DOWNLOADS_URL, getModalData()).then(function(htmlResponse) {
                    ns.Elements.update(htmlResponse, WHEN_DOWNLOADS_UPDATED);
                    init();
                });
            }
        }

        function show(e) {
            e.preventDefault();
            e.stopPropagation();

            semanticModal.show([getModal()]);
        }

        function refresh(e) {
            e.preventDefault();
            e.stopPropagation();

            update();
        }

        function remove(e) {
            e.stopPropagation();
            e.preventDefault();

            var downloadId = $(e.currentTarget).data('asset-share-download-id');

            if (downloadId) {
                download.removeDownloadById(downloadId);
                update();
            }

        }

        function clear(e) {
            e.preventDefault();
            e.stopPropagation();

            download.removeAllDownloads();

            update();
        }

        function init() {
            ns.Elements.element('downloads-modal').each(function() {
                var el = $(this);
                el.find('.ui.accordion').accordion({
                    selector: {
                        accordion: '.accordion',
                        title: '.accordion-title',
                        trigger: '.accordion-trigger',
                        content: '.accordion-content'
                    }
                });
            });
        }

        /** REGISTER EVENTS WHEN DOCUMENT IS READY * */
        $("body").on("click", ns.Elements.selector([ "refresh-downloads" ]), refresh);
        $("body").on("click", ns.Elements.selector([ "clear-downloads" ]), clear);
        $("body").on("click", ns.Elements.selector([ "show-downloads" ]), show);
        $("body").on("click", ns.Elements.selector([ "remove-from-downloads" ]), remove);
        $("body").on(ns.Events.MODAL_SHOWN, init);
        $("body").on(ns.Events.DOWNLOADS_UPDATE, updateBadge);

        init();
        updateBadge();

        return {
            show: show,
            init: init
        };
    }());
}(jQuery,
    AssetShare,
    AssetShare.SemanticUI.Modal,
    AssetShare.Store.Download)));
