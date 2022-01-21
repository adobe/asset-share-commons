package com.adobe.aem.commons.assetshare.testing;

import com.adobe.aem.commons.assetshare.util.RequireAem;
import com.google.common.collect.ImmutableMap;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

import static org.mockito.Mockito.*;

public class RequireAemMock {
    public static void setAem(AemContext ctx, RequireAem.Distribution distribution, RequireAem.ServiceType serviceType) {

        ctx.registerService(
                RequireAem.class,
                new RequireAem() {
                    @Override
                    public Distribution getDistribution() {
                        return distribution;
                    }

                    @Override
                    public ServiceType getServiceType() {
                        return serviceType;
                    }
                },
                ImmutableMap.<String, Object>builder().
                        put(Constants.SERVICE_RANKING, 1).
                        put("distribution", distribution.getValue()).
                        put("service", serviceType.getValue()).
                        build());
    }
}
