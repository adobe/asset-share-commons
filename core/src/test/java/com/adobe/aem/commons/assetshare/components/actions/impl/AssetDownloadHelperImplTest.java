package com.adobe.aem.commons.assetshare.components.actions.impl;

import com.adobe.aem.commons.assetshare.components.actions.AssetDownloadHelper;
import com.day.cq.dam.api.jobs.AssetDownloadService;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@RunWith(MockitoJUnitRunner.class)
public class AssetDownloadHelperImplTest {

    private static final String ASSET_DOWNLOAD_SERVLET_PID = "com.day.cq.dam.core.impl.servlet.AssetDownloadServlet";
    private static final String MAX_SIZE_PROPERTY = "asset.download.prezip.maxcontentsize";

    @Rule
    public final AemContext ctx = new AemContext();

    @Mock
    AssetDownloadService assetDownloadService;

    ConfigurationAdmin spyConfigurationAdmin;

    @Before
    public void setUp() throws Exception {
        //spy ConfigAdmin since the method listConfigurations is not supported by the osgi mocks
        spyConfigurationAdmin = spy(ctx.getService(ConfigurationAdmin.class));

        //create expected assetdownload configuration
        Configuration assetDownloadConfig = spyConfigurationAdmin.getConfiguration(ASSET_DOWNLOAD_SERVLET_PID);

        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(MAX_SIZE_PROPERTY, 52428800);
        assetDownloadConfig.update(props);
        Configuration[] serviceConfigs = {assetDownloadConfig};

        doReturn(serviceConfigs).when(spyConfigurationAdmin).listConfigurations("(service.pid=" + ASSET_DOWNLOAD_SERVLET_PID + ")");

        ctx.registerService(ConfigurationAdmin.class, spyConfigurationAdmin, Constants.SERVICE_RANKING, Integer.MAX_VALUE);
        ctx.registerService(AssetDownloadService.class, assetDownloadService);
        ctx.registerInjectActivateService(new AssetDownloadHelperImpl());
    }

    @Test
    public void testGetMaxContentSizeLimit() {
        long expected = 52428800;
        final AssetDownloadHelper assetDownloadHelper = ctx.getService(AssetDownloadHelper.class);

        assertEquals(expected, assetDownloadHelper.getMaxContentSizeLimit());
    }

    @Test
    public void testGetMaxContentSizeLimit_NullConfig() throws IOException, InvalidSyntaxException {
        long expected = -1;

        doReturn(null).when(spyConfigurationAdmin).listConfigurations("(service.pid=" + ASSET_DOWNLOAD_SERVLET_PID + ")");

        final AssetDownloadHelper assetDownloadHelper = ctx.getService(AssetDownloadHelper.class);
        assertEquals(expected, assetDownloadHelper.getMaxContentSizeLimit());
    }
}
