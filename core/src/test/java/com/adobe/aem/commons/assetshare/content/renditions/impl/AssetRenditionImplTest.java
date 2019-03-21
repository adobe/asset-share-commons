package com.adobe.aem.commons.assetshare.content.renditions.impl;

import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.content.AssetResolver;
import com.adobe.aem.commons.assetshare.content.impl.AssetModelImpl;
import com.adobe.aem.commons.assetshare.content.properties.ComputedProperties;
import com.adobe.aem.commons.assetshare.content.properties.impl.ComputedPropertiesImpl;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRendition;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionsHelper;
import com.day.cq.dam.commons.util.DamUtil;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class AssetRenditionImplTest {
    @Rule
    public AemContext ctx = new AemContext();

    @Before
    public void setUp() throws Exception {
        ctx.load().json("/com/adobe/aem/commons/assetshare/content/renditions/impl/AssetRenditionImplTest.json", "/content/dam");
        ctx.currentResource("/content/dam/test.png");

        ctx.registerService(AssetRenditionsHelper.class, new AssetRenditionsHelperImpl());

        final AssetResolver assetResolver = mock(AssetResolver.class);
        doReturn(DamUtil.resolveToAsset(ctx.resourceResolver().getResource("/content/dam/test.png"))).when(assetResolver).resolveAsset(ctx.request());
        ctx.registerService(AssetResolver.class, assetResolver);

        ctx.registerService(ComputedProperties.class, new ComputedPropertiesImpl());
        ctx.addModelsForClasses(AssetModelImpl.class);

        ctx.registerService(AssetRenditionsHelper.class, new AssetRenditionsHelperImpl());
    }

    @Test
    public void getUrl() {
        final AssetModel assetModel = ctx.request().adaptTo(AssetModel.class);

        ctx.addModelsForClasses(AssetRenditionImpl.class);
        ctx.request().setAttribute("asset", assetModel);
        ctx.request().setAttribute("renditionName", "test-rendition");
        ctx.request().setAttribute("renditionDownload", true);

        final AssetRendition assetRendition = ctx.request().adaptTo(AssetRendition.class);

        assertEquals("/content/dam/test.png.renditions/test-rendition/download/asset.rendition", assetRendition.getUrl());
    }
}
