jQuery((function($, ns, cart) {
    "use strict";

    function isAnonymous() {
        var authorizableId = profile.getItem("/authorizableId");
        return !authorizableId || "anonymous" === authorizableId;
    }

    function showAnonymous() {
        var anonymousSection = ns.Elements.element(ANONYMOUS_SECTION_ID),
            authenticatedSection = ns.Elements.element(AUTHENTICATED_SECTION_ID);

        authenticatedSection.hide();
        anonymousSection.show();
    }

    

}(jQuery,
    AssetShare,
    AssetShare.Cart)));

