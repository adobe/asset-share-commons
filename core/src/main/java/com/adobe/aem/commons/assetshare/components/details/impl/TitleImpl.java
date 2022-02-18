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

package com.adobe.aem.commons.assetshare.components.details.impl;

import com.adobe.aem.commons.assetshare.components.details.Title;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ExporterConstants;
import com.day.cq.wcm.api.Page;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.ScriptVariable;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;

@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {Title.class, ComponentExporter.class},
        resourceType = {TitleImpl.RESOURCE_TYPE},
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
@Exporter(name = ExporterConstants.SLING_MODEL_EXPORTER_NAME, extensions = ExporterConstants.SLING_MODEL_EXTENSION)
public class TitleImpl extends AbstractEmptyTextComponent implements Title {
    protected static final String RESOURCE_TYPE = "asset-share-commons/components/details/title";

    @Self
    @Required
    private SlingHttpServletRequest request;

    @ValueMapValue
    private String returnPath;

    @ValueMapValue @Default(values = { "asset-share-commons/components/structure/search-page" })
    private String[] returnPathResourceTypes;

    @ValueMapValue
    @Default(values = "title")
    private String propertyName;

    @Self
    @Required
    private AssetModel asset;

    @ScriptVariable
    @Required
    private Page currentPage;

    /***
     * ValueMap of the properties of the Asset currently being viewed
     */
    private ValueMap combinedProperties;

    @PostConstruct
    public void init() {
        this.combinedProperties = asset.getProperties();
    }

    @Override
    public String getTitle() {
        if (StringUtils.isBlank(propertyName)) {
            propertyName = com.adobe.aem.commons.assetshare.content.properties.impl.TitleImpl.NAME;
        }

        final String value = combinedProperties.get(propertyName, "");

        if (StringUtils.isBlank(value)) {
            return combinedProperties.get(com.adobe.aem.commons.assetshare.content.properties.impl.TitleImpl.NAME, "");
        } else {
            return value;
        }
    }

    @Override
    public String getReturnPath() {
        if (StringUtils.isBlank(returnPath)) {
            Page page = currentPage;

            while (page != null) {

                for(String returnPathResourceType : returnPathResourceTypes) {
                    if (page.getContentResource().isResourceType(returnPathResourceType)) {
                        return page.getPath();
                    }
                }

                page = page.getParent();
            }
        }

        return returnPath;
    }

    @Override
    public boolean isReady() {
        return !isEmpty();
    }

    @Override
    public boolean isEmpty() {
        return StringUtils.isBlank(getTitle());
    }

    @Nonnull
    @Override
    public String getExportedType() {
        return RESOURCE_TYPE;
    }
}
