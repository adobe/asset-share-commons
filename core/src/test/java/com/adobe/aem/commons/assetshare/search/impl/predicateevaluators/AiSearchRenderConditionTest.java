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

package com.adobe.aem.commons.assetshare.search.impl.predicateevaluators;

import com.adobe.granite.ui.components.rendercondition.RenderCondition;
import com.adobe.granite.ui.components.rendercondition.SimpleRenderCondition;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Rule;
import org.junit.Test;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AiSearchRenderConditionTest {
    private static final Object LOCK = new Object();

    @Rule
    public final AemContext ctx = new AemContext();

    @Test
    public void isAiSearchEnabled_WhenSystemPropertyTrue() {
        synchronized (LOCK) {
            try {
                System.setProperty("oak.query.InferenceEnabled", "true");

                assertTrue(AiSearchRenderCondition.isAiSearchEnabled());
            } finally {
                System.clearProperty("oak.query.InferenceEnabled");
            }
        }
    }

    @Test
    public void isAiSearchEnabled_WhenSystemPropertyFalse() {
        synchronized (LOCK) {
            try {
                System.setProperty("oak.query.InferenceEnabled", "false");

                assertFalse(AiSearchRenderCondition.isAiSearchEnabled());
            } finally {
                System.clearProperty("oak.query.InferenceEnabled");
            }
        }
    }

    @Test
    public void isAiSearchEnabled_WhenSystemPropertyNotSet() {
        synchronized (LOCK) {
            try {
                System.clearProperty("oak.query.InferenceEnabled");

                assertFalse(AiSearchRenderCondition.isAiSearchEnabled());
            } finally {
                System.clearProperty("oak.query.InferenceEnabled");
            }
        }
    }

    @Test
    public void doGet_SetsRenderConditionAttributeBasedOnAiSearchEnabled() throws ServletException, IOException {
        synchronized (LOCK) {
            try {
                System.setProperty("oak.query.InferenceEnabled", "true");

                final Servlet servlet = ctx.registerInjectActivateService(new AiSearchRenderCondition(),
                        "sling.servlet.methods", "GET",
                        "sling.servlet.resourceTypes", "asset-share-commons/authoring/renderconditions/ai-search");

                servlet.service(ctx.request(), ctx.response());

                final RenderCondition renderCondition =
                        (RenderCondition) ctx.request().getAttribute(RenderCondition.class.getName());

                assertNotNull(renderCondition);
                assertTrue(renderCondition.check());
            } finally {
                System.clearProperty("oak.query.InferenceEnabled");
            }
        }
    }

    @Test
    public void doGet_SetsRenderConditionAttributeFalse_WhenAiSearchDisabled() throws ServletException, IOException {
        synchronized (LOCK) {
            try {
                System.clearProperty("oak.query.InferenceEnabled");

                final Servlet servlet = ctx.registerInjectActivateService(new AiSearchRenderCondition(),
                        "sling.servlet.methods", "GET",
                        "sling.servlet.resourceTypes", "asset-share-commons/authoring/renderconditions/ai-search");

                servlet.service(ctx.request(), ctx.response());

                final RenderCondition renderCondition =
                        (RenderCondition) ctx.request().getAttribute(RenderCondition.class.getName());

                assertNotNull(renderCondition);
                assertFalse(renderCondition.check());
            } finally {
                System.clearProperty("oak.query.InferenceEnabled");
            }
        }
    }
}
