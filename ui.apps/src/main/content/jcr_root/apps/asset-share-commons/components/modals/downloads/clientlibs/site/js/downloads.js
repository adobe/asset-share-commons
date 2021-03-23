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

        /**
        * Function copied from: https://stackoverflow.com/questions/10420352/converting-file-size-in-bytes-to-human-readable-string/10420404
        * Format bytes as human-readable text.
        *
        * @param bytes Number of bytes.
        * @param si True to use metric (SI) units, aka powers of 1000. False to use
        *           binary (IEC), aka powers of 1024.
        * @param dp Number of decimal places to display.
        *
        * @return Formatted string.
        */
        function humanFileSize(bytes, si=false, dp=1) {
            const thresh = si ? 1000 : 1024;

            if (Math.abs(bytes) < thresh) {
                return bytes + ' B';
            }

            const units = si
             ? ['kB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB']
             : ['KiB', 'MiB', 'GiB', 'TiB', 'PiB', 'EiB', 'ZiB', 'YiB'];
            let u = -1;
            const r = 10**dp;

            do {
                bytes /= thresh;
                ++u;
            } while (Math.round(Math.abs(bytes) * r) / r >= thresh && u < units.length - 1);

            return bytes.toFixed(dp) + ' ' + units[u];
        }

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
            $.post(DOWNLOADS_URL, getModalData()).then(function(htmlResponse) {
                var count = ns.Elements.element('downloads-modal', htmlResponse).data('asset-share-downloads-count') || 0;
                ns.Elements.element("downloads-count").text(count);
            });
        }

		function update() {
			$.post(DOWNLOADS_URL, getModalData()).then(function(htmlResponse) {
				ns.Elements.update(htmlResponse, WHEN_DOWNLOADS_UPDATED);
				init();
			});
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

		function clear(e) {
			e.preventDefault();
			e.stopPropagation();

			download.removeAllDownloadIds();

            update();
		}

        function init() {
            ns.Elements.element('downloads-modal').each(function() {
                var el = $(this);
                el.find('.ui.accordion').accordion();
                el.find('[data-asset-share-id="file-size"]').each(function() {
                    $(this).text(function() {
                        return humanFileSize(($(this).text() || 0), true, 0)
                    });
                });
            });
        }

		/** REGISTER EVENTS WHEN DOCUMENT IS READY * */
        $("body").on("click", ns.Elements.selector([ "refresh-downloads" ]), refresh);
        $("body").on("click", ns.Elements.selector([ "clear-downloads" ]), clear);
        $("body").on("click", ns.Elements.selector([ "show-downloads" ]), show);
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
