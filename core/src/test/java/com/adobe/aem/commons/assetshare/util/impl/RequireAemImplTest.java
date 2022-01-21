/*
 * #%L
 * ACS AEM Commons Bundle
 * %%
 * Copyright (C) 2020 Adobe
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

/* Copied from: https://github.com/Adobe-Consulting-Services/acs-aem-commons/blob/master/bundle/src/test/java/com/adobe/acs/commons/util/impl/RequireAemImplTest.java */

package com.adobe.aem.commons.assetshare.util.impl;

import com.adobe.aem.commons.assetshare.util.RequireAem;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Rule;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class RequireAemImplTest {

    @Rule
    public final AemContext ctx = new AemContext();

    private void setUpAsCloudReady() {
        final RequireAemImpl requireAemImpl = spy(new RequireAemImpl());
        doReturn(true).when(requireAemImpl).isCloudService(any(BundleContext.class));

        ctx.registerInjectActivateService(requireAemImpl);
    }

    private void setUpAsNotCloudReady() {
        ctx.registerInjectActivateService(new RequireAemImpl());
    }

    @Test
    public void isCloudReady_True() {
        setUpAsCloudReady();

        RequireAem requireAem = ctx.getService(RequireAem.class);

        assertEquals(RequireAem.Distribution.CLOUD_READY, requireAem.getDistribution());
    }

    @Test
    public void isCloudReady_False() {
        setUpAsNotCloudReady();

        RequireAem requireAem = ctx.getService(RequireAem.class);

        assertEquals(RequireAem.Distribution.CLASSIC, requireAem.getDistribution());
    }

    @Test
    public void referenceFilter_CloudReady_True_Satisfied() {
        setUpAsCloudReady();

        RequireAem[] requireAems = ctx.getServices(RequireAem.class, "(distribution=cloud-ready)");

        assertEquals(1, requireAems.length);
        assertEquals(RequireAem.Distribution.CLOUD_READY, requireAems[0].getDistribution());
    }

    @Test
    public void referenceFilter_CloudReady_True_Unsatisfied() {
        setUpAsNotCloudReady();

        RequireAem[] requireAems = ctx.getServices(RequireAem.class, "(distribution=cloud-ready)");

        assertEquals(0, requireAems.length);
    }

    @Test
    public void referenceFilter_CloudReady_False_Satisfied() {
        setUpAsNotCloudReady();

        RequireAem[] requireAems = ctx.getServices(RequireAem.class, "(distribution=classic)");

        assertEquals(1, requireAems.length);
        assertEquals(RequireAem.Distribution.CLASSIC, requireAems[0].getDistribution());
    }

    @Test
    public void referenceFilter_CloudReady_False_Unsatisfied() {
        setUpAsCloudReady();

        RequireAem[] requireAems = ctx.getServices(RequireAem.class, "(distribution=classic)");

        assertEquals(0, requireAems.length);
    }
}