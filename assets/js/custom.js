$(function() {
    "use strict";

    $('a[href^="http"],a[href^="https"]').attr('target', '_blank');
});


$(function() {
    "use strict";

    var path = window.location.pathname.replace(/\/$/, ''),
        listItem,
        subList,
        opener;

        $("#menu .opener").removeClass("active");

        listItem = $("#menu a[href^='" + path + "']");

        if (listItem.length === 1) {
            subList = listItem.closest("ul");

            if (subList.length === 1) {
                opener = subList.siblings('span.opener');

                if (opener) {
                    opener.addClass("active");
                }
            }
        }
});

$(function() {
    $(document).pjax('a', '#main');
});



