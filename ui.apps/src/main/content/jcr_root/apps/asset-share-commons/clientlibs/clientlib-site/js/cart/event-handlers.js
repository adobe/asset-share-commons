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

/*global jQuery: false, AssetShare: false, handleButtonsUpdateOnDetails, handleBulkCartButtonsUpdate, toggleCartButtons, flipAction  */
jQuery((function($, ns, cart) {
	"use strict";
	var ADD_TO_CART = "add-to-cart",
		REMOVE_FROM_CART = "remove-from-cart",
		EVENT_PAGE_LOAD = "asset-share-commons.page.load",
		EVENT_SEARCH_END = "asset-share-commons.search.end",
		EVENT_CART_CLEAR = "asset-share-commons.cart.clear",
		EVENT_CART_ADD = "asset-share-commons.cart.add",
		EVENT_CART_REMOVE = "asset-share-commons.cart.remove";
	$("body").on(EVENT_PAGE_LOAD,function(event, actionButtons) {
		handleBulkCartButtonsUpdate();
	});

	$("body").on(EVENT_SEARCH_END, function(event, search) {
		handleBulkCartButtonsUpdate();
	});

	$("body").on(EVENT_CART_CLEAR, function(event, search) {
		handleBulkCartButtonsUpdate();
	});

	$("body").on(EVENT_CART_ADD, function(event, size, paths) {
		toggleCartButtons(true, paths);
	});

	$("body").on(EVENT_CART_REMOVE,function(event, size, paths) {
		toggleCartButtons(false, paths);
	});

	function handleBulkCartButtonsUpdate() {
		var assetPath;
		if($('[data-asset-share-id="add-to-cart"]').length > 0){
			$('[data-asset-share-id="add-to-cart"]').each(function(index) {
				assetPath = ns.Data.attr(this, "asset");
				flipAction(assetPath);
			});
		}
	}

	function flipAction(assetPath) {
		if (cart.contains(assetPath)) {
			toggleCartButtons(true, assetPath);
		} else {
			toggleCartButtons(false, assetPath);
		}
	}
	
	function toggleCartButtons(addOperation, paths) {
		var showState = REMOVE_FROM_CART,
		    hideState = ADD_TO_CART,
		    $el;
		if (!addOperation) {
			showState = ADD_TO_CART;
			hideState = REMOVE_FROM_CART;
		}
		
		$el = $('[data-asset-share-asset="' + paths + '"]');
		if($el.find('[data-asset-share-id="' + showState + '"]').length > 0 ){
			$el.find('[data-asset-share-id="' + showState + '"]').removeClass("hidden");
			$el.find('[data-asset-share-id="' + hideState + '"]').addClass("hidden");
		}else{
			$('[data-asset-share-id="' + showState + '"]').removeClass("hidden");
			$('[data-asset-share-id="' + hideState + '"]').addClass("hidden");
		}
	}

}(jQuery, AssetShare, AssetShare.Cart)));
