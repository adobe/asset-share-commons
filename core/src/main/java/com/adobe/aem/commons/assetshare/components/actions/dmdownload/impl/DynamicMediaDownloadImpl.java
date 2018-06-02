/*
 * Asset Share Commons
 *
 * Copyright (C) 2018 Adobe
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

package com.adobe.aem.commons.assetshare.components.actions.dmdownload.impl;

import com.adobe.aem.commons.assetshare.components.actions.dmdownload.DynamicMediaDownload;
import com.adobe.aem.commons.assetshare.components.actions.download.impl.DownloadImpl;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collection;

@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {DynamicMediaDownload.class},
        resourceType = {DynamicMediaDownloadImpl.RESOURCE_TYPE}
)
public class DynamicMediaDownloadImpl extends DownloadImpl implements DynamicMediaDownload {
    protected static final String RESOURCE_TYPE = "asset-share-commons/components/modals/download-dynamic-media";

    @ValueMapValue
    @Optional
    @Default(values = {})
    private String[] imagePresets;

    @PostConstruct
    protected void init() {
        super.init();
    }

    public final Collection<String> getImagePresets() {
        return Arrays.asList(imagePresets);
    }
}