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
import com.adobe.aem.commons.assetshare.util.assetkit.AssetKitHelper;
import com.adobe.aem.commons.assetshare.util.assetkit.ComponentUpdater;
import com.day.cq.wcm.api.Page;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import javax.jcr.RepositoryException;
@Component
@Designate(ocd = AssetKitComponentUpdaterImpl.Config.class)
public class AssetKitComponentUpdaterImpl implements ComponentUpdater {
    private static String PROPERTY_NAME = "paths";

    @Reference
    private transient AssetKitHelper assetKitHelper;
    private Config config;

    @Override
    public String getName() {
        return "Asset kit component (Asset Share Commons)";
    }

    @Override
    public void updateComponent(Page assetKitPage, Resource assetKit) throws PersistenceException, RepositoryException {
        assetKitHelper.updateComponentOnPage(assetKitPage, config.resource_type(), PROPERTY_NAME, assetKit.getPath());
    }

    @Activate
    @Modified
    protected void activate(Config config) {
        this.config = config;
    }

    @ObjectClassDefinition(
        name = "Asset Share Commons - Asset Kit Component Updater",
        description = "Component updater that updates an Asset Kit component"
    )
    @interface Config {
        @AttributeDefinition(
            name = "Resource Type",
            description = "The resource type of the component to update."
        )
        String resource_type() default AssetKitImpl.RESOURCE_TYPE;
    }
}
