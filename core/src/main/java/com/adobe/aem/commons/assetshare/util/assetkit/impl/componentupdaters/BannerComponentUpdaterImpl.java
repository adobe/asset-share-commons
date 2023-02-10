/*
 * Asset Share Commons
 *
 * Copyright (C) 2023 Adobe
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
package com.adobe.aem.commons.assetshare.util.assetkit.impl.componentupdaters;

import com.adobe.aem.commons.assetshare.components.assetkit.impl.AssetKitImpl;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.util.assetkit.AssetKitHelper;
import com.adobe.aem.commons.assetshare.util.assetkit.ComponentUpdater;
import com.day.cq.wcm.api.Page;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.Collection;

@Component
@Designate(ocd = BannerComponentUpdaterImpl.Config.class)
public class BannerComponentUpdaterImpl implements ComponentUpdater {
    private static final Logger log = LoggerFactory.getLogger(BannerComponentUpdaterImpl.class);

    @Reference
    private transient AssetKitHelper assetKitHelper;

    private Config config;

    @Override
    public String getName() {
        return "Banner component (Asset Share Commons)";
    }

    @Override
    public void updateComponent(Page assetKitPage, Resource assetKit) {

        final Collection<? extends AssetModel> assets = assetKitHelper.getAssets(new Resource[]{assetKit});

        assets.stream().filter(asset -> StringUtils.equals("banner", StringUtils.lowerCase(asset.getTitle()))).findFirst().ifPresent(asset -> {
            try {
                assetKitHelper.updateComponentOnPage(assetKitPage, config.resource_type(), config.banner_asset_path_property(), asset.getPath());
            } catch (PersistenceException | RepositoryException e) {
                log.error(String.format("Failed to update banner component on page [ %s ]", assetKitPage.getPath()), e);
            }
        });
    }

    @Activate
    @Modified
    protected void activate(Config config) {
        this.config = config;
    }

    @ObjectClassDefinition(
        name = "Asset Share Commons - Banner Component Updater",
        description = "Component updater that updates an Banner component"
    )
    @interface Config {
        @AttributeDefinition(
            name = "Resource Type",
            description = "The resource type of the component to update."
        )
        String resource_type() default "asset-share-commons/components/content/image";

        @AttributeDefinition(
            name = "Banner asset property",
            description = "The property name that holds the banner asset's path."
        )
        String banner_asset_path_property() default "fileReference";
    }
}
