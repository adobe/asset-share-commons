/*
 * Asset Share Commons
 *
 * Copyright (C) 2019 Adobe
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

package com.adobe.aem.commons.assetshare.components.details.impl;

import com.adobe.cq.wcm.core.components.models.form.OptionItem;
import com.adobe.cq.wcm.core.components.models.form.Options;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;

import java.util.ArrayList;
import java.util.List;

/**
 * Test-only Sling Model that stands in for the Core Components {@code Options} model that
 * would normally be registered against the legacy "renditions" multifield widget dialog.
 *
 * It is registered for the same resourceType as {@link RenditionsImpl} (but adapts to a
 * different type - {@link Options}), which allows {@code RenditionsImpl}'s
 * {@code request.adaptTo(Options.class)} call (used for legacy rendition configuration) to
 * resolve to real, testable data driven by a "legacy-options" child resource.
 */
@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {Options.class},
        resourceType = {RenditionsImpl.RESOURCE_TYPE}
)
public class LegacyOptionsTestModel implements Options {
    private static final String NN_LEGACY_OPTIONS = "legacy-options";

    @Self
    private SlingHttpServletRequest request;

    private List<OptionItem> items;

    @Override
    public List<OptionItem> getItems() {
        if (items == null) {
            items = new ArrayList<>();

            final Resource legacyOptions = request.getResource().getChild(NN_LEGACY_OPTIONS);

            if (legacyOptions != null) {
                for (final Resource child : legacyOptions.getChildren()) {
                    final String text = child.getValueMap().get("text", String.class);
                    final String value = child.getValueMap().get("value", String.class);
                    items.add(new TestOptionItem(text, value));
                }
            }
        }

        return items;
    }

    @Override
    public Type getType() {
        return Type.CHECKBOX;
    }

    private static class TestOptionItem implements OptionItem {
        private final String text;
        private final String value;

        TestOptionItem(String text, String value) {
            this.text = text;
            this.value = value;
        }

        @Override
        public boolean isSelected() {
            return false;
        }

        @Override
        public boolean isDisabled() {
            return false;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public String getText() {
            return text;
        }
    }
}
