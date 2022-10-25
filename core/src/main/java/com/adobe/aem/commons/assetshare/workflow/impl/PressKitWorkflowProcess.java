package com.adobe.aem.commons.assetshare.workflow.impl;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.launcher.WorkflowLauncher;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.WCMException;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component(service = WorkflowProcess.class, property = {
        "workflow.label" + "=Press Kit workflow",
})
public class PressKitWorkflowProcess implements WorkflowProcess {
    private static final Logger log = LoggerFactory.getLogger(PressKitWorkflowProcess.class);

    @Reference
    private QueryBuilder queryBuilder;

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap) throws WorkflowException {
        ResourceResolver resourceResolver = workflowSession.adaptTo(ResourceResolver.class);
        String payload = workItem.getWorkflowData().getPayload().toString();

        String templatePath = metaDataMap.get("PAGE_TEMPLATE_PATH", String.class);

        String pressKitResourceType = metaDataMap.get("PRESS_KIT_COMPONENT_RESOURCE_TYPE", String.class);
        String pressKitProperty = metaDataMap.get("PRESS_KIT_COMPONENT_PROPERTY_NAME", String.class);

        String heroResourceType = metaDataMap.get("HERO_COMPONENT_RESOURCE_TYPE", String.class);
        String heroProperty = metaDataMap.get("HERO_COMPONENT_PROPERTY_NAME", String.class);

        try {
            Page page = createPage(resourceResolver, payload, templatePath);
            updatePage(page, heroResourceType, heroProperty, getHeroPropertyValue(resourceResolver, payload));
            updatePage(page, pressKitResourceType, pressKitProperty, new String[]{payload});
            persistData(workItem, workflowSession, "PRESS_KIT_PAGE_PATH", page.getPath());
        } catch (WCMException | PersistenceException | RepositoryException e) {
            throw new WorkflowException(e);
        }
    }

    private String[] getHeroPropertyValue(ResourceResolver resourceResolver, String payload) throws RepositoryException {
        Map<String, String> params = new HashMap<>();
        params.put("path", payload);
        params.put("type", "dam:Asset");
        params.put("nodename", "hero.*");
        params.put("p.limit", "1");

        Query query = queryBuilder.createQuery(PredicateGroup.create(params), resourceResolver.adaptTo(Session.class));
        SearchResult result = query.getResult();

        String[] heroPropertyValue = new String[result.getHits().size()];
        int i = 0;
        for (Hit hit : result.getHits()) {
            heroPropertyValue[i] = hit.getPath();
            i++;
        }

        return heroPropertyValue;
    }

    private void updatePage(Page page, String resourceType, String propertyName, String[] propertyValue) throws PersistenceException {
        Resource resource = findResourceByResourceType(page, resourceType);

        if (resource != null) {
            final ModifiableValueMap properties = resource.adaptTo(ModifiableValueMap.class);
            properties.put(propertyName, propertyValue);
            resource.getResourceResolver().commit();
        }
    }

    private Resource findResourceByResourceType(Page page, String resourceType) {
        final ResourceResolver resourceResolver = page.adaptTo(ResourceResolver.class);
        final Map<String, String> map = new HashMap<>();

        map.put("path", page.getContentResource().getPath());
        map.put("path.self", "true");
        map.put("property", "sling:resourceType");
        map.put("property.value", resourceType);
        map.put("p.offset", "0");
        map.put("p.limit", "1");

        final Query query = queryBuilder.createQuery(PredicateGroup.create(map), resourceResolver.adaptTo(Session.class));
        final SearchResult result = query.getResult();

        ResourceResolver leakingResourceResolver = null;

        try {
            for (final Hit hit : result.getHits()) {
                if (leakingResourceResolver == null) {
                    leakingResourceResolver = hit.getResource().getResourceResolver();
                }
                return resourceResolver.getResource(hit.getPath());
            }
        } catch (RepositoryException e) {
            log.error("Error collecting search results", e);
        } finally {
            if (leakingResourceResolver != null) {
                leakingResourceResolver.close();
            }
        }
        return  null;

    }


    private Page createPage(ResourceResolver resourceResolver, String title, String templatePath) throws WCMException {

        // Create as string from today's date in the format YYYY/MM
        final String date = new SimpleDateFormat("yyyy/MM").format(new Date());
        final UUID uuid = UUID.randomUUID();

        final String path = "/content/company/en/" + date + "/" + uuid;

        final PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
        final Page page = pageManager.create(path, title, templatePath, title, true);

        return page;
    }


    private <T> boolean persistData(WorkItem workItem, WorkflowSession workflowSession, String key, T val) {
        WorkflowData data = workItem.getWorkflow().getWorkflowData();
        if (data.getMetaDataMap() == null) {
            return false;
        }

        data.getMetaDataMap().put(key, val);
        workflowSession.updateWorkflowData(workItem.getWorkflow(), data);

        return true;
    }


}
