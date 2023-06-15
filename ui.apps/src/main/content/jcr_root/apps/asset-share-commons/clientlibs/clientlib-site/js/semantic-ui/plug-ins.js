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

/*global $: false, AssetShare: false*/

/* SemanticUI controls */
$(function () {
    "use strict";

    function init() {
        /** Semantic UI Plugins **/
        $(".ui.accordion").not("[data-asset-share-processed]").each(function() {
            $(this).attr("data-asset-share-processed", "true").accordion();
        });

        $(".ui.dropdown").not("[data-asset-share-processed]").each(function() {
            $(this).attr("data-asset-share-processed", "true").dropdown();
        });

        /** Semantic UI Calendar Plugins - Initialize Date Range Predicates **/
        function dateFormatter(date, settings) {
            var day, month, year;

            if (!date) {
                return '';
            }

            day = date.getDate();
            month = date.getMonth() + 1;
            year = date.getFullYear();

            // Format is important here as the date is used in the query builder.
            return year + '-' + month + '-' + day;
        }

        $('.ui.calendar.rangestart').not("[data-asset-share-processed]").each(function (index) {
            var predicateId = $(this).attr('data-asset-share-calendar-start-id'),
                rangeStart = $(this),
                rangeEnd = $('[data-asset-share-calendar-end-id="' + predicateId + '"]');

            rangeStart.attr("data-asset-share-processed", "true");

            $(rangeStart).calendar({
                type: 'date',
                endCalendar: $(rangeEnd),
                formatter: {
                    date: dateFormatter
                },
                onChange: function (date, text, mode) {
                    $(rangeStart).find('input[type="text"]').val(text).trigger("change");
                }
            });

            // Setting the rangeEnd to have the last milliseconds of the selected day is handled in serach/search-form.js @ _adjustFormData()
            $(rangeEnd).calendar({
                type: 'date',
                startCalendar: $(this),
                formatter: {
                    date: dateFormatter
                },
                onChange: function (date, text, mode) {
                    rangeEnd.find('input[type="text"]').val(text).trigger("change");
                }
            });
        });
    }

    /* On initial load */
    init();

    /** On search end as this updates the DOM significantly */
    $("body").on(AssetShare.Events.SEARCH_END, init);
});