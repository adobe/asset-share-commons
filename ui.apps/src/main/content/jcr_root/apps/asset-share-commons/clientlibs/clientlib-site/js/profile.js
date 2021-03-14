/**
 * Utility specific to storing data about the users Profile
 */
AssetShare.Profile = (function (ns, profileStore) {
    'use strict';

    const CURRENT_USER_URI  = '/libs/granite/security/currentuser.json';

    /**
     * Make the initial request to retrieve user profile information
     */
    async function initProfile() {
        let response = await fetch(CURRENT_USER_URI + '?nocache=' + new Date().getTime());
        if(response.ok) {
           return await response.json();
        } else {
            throw new Error(`Error retrieving profile: ${response.status}`);
        }
    }

    /**
     * Dispatch custom event that the user profile has been loaded
     * @param {*} profile 
     */
    function announceProfileLoaded(profile) {
        const event = new CustomEvent(ns.Events.PROFILE_LOAD, {detail:profile}),
              element = document.getElementsByTagName('body')[0];
        
        element.dispatchEvent(event);
    }

    /**
     * Initial call to request user profile 
     * On a successful response the profile is set with local storage
     * and custom event is dispatched to alert dependent JavaScript objects
     */
    initProfile().then((json) => {
        if(json.type === 'user') {
            profileStore.setUserProfile(json);
            announceProfileLoaded(json);
        } else {
            console.error('Could not retrieve user profile, cart functionality disabled');
        }
    })
    .catch((e) =>
        console.log(e)
    );

}(AssetShare,
  AssetShare.Store.Profile));