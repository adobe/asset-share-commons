package com.adobe.aem.commons.assetshare.testing;

import com.day.cq.wcm.api.components.Component;
import com.day.cq.wcm.api.components.ComponentManager;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test helper that registers a mocked {@link ComponentManager} on the ResourceResolver so that
 * {@code com.day.cq.wcm.commons.WCMUtils.getComponent(Resource)} (used by AbstractPredicate) resolves
 * to a component with the supplied properties (e.g. generatePredicateGroupId=true).
 */
public final class ComponentManagerMock {
    private ComponentManagerMock() {
    }

    public static void setComponentProperties(AemContext ctx, Map<String, Object> properties) {
        final ValueMap valueMap = new ValueMapDecorator(new HashMap<>(properties));

        final Component component = mock(Component.class);
        when(component.getProperties()).thenReturn(valueMap);

        final ComponentManager componentManager = mock(ComponentManager.class);
        when(componentManager.getComponentOfResource(org.mockito.ArgumentMatchers.any(Resource.class))).thenReturn(component);

        ctx.registerAdapter(ResourceResolver.class, ComponentManager.class, componentManager);
    }
}
