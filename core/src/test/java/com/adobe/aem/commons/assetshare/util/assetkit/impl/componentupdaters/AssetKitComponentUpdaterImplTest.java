package com.adobe.aem.commons.assetshare.util.assetkit.impl.componentupdaters;

import com.adobe.aem.commons.assetshare.util.assetkit.ComponentUpdater;
import com.adobe.aem.commons.assetshare.util.assetkit.impl.AssetKitHelperImpl;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.day.cq.wcm.api.Page;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.factory.ModelFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.Constants;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import java.util.Arrays;
import java.util.Map;

import static com.adobe.aem.commons.assetshare.testing.MockAssetModels.mockModelFactory;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AssetKitComponentUpdaterImplTest {

    @Rule
    public AemContext ctx = new AemContext();

    @Mock
    QueryBuilder queryBuilder;

    @Mock
    Query query;

    @Mock
    SearchResult searchResult;

    @Mock
    ModelFactory modelFactory;

    @Before
    public void setUp() throws Exception {
        ctx.load().json(getClass().getResourceAsStream("ComponentUpdaterTest.json"), "/content");

        mockModelFactory(ctx, modelFactory, "/content/dam/folder/banner.png");
        mockModelFactory(ctx, modelFactory, "/content/dam/folder/test-1.png");
        mockModelFactory(ctx, modelFactory, "/content/dam/folder/test-2.png");
        mockModelFactory(ctx, modelFactory, "/content/dam/folder/test-3.png");

        ctx.registerService(ModelFactory.class, modelFactory, Constants.SERVICE_RANKING, Integer.MAX_VALUE);

        ctx.registerService(QueryBuilder.class, queryBuilder);

        ctx.registerInjectActivateService(new AssetKitHelperImpl());
        ctx.registerInjectActivateService(new AssetKitComponentUpdaterImpl());
    }

    @Test
    public void getName() {
        ComponentUpdater componentUpdater = ctx.getService(ComponentUpdater.class);
        assertEquals("Asset kit component (Asset Share Commons)", componentUpdater.getName());
    }

    @Test
    public void updateComponent() throws PersistenceException, RepositoryException {
        when(queryBuilder.createQuery(any(), any())).thenReturn(query);
        when(query.getResult()).thenReturn(searchResult);
        when(searchResult.getHits()).thenReturn(Arrays.asList(new Hit[]{new MockHit("/content/page/jcr:content/root/responsivegrid/asset-kit")}));

        final int expected = 1;
        ComponentUpdater componentUpdater = ctx.getService(ComponentUpdater.class);

        componentUpdater.updateComponent(ctx.resourceResolver().getResource("/content/page").adaptTo(Page.class), ctx.resourceResolver().getResource("/content/dam/folder"));

        String[] actual = ctx.resourceResolver().getResource("/content/page/jcr:content/root/responsivegrid/asset-kit").getValueMap().get("paths", String[].class);

        assertEquals(expected, actual.length);
        assertEquals("/content/dam/folder", actual[0]);
    }

    private class MockHit implements Hit {
        private final String path;

        public MockHit(String path) {
            this.path = path;
        }

        @Override
        public long getIndex() {
            return 0;
        }

        @Override
        public Map<String, String> getExcerpts() throws RepositoryException {
            return null;
        }

        @Override
        public String getExcerpt() throws RepositoryException {
            return null;
        }

        @Override
        public Resource getResource() throws RepositoryException {
            return null;
        }

        @Override
        public Node getNode() throws RepositoryException {
            return null;
        }

        @Override
        public String getPath() throws RepositoryException {
            return path;
        }

        @Override
        public ValueMap getProperties() throws RepositoryException {
            return null;
        }

        @Override
        public String getTitle() throws RepositoryException {
            return null;
        }

        @Override
        public double getScore() throws RepositoryException {
            return 0;
        }
    }
}
