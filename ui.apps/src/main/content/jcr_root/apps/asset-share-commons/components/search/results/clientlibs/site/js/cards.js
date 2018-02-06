/*
 * Asset Share Commons
 *
 * Copyright [2018]  Adobe
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

/*global jQuery: false, AssetShare: false, window: false */

jQuery((function ($, ns) {
    "use strict";

    var resizeDebounce;

    function alignCards() {
        $('.cmp-search-results .cmp-cards').has('.cmp-card').each(function() {
            var cards = $(this),
                minimumMargin = 20,
                availableWidth = Math.floor(cards.innerWidth()),
                cardWidth = $(cards.find('.cmp-card')[0]).outerWidth(),
                cardAdjustedWidth = cardWidth + minimumMargin,
                cardsPerRow = Math.floor(availableWidth / cardAdjustedWidth),
                widthTakenByCards = cardsPerRow * cardAdjustedWidth,
                widthAvailableForMargin = availableWidth - widthTakenByCards,
                rightMargin = Math.floor(widthAvailableForMargin / cardsPerRow) + minimumMargin - 2;

            cards.find('.cmp-card').css("margin-left", 0).css("margin-right", rightMargin);
        });
    }

    // Align with debounce
    $(window).on("resize", function(e) {
        clearTimeout(resizeDebounce);
        resizeDebounce = setTimeout(alignCards, 250);
    });

    // Align when searches end
    $("body").on(ns.Events.SEARCH_END, function() {
        alignCards();
    });

    // Align on page load
    alignCards();

}(jQuery,
    AssetShare)));