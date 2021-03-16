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
            //dispatch event
            $('body').trigger(ns.Events.PROFILE_LOAD, {detail:data});
           // announceProfileLoaded(data);
        }
        
    }).fail(function() {
        console.error('Could not retrieve user profile, cart functionality disabled');
    });

}(jQuery,
    AssetShare,
    AssetShare.Store.Profile));