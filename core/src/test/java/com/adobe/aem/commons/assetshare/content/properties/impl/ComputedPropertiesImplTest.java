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

package com.adobe.aem.commons.assetshare.content.properties.impl;

import com.adobe.aem.commons.assetshare.content.properties.ComputedProperties;
import com.adobe.aem.commons.assetshare.content.properties.ComputedProperty;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Rule;
import org.junit.Test;
import org.osgi.framework.Constants;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ComputedPropertiesImplTest {
    @Rule
    public final AemContext ctx = new AemContext();

    @Test
    public void getComputedProperties() {
        final TitleImpl title1 = new TitleImpl();
        final TitleImpl title2 = new TitleImpl();
        final TitleImpl title3 = new TitleImpl();
        final FileSizeImpl fileSize = new FileSizeImpl();

        final ComputedProperties computedProperties = new ComputedPropertiesImpl();

        ctx.registerInjectActivateService(computedProperties);
        ctx.registerInjectActivateService(title1, Constants.SERVICE_RANKING, "0");
        ctx.registerInjectActivateService(title2, Constants.SERVICE_RANKING, "200");
        ctx.registerInjectActivateService(title3, Constants.SERVICE_RANKING, "100");
        ctx.registerInjectActivateService(fileSize, Constants.SERVICE_RANKING, "1000");

        List<ComputedProperty> actual = computedProperties.getComputedProperties();

        assertEquals(2, actual.size());
        assertEquals(actual.get(0), fileSize);
        assertEquals(actual.get(1), title2);
    }

    @Test
    public void getComputedProperties_Bind() {
        final TitleImpl title1 = new TitleImpl();
        final TitleImpl title2 = new TitleImpl();
        final TitleImpl title3 = new TitleImpl();
        final TitleImpl title4 = new TitleImpl();

        final ComputedProperties computedProperties = new ComputedPropertiesImpl();

        ctx.registerInjectActivateService(computedProperties);
        ctx.registerInjectActivateService(title1, Constants.SERVICE_RANKING, "0");
        ctx.registerInjectActivateService(title2, Constants.SERVICE_RANKING, "200");

        // Verify Title2 is selected
        
        List<ComputedProperty> actual = computedProperties.getComputedProperties();
        assertEquals(1, actual.size());
        assertEquals(actual.get(0), title2);

        // Register a new service with a higher ranking and validate that is selected

        ctx.registerInjectActivateService(title3, Constants.SERVICE_RANKING, "300");
        actual = computedProperties.getComputedProperties();
        assertEquals(1, actual.size());
        assertEquals(actual.get(0), title3);

        // Register a new service with a low ranking and validate that the previous is still selected

        ctx.registerInjectActivateService(title4, Constants.SERVICE_RANKING, "-1");
        actual = computedProperties.getComputedProperties();
        assertEquals(1, actual.size());
        assertEquals(actual.get(0), title3);
    }
}