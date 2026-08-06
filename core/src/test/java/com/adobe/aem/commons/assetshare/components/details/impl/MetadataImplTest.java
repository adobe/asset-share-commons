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

package com.adobe.aem.commons.assetshare.components.details.impl;

import com.adobe.aem.commons.assetshare.components.details.Metadata;
import com.adobe.aem.commons.assetshare.content.AssetResolver;
import com.adobe.aem.commons.assetshare.content.impl.AssetModelImpl;
import com.adobe.aem.commons.assetshare.content.impl.AssetResolverImpl;
import com.adobe.aem.commons.assetshare.content.properties.ComputedProperties;
import com.adobe.aem.commons.assetshare.content.properties.impl.ComputedPropertiesImpl;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.*;

public class MetadataImplTest {
    @Rule
    public final AemContext ctx = new AemContext();

    @Before
    public void setUp() throws Exception {
        ctx.load().json("/com/adobe/aem/commons/assetshare/components/details/impl/MetadataImplTest.json", "/content");

        ctx.addModelsForClasses(MetadataImpl.class);

        ctx.requestPathInfo().setSuffix("/content/dam/test.png");

        // Dependencies to instantiate AssetModels
        ctx.registerService(ComputedProperties.class, new ComputedPropertiesImpl());
        ctx.registerService(AssetResolver.class, new AssetResolverImpl());
        ctx.addModelsForClasses(AssetModelImpl.class);
    }

    @Test
    public void getType() {
        final Metadata.DataType expected = Metadata.DataType.TEXT;

        ctx.currentResource("/content/metadata");
        final Metadata metadata = ctx.request().adaptTo(Metadata.class);

        assertEquals(expected, metadata.getType());
    }

    @Test
    public void getLocale_Default() {
        final String expected = Locale.getDefault().getLanguage();
        ctx.currentResource("/content/metadata");
        final Metadata metadata = ctx.request().adaptTo(Metadata.class);

        assertEquals(expected, metadata.getLocale());
    }

    @Test
    public void getFormat() {
        ctx.currentResource("/content/metadata");
        final Metadata metadata = ctx.request().adaptTo(Metadata.class);

        assertNull(metadata.getFormat());
    }

    @Test
    public void getFormat_Date() {
        final String expected = "yyyy-MM-dd";

        ctx.currentResource("/content/date");
        final Metadata metadata = ctx.request().adaptTo(Metadata.class);

        assertEquals(expected, metadata.getFormat());
    }

    @Test
    public void getFormat_Number() {
        final String expected = "#.###";

        ctx.currentResource("/content/number");
        final Metadata metadata = ctx.request().adaptTo(Metadata.class);

        assertEquals(expected, metadata.getFormat());
    }

    @Test
    public void getProperties() {
    }

    @Test
    public void getAsset() {
        final String expected = "/content/dam/test.png";

        ctx.currentResource("/content/metadata");
        final Metadata metadata = ctx.request().adaptTo(Metadata.class);

        assertEquals(expected, metadata.getAsset().getPath());
    }

    @Test
    public void getPropertyName() {
        final String expected = "./dc:title";

        ctx.currentResource("/content/metadata");
        final Metadata metadata = ctx.request().adaptTo(Metadata.class);

        assertEquals(expected, metadata.getPropertyName());
    }

    @Test
    public void getPropertyName_ComputedProperty() {
        final String expected = "title";

        ctx.currentResource("/content/computed");
        final Metadata metadata = ctx.request().adaptTo(Metadata.class);

        assertEquals(expected, metadata.getPropertyName());
    }

    @Test
    public void isEmpty_NullValue() {
        ctx.currentResource("/content/empty");
        final Metadata metadata = ctx.request().adaptTo(Metadata.class);
        assertTrue(metadata.isEmpty());
    }

    @Test
    public void isEmpty_EmptyText() {
        ctx.currentResource("/content/empty-text");
        final Metadata metadata = ctx.request().adaptTo(Metadata.class);
        assertTrue(metadata.isEmpty());
    }

    @Test
    public void isEmpty_NotEmpty() {
        ctx.currentResource("/content/metadata");
        final Metadata metadata = ctx.request().adaptTo(Metadata.class);
        assertFalse(metadata.isEmpty());
    }

    @Test
    public void isReady() {
        ctx.currentResource("/content/metadata");
        final Metadata metadata = ctx.request().adaptTo(Metadata.class);
        assertTrue(metadata.isReady());
    }

    @Test
    public void isReady_NotReady() {
        ctx.currentResource("/content/empty");
        final Metadata metadata = ctx.request().adaptTo(Metadata.class);
        assertFalse(metadata.isReady());
    }

