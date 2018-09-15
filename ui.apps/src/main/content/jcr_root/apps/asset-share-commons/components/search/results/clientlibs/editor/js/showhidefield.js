(function(document, $) {

    var CHECKBOX_CLASS = ".cmp-editor-custom-missing-image";
    var PATHFIELD_WRAPPER_CLASS = ".cmp-editor-missing-image-wrapper";

    $(document).on("foundation-contentloaded", function (e) {

        togglePathField();

        $(CHECKBOX_CLASS).on('change', function (event, payload) {
           togglePathField();
        });

    });

    function togglePathField(){

        if( $(CHECKBOX_CLASS).prop( "checked" ) == true ){
            $(PATHFIELD_WRAPPER_CLASS).removeAttr("hidden");
        } else {
            $(PATHFIELD_WRAPPER_CLASS).attr("hidden", "");
        }
    }

})(document,Granite.$);
