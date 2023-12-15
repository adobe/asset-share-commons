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
import com.adobe.aem.commons.assetshare.content.properties.impl.AssetTypeImpl;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@Component(
        property = {
                "label:String=" + AssetTypeSelectorImpl.LABEL,
                "id:String=" + AssetTypeSelectorImpl.ID
        },
        service = AssetDetailsSelector.class
)
@Designate(ocd = AssetTypeSelectorImpl.Cfg.class)
public class AssetTypeSelectorImpl extends AbstractSelector implements AssetDetailsSelector {
    public static final String LABEL = "Asset Type";
    public static final String ID = "asset-type";

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
        return StringUtils.equalsIgnoreCase(config.getAssetDetailsSelector(), ID);
    }

    @Override
    public String getUrl(final Config config, final AssetModel asset) {
        return buildUrl(config, asset.getProperties().get(AssetTypeImpl.NAME, String.class));
    }

    @Activate
    protected void activate(Cfg cfg) {
        this.cfg = cfg;
    }

    @ObjectClassDefinition(name = "Asset Share Commons - Asset Details Selector - Asset Type")
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