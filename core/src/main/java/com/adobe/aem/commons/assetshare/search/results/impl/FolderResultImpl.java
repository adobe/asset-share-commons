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

package com.adobe.aem.commons.assetshare.search.results.impl;

import com.adobe.aem.commons.assetshare.search.results.FolderResult;
import com.adobe.aem.commons.assetshare.search.results.Size;
import com.day.cq.dam.commons.util.DamUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.util.Calendar;
import java.util.Iterator;

@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {FolderResult.class},
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class FolderResultImpl implements FolderResult {
    private static final Logger log = LoggerFactory.getLogger(FolderResultImpl.class);

    private static final int MAX_SIZE = 1000;

    @Self
    @Required
    private SlingHttpServletRequest request;

    @SlingObject
    @Required
    private Resource resource;

    @ValueMapValue
    @Named(value = "jcr:content/jcr:title")
    private String title;

    @ValueMapValue
    @Named(value = "jcr:title")
    private String legacyTitle;

    @ValueMapValue
    @Named(value = "jcr:created")
    private Calendar created;

    @ValueMapValue
    @Named(value = "jcr:createdBy")
    private String createdBy;

    @ValueMapValue
    @Named(value = "jcr:lastModified")
    private Calendar lastModified;

    @ValueMapValue
    @Named(value = "jcr:lastModifiedBy")
    private String lastModifiedBy;

    // Lazy-loaded in getSize()
    private Size size = null;

    public String getType() {
        return FolderResult.TYPE;
    }

    public String getTitle() {
        return StringUtils.defaultIfEmpty(StringUtils.defaultIfEmpty(title, legacyTitle), getName());
    }

    public String getName() {
        return resource.getName();
    }

    public String getPath() {
        return resource.getPath();
    }

    public Size getSize() {
        if (size == null) {
            int count = 0;
            final Iterator<Resource> children = resource.listChildren();

            while (children.hasNext() && count <= MAX_SIZE) {
                if (DamUtil.isAsset(children.next())) {
                    count++;
                }
            }

            size = new Size(count, children.hasNext());
        }

        return size;
    }

    public Calendar getCreated() {
        return created;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Calendar getLastModified() {
        return lastModified == null ? created : lastModified;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy == null ? createdBy : lastModifiedBy;
    }
}