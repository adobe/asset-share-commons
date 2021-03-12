AssetShare.StorageProfile = (function ($, ns, storage) {
    "use strict";

    var PROFILE_KEY       = "profile",
        CURRENT_USER_URI  = "/libs/granite/security/currentuser.json",
        currentUserId;

    /**
     * currentUserId is set
     */
    function isReady() {
        return currentUserId !== undefined && currentUserId.length > 0;
    }

    /* Return the user profile for the current logged in user from local storage */
    function getUserProfile() {

        if(currentUserId === undefined) {
            return null;
        }

        var userObject = storage.getObject(currentUserId);
        if(userObject) {
            return userObject[PROFILE_KEY];
        }
        return null;
    }

    /**
     * Returns an object beneath the current user's profile in storage
     * @param {*} key 
     */
    function getUserStorageObject(key) {
        var userObject = storage.getObject(currentUserId);
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
    function setUserStorageObject(key, object) {
        var userObject = storage.getObject(currentUserId);
        if(userObject) {
            //update key beneath user with new value
            userObject[key] = object;
            // persist user to storage
            storage.setObject(currentUserId, userObject);
        }
    }

    /* Set the User profile in local storage */
    function _setUserProfile(profile) {

        if(typeof profile === 'undefined' || profile.type !== 'user') {
            return;
        }

        //set current user
        currentUserId = profile.authorizableId_xss;
        
        var userObject = storage.getObject(currentUserId);
        if(userObject) {
            //update existing profile
            userObject[PROFILE_KEY] = profile;
        } else {
            //new user profile
            userObject = {profile: profile};
        }

        storage.setObject(currentUserId, userObject);
    }

    /* make a new request to get current user's profile */
    function _initProfile() {

        return new Promise(function(resolve, reject) {
            var request = new XMLHttpRequest();
            request.open('GET', CURRENT_USER_URI + '?nocache=' + new Date().getTime());
            request.responseType = 'json';
            request.onload = function() {
                if (request.status === 200) {
                    resolve(request.response);
                }
                else {
                    reject(Error('profile did not load' + request.statusText));
                }
            };
            request.onerror = function() {
                // Also deal with the case when the entire request fails to begin with
                // This is probably a network error, so reject the promise with an appropriate message
                    reject(Error('There was a network error.'));
             };
            request.send();
        });
    }

    /* dispatch event that profile was loaded sucessfully */
    function _announceProfileLoaded(profile) {
        var event = new CustomEvent(ns.Events.PROFILE_LOAD, {detail:profile}),
            element = document.getElementsByTagName('body')[0];
            element.dispatchEvent(event);
    }

    //initial call to retrieve profile
    _initProfile().then(function(response) {
        if(response.type === 'user') {
           _setUserProfile(response);
           _announceProfileLoaded(response);
        }
    });

    return {
        getUserProfile: getUserProfile,
        isReady: isReady,
        getUserStorageObject, getUserStorageObject,
        setUserStorageObject, setUserStorageObject
    };

}(jQuery,
    AssetShare,
    AssetShare.Storage));