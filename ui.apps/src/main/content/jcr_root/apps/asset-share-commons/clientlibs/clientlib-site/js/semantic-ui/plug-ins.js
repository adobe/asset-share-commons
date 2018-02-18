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

/*global $: false*/

/* SemanticUI controls */
$(function () {
    "use strict";

    /** Semantic UI Plugins **/
    $(".ui.accordion").accordion();
    $(".ui.dropdown").dropdown();

    /** Semantic UI Calendar Plugins - Initialize Date Range Predicates **/
    function dateFormatter(date, settings) {
        var day, month, year;

        if (!date) {
            return '';
        }

        day = date.getDate();
        month = date.getMonth() + 1;
        year = date.getFullYear();

        return year + '-' + month + '-' + day;
    }

    $('.ui.calendar.rangestart').each(function (index) {
        var predicateId = $(this).attr('data-asset-share-calendar-start-id'),
            rangeStart = $(this),
            rangeEnd = $('[data-asset-share-calendar-end-id="' + predicateId + '"]');

        $(rangeStart).calendar({
            type: 'date',
            endCalendar: $(rangeEnd),
            formatter: {
                date: dateFormatter
            },
            onChange: function (date, text, mode) {
                $(rangeStart).find('input[type="text"]').trigger("change");
            }
        });

        $(rangeEnd).calendar({
            type: 'date',
            startCalendar: $(this),
            formatter: {
                date: dateFormatter
            },
            onChange: function (date, text, mode) {
                $(rangeEnd).find('input[type="text"]').trigger("change");
            }
        });
    });
});