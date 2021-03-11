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

/*global ContextHub: false, jQuery: false, AssetShare: false */

AssetShare.Downloads = (function($, ns, messages) {
	"use strict";

	function submitDownload(formdata, formurl) {

		$.ajax({
			type : "POST",
			url : formurl,
			data : formdata,
			success : function(data) {
                var downloadIds = sessionStorage.getItem("downloadIds");
				if (data.downlaodID) {					
					if (downloadIds == null || downloadIds == "") {
                        sessionStorage.setItem("downloadIds", data.downlaodID);
					} else {
                        sessionStorage.setItem("downloadIds", data.downlaodID + ','+downloadIds);
					}

					downloadIds = sessionStorage.getItem("downloadIds");
					var count = downloadIds.split(',').length;
					ns.Elements.element("downloads-count").text(count);
					messages.show('download-add');
				}
			}
		});
	}


	function getContextHubStore() {
		return contextHubStore;
	}

	return {
		submitDownload : submitDownload
	};

}(jQuery, AssetShare, AssetShare.Messages));