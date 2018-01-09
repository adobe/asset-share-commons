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

import com.adobe.aem.commons.assetshare.components.details.ActionButtons;
import com.adobe.aem.commons.assetshare.configuration.Config;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {ActionButtons.class},
        resourceType = ActionButtonsImpl.RESOURCE_TYPE,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class ActionButtonsImpl extends AbstractEmptyTextComponent implements ActionButtons {
    protected static final String RESOURCE_TYPE = "asset-share-commons/components/details/action-buttons";

    @Self
    @Required
    private SlingHttpServletRequest request;

    @ValueMapValue
    private String downloadLabel;

    @ValueMapValue
    private String addToCartLabel;

    @ValueMapValue
    private String removeFromCartLabel;

    @ValueMapValue
    private String shareLabel;

    @Override
    public boolean isEmpty() {
        return !isReady();
    }

    @Override
    public boolean isReady() {
        Config config = request.adaptTo(Config.class);
        if (config == null) {
            return false;
        } else if (isCartEnabled(config) || isDownloadEnabled(config) || isShareEnabled(config)) {
            // At least one action is available
            return true;
        }

        return false;
    }

    private boolean isCartEnabled(Config config) {
        return config.isCartEnabled() && StringUtils.isNotBlank(addToCartLabel) && StringUtils.isNotBlank(removeFromCartLabel);
    }

    private boolean isDownloadEnabled(Config config) {
        return config.isDownloadEnabled() && StringUtils.isNotBlank(downloadLabel);
    }

    private boolean isShareEnabled(Config config) {
        return config.isShareEnabled() && StringUtils.isNotBlank(shareLabel);
    }
}
