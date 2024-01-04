jQuery((function($) {
	"use strict";

    function updateChecked(target) {
        setDescendants(target);
        setAncestors(target);
    }
    function setDescendants(parent) {
        parent.indeterminate = false;
        let children = document.querySelectorAll(`[data-parentid='${parent.id}']`);
        children.forEach((child) => {
            child.checked = parent.checked;
            child.setAttribute("form", parent.checked ? "none" : child.dataset.formid);
            setDescendants(child);
        });
    }
    function setAncestors(child) {
        let parent = document.getElementById(child.dataset.parentid);
        if (!parent) return;
        
        let children = Array.from(document.querySelectorAll(`[data-parentid='${parent.id}']`));
        parent.checked = children.every((child) => child.checked);
        children.forEach((child) => {
            child.setAttribute("form", parent.checked ? "none" : child.dataset.formid)
        });
        parent.indeterminate = parent.checked ? false : children.some((child) => child.checked || child.indeterminate);
        setAncestors(parent);
    }
    function init() {
        let inputs =  document.querySelectorAll("input[type=checkbox][data-parentid]");
        inputs.forEach((input) => {
            if (input.checked) updateChecked(input);
            input.addEventListener("change", (event) => updateChecked(event.target));
            input.addEventListener("click", (event) => event.stopPropagation());
        });
    }
    init();

}(jQuery)));
