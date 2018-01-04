package com.adobe.aem.commons.assetshare.components.predicates.impl.postprocessor;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.commons.WCMUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.AbstractResourceVisitor;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.apache.sling.servlets.post.Modification;
import org.apache.sling.servlets.post.SlingPostProcessor;
import org.osgi.service.component.annotations.Component;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Component(service = SlingPostProcessor.class)
public class PredicateGroupIdPostProcessor implements SlingPostProcessor {
    public static final String PN_GROUP_ID = "predicateGroupId";
    public static final String PN_GENERATE_GROUP_ID = "generatePredicateGroupId";
    @Override
    synchronized public void process(final SlingHttpServletRequest request, final List<Modification> list) throws Exception {

        Resource resource = request.getResource();

        if (JcrConstants.JCR_CONTENT.equals(resource.getName())
                || StringUtils.containsNone(resource.getPath(), JcrConstants.JCR_CONTENT)
                || resource.getValueMap().get(PN_GROUP_ID, Long.class) != null) {
            // Note a validate candidate resource
            return;
        }

        final Page currentPage = request.getResourceResolver().adaptTo(PageManager.class).getContainingPage(resource);

        final GroupIdVisitor visitor = new GroupIdVisitor();

        visitor.accept(currentPage.getContentResource());

        final Collection<Long> groupIds = visitor.getGroupIds();

        Long nextGroupId = null;
        Long count = 1l;

        while (nextGroupId == null) {
            if (!groupIds.contains(count)) {
                nextGroupId = count;
            } else {
                count++;
            }
        }

        final ModifiableValueMap mvm = resource.adaptTo(ModifiableValueMap.class);
        mvm.put(PN_GROUP_ID, nextGroupId);

        list.add(Modification.onModified(resource.getPath()));
    }



    private class GroupIdVisitor extends AbstractResourceVisitor {
        final Set<Long> groupIds = new TreeSet<>();

        public Collection getGroupIds() {
            return groupIds;
        }

        @Override
        public final void accept(Resource resource) {
            final ValueMap properties = resource.getValueMap();

            // Only traverse resources that have a sling:resourceType; those without sling:resourceTypes are not components and simply sub-component configurations resources (such as Option lists)
            if (properties.get(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, String.class) != null) {
                super.accept(resource);
            }
        }

        @Override
        protected void visit(final Resource resource) {
            com.day.cq.wcm.api.components.Component component = WCMUtils.getComponent(resource);
                if (component.getProperties().get(PN_GENERATE_GROUP_ID, false)) {
                    final ValueMap properties = resource.getValueMap();
                    final Long groupId = properties.get(PN_GROUP_ID, Long.class);

                    if (groupId != null) {
                        groupIds.add(groupId);
                    }
                }
            }
    }
}
