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

import com.adobe.aem.commons.assetshare.components.details.Tags;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.content.properties.impl.TagTitlesImpl;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.Page;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.ScriptVariable;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {Tags.class},
        resourceType = {TagsImpl.RESOURCE_TYPE},
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class TagsImpl extends AbstractEmptyTextComponent implements Tags {
    protected static final String RESOURCE_TYPE = "asset-share-commons/components/details/tags";

    @Self
    @Required
    private SlingHttpServletRequest request;

    @Self
    @Required
    private AssetModel asset;

    @ValueMapValue
    private String tagPropertyName;

    @ScriptVariable
    private Page currentPage;

    private List<String> tagTitles;
    
    @ValueMapValue
    @Optional
    @Default(values = "false")
    private Boolean displayComputedProperty;

    @Override
    public List<String> getTagTitles() {
        if (tagTitles == null) {
            if (StringUtils.isBlank(tagPropertyName) && asset.getProperties() != null) {
                tagTitles = asset.getProperties().get(TagTitlesImpl.NAME, tagTitles);
            } else {
                tagTitles = getOverrideTags();
            }
        }

        return tagTitles;
    }

    private List<String> getOverrideTags() {
        final List<String> overrideTagTitles = new ArrayList<>();
        final Locale locale = currentPage == null ? request.getLocale() : currentPage.getLanguage(false);

        final TagManager tagManager = request.getResourceResolver().adaptTo(TagManager.class);
        final String[] tagIds = asset.getProperties().get(tagPropertyName, new String[]{});

        for (final String tagId : tagIds) {
        	final Tag tag = tagManager.resolve(tagId);

        	if (tag != null) {
        		overrideTagTitles.add(tag.getTitle(locale));
        	}
        	else if (StringUtils.isNotBlank(tagId) && displayComputedProperty) {
            	List<String> listOfComputedPropValues = new ArrayList<>();
            	if (asset.getProperties().get(tagPropertyName) instanceof List) {
                	listOfComputedPropValues = (List<String>) asset.getProperties().get(tagPropertyName);
                }
            	for (final String dispVal : listOfComputedPropValues) {
            		overrideTagTitles.add(dispVal);
            	}
            }
        }

        return overrideTagTitles;
    }

    @Override
    public boolean isEmpty() {
        return getTagTitles() == null || getTagTitles().size() == 0;
    }

    @Override
    public boolean isReady() {
        return !isEmpty() || hasEmptyText();
    }
}
