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

import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class MetadataSchemaPropertiesImplTest {

    @Rule
    public final AemContext ctx = new AemContext();

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void collectExtraMetadataProperties() {
        ctx.registerInjectActivateService(new MetadataSchemaPropertiesImpl(),
                Collections.unmodifiableMap(new HashMap<String, Object>() {{
                        put("extra.metadata.properties", new String[]{"jcr:content/foo=My Foo", "jcr:content/metadata/bar=My Bar", "./jcr:content/foo=My Foo 2"});
                    }}));
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
                Collections.unmodifiableMap(new HashMap<String, Object>() {{
                    put("blacklisted.metadata.properties", new String[]{"jcr:content/foo"});
                }}));
        MetadataSchemaPropertiesImpl metadataSchemaProperties = (MetadataSchemaPropertiesImpl) ctx.getService(MetadataProperties.class);

        Map<String, List<String>> collectedMetadata = new HashMap<>();
        collectedMetadata.put("jcr:content/foo", Collections.singletonList("Blacklisted"));
        collectedMetadata.put("./jcr:content/foo", Collections.singletonList("Blacklisted Too"));
        collectedMetadata.put("./jcr:content/metadata/bar", Collections.singletonList("Not blacklisted"));

        collectedMetadata = metadataSchemaProperties.removeBlacklistedMetadataProperties(collectedMetadata);

        assertEquals(1, collectedMetadata.size());
        assertEquals(1, collectedMetadata.get("./jcr:content/metadata/bar").size());
        assertEquals("Not blacklisted", collectedMetadata.get("./jcr:content/metadata/bar").get(0));
    }

    @Test
    public void collectMetadataProperty() {
    }

    @Test
    public void getMetadataProperties_TraversesSchemaFormAndCollectsWidgetFields() {
        ctx.registerInjectActivateService(new MetadataSchemaPropertiesImpl());

        final String base = "/conf/global/settings/dam/adminui-extension/metadataschema";

        ctx.create().resource(base + "/myform",
                Collections.singletonMap("jcr:primaryType", "nt:folder"));
        ctx.create().resource(base + "/myform/items",
                Collections.singletonMap("jcr:primaryType", "nt:unstructured"));
        ctx.create().resource(base + "/myform/items/tab1",
                Collections.singletonMap("jcr:primaryType", "nt:unstructured"));
        ctx.create().resource(base + "/myform/items/tab1/items",
                Collections.singletonMap("jcr:primaryType", "nt:unstructured"));

        final Map<String, Object> titleWidget = new HashMap<>();
        titleWidget.put("sling:resourceType", "granite/ui/components/coral/foundation/form/field");
        titleWidget.put("name", "./jcr:content/metadata/dc:title");
        titleWidget.put("fieldLabel", "Title");
        ctx.create().resource(base + "/myform/items/tab1/items/title", titleWidget);

        // A non-widget grouping container that should simply be traversed into.
        ctx.create().resource(base + "/myform/items/tab1/items/group",
                Collections.singletonMap("jcr:primaryType", "nt:unstructured"));
        ctx.create().resource(base + "/myform/items/tab1/items/group/items",
                Collections.singletonMap("jcr:primaryType", "nt:unstructured"));

        final Map<String, Object> customWidget = new HashMap<>();
        customWidget.put("sling:resourceType", "granite/ui/components/foundation/form/field");
        customWidget.put("fieldLabel", "Custom Property");
        ctx.create().resource(base + "/myform/items/tab1/items/group/items/custom", customWidget);
        ctx.create().resource(base + "/myform/items/tab1/items/group/items/custom/field",
                Collections.singletonMap("name", "customProp"));

        final MetadataProperties metadataProperties = ctx.getService(MetadataProperties.class);

        final Map<String, List<String>> actual = metadataProperties.getMetadataProperties(ctx.request());

        assertEquals(1, actual.get("./jcr:content/metadata/dc:title").size());
        assertEquals("Title", actual.get("./jcr:content/metadata/dc:title").get(0));

        assertEquals(1, actual.get("./customProp").size());
        assertEquals("Custom Property", actual.get("./customProp").get(0));
    }

    @Test
    public void getMetadataProperties_WithCustomMetadataFieldResourceTypeMatch() {
        ctx.registerInjectActivateService(new MetadataSchemaPropertiesImpl());

        final String base = "/conf/global/settings/dam/adminui-extension/metadataschema";

        ctx.create().resource(base + "/myform",
                Collections.singletonMap("jcr:primaryType", "nt:folder"));

        final Map<String, Object> customTypeWidget = new HashMap<>();
        customTypeWidget.put("sling:resourceType", "some/other/widget");
        customTypeWidget.put("name", "customTypeProp");
        customTypeWidget.put("fieldLabel", "Custom Type Property");
        ctx.create().resource(base + "/myform/customTypeWidget", customTypeWidget);
        ctx.create().resource(base + "/myform/customTypeWidget/granite:data",
                Collections.singletonMap("metaType", "my-custom-type"));

        final MetadataProperties metadataProperties = ctx.getService(MetadataProperties.class);

        final Map<String, List<String>> withoutOverride = metadataProperties.getMetadataProperties(ctx.request());
        assertEquals(0, withoutOverride.size());

        final Map<String, List<String>> withOverride = metadataProperties.getMetadataProperties(ctx.request(),
                Collections.singletonList("my-custom-type"));
        assertEquals(1, withOverride.get("./customTypeProp").size());
        assertEquals("Custom Type Property", withOverride.get("./customTypeProp").get(0));
    }

    @Test
    public void getMetadataProperties_SkipsFormsWithAllowCustomizationFalse() {
        ctx.registerInjectActivateService(new MetadataSchemaPropertiesImpl());

        final String base = "/conf/global/settings/dam/adminui-extension/metadataschema";

        final Map<String, Object> formProps = new HashMap<>();
        formProps.put("jcr:primaryType", "nt:folder");
        formProps.put("allowCustomization", false);
        ctx.create().resource(base + "/myform", formProps);

        final Map<String, Object> titleWidget = new HashMap<>();
        titleWidget.put("sling:resourceType", "granite/ui/components/coral/foundation/form/field");
        titleWidget.put("name", "./jcr:content/metadata/dc:title");
        titleWidget.put("fieldLabel", "Title");
        ctx.create().resource(base + "/myform/title", titleWidget);

        final MetadataProperties metadataProperties = ctx.getService(MetadataProperties.class);

        final Map<String, List<String>> actual = metadataProperties.getMetadataProperties(ctx.request());

        assertEquals(0, actual.size());
    }

}