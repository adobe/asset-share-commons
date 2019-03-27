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


import com.adobe.aem.commons.assetshare.content.properties.AbstractComputedProperty;
import com.adobe.aem.commons.assetshare.content.properties.ComputedProperty;
import com.adobe.aem.commons.assetshare.content.properties.impl.FileNameImpl;
import com.adobe.aem.commons.assetshare.content.properties.impl.TitleImpl;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.commons.util.DamUtil;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CombinedPropertiesTest {

    @Rule
    public AemContext ctx = new AemContext();

    private CombinedProperties combinedProperties;

    private Asset asset;

    private  List<ComputedProperty> computedPropertiesList = new ArrayList<>();

    private ComputedProperty<String> fileNameComputedProperty;

    private ComputedProperty<String> titleComputedProperty;

    private ComputedProperty<String> testWithRequestComputedProperty;

    private ComputedProperty<String> testWithNoRequestComputedProperty;

    @Before
    public void setUp() throws Exception {
        ctx.load().json("/com/adobe/aem/commons/assetshare/content/impl/CombinedPropertiesTest.json", "/content/dam");

        asset = DamUtil.resolveToAsset(ctx.resourceResolver().getResource("/content/dam/test.png"));

        fileNameComputedProperty = spy(new FileNameImpl());
        titleComputedProperty = spy(new TitleImpl());
        testWithRequestComputedProperty = spy(new TestWithRequestComputedProperty());
        testWithNoRequestComputedProperty = spy(new TestWithNoRequestComputedProperty());

        computedPropertiesList.add(fileNameComputedProperty);
        computedPropertiesList.add(titleComputedProperty);
        computedPropertiesList.add(testWithRequestComputedProperty);
        computedPropertiesList.add(testWithNoRequestComputedProperty);

        combinedProperties = new CombinedProperties(computedPropertiesList, ctx.request(), asset);
    }

    @Test
    public void containsKey_WithoutParameters() {
        final boolean actual = combinedProperties.containsKey(TitleImpl.NAME);

        assertTrue(actual);
    }

    @Test
    public void containsKey_WithParameters() {
        final boolean actual = combinedProperties.containsKey(FileNameImpl.NAME + "?a=b&c=d");

        assertTrue(actual);
    }

    @Test
    public void containsKey_NoMatch() {
        final boolean actual = combinedProperties.containsKey("unknown");

        assertFalse(actual);
    }

    @Test
    public void get_WithoutParameters() {
        final String expected = "Test Asset";
        final String actual = (String) combinedProperties.get(TitleImpl.NAME);

        assertEquals(expected, actual);
    }

    @Test
    public void get_WithRequestAndWithParameters() {
        final String expected = "The asset [ /content/dam/test.png ] and Request and [ 3 ] parameters are provided";
        final String actual = (String) combinedProperties.get("test/request" + "?a=b&c=d&testParameter=Hello World!");

        assertEquals(expected, actual);
    }

    @Test
    public void get_WithoutRequestAndWithParameters() {
        final String expected = "The asset [ /content/dam/test.png ] and [ 0 ] parameters are provided";
        final String actual = (String) combinedProperties.get("test/no-request" + "?a=b&c=d&testParameter=Hello World!");

        assertEquals(expected, actual);
    }

    @Test
    public void get_CacheMiss() {
        final String expected = "Test Asset";
        final String actual1 = (String) combinedProperties.get(TitleImpl.NAME);
        final String actual2 = (String) combinedProperties.get(TitleImpl.NAME + "?a=b");
        final String actual3 = (String) combinedProperties.get(TitleImpl.NAME + "?a=b&c=d");

        assertEquals(expected, actual1);
        assertEquals(expected, actual2);
        assertEquals(expected, actual3);

        verify(titleComputedProperty, times(3)).get(eq(asset), eq(ctx.request()), any(ValueMap.class));
    }

    @Test
    public void get_CacheHit() {
        final String expected = "Test Asset";
        final String actual1 = (String) combinedProperties.get(TitleImpl.NAME + "?a=b");
        final String actual2 = (String) combinedProperties.get(TitleImpl.NAME + "?a=b");
        final String actual3 = (String) combinedProperties.get(TitleImpl.NAME + "?a=b");

        assertEquals(expected, actual1);
        assertEquals(expected, actual2);
        assertEquals(expected, actual3);

        verify(titleComputedProperty, times(1)).get(eq(asset), eq(ctx.request()), any(ValueMap.class));
    }

    @Test
    public void ComputedPropertyParameter_getName() {
        final String input = "test?a=b&c=d&e=f&g=";
        final String expected = "test";

        CombinedProperties.ComputedPropertyParameter computedPropertyParameter = new CombinedProperties.ComputedPropertyParameter(input);

        final String actual = computedPropertyParameter.getName();

        assertEquals(expected, actual);
    }

    @Test
    public void ComputedPropertyParameter_getNameWithoutParameters() {
        final String input = "test";
        final String expected = "test";

        CombinedProperties.ComputedPropertyParameter computedPropertyParameter = new CombinedProperties.ComputedPropertyParameter(input);

        final String actual = computedPropertyParameter.getName();

        assertEquals(expected, actual);
    }

    @Test
    public void ComputedPropertyParameter_getParameterIsEmpty() {
        final String input = "test";

        CombinedProperties.ComputedPropertyParameter computedPropertyParameter = new CombinedProperties.ComputedPropertyParameter(input);

        final ValueMap actual = computedPropertyParameter.getParameters();

        assertTrue(actual.isEmpty());
    }

    @Test
    public void ComputedPropertyParameter_getParameters() {
        final String input = "test?a=b&c=d&e=f&g=";

        CombinedProperties.ComputedPropertyParameter computedPropertyParameter = new CombinedProperties.ComputedPropertyParameter(input);

        final ValueMap actual = computedPropertyParameter.getParameters();

        assertEquals(4, actual.size());
        assertEquals("b", actual.get("a", ""));
        assertEquals("d", actual.get("c", ""));
        assertEquals("f", actual.get("e", ""));
        assertEquals("", actual.get("g", ""));
    }

    @Test
    public void ComputedPropertyParameter_getId() {
        final String input = "test?g=&c=d&e=f&a=b&";
        final String expected = "test?a=b&c=d&e=f&g=";

        CombinedProperties.ComputedPropertyParameter computedPropertyParameter = new CombinedProperties.ComputedPropertyParameter(input);

        final String actual = computedPropertyParameter.getCacheId();

        assertEquals(expected, actual);
    }

    @Test
    public void ComputedPropertyParameter_getIdWithNoParameters() {
        final String input = "test";
        final String expected = "test";

        CombinedProperties.ComputedPropertyParameter computedPropertyParameter = new CombinedProperties.ComputedPropertyParameter(input);

        final String actual = computedPropertyParameter.getCacheId();

        assertEquals(expected, actual);
    }


    class TestWithRequestComputedProperty extends AbstractComputedProperty<String> {

        @Override
        public String getName() {
            return "test/request";
        }

        @Override
        public String getLabel() {
            return "Test With Request Computed Property";
        }

        @Override
        public String[] getTypes() {
            return new String[0];
        }

        @Override
        public String get(Asset asset) {
            return String.format("Only the asset [ %s ] is provided", asset.getPath());
        }

        @Override
        public String get(Asset asset, SlingHttpServletRequest request) {
            return String.format("The asset [ %s ] and Request are provided", asset.getPath());
        }

        @Override
        public String get(Asset asset, SlingHttpServletRequest request, ValueMap parameters) {
            return String.format("The asset [ %s ] and Request and [ %d ] parameters are provided", asset.getPath(), parameters.size());
        }

        @Override
        public String get(Asset asset, ValueMap parameters) {
            return String.format("The asset [ %s ] and [ %d ] parameters are provided", asset.getPath(), parameters.size());
        }
    }

    class TestWithNoRequestComputedProperty extends AbstractComputedProperty<String> {

        @Override
        public String getName() {
            return "test/no-request";
        }

        @Override
        public String getLabel() {
            return "Test With No Request Computed Property";
        }

        @Override
        public String[] getTypes() {
            return new String[0];
        }

        @Override
        public String get(Asset asset) {
            return String.format("Only the asset [ %s ] is provided", asset.getPath());
        }

        @Override
        public String get(Asset asset, ValueMap parameters) {
            return String.format("The asset [ %s ] and [ %d ] parameters are provided", asset.getPath(), parameters.size());
        }
    }
}