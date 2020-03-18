package com.adobe.aem.commons.assetshare.testhelpers;

import com.adobe.cq.wcm.core.components.models.form.OptionItem;
import com.adobe.cq.wcm.core.components.models.form.Options;
import org.apache.sling.api.resource.Resource;

import java.util.ArrayList;
import java.util.List;


public class TestOptionsImpl implements Options {

    private final Resource resource;
    private List<OptionItem> optionItems = null;

    public TestOptionsImpl(Resource resource) {
        this.resource = resource;
    }

    public List<OptionItem> getItems() {
        if (optionItems == null) {
            optionItems = new ArrayList<>();
            resource.listChildren().forEachRemaining(r -> {
                final String text = r.getValueMap().get("text", String.class);
                final String value = r.getValueMap().get("value", String.class);
                optionItems.add(new TestOptionItemImpl(text, value));
            });
        }

        return optionItems;
    }

    @Override
    public Options.Type getType() {
        return Type.CHECKBOX;
    }

    public class TestOptionItemImpl implements OptionItem {
        private final String text;
        private final String value;

        public TestOptionItemImpl(String text, String value) {
            this.text = text;
            this.value = value;
        }

        public boolean isSelected() {
            return false;
        }

        public boolean isDisabled() {
            return false;
        }

        public String getValue() {
            return value;
        }

        public String getText() {
            return text;
        }
    }
}
