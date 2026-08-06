/*
 * Asset Share Commons
 *
 * Copyright (C) 2024 Adobe
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

package com.adobe.aem.commons.assetshare.util;

import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.factory.ModelFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ComponentModelVisitorTest {

    @Rule
    public final AemContext ctx = new AemContext();

    @Before
    public void setUp() {
        ctx.create().resource("/content/root", "sling:resourceType", "type/root");
        ctx.create().resource("/content/root/child-a", "sling:resourceType", "type/a");
        ctx.create().resource("/content/root/child-b", "sling:resourceType", "type/b");
        ctx.create().resource("/content/root/child-unmodelable", "sling:resourceType", "type/a");

        ctx.currentResource("/content/root");

        ctx.addModelsForClasses(DummyComponentModel.class);
    }

    @Test
    public void getModels_collectsModelsForMatchingResourceTypes() {
        final ModelFactory modelFactory = ctx.getService(ModelFactory.class);

        final ComponentModelVisitor<DummyComponentModel> visitor = new ComponentModelVisitor<>(
                ctx.request(), modelFactory, new String[]{"type/a"}, DummyComponentModel.class);

        visitor.accept(ctx.resourceResolver().getResource("/content/root"));

        final Collection<DummyComponentModel> models = visitor.getModels();

        assertEquals(2, models.size());
    }

    @Test
    public void getModels_noResourceTypesSpecified_visitsAllResources() {
        final ModelFactory modelFactory = ctx.getService(ModelFactory.class);

        final ComponentModelVisitor<DummyComponentModel> visitor = new ComponentModelVisitor<>(
                ctx.request(), modelFactory, DummyComponentModel.class);

        visitor.accept(ctx.resourceResolver().getResource("/content/root"));

        // Every resource with a sling:resourceType is attempted (root, child-a, child-b, child-unmodelable) = 4
        final Collection<DummyComponentModel> models = visitor.getModels();
        assertEquals(4, models.size());
    }

    @Test
    public void getModels_nonMatchingResourceType_returnsEmpty() {
        final ModelFactory modelFactory = ctx.getService(ModelFactory.class);

        final ComponentModelVisitor<DummyComponentModel> visitor = new ComponentModelVisitor<>(
                ctx.request(), modelFactory, new String[]{"type/does-not-exist"}, DummyComponentModel.class);

        visitor.accept(ctx.resourceResolver().getResource("/content/root"));

        assertTrue(visitor.getModels().isEmpty());
    }

    @Model(adaptables = org.apache.sling.api.SlingHttpServletRequest.class)
    public static class DummyComponentModel {
    }
}
