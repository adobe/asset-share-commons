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

// toggles the rail functionality on mobile/tablet view
$(function () {

    $('[data-asset-share-commons-toggle="true"]').click(function () {
        var rail = '[data-asset-share-id="rail"]',
            sidebar = '.ui.sidebar',
            $railEl,
            $sidebarEl;

        $railEl = $(rail).removeClass('desktopvisible');
        $sidebarEl = $(rail + ' ' + sidebar);

        $sidebarEl
            .transition({
                animation: 'slide left',
                onComplete: function () {
                    if ($sidebarEl.hasClass('hidden')) {
                        $sidebarEl.removeClass('hidden');
                        $sidebarEl.removeClass('visible');
                        $railEl.addClass('desktopvisible');
                    }
                }
            })
        ;
    });
});