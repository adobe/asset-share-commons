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
import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ResourceTypeVisitorTest {

    @Rule
    public final AemContext ctx = new AemContext();

    @Before
    public void setUp() {
        ctx.create().resource("/content/root", "sling:resourceType", "type/root");

        ctx.create().resource("/content/root/child-a", "sling:resourceType", "type/a");
        ctx.create().resource("/content/root/child-a/grandchild-b", "sling:resourceType", "type/b");

        ctx.create().resource("/content/root/child-no-type");
        ctx.create().resource("/content/root/child-no-type/grandchild-under-no-type", "sling:resourceType", "type/c");

        ctx.create().resource("/content/root/child-c", "sling:resourceType", "type/c");
    }

    @Test
    public void accept_collectsMatchingResourceTypes() {
        final ResourceTypeVisitor visitor = new ResourceTypeVisitor(new String[]{"type/a", "type/b"});

        visitor.accept(ctx.resourceResolver().getResource("/content/root"));

        final Collection<Resource> resources = visitor.getResources();
        assertEquals(2, resources.size());
        assertTrue(resources.stream().anyMatch(r -> "/content/root/child-a".equals(r.getPath())));
        assertTrue(resources.stream().anyMatch(r -> "/content/root/child-a/grandchild-b".equals(r.getPath())));
    }

    @Test
    public void accept_doesNotDescendIntoResourcesWithoutResourceType() {
        final ResourceTypeVisitor visitor = new ResourceTypeVisitor(new String[]{"type/c"});

        visitor.accept(ctx.resourceResolver().getResource("/content/root"));

        final Collection<Resource> resources = visitor.getResources();

        // "child-c" (direct child of root, itself carrying a sling:resourceType) IS visited & matches.
        assertTrue(resources.stream().anyMatch(r -> "/content/root/child-c".equals(r.getPath())));

        // "grandchild-under-no-type" also matches type/c, but its parent ("child-no-type") lacks a
        // sling:resourceType, so accept(..) never recurses into that subtree.
        assertFalse(resources.stream().anyMatch(r -> "/content/root/child-no-type/grandchild-under-no-type".equals(r.getPath())));
        assertEquals(1, resources.size());
    }

    @Test
    public void handleResourceVisit_nullResource_returnsFalse() {
        final ResourceTypeVisitor visitor = new ResourceTypeVisitor(new String[]{"type/a"});

        assertFalse(visitor.handleResourceVisit(null, "type/a"));
    }

    @Test
    public void handleResourceVisit_nonMatchingResourceType_returnsFalse() {
        final ResourceTypeVisitor visitor = new ResourceTypeVisitor(new String[]{"type/a"});

        final Resource resource = ctx.resourceResolver().getResource("/content/root/child-c");

        assertFalse(visitor.handleResourceVisit(resource, "type/a"));
        assertTrue(visitor.getResources().isEmpty());
    }

    @Test
    public void getResources_startsEmpty() {
        final ResourceTypeVisitor visitor = new ResourceTypeVisitor(new String[]{"type/a"});

        assertTrue(visitor.getResources().isEmpty());
    }
}
