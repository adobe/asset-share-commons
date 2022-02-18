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

package com.adobe.aem.commons.assetshare.components.structure.impl;

import com.adobe.aem.commons.assetshare.components.structure.UserMenu;
import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ExporterConstants;
import com.day.cq.wcm.api.designer.Style;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;

@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {UserMenu.class, ComponentExporter.class},
        resourceType = UserMenuImpl.RESOURCE_TYPE
)
@Exporter(name = ExporterConstants.SLING_MODEL_EXPORTER_NAME, extensions = ExporterConstants.SLING_MODEL_EXTENSION)
public class UserMenuImpl implements UserMenu {
    private static final Logger log = LoggerFactory.getLogger(UserMenuImpl.class);

    protected static final String RESOURCE_TYPE = "asset-share-commons/components/structure/user-menu";

    private static final String PROFILE_PATH_SUFFIX = "/primary/image.prof.thumbnail.48.48.png";

    private static final String ANONYMOUS = "anonymous";
    private static final String NN_PROFILE = "profile";

    private static final String PN_LOG_IN_LABEL = "logInLabel";
    private static final String PN_LOG_IN_LINK = "logInLink";
    private static final String PN_LOG_OUT_LABEL = "logOutLabel";
    private static final String PN_LOG_OUT_LINK = "logOutLink";

    @Self
    @Required
    private SlingHttpServletRequest request;

    @Inject
    @Required
    private Style currentStyle;

    @Override
    public boolean isReady() {
        final String logInLabel = currentStyle.get(PN_LOG_IN_LABEL, String.class);
        final String logInLink = currentStyle.get(PN_LOG_IN_LINK, String.class);
        final String logOutLabel = currentStyle.get(PN_LOG_OUT_LABEL, String.class);
        final String logOutLink = currentStyle.get(PN_LOG_OUT_LINK, String.class);

        return StringUtils.isNotEmpty(logInLabel) &&
                StringUtils.isNotEmpty(logInLink) &&
                StringUtils.isNotEmpty(logOutLabel) &&
                StringUtils.isNotEmpty(logOutLink);
    }

    @Nonnull
    @Override
    public String getExportedType() {
        return RESOURCE_TYPE;
    }
}