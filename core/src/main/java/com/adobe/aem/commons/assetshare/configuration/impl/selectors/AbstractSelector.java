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

package com.adobe.aem.commons.assetshare.configuration.impl.selectors;

import com.adobe.aem.commons.assetshare.configuration.Config;
import org.apache.commons.lang3.StringUtils;

public class AbstractSelector {
    protected String buildUrl(final Config config, String pageName) {
        if (pageName == null) {
            return null;
        } else {
            pageName = StringUtils.lowerCase(pageName);
            pageName = StringUtils.replace(pageName, " ", "-");
            pageName = StringUtils.trim(pageName);

            if (StringUtils.isBlank(config.getAssetDetailsPath())) {
                return null;
            } else {
                if (StringUtils.isNotBlank(pageName)) {
                    pageName = "/" + pageName;
                }
                return config.getAssetDetailsPath() + pageName + ".html";
            }
        }
    }
}
