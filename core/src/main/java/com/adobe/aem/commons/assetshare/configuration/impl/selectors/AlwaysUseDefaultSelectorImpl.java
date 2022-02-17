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

package com.adobe.aem.commons.assetshare.configuration.impl.selectors;

import com.adobe.aem.commons.assetshare.configuration.AssetDetailsSelector;
import com.adobe.aem.commons.assetshare.configuration.Config;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@Component(
        property = {
                "label:String=" + AlwaysUseDefaultSelectorImpl.LABEL,
                "id:String=" + AlwaysUseDefaultSelectorImpl.ID
        },
        service = AssetDetailsSelector.class
)
@Designate(ocd = AlwaysUseDefaultSelectorImpl.Cfg.class)
public class AlwaysUseDefaultSelectorImpl implements AssetDetailsSelector {
    public static final String LABEL = "Always use default Asset Details page";
    public static final String ID = "always-use-default";

    private Cfg cfg;

    @Override
    public String getLabel() {
        return cfg.label();
    }

    @Override
    public String getId() {
        return cfg.id();
    }

    @Override
    public boolean accepts(final Config config, final AssetModel asset) {
        return StringUtils.equals(config.getAssetDetailsSelector(), ID);
    }

    @Override
    public String getUrl(final Config config, final AssetModel asset) {
        return config.getAssetDetailsUrl();
    }

    @Activate
    protected void activate(Cfg cfg) {
        this.cfg = cfg;
    }

    @ObjectClassDefinition(name = "Asset Share Commons - Asset Details Selector - Always Use Default Asset Details Page")
    public @interface Cfg {
        @AttributeDefinition(
                name = "Label",
                description = "Human read-able label."
        )
        String label() default LABEL;

        @AttributeDefinition(
                name = "ID",
                description = "Defines the id of data this exposes. Should be unique across selectors."
        )
        String id() default ID;
    }
}
