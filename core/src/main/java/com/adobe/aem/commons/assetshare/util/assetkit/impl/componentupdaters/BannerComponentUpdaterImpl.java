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

import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.util.assetkit.AssetKitHelper;
import com.adobe.aem.commons.assetshare.util.assetkit.ComponentUpdater;
import com.day.cq.wcm.api.Page;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.Collection;
@Component
public class BannerComponentUpdaterImpl implements ComponentUpdater {
    private static final Logger log = LoggerFactory.getLogger(BannerComponentUpdaterImpl.class);

    private static String RESOURCE_TYPE =  "asset-share-commons/components/content/image";
    private static String PROPERTY_NAME = "fileReference";

    @Reference
    private transient AssetKitHelper assetKitHelper;

    @Override
    public String getName() {
        return "Banner component (Asset Share Commons)";
    }

    @Override
    public void updateComponent(Page assetKitPage, Resource assetKit) {

        final Collection<? extends AssetModel> assets = assetKitHelper.getAssets(new Resource[]{assetKit});

        assets.stream().filter(asset -> StringUtils.equals("banner", StringUtils.lowerCase(asset.getTitle()))).findFirst().ifPresent(asset -> {
            try {
                assetKitHelper.updateComponentOnPage(assetKitPage, RESOURCE_TYPE, PROPERTY_NAME, asset.getPath());
            } catch (PersistenceException | RepositoryException e) {
                log.error(String.format("Failed to update banner component on page [ %s ]", assetKitPage.getPath()), e);
            }
        });
    }
}
