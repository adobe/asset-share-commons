package com.adobe.aem.commons.assetshare.configuration.impl;

import com.adobe.aem.commons.assetshare.configuration.Config;
import com.adobe.aem.commons.assetshare.util.RequireAem;
import com.adobe.aem.commons.assetshare.util.impl.RequireAemImpl;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.models.factory.ModelFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class ConfigImplTest {

    @Rule
    public final AemContext ctx = new AemContext();

    @Mock
    ModelFactory modelFactory;

    @Before
    public void setUp() throws Exception {
        ctx.load().json("/com/adobe/aem/commons/assetshare/configuration/impl/ConfigImplTest.json",
                "/content");

        ctx.registerService(RequireAem.class, new RequireAemImpl());

        ctx.registerService(ModelFactory.class, modelFactory, org.osgi.framework.Constants.SERVICE_RANKING,
                Integer.MAX_VALUE);

        ctx.addModelsForClasses(Config.class);
    }

    @Test
    public void isContextHubEnabled_noContextHub() {
        ctx.currentResource("/content/no-contexthub");
        final Config config = ctx.request().adaptTo(Config.class);

        assertFalse(config.isContextHubEnabled());
    }

    @Test
    public void isContextHubEnabled_invalidContextHub() {
        ctx.currentResource("/content/invalid-contexthub");
        final Config config = ctx.request().adaptTo(Config.class);

        assertFalse(config.isContextHubEnabled());
    }

    @Test
    public void isContextHubEnabled_validContextHub() {
        ctx.currentResource("/content/valid-contexthub");
        final Config config = ctx.request().adaptTo(Config.class);

        assertTrue(config.isContextHubEnabled());
    }

}