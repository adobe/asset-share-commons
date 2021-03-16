/*
 * Asset Share Commons
 *
 * Copyright [2020]  Adobe
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

/**
 * Utility specific to storing data about the users Profile
 */
AssetShare.Profile = (function ($, ns, profileStore) {
    'use strict';

    const CURRENT_USER_URI  = '/libs/granite/security/currentuser.json';

    /**
     * Initial call to request user profile 
     * On a successful response the profile is set with local storage
     * and custom event is dispatched to alert dependent JavaScript objects
     */
    $.get(CURRENT_USER_URI + '?nocache=' + new Date().getTime(), function(data) {
        if(data.type === 'user') {
            // Set the profile store to enable local storage
            profileStore.setUserProfile(data);
            //dispatch event announcing the profile has loaded
            $('body').trigger(ns.Events.PROFILE_LOAD, {detail:data});
        } else {
            console.error('Could not retrieve a user profile, cart functionality disabled');
        }
        
    }).fail(function() {
        console.error('Could not retrieve a user profile, cart functionality disabled');
    });

}(jQuery,
    AssetShare,
    AssetShare.Store.Profile));