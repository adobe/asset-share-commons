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

package com.adobe.aem.commons.assetshare.search.impl;

import com.adobe.aem.commons.assetshare.search.SearchSafety;
import com.day.cq.search.PredicateGroup;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Rule;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class SearchSafetyImplTest {

    @Rule
    public final AemContext ctx = new AemContext();

    @Test
    public void isSafe_WithParamsMap() throws Exception {
        final SearchSafety searchSafety = ctx.registerInjectActivateService(new SearchSafetyImpl());

        final Map<String, String> params = new HashMap<>();
        params.put("type", "dam:Asset");

        assertTrue(searchSafety.isSafe(ctx.resourceResolver(), params));
    }

    @Test
    public void isSafe_WithEmptyParamsMap() throws Exception {
        final SearchSafety searchSafety = ctx.registerInjectActivateService(new SearchSafetyImpl());

        assertTrue(searchSafety.isSafe(ctx.resourceResolver(), Collections.emptyMap()));
    }

    @Test
    public void isSafe_WithPredicateGroup() throws Exception {
        final SearchSafety searchSafety = ctx.registerInjectActivateService(new SearchSafetyImpl());

        assertTrue(searchSafety.isSafe(ctx.resourceResolver(), new PredicateGroup()));
    }

    @Test
    public void isSafe_WithLanguageAndStatement() throws Exception {
        final SearchSafety searchSafety = ctx.registerInjectActivateService(new SearchSafetyImpl());

        assertTrue(searchSafety.isSafe(ctx.resourceResolver(), "JCR-SQL2", "SELECT * FROM [dam:Asset]"));
    }
}
