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

package com.adobe.aem.commons.assetshare.configuration.impl.resourcestatusproviders;

import com.adobe.granite.resourcestatus.ResourceStatus;
import com.day.cq.wcm.commons.status.EditorResourceStatus;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SearchPageConfigurationResourceStatusProviderTest {

    @Rule
    public final AemContext ctx = new AemContext();

    private SearchPageConfigurationResourceStatusProvider provider;

    @Before
    public void setUp() throws Exception {
        ctx.load().json("/com/adobe/aem/commons/assetshare/configuration/impl/resourcestatusproviders/SearchPageConfigurationResourceStatusProviderTest.json",
                "/content");

        provider = ctx.registerInjectActivateService(new SearchPageConfigurationResourceStatusProvider());
    }

    @Test
    public void getType() {
        assertEquals("asset-share-commons__search-page-configuration", provider.getType());
    }

    @Test
    public void getStatuses_pageDoesNotMatchResourceType_returnsEmpty() {
        final Resource resource = ctx.resourceResolver().getResource("/content/other-page/jcr:content");

        final List<ResourceStatus> statuses = provider.getStatuses(resource);

        assertTrue(statuses.isEmpty());
    }

    @Test
    public void getStatuses_matchingPageWithoutResultsComponent_returnsStatus() {
        final Resource resource = ctx.resourceResolver().getResource("/content/search-page-no-results/jcr:content");

        final List<ResourceStatus> statuses = provider.getStatuses(resource);

        assertEquals(1, statuses.size());

        final EditorResourceStatus status = (EditorResourceStatus) statuses.get(0);
        assertEquals("asset-share-commons__search-page-configuration", status.getType());
        assertEquals(EditorResourceStatus.Variant.WARNING, status.getVariant());
        assertEquals("beaker", status.getIcon());
        assertEquals(200000, status.getPriority());
        assertEquals("Missing Search Results component", status.getData().get("shortMessage"));
    }

    @Test
    public void getStatuses_matchingPageWithResultsComponent_returnsEmpty() {
        final Resource resource = ctx.resourceResolver().getResource("/content/search-page-with-results/jcr:content");

        final List<ResourceStatus> statuses = provider.getStatuses(resource);

        assertTrue(statuses.isEmpty());
    }
}
