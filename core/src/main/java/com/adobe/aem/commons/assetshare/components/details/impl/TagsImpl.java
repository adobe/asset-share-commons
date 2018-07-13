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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.ScriptVariable;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.util.*;

@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {Tags.class},
        resourceType = {TagsImpl.RESOURCE_TYPE},
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class TagsImpl extends AbstractEmptyTextComponent implements Tags {
    private static final Logger log = LoggerFactory.getLogger(TagsImpl.class);

    protected static final String RESOURCE_TYPE = "asset-share-commons/components/details/tags";

    @Self
    @Required
    private SlingHttpServletRequest request;

    @Self
    @Required
    private AssetModel asset;

    @ValueMapValue
    @Named("tagPropertyName")
    private String[] tagPropertyNames;

    @ScriptVariable
    private Page currentPage;

    private List<String> tagTitles;

    @Override
    public List<String> getTagTitles() {
        if (tagTitles == null) {
            if (ArrayUtils.isEmpty(tagPropertyNames)) {
                tagTitles = asset.getProperties().get(TagTitlesImpl.NAME, tagTitles);
            } else {
                tagTitles = getCombinedTagTitles();
            }
        }

        return tagTitles;
    }

    /**
     * @return a list of combined (ordered and de-duped) tag titles for all provided tag properties.
     */
    private List<String> getCombinedTagTitles() {
        // De-dupes content.
        final Set<String> combinedTagTitles = new LinkedHashSet<>();

        for (final String tagPropertyName : tagPropertyNames) {
        	final Collection<String> tagValues = getTagValuesAsList(tagPropertyName);

            if (!tagValues.isEmpty()) {
                combinedTagTitles.addAll(getTagTitles(tagValues));
            }
        }
       
        return new ArrayList<>(combinedTagTitles);
    }

    /**
     * Gets the tag titles from for a single property.
     *
     * @param tagValues the tag values from the single property (may be tagIds or may be tag titles (ie. in the case of smart tags).
     * @return a collection of tag titles.
     */
    private Collection<String> getTagTitles(final Collection<String> tagValues) {
        final Locale locale = getLocale();
        final TagManager tagManager = request.getResourceResolver().adaptTo(TagManager.class);
        final Collection<String> combinedTagTitles = new LinkedHashSet<>();

        for (final String tagId: tagValues) {
            final Tag tag = tagManager.resolve(tagId);

            if (tag != null) {
                combinedTagTitles.add(tag.getTitle(locale));
            } else {
                combinedTagTitles.add(tagId);
            }
        }

        return combinedTagTitles;
    }


    /**
     * This method tries to coerce the property value into a Collection, which is the expected collection type.
     * This is necessary since computed properties can be passed in as the {@tagPropertyName} and can return various values.
     * Only List, Set, String[], and String are supported return types. All other returned types will be skipped.
     *
     * @param tagPropertyName the property name (may be real or computed) from which to gather the tag values.
     * @return a collection of tag values, or an empty collection if none can be found.
     */
    private Collection<String> getTagValuesAsList(final String tagPropertyName) {
        final Object value = asset.getProperties().get(tagPropertyName);
        Collection<String> tagValues = new ArrayList<>();

        if (value instanceof List) {
            tagValues = (List<String>) value;
        } else if (value instanceof String[]) {
            tagValues = Arrays.asList((String[])value);
        } else if (value instanceof String) {
            tagValues.add((String) value);
        } else if (value instanceof Set) {
            tagValues = new ArrayList<>((Set) value);
        } else {
            log.warn("Failed to collect Tags from incompatible Computed Property [ {} ]", tagPropertyName);
        }
        return tagValues;
    }

    private Locale getLocale() {
        if (currentPage == null) {
            return request.getLocale();
        } else {
            return currentPage.getLanguage(false);
        }
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