    @Test
    public void getValues_Json_ArrayOfOptions_Match() throws Exception {
        final com.adobe.aem.commons.assetshare.util.JsonResolver jsonResolver =
                org.mockito.Mockito.mock(com.adobe.aem.commons.assetshare.util.JsonResolver.class);
        final com.google.gson.JsonArray options = new com.google.gson.JsonArray();
        options.add(jsonOption("Option One", "opt1"));
        options.add(jsonOption("Option Two", "opt2"));
        org.mockito.Mockito.when(jsonResolver.resolveJson(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq("/mock/options-array.json")))
                .thenReturn(options);
        ctx.registerService(com.adobe.aem.commons.assetshare.util.JsonResolver.class, jsonResolver);

        ctx.currentResource("/content/json-match");
        final Metadata metadata = ctx.request().adaptTo(Metadata.class);

        assertEquals(java.util.Collections.singletonList("Option One"), metadata.getValues());
        assertFalse(metadata.isEmpty());
    }

    @Test
    public void getValues_Json_ObjectWithOptions_Match() throws Exception {
        final com.adobe.aem.commons.assetshare.util.JsonResolver jsonResolver =
                org.mockito.Mockito.mock(com.adobe.aem.commons.assetshare.util.JsonResolver.class);
        final com.google.gson.JsonArray options = new com.google.gson.JsonArray();
        options.add(jsonOption("Option One", "opt1"));
        final com.google.gson.JsonObject wrapper = new com.google.gson.JsonObject();
        wrapper.add("options", options);
        org.mockito.Mockito.when(jsonResolver.resolveJson(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq("/mock/options-object.json")))
                .thenReturn(wrapper);
        ctx.registerService(com.adobe.aem.commons.assetshare.util.JsonResolver.class, jsonResolver);

        ctx.currentResource("/content/json-object-options");
        final Metadata metadata = ctx.request().adaptTo(Metadata.class);

        assertEquals(java.util.Collections.singletonList("Option One"), metadata.getValues());
    }

    @Test
    public void getValues_Json_NoMatch_FallsBackToRawValue() throws Exception {
        final com.adobe.aem.commons.assetshare.util.JsonResolver jsonResolver =
                org.mockito.Mockito.mock(com.adobe.aem.commons.assetshare.util.JsonResolver.class);
        final com.google.gson.JsonArray options = new com.google.gson.JsonArray();
        options.add(jsonOption("Option One", "opt1"));
        org.mockito.Mockito.when(jsonResolver.resolveJson(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq("/mock/options-array.json")))
                .thenReturn(options);
        ctx.registerService(com.adobe.aem.commons.assetshare.util.JsonResolver.class, jsonResolver);

        ctx.currentResource("/content/json-no-match");
        final Metadata metadata = ctx.request().adaptTo(Metadata.class);

        assertEquals(java.util.Collections.singletonList("unknown"), metadata.getValues());
    }

    @Test
    public void getValues_Json_InvalidShape_ReturnsRawValuesUnchanged() throws Exception {
        final com.adobe.aem.commons.assetshare.util.JsonResolver jsonResolver =
                org.mockito.Mockito.mock(com.adobe.aem.commons.assetshare.util.JsonResolver.class);
        org.mockito.Mockito.when(jsonResolver.resolveJson(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq("/mock/invalid.json")))
                .thenReturn(new com.google.gson.JsonPrimitive("not-an-array-or-object"));
        ctx.registerService(com.adobe.aem.commons.assetshare.util.JsonResolver.class, jsonResolver);

        ctx.currentResource("/content/json-invalid-shape");
        final Metadata metadata = ctx.request().adaptTo(Metadata.class);

        assertEquals(java.util.Collections.singletonList("opt1"), metadata.getValues());
    }

    @Test
    public void getValues_Json_ObjectWithoutOptionsKey_FallsBackToRawValue() throws Exception {
        final com.adobe.aem.commons.assetshare.util.JsonResolver jsonResolver =
                org.mockito.Mockito.mock(com.adobe.aem.commons.assetshare.util.JsonResolver.class);
        org.mockito.Mockito.when(jsonResolver.resolveJson(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq("/mock/object-no-options.json")))
                .thenReturn(new com.google.gson.JsonObject());
        ctx.registerService(com.adobe.aem.commons.assetshare.util.JsonResolver.class, jsonResolver);

        ctx.currentResource("/content/json-object-no-options");
        final Metadata metadata = ctx.request().adaptTo(Metadata.class);

        assertEquals(java.util.Collections.singletonList("opt1"), metadata.getValues());
    }

    @Test
    public void getValues_Json_BlankJsonSource_SkipsJsonResolution() throws Exception {
        // No JsonResolver registered at all - if the (blank jsonSource) branch incorrectly attempted JSON
        // resolution, this would NPE.
        ctx.currentResource("/content/json-blank-source");
        final Metadata metadata = ctx.request().adaptTo(Metadata.class);

        assertEquals(java.util.Collections.singletonList("opt1"), metadata.getValues());
    }

