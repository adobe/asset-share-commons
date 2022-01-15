package com.adobe.aem.commons.assetshare.components.actions.download.impl;

import com.adobe.aem.commons.assetshare.components.actions.impl.ActionHelperImpl;
import com.adobe.aem.commons.assetshare.content.renditions.download.async.impl.AsyncAssetRenditionsDownloadServlet;
import com.adobe.aem.commons.assetshare.testing.RequireAemMock;
import com.adobe.aem.commons.assetshare.util.RequireAem;
import com.adobe.aem.commons.assetshare.util.impl.ExpressionEvaluatorImpl;
import com.adobe.cq.dam.download.api.DownloadApiFactory;
import com.adobe.cq.dam.download.api.DownloadService;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.Servlet;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class AsyncAssetRenditionsDownloadServletTest {

    @Rule
    public final AemContext ctx = new AemContext();

    @Before
    public void setUp() throws Exception {
        ctx.registerInjectActivateService(new ActionHelperImpl());
        ctx.registerInjectActivateService(new ExpressionEvaluatorImpl());

        ctx.registerService(DownloadService.class, mock(DownloadService.class));
        ctx.registerService(DownloadApiFactory.class, mock(DownloadApiFactory.class));
    }

    @Test
    public void activateInCloudReady() {
        RequireAemMock.setAem(ctx, RequireAem.Distribution.CLOUD_READY, RequireAem.ServiceType.PUBLISH);

        ctx.registerInjectActivateService(new AsyncAssetRenditionsDownloadServlet());

        AsyncAssetRenditionsDownloadServlet servlet = (AsyncAssetRenditionsDownloadServlet) ctx.getService(Servlet.class);

        assertNotNull(servlet);
    }

    @Test(expected = org.apache.sling.testing.mock.osgi.ReferenceViolationException.class)
    public void activateInClassic() {
        RequireAemMock.setAem(ctx, RequireAem.Distribution.CLASSIC, RequireAem.ServiceType.PUBLISH);

        ctx.registerInjectActivateService(new AsyncAssetRenditionsDownloadServlet());

        ctx.getService(Servlet.class);
    }
}