package com.adobe.aem.commons.assetshare.content.renditions.impl;

import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionDispatcher;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionDispatchers;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditions;
import com.adobe.aem.commons.assetshare.content.renditions.impl.dispatchers.InternalRedirectRenditionDispatcherImpl;
import com.adobe.aem.commons.assetshare.content.renditions.impl.dispatchers.StaticRenditionDispatcherImpl;
import com.adobe.aem.commons.assetshare.testing.RequireAemMock;
import com.adobe.aem.commons.assetshare.util.RequireAem;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.Constants;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class AssetRenditionDispatchersImplTest {
    @Rule
    public AemContext ctx = new AemContext();

    @Before
    public void setUp() throws Exception {
        RequireAemMock.setAem(ctx, RequireAem.Distribution.CLASSIC, RequireAem.ServiceType.PUBLISH);

        ctx.registerService(AssetRenditions.class, new AssetRenditionsImpl());
        ctx.registerService(AssetRenditionDispatchers.class, new AssetRenditionDispatchersImpl());
    }

    @Test
    public void getAssetRenditionDispatchers() {
        AssetRenditionDispatcher one = new StaticRenditionDispatcherImpl();
        AssetRenditionDispatcher two = new InternalRedirectRenditionDispatcherImpl();
        AssetRenditionDispatcher three = new InternalRedirectRenditionDispatcherImpl();

        ctx.registerService(AssetRenditionDispatcher.class, two, Constants.SERVICE_RANKING, 90);
        ctx.registerService(AssetRenditionDispatcher.class, one, Constants.SERVICE_RANKING, 100);
        ctx.registerService(AssetRenditionDispatcher.class, three, Constants.SERVICE_RANKING, 80,
                "rendition.mappings", new String[] {"foo=bar", "test-rendition=im real"});

        final AssetRenditionDispatchers assetRenditionDispatchers = ctx.getService(AssetRenditionDispatchers.class);
        final List<AssetRenditionDispatcher> actual = assetRenditionDispatchers.getAssetRenditionDispatchers();

        assertEquals(3, actual.size());
        assertSame(one, actual.get(0));
        assertSame(two, actual.get(1));
        assertSame(three, actual.get(2));
    }

    @Test
    public void isValidAssetRenditionName() {
        AssetRenditionDispatcher one = new InternalRedirectRenditionDispatcherImpl();

        ctx.registerInjectActivateService(one, Constants.SERVICE_RANKING, 80, "rendition.mappings", new String[] {"test-rendition=im real"});

        final AssetRenditionDispatchers assetRenditionDispatchers = ctx.getService(AssetRenditionDispatchers.class);

        assertTrue(assetRenditionDispatchers.isValidAssetRenditionName("test-rendition"));
        assertFalse(assetRenditionDispatchers.isValidAssetRenditionName("fake-rendition-name"));
    }
}