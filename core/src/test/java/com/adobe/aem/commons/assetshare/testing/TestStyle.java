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

package com.adobe.aem.commons.assetshare.testing;

import com.day.cq.wcm.api.designer.Cell;
import com.day.cq.wcm.api.designer.Design;
import com.day.cq.wcm.api.designer.Style;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.api.wrappers.ValueMapDecorator;

import java.util.Map;

/**
 * A minimal, real (non-Mockito) implementation of {@link Style} backed by a simple property Map, for use in unit
 * tests of Sling Models that consume the {@code currentStyle} {@code @ScriptVariable}. AEM Mocks (aem-mock) does not
 * automatically populate the "currentStyle" SlingBindings entry the way real AEM request processing does, so tests
 * need to bind one explicitly - see {@link #bind(io.wcm.testing.mock.aem.junit.AemContext, Map)}.
 */
public class TestStyle extends ValueMapDecorator implements Style {

    public TestStyle(final Map<String, Object> map) {
        super(map);
    }

    /**
     * Creates a {@link TestStyle} backed by the provided properties and binds it to the current request's
     * SlingBindings under the "currentStyle" key, so that {@code @ScriptVariable private Style currentStyle;}
     * resolves to it.
     *
     * @param ctx   the AemContext whose current request should expose the Style.
     * @param props the style properties.
     * @return the bound TestStyle instance.
     */
    public static TestStyle bind(final io.wcm.testing.mock.aem.junit.AemContext ctx, final Map<String, Object> props) {
        final TestStyle style = new TestStyle(props);

        Object bindingsAttr = ctx.request().getAttribute(SlingBindings.class.getName());
        final SlingBindings bindings;
        if (bindingsAttr instanceof SlingBindings) {
            bindings = (SlingBindings) bindingsAttr;
        } else {
            bindings = new SlingBindings();
            ctx.request().setAttribute(SlingBindings.class.getName(), bindings);
        }
        bindings.put("currentStyle", style);

        return style;
    }

    @Override
    public Design getDesign() {
        return null;
    }

    @Override
    public String getPath() {
        return null;
    }

    @Override
    public Cell getCell() {
        return null;
    }

    @Override
    public Resource getDefiningResource(final String s) {
        return null;
    }

    @Override
    public String getDefiningPath(final String s) {
        return null;
    }

    @Override
    public Style getSubStyle(final String s) {
        return null;
    }
}
