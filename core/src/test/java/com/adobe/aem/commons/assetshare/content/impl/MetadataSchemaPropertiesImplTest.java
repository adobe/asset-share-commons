/*
 * Asset Share Commons
 *
 * Copyright (C) 2019 Adobe
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

package com.adobe.aem.commons.assetshare.content.impl;

import com.adobe.aem.commons.assetshare.content.MetadataProperties;
import com.google.common.collect.ImmutableMap;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class MetadataSchemaPropertiesImplTest {

    @Rule
    public final AemContext ctx = new AemContext();

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void collectExtraMetadataProperties() {
        ctx.registerInjectActivateService(new MetadataSchemaPropertiesImpl(),
                ImmutableMap.<String, Object>builder().
                put("extra.metadata.properties", new String[]{ "jcr:content/foo=My Foo", "jcr:content/metadata/bar=My Bar", "./jcr:content/foo=My Foo 2" }).
                build());

        MetadataSchemaPropertiesImpl metadataSchemaProperties = (MetadataSchemaPropertiesImpl) ctx.getService(MetadataProperties.class);

        Map<String, List<String>> collectedMetadata = new HashMap<>();
        collectedMetadata = metadataSchemaProperties.collectExtraMetadataProperties(collectedMetadata);

        assertEquals(2, collectedMetadata.size());

        assertEquals(2, collectedMetadata.get("./jcr:content/foo").size());
        assertEquals("My Foo", collectedMetadata.get("./jcr:content/foo").get(0));
        assertEquals("My Foo 2", collectedMetadata.get("./jcr:content/foo").get(1));

        assertEquals(1, collectedMetadata.get("./jcr:content/metadata/bar").size());
        assertEquals("My Bar", collectedMetadata.get("./jcr:content/metadata/bar").get(0));
    }

    @Test
    public void removeMetadataProperties() {
        ctx.registerInjectActivateService(new MetadataSchemaPropertiesImpl(),
                ImmutableMap.<String, Object>builder().
                        put("blacklisted.metadata.properties", new String[]{ "jcr:content/foo" }).
                        build());

        MetadataSchemaPropertiesImpl metadataSchemaProperties = (MetadataSchemaPropertiesImpl) ctx.getService(MetadataProperties.class);

        Map<String, List<String>> collectedMetadata = new HashMap<>();
        collectedMetadata.put("jcr:content/foo", Arrays.asList("Blacklisted"));
        collectedMetadata.put("./jcr:content/foo", Arrays.asList("Blacklisted Too"));
        collectedMetadata.put("./jcr:content/metadata/bar", Arrays.asList("Not blacklisted"));

        collectedMetadata = metadataSchemaProperties.removeMetadataProperties(collectedMetadata);

        assertEquals(1, collectedMetadata.size());
        assertEquals(1, collectedMetadata.get("./jcr:content/metadata/bar").size());
        assertEquals("Not blacklisted", collectedMetadata.get("./jcr:content/metadata/bar").get(0));
    }

    @Test
    public void collectMetadataProperty() {
    }

}