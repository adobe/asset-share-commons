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

package com.adobe.aem.commons.assetshare.search.impl;

import com.adobe.aem.commons.assetshare.search.FastProperties;
import com.google.common.collect.ImmutableList;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class FastPropertiesImplTest {

    @Rule
    public AemContext ctx = new AemContext();

    @Before
    public void setUp() throws Exception {
        ctx.load().json(this.getClass().getResourceAsStream("FastPropertiesImplTest.json"), "/oak:index");

        ctx.registerInjectActivateService(new FastPropertiesImpl());
    }

    @Test
    public void getFastProperties_WithNoParams() {
        final FastProperties fastProperties = ctx.getService(FastProperties.class);

        final List<String> expected = ImmutableList.of(
                "jcr:content/analyzedIndexRule",
                "jcr:content/propertyAndAnalyzedIndexRule",
                "jcr:content/propertyIndexRule",
                "jcr:content/vanilla");

        final List<String> actual = fastProperties.getFastProperties();

        assertEquals(expected, actual);
    }

    @Test
    public void getFastProperties_WithPropertyIndexParam() {
        final FastProperties fastProperties = ctx.getService(FastProperties.class);

        final List<String> expected = ImmutableList.of(
                "jcr:content/propertyAndAnalyzedIndexRule",
                "jcr:content/propertyIndexRule");

        final List<String> actual = fastProperties.getFastProperties("propertyIndex");

        assertEquals(expected, actual);
    }

    @Test
    public void getFastProperties_WithAnalyzedIndexParam() {
        final FastProperties fastProperties = ctx.getService(FastProperties.class);

        final List<String> expected = ImmutableList.of(
                "jcr:content/analyzedIndexRule",
                "jcr:content/propertyAndAnalyzedIndexRule");

        final List<String> actual = fastProperties.getFastProperties("analyzed");

        assertEquals(expected, actual);
    }


    @Test
    public void getFastProperties_WithAnalyzedAndPropertyIndexParams() {
        final FastProperties fastProperties = ctx.getService(FastProperties.class);

        final List<String> expected = ImmutableList.of(
                "jcr:content/propertyAndAnalyzedIndexRule");

        final List<String> actual = fastProperties.getFastProperties(ImmutableList.of("analyzed", "propertyIndex"));

        assertEquals(expected, actual);
    }

    @Test
    public void getDeltaProperties() {
        final FastProperties fastProperties = ctx.getService(FastProperties.class);

        final Collection<String> fast = ImmutableList.of(
                "./jcr:content/one",
                "./jcr:content/two",
                "./jcr:content/three",
                "./jcr:content/four",
                "./jcr:content/five");

        final Collection<String> other = ImmutableList.of(
                "jcr:content/one",
                "jcr:content/two",
                "jcr:content/three",
                "jcr:content/five");

        final List<String> actual = fastProperties.getDeltaProperties(fast, other);

        assertEquals(1, actual.size());
        assertEquals("./jcr:content/four", actual.get(0));
    }

    @Test
    public void getFastLabel() {
        final FastProperties fastProperties = ctx.getService(FastProperties.class);

        final String expected = FastProperties.FAST + "  I'm a fast property";
        final String actual = fastProperties.getFastLabel("I'm a fast property");

        assertEquals(expected, actual);
    }

    @Test
    public void getSlowLabel() {
        final FastProperties fastProperties = ctx.getService(FastProperties.class);

        final String expected = FastProperties.SLOW + "  I'm a slow property";
        final String actual = fastProperties.getSlowLabel("I'm a slow property");

        assertEquals(expected, actual);
    }
}