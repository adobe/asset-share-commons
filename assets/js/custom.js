$(function() {
    "use strict";

    $('a[href^="http"],a[href^="https"]').attr('target', '_blank');
});


$(function() {
    "use strict";

    $('.doc-page img[alt]').each(function() {
        var $img = $(this),
            $wrapper = $("<div class='image--with-caption'></div>");

        $img.wrap($wrapper);
        $img.after($('<p class="image__caption">' + $img.attr('alt') + '</p>'));
    });
});

$(function() {
    $(document).pjax('a', '#main');
});
