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
AssetShare.Store.Profile = (function (ns, store) {
    'use strict';

    var PROFILE_KEY       = 'profile',
        currentUserId;

    /**
     * currentUserId is set
     */
    function isReady() {
        return currentUserId !== undefined && currentUserId.length > 0;
    }

    /* Return the user profile for the current logged in user from local store */
    function getUserProfile() {

        if(currentUserId === undefined) {
            return null;
        }

        var userObject = store.getObject(currentUserId);
        if(userObject) {
            return userObject[PROFILE_KEY];
        }
        return null;
    }

    /**
     * Returns an object beneath the current user's profile in store
     * @param {*} key 
     */
    function getUserStoreObject(key) {
        var userObject = store.getObject(currentUserId);
        if(userObject) {
            return userObject[key];
        }
        return null;
       
    }

    /**
     * Sets a key and object beneath the current user's profile.
     * @param {*} key 
     * @param {*} object 
     */
    function setUserStoreObject(key, object) {
        var userObject = store.getObject(currentUserId);
        if(userObject) {
            //update key beneath user with new value
            userObject[key] = object;
            // persist user to store
            store.setObject(currentUserId, userObject);
        }
    }

    /**
     * Initialize the profile store
     * To be called after profile ajax request is performed
     * Only after the user profile is set will the store be ready.
     * @param {*} profile 
     */
    function setUserProfile(profile) {

        if(typeof profile === 'undefined' || profile.type !== 'user') {
            return;
        }

        //set current user
        currentUserId = profile.authorizableId_xss;
        
        var userObject = store.getObject(currentUserId);
        if(userObject) {
            //update existing profile
            userObject[PROFILE_KEY] = profile;
        } else {
            //new user profile
            userObject = {profile: profile};
        }

        store.setObject(currentUserId, userObject);
    }

    return {
        setUserProfile: setUserProfile,
        getUserProfile: getUserProfile,
        isReady: isReady,
        getUserStoreObject, getUserStoreObject,
        setUserStoreObject, setUserStoreObject
    };

}(AssetShare,
  AssetShare.Store));