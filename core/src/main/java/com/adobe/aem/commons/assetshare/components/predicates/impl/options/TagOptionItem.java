/*
 * Asset Share Commons
 *
 * Copyright (C) 2017 Adobe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.adobe.aem.commons.assetshare.components.predicates.impl.options;

import com.adobe.cq.wcm.core.components.models.form.OptionItem;
import com.day.cq.tagging.Tag;

import java.util.Locale;

public class TagOptionItem implements OptionItem {
    private Tag tag;
    private Locale locale;
    private boolean selected;

    public TagOptionItem(Tag tag, Locale locale, boolean selected) {
        this.tag = tag;
        this.locale = locale;
        this.selected = selected;
    }

    @Override
    public boolean isSelected() {
        return selected;
    }

    @Override
    public boolean isDisabled() {
        return false;
    }

    @Override
    public String getValue() {
        if (tag != null) {
            return tag.getTagID();
        }
        return "";
    }

    @Override
    public String getText() {
        if (tag != null) {
            return tag.getTitle(locale);
        }
        return "";
    }
}
