package com.adobe.aem.commons.assetshare.workflow.impl;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.WCMException;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.day.cq.commons.jcr.JcrConstants.JCR_CONTENT;
import static com.day.cq.commons.jcr.JcrConstants.JCR_TITLE;
import static com.day.cq.dam.api.DamConstants.NT_DAM_ASSET;
import static org.apache.sling.jcr.resource.api.JcrResourceConstants.NT_SLING_ORDERED_FOLDER;
import static org.apache.sling.jcr.resource.api.JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY;

@Component(service = WorkflowProcess.class, property = {
        "process.label=Press Kit generator",
})
public class PressKitWorkflowProcess implements WorkflowProcess {
    private static final Logger log = LoggerFactory.getLogger(PressKitWorkflowProcess.class);

    @Reference
    private QueryBuilder queryBuilder;

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap) throws WorkflowException {
        log.error("IN PressKitWorkflowProcess");
        ResourceResolver resourceResolver = workflowSession.adaptTo(ResourceResolver.class);
        String payload = workItem.getWorkflowData().getPayload().toString();

        String templatePath = metaDataMap.get("PAGE_TEMPLATE_PATH", String.class);
        log.debug("templatePath: {}", templatePath);
        String pressKitResourceType = metaDataMap.get("PRESS_KIT_COMPONENT_RESOURCE_TYPE", String.class);
        log.debug("pressKitResourceType: {}", pressKitResourceType);
        String pressKitProperty = metaDataMap.get("PRESS_KIT_COMPONENT_PROPERTY_NAME", String.class);
        log.debug("pressKitProperty: {}", pressKitProperty);

        String heroResourceType = metaDataMap.get("HERO_COMPONENT_RESOURCE_TYPE", String.class);
        String heroProperty = metaDataMap.get("HERO_COMPONENT_PROPERTY_NAME", String.class);
        String rootPagePath = metaDataMap.get("ROOT_PAGE_PATH", "/content/press-kit");


        Resource payloadResource = resourceResolver.getResource(payload);

        if (payloadResource.getChild(JCR_CONTENT) != null) {
            payloadResource = payloadResource.getChild(JCR_CONTENT);
        }

        String pageTitle = payloadResource.getValueMap().get(JCR_TITLE, resourceResolver.getResource(payload).getName());
        String pageName = payloadResource.getValueMap().get("pressKitId", UUID.randomUUID().toString());

        try {
            Page page = createPage(resourceResolver, rootPagePath, pageName, pageTitle, templatePath);
            log.debug("page: {}", page.getPath());
            updatePage(page, heroResourceType, heroProperty, getHeroPropertyValue(resourceResolver, payload));
            updatePage(page, pressKitResourceType, pressKitProperty, payload);
            payloadResource.adaptTo(ModifiableValueMap.class).put("pressKitId", page.getName());
            persistData(workItem, workflowSession, "PRESS_KIT_PAGE_PATH", page.getPath());
        } catch (WCMException | PersistenceException | RepositoryException e) {
            throw new WorkflowException(e);
        }
    }

    private String getHeroPropertyValue(ResourceResolver resourceResolver, String payload) throws RepositoryException {
        Map<String, String> params = new HashMap<>();
        params.put("path", payload);
        params.put("type", NT_DAM_ASSET);
        params.put("nodename", "hero.*");
        params.put("p.limit", "1");

        Query query = queryBuilder.createQuery(PredicateGroup.create(params), resourceResolver.adaptTo(Session.class));
        SearchResult result = query.getResult();

        for (Hit hit : result.getHits()) {
            return hit.getPath();
        }

        return null;
    }

    private void updatePage(Page page, String resourceType, String propertyName, String propertyValue) throws PersistenceException, RepositoryException {
        Resource resource = findResourceByResourceType(page, resourceType);

        if (resource != null) {
            final ModifiableValueMap properties = resource.adaptTo(ModifiableValueMap.class);
            properties.put(propertyName, propertyValue);
            resource.getResourceResolver().commit();
        }
    }

    private Resource findResourceByResourceType(Page page, String resourceType) throws RepositoryException {
        final ResourceResolver resourceResolver = page.getContentResource().getResourceResolver();
        final Map<String, String> map = new HashMap<>();

        map.put("path", page.getContentResource().getPath());
        map.put("path.self", "true");
        map.put("property", SLING_RESOURCE_TYPE_PROPERTY);
        map.put("property.value", resourceType);
        map.put("p.offset", "0");
        map.put("p.limit", "1");

        final Query query = queryBuilder.createQuery(PredicateGroup.create(map), resourceResolver.adaptTo(Session.class));
        final SearchResult result = query.getResult();

        if (result.getHits().size() > 0) {
            return resourceResolver.getResource(result.getHits().get(0).getPath());
        } else {
            return null;
        }

    }


    private Page createPage(ResourceResolver resourceResolver,
                            String rootPagePath, String pageName, String pageTitle, String templatePath) throws WCMException, RepositoryException {

        // Create as string from today's date in the format YYYY/MM
        final String date = new SimpleDateFormat("yyyy/MM").format(new Date());

        final String path = StringUtils.removeEnd(rootPagePath, "/") + "/" + date;

        Node node = JcrUtil.createPath(path, NT_SLING_ORDERED_FOLDER, NT_SLING_ORDERED_FOLDER, resourceResolver.adaptTo(Session.class), false);

        final PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
        final Page page = pageManager.create(node.getPath(), pageName, templatePath, pageTitle, false);

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




