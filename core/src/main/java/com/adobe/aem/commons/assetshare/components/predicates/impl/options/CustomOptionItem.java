/*
 * Asset Share Commons
 *
 * Copyright (C) 2018 Adobe
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
import org.apache.commons.lang3.StringUtils;

public class CustomOptionItem implements OptionItem {
    private final boolean selected;
    private final String text;
    private String value;
    private String customValue;

    public CustomOptionItem(final String value, final String customValue) {
        this(null, value, customValue, false);
    }

    public CustomOptionItem(final String text, final String value, final String customValue, final boolean selected) {
        this.text = text;
        this.selected = selected;
        this.value = value;
        this.customValue = value;
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
        return StringUtils.defaultIfEmpty(customValue, value);
    }

    @Override
    public String getText() {
        return text;
    }

}
