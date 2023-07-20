package com.adobe.aem.commons.assetshare.components.predicates.impl.options;

import com.adobe.cq.wcm.core.components.models.form.OptionItem;

public class JsonOptionItem implements OptionItem {
	private String text;
    private String value;
    private boolean selected;
    
    
    public JsonOptionItem(String text, String value, boolean selected) {
        this.text = text;
        this.value = value;
        this.selected = selected;
    }


	public String getText() {
		return text;
	}


	public String getValue() {
		return value;
	}


	public boolean isSelected() {
		return selected;
	}

    
}
