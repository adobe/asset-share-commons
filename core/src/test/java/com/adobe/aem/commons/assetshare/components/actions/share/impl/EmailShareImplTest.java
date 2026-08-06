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

import com.adobe.aem.commons.assetshare.components.actions.ActionHelper;
import com.adobe.aem.commons.assetshare.components.actions.share.EmailShare;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
public class EmailShareImplTest {

    @Rule
    public final AemContext ctx = new AemContext();

    @Mock
    private ActionHelper actionHelper;

    @Before
    public void setUp() {
        ctx.registerService(ActionHelper.class, actionHelper);
        ctx.addModelsForClasses(EmailShareImpl.class);

        doReturn(new ArrayList<AssetModel>()).when(actionHelper).getAssetsFromQueryParameter(any(), any());
        doReturn(new ArrayList<AssetModel>()).when(actionHelper).getPlaceholderAsset(any());
    }

    @Test
    public void getProperties() {
        ctx.create().resource("/content/share",
                "sling:resourceType", EmailShareImpl.RESOURCE_TYPE,
                "emailTemplatePath", "/etc/notification/email/custom.html");
        ctx.currentResource("/content/share");

        final EmailShare emailShare = ctx.request().adaptTo(EmailShare.class);

        assertEquals("/etc/notification/email/custom.html", emailShare.getProperties().get("emailTemplatePath", String.class));
        assertEquals("/etc/notification/email/custom.html", emailShare.getEmailTemplatePath());
    }

    @Test
    public void getConfiguredData_noDataChild() {
        ctx.create().resource("/content/share", "sling:resourceType", EmailShareImpl.RESOURCE_TYPE);
        ctx.currentResource("/content/share");

        final EmailShare emailShare = ctx.request().adaptTo(EmailShare.class);

        final ValueMap configuredData = emailShare.getConfiguredData();
        assertTrue(configuredData.isEmpty());
    }

    @Test
    public void getConfiguredData_withDataChild() {
        ctx.create().resource("/content/share", "sling:resourceType", EmailShareImpl.RESOURCE_TYPE);
        ctx.create().resource("/content/share/data", "signature", "Custom Signature");
        ctx.currentResource("/content/share");

        final EmailShare emailShare = ctx.request().adaptTo(EmailShare.class);

        final ValueMap configuredData = emailShare.getConfiguredData();
        assertEquals("Custom Signature", configuredData.get("signature", String.class));
    }

    @Test
    public void getUserData_filtersToAllowedQueryParams() {
        ctx.create().resource("/content/share",
                "sling:resourceType", EmailShareImpl.RESOURCE_TYPE,
                "allowedQueryParams", new String[]{"email", "path", "message"});
        ctx.currentResource("/content/share");

        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("email", "someone@example.com");
        parameters.put("path", "/content/dam/asset.png");
        parameters.put("message", "hello there");
        parameters.put("notAllowed", "should not appear");

        ctx.request().setParameterMap(parameters);

        final EmailShare emailShare = ctx.request().adaptTo(EmailShare.class);

        final ValueMap userData = emailShare.getUserData();
        assertTrue(userData.containsKey("email"));
        assertTrue(userData.containsKey("path"));
        assertTrue(userData.containsKey("message"));
        assertTrue(!userData.containsKey("notAllowed"));
    }

    @Test
    public void getUserData_customAllowedQueryParams() {
        ctx.create().resource("/content/share",
                "sling:resourceType", EmailShareImpl.RESOURCE_TYPE,
                "allowedQueryParams", new String[]{"custom"});
        ctx.currentResource("/content/share");

        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("custom", "value");
        parameters.put("email", "someone@example.com");

        ctx.request().setParameterMap(parameters);

        final EmailShare emailShare = ctx.request().adaptTo(EmailShare.class);

        final ValueMap userData = emailShare.getUserData();
        assertTrue(userData.containsKey("custom"));
        assertTrue(!userData.containsKey("email"));
    }

    @Test
    public void getExportedType() {
        ctx.create().resource("/content/share", "sling:resourceType", EmailShareImpl.RESOURCE_TYPE);
        ctx.currentResource("/content/share");

        final EmailShareImpl emailShare = (EmailShareImpl) ctx.request().adaptTo(EmailShare.class);

        assertEquals(EmailShareImpl.RESOURCE_TYPE, emailShare.getExportedType());
    }
}
