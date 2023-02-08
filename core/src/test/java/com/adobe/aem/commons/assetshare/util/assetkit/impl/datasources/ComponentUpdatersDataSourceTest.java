package com.adobe.aem.commons.assetshare.util.assetkit.impl.datasources;

import com.adobe.aem.commons.assetshare.util.DataSourceBuilder;
import com.adobe.aem.commons.assetshare.util.assetkit.ComponentUpdater;
import com.adobe.aem.commons.assetshare.util.assetkit.impl.componentupdaters.BannerComponentUpdaterImpl;
import com.adobe.aem.commons.assetshare.util.assetkit.impl.componentupdaters.PageMetadataComponentUpdaterImpl;
import com.adobe.aem.commons.assetshare.util.impl.DataSourceBuilderImpl;
import com.adobe.granite.ui.components.ds.DataSource;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;

@RunWith(MockitoJUnitRunner.class)
public class ComponentUpdatersDataSourceTest {
    @Rule
    public AemContext ctx = new AemContext();

    Servlet servlet;

    @Before
    public void setUp() throws Exception {
        ctx.load().json(getClass().getResourceAsStream("ComponentUpdatersDataSourceTest.json"), "/apps/dialog");

        ctx.registerService(DataSourceBuilder.class, new DataSourceBuilderImpl());

        ctx.registerService(ComponentUpdater.class, new PageMetadataComponentUpdaterImpl());
        ctx.registerService(ComponentUpdater.class, new BannerComponentUpdaterImpl());

        servlet = ctx.registerInjectActivateService(new ComponentUpdatersDataSource(),
                "sling.servlet.resourceTypes", "asset-share-commons/data-sources/asset-kit/component-updaters",
                "sling.servlet.methods", "GET");
    }

    @Test
    public void doGet() throws ServletException, IOException {
        String[] expectedLabels = new String[]{new BannerComponentUpdaterImpl().getName(), new PageMetadataComponentUpdaterImpl().getName()};
        String[] expectedValues = new String[]{BannerComponentUpdaterImpl.class.getName(), PageMetadataComponentUpdaterImpl.class.getName()};

        ctx.currentResource("/apps/dialog/default");

        servlet.service(ctx.request(), ctx.response());

        final DataSource sds = (DataSource) ctx.request().getAttribute(DataSource.class.getName());
        final Map<String, String> actual = toMap(sds);

        assertArrayEquals(expectedLabels, actual.keySet().toArray());
        assertArrayEquals(expectedValues, actual.values().toArray());
    }

    private Map<String, String> toMap(DataSource dataSource) {
        final Map<String, String> results = new LinkedHashMap<>();
        final Iterator<Resource> resourcesIterator = dataSource.iterator();
        while (resourcesIterator.hasNext()) {
            final Resource resource = resourcesIterator.next();
            final ValueMap properties = resource.getValueMap();

            results.put(properties.get(DataSourceBuilder.TEXT, String.class),
                    properties.get(DataSourceBuilder.VALUE, String.class));
        }
        return results;
    }
}