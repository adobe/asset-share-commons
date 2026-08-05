package com.adobe.aem.commons.assetshare.util.impl;

import com.adobe.aem.commons.assetshare.util.ModelCache;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.commons.classloader.DynamicClassLoaderManager;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.factory.ModelFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ModelCacheImplTest {

    @Rule
    public final AemContext ctx = new AemContext();

    @Before
    public void setUp() throws Exception {
        ctx.create().resource("/content/test");
        ctx.currentResource("/content/test");

        ctx.registerService(ModelFactory.class, mock(ModelFactory.class));

        final DynamicClassLoaderManager dynamicClassLoaderManager = mock(DynamicClassLoaderManager.class);
        when(dynamicClassLoaderManager.getDynamicClassLoader()).thenReturn(getClass().getClassLoader());
        ctx.registerService(DynamicClassLoaderManager.class, dynamicClassLoaderManager);

        ctx.addModelsForClasses(ModelCacheImpl.class, DummyModel.class);
    }

    @Test
    public void get_byClass_cachesModelAcrossCalls() {
        final ModelCache modelCache = ctx.request().adaptTo(ModelCache.class);

        final DummyModel first = modelCache.get(DummyModel.class);
        final DummyModel second = modelCache.get(DummyModel.class);

        assertNotNull(first);
        assertSame("The second get() call should return the SAME cached instance", first, second);
    }

    @Test
    public void get_byClassName_cachesModelAcrossCalls() {
        final ModelCache modelCache = ctx.request().adaptTo(ModelCache.class);

        final Object first = modelCache.get((Object) DummyModel.class.getName());
        final Object second = modelCache.get((Object) DummyModel.class.getName());

        assertNotNull(first);
        assertSame(first, second);
    }

    @Test(expected = IllegalArgumentException.class)
    public void get_byClassName_notAString_throws() {
        final ModelCache modelCache = ctx.request().adaptTo(ModelCache.class);

        modelCache.get((Object) DummyModel.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void get_byClassName_unresolvableClass_throws() {
        final ModelCache modelCache = ctx.request().adaptTo(ModelCache.class);

        modelCache.get((Object) "com.adobe.aem.commons.assetshare.util.impl.DoesNotExistAtAll");
    }

    @Test
    public void get_byClass_notAdaptable_returnsNull() {
        final ModelCache modelCache = ctx.request().adaptTo(ModelCache.class);

        final NotAModel result = modelCache.get(NotAModel.class);

        assertNull(result);
    }

    @Model(adaptables = org.apache.sling.api.SlingHttpServletRequest.class)
    public static class DummyModel {
        public String getValue() {
            return "value";
        }
    }

    /**
     * A plain (non Sling Model) class that a SlingHttpServletRequest can never be adapted to,
     * used to exercise the "could not create a model to cache" branch of ModelCacheImpl#get(Class).
     */
    public static final class NotAModel {
    }
}
