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

package com.adobe.aem.commons.assetshare.configuration;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConfigTest {

    private final Config config = Mockito.mock(Config.class, Mockito.CALLS_REAL_METHODS);

    @Test
    public void getAssetDetailReferenceById_defaultsToTrue() {
        assertTrue(config.getAssetDetailReferenceById());
    }

    @Test
    public void isAemClassic_defaultsToFalse() {
        assertFalse(config.isAemClassic());
    }

    @Test
    public void isContextHubEnabled_defaultsToFalse() {
        assertFalse(config.isContextHubEnabled());
    }

    @Test
    public void isDynamicMediaEnabled_defaultsToFalse() {
        assertFalse(config.isDynamicMediaEnabled());
    }
}
