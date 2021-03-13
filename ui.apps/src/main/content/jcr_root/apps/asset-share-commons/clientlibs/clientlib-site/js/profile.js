/**
 * Utility specific to storing data about the users Profile
 */
AssetShare.Profile = (function (ns, profileStore) {
    'use strict';

    var CURRENT_USER_URI  = '/libs/granite/security/currentuser.json';

    /* make a new request to get current user's profile */
    function initProfile() {

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

    /**
     * Dispatch custom event that the user profile has been loaded
     * @param {*} profile 
     */
    function announceProfileLoaded(profile) {
        var event = new CustomEvent(ns.Events.PROFILE_LOAD, {detail:profile}),
            element = document.getElementsByTagName('body')[0];
            element.dispatchEvent(event);
    }

    /**
     * Initial call to request user profile 
     * On a successful response the profile is set with local storage
     * and custom event is dispatched to alert dependent JavaScript objects
     */
    initProfile().then(function(response) {
        if(response.type === 'user') {
            profileStore.setUserProfile(response);
            announceProfileLoaded(response);
        }
    });

}(AssetShare,
  AssetShare.Store.Profile));