    @Test
    public void getValues_Json_MissingProperty_IsEmpty() throws Exception {
        ctx.currentResource("/content/json-missing-property");
        final Metadata metadata = ctx.request().adaptTo(Metadata.class);

        assertTrue(metadata.getValues().isEmpty());
        assertTrue(metadata.isEmpty());
    }

    @Test
    public void isEmpty_StringArray_AllBlank() {
        ctx.currentResource("/content/string-array-blank");
        final Metadata metadata = ctx.request().adaptTo(Metadata.class);
        assertTrue(metadata.isEmpty());
    }

    @Test
    public void isEmpty_StringArray_HasNonBlankValue() {
        ctx.currentResource("/content/string-array-nonblank");
        final Metadata metadata = ctx.request().adaptTo(Metadata.class);
        assertFalse(metadata.isEmpty());
    }

    @Test
    public void isEmpty_ObjectArray_NotEmpty() {
        final org.apache.sling.api.resource.Resource metadataResource =
                ctx.resourceResolver().getResource("/content/dam/test.png/jcr:content/metadata");
        final org.apache.sling.api.resource.ModifiableValueMap mvm =
                metadataResource.adaptTo(org.apache.sling.api.resource.ModifiableValueMap.class);
        mvm.put("objArrProp", new Long[]{1L, 2L});

        ctx.currentResource("/content/object-array");
        final Metadata metadata = ctx.request().adaptTo(Metadata.class);
        assertFalse(metadata.isEmpty());
    }

    @Test
    public void isEmpty_ObjectArray_Empty() {
        final org.apache.sling.api.resource.Resource metadataResource =
                ctx.resourceResolver().getResource("/content/dam/test.png/jcr:content/metadata");
        final org.apache.sling.api.resource.ModifiableValueMap mvm =
                metadataResource.adaptTo(org.apache.sling.api.resource.ModifiableValueMap.class);
        mvm.put("objArrPropEmpty", new Long[0]);

        ctx.currentResource("/content/object-array-empty");
        final Metadata metadata = ctx.request().adaptTo(Metadata.class);
        assertTrue(metadata.isEmpty());
    }

    @Test
    public void isEmpty_Collection_NotEmpty() {
        final org.apache.sling.api.resource.Resource metadataResource =
                ctx.resourceResolver().getResource("/content/dam/test.png/jcr:content/metadata");
        final org.apache.sling.api.resource.ModifiableValueMap mvm =
                metadataResource.adaptTo(org.apache.sling.api.resource.ModifiableValueMap.class);
        mvm.put("setProp", new java.util.LinkedHashSet<>(java.util.Arrays.asList("a", "b")));

        ctx.currentResource("/content/collection-prop");
        final Metadata metadata = ctx.request().adaptTo(Metadata.class);
        assertFalse(metadata.isEmpty());
    }

    @Test
    public void isEmpty_Collection_Empty() {
        final org.apache.sling.api.resource.Resource metadataResource =
                ctx.resourceResolver().getResource("/content/dam/test.png/jcr:content/metadata");
        final org.apache.sling.api.resource.ModifiableValueMap mvm =
                metadataResource.adaptTo(org.apache.sling.api.resource.ModifiableValueMap.class);
        mvm.put("setPropEmpty", new java.util.LinkedHashSet<String>());

        ctx.currentResource("/content/collection-prop-empty");
        final Metadata metadata = ctx.request().adaptTo(Metadata.class);
        assertTrue(metadata.isEmpty());
    }

    @Test
    public void isEmpty_OtherType_NeverConsideredEmpty() {
        final org.apache.sling.api.resource.Resource metadataResource =
                ctx.resourceResolver().getResource("/content/dam/test.png/jcr:content/metadata");
        final org.apache.sling.api.resource.ModifiableValueMap mvm =
                metadataResource.adaptTo(org.apache.sling.api.resource.ModifiableValueMap.class);
        mvm.put("numberProp", 42L);

        ctx.currentResource("/content/other-type-prop");
        final Metadata metadata = ctx.request().adaptTo(Metadata.class);
        assertFalse(metadata.isEmpty());
    }

    @Test
    public void getExportedType() {
        ctx.currentResource("/content/metadata");
        final Metadata metadata = ctx.request().adaptTo(Metadata.class);
        assertEquals("asset-share-commons/components/details/metadata", ((MetadataImpl) metadata).getExportedType());
    }

    /**
     * Builds a JsonObject matching MetadataImpl.JsonOption's expected shape: {"text": ..., "value": ...}.
     */
    private com.google.gson.JsonObject jsonOption(final String text, final String value) {
        final com.google.gson.JsonObject option = new com.google.gson.JsonObject();
        option.addProperty("text", text);
        option.addProperty("value", value);
        return option;
    }
}