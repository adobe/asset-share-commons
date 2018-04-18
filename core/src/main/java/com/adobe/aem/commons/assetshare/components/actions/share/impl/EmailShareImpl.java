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

package com.adobe.aem.commons.assetshare.components.actions.share.impl;

import com.adobe.aem.commons.assetshare.components.actions.share.EmailShare;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {EmailShare.class},
        resourceType = {EmailShareImpl.RESOURCE_TYPE},
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class EmailShareImpl extends ShareImpl implements EmailShare {
    protected static final String RESOURCE_TYPE = "asset-share-commons/components/modals/share";

    @Self
    @Required
    private SlingHttpServletRequest request;

    @ValueMapValue
    private String emailTemplatePath;

    @ValueMapValue
    @Default(values = {"email", "path", "message"})
    private List<String> allowedQueryParams;

    private Resource resource;

    @PostConstruct
    protected void init() {
        super.init();
        resource = request.getResource();
    }

    @Override
    public ValueMap getProperties() {
        return resource.getValueMap();
    }

    @Override
    public ValueMap getConfiguredData() {
        final Resource configuredData = resource.getChild("data");
        if (configuredData != null) {
            return configuredData.getValueMap();
        } else {
            return ValueMap.EMPTY;
        }
    }

    @Override
    public ValueMap getUserData() {
        if (request == null) {
            return ValueMap.EMPTY;
        }

        final Map<String, Object> userData = new HashMap<>();
        final Enumeration<String> keys = request.getParameterNames();
        while (keys.hasMoreElements()) {
            final String key = keys.nextElement();

            if (allowedQueryParams.contains(key)) {
                userData.put(key, request.getParameterMap().get(key));
            }
        }

        return new ValueMapDecorator(userData);
    }

    @Override
    public String getEmailTemplatePath() {
        return emailTemplatePath;
    }
}