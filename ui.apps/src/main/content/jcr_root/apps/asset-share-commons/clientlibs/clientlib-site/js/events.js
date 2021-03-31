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

AssetShare.Events = {
    CART_ADD: "asset-share-commons.cart.add",
    CART_REMOVE: "asset-share-commons.cart.remove",
    CART_UPDATE: "asset-share-commons.cart.update",
    CART_CLEAR: "asset-share-commons.cart.clear",
    CART_ALREADY_EXISTS: "asset-share-commons.cart.exists",


    DOWNLOADS_ADD: "asset-share-commons.download.add",
    DOWNLOADS_REMOVE: "asset-share-commons.download.remove",
    DOWNLOADS_UPDATE: "asset-share-commons.download.update",
    DOWNLOADS_CLEAR: "asset-share-commons.download.clear",

    PROFILE_LOAD: "asset-share-commons.storage.profile.load",
	PAGE_LOAD: "asset-share-commons.page.load",

    SEARCH_BEGIN: "asset-share-commons.search.begin",
    SEARCH_END: "asset-share-commons.search.end",
    SEARCH_INVALID: "asset-share-commons.search.invalid",

    MODAL_SHOWN: "asset-share-commons.modal.shown"
};