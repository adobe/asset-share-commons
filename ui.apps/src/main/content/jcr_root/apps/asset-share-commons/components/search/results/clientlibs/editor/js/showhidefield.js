(function(document, $) {

    var CHECKBOX_CLASS = ".cmp-editor-use-thumbnail-servlet";
    var PATHFIELD_WRAPPER_CLASS = ".cmp-editor-missing-image-wrapper";

    $(document).on("foundation-contentloaded", function (e) {

        togglePathField();

        $(CHECKBOX_CLASS).on('change', function (event, payload) {
           togglePathField();
        });

    });

    function togglePathField(){

        if( $(CHECKBOX_CLASS).prop( "checked" ) === true ){
            console.log("true");
            $(PATHFIELD_WRAPPER_CLASS).attr("hidden", "");
        } else {
            console.log("false");
            $(PATHFIELD_WRAPPER_CLASS).removeAttr("hidden");
        }
    }

})(document,Granite.$);
