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

    const CURRENT_USER_URI   = '/libs/granite/security/currentuser.json',
          ANONYMOUS_USER     = 'anonymous',
          PN_AUTHORIZABLE_ID = 'authorizableId_xss',
          PN_HOME            = 'home',
          PROFILE_EXT        = 'profile.json';

    // initally check to see if profile is ready based on local storage
    if(profileStore.isReady() && profileStore.getUserProfile()) {
        announceProfileLoaded(profileStore.getUserProfile());
    }

    /**
     * Always request user profile for every request 
     * On a successful response the profile is set with local storage
     * and custom event is dispatched to alert dependent JavaScript objects
     */
    $.get(CURRENT_USER_URI + '?nocache=' + new Date().getTime(), function(data) {
        if(data.type === 'user') {
            // Set the profile store to enable local storage
            profileStore.setUserProfile(data);

            const currentUser = profileStore.getUserProfile();

            // if not anonymous, request extra attributes
            if(currentUser[PN_AUTHORIZABLE_ID] !== ANONYMOUS_USER) {
                getExtraProfileAttributes(currentUser);
            } else {
                // announce profile loaded
                announceProfileLoaded(currentUser);
            }
        } else {
            console.error('Could not retrieve a user profile, cart functionality disabled');
        }

    }).fail(function() {
        console.error('Could not retrieve a user profile, cart functionality disabled');
    });

    /**
     * Based on a user home retrieve extra attributes to populate the profile
     * Announce profile is loaded after successful call
     * @param {*} currentUser - current user object
     */
    function getExtraProfileAttributes(currentUser) {
        $.get(currentUser[PN_HOME] + '/' + PROFILE_EXT, function(data) {

            for (const [key, value] of Object.entries(data)) {
                // skip keys that start with jcr or sling
                if (!key.startsWith('jcr:') && !key.startsWith('sling:')) {
                    currentUser[key] = value;
                }
            }

            // update profile in local storage
            profileStore.setUserProfile(currentUser);

            //announce profile updated
            announceProfileLoaded(currentUser);

        }).fail(function() {
            if (currentUser) {
                // update profile in local storage
                profileStore.setUserProfile(currentUser);

                //announce profile updated
                announceProfileLoaded(currentUser);
            }
            console.error('Could not retrieve a user home, extra profile attributes not set.');
        });
    }

    /**
     * Dispatch event that user profile has been loaded.
     * @param {*} currentUser 
     */
    function announceProfileLoaded(currentUser) {
        $('body').trigger(ns.Events.PROFILE_LOAD, {detail:currentUser});
    }

}(jQuery,
    AssetShare,
    AssetShare.Store.Profile));
