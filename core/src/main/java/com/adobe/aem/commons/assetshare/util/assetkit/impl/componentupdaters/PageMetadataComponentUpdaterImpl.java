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

import com.adobe.aem.commons.assetshare.util.assetkit.ComponentUpdater;
import com.day.cq.wcm.api.Page;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Component;

import javax.jcr.RepositoryException;

import static com.day.cq.commons.jcr.JcrConstants.*;
@Component
public class PageMetadataComponentUpdaterImpl implements ComponentUpdater {
    @Override
    public String getName() {
        return "Page metadata (Asset Share Commons)";
    }

    @Override
    public void updateComponent(Page assetKitPage, Resource assetKit) throws PersistenceException, RepositoryException {
        final ModifiableValueMap properties = assetKitPage.getContentResource().adaptTo(ModifiableValueMap.class);
        final ValueMap assetKitProperties = assetKit.getValueMap();

        final String title = assetKitProperties.get(JCR_CONTENT + "/" + JCR_TITLE, assetKitProperties.get(JCR_TITLE, assetKit.getName()));
        final String description = assetKitProperties.get(JCR_CONTENT + "/" + JCR_DESCRIPTION, assetKitProperties.get(JCR_DESCRIPTION, ""));

        properties.put(JCR_TITLE, title);
        properties.put(JCR_DESCRIPTION, description);
    }
}

