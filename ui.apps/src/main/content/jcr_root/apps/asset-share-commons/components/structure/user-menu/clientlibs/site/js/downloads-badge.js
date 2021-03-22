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

/*global jQuery: false, AssetShare: false */

jQuery((function($, ns) {
	"use strict";

    // TODO REMOVE default
    var DOWNLOADS_URL = ns.Data.val("downloads-url") || '/content/asset-share-commons/en/light/actions/downloads.partial.html';

    function update() {
        var count = 0;

        var allowedDownloadIds = [{
                                    name: 'downloadId',
                                    value: '137fd707-a62d-4ae9-a819-e0c405e1a8a7'
                                  },
                                  {
                                     name: 'downloadId',
                                     value: '137fd707-a62d-4ae9-a819-1234'
                                  }];
        $.post(DOWNLOADS_URL, allowedDownloadIds).then(function(htmlResponse) {
            count = ns.Elements.element('downloads-modal', htmlResponse).data('asset-share-downloads-count') || 0;
            ns.Elements.element("downloads-count").text(count);
        });
    }

    update();

}(jQuery, AssetShare)));
