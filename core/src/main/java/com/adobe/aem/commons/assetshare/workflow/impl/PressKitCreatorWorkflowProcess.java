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
import com.google.common.collect.ImmutableMap;
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

import static com.adobe.aem.commons.assetshare.util.DamUtil.isAssetFolder;
import static com.day.cq.commons.jcr.JcrConstants.*;
import static com.day.cq.dam.api.DamConstants.NT_DAM_ASSET;
import static org.apache.sling.jcr.resource.api.JcrResourceConstants.*;

@Component(service = WorkflowProcess.class, property = {
        "process.label=Press Kit generator",
})
public class PressKitCreatorWorkflowProcess implements WorkflowProcess {
    private static final Logger log = LoggerFactory.getLogger(PressKitCreatorWorkflowProcess.class);

    private static final String FOLDER_PROPERTY_PRESS_KIT_NAME = "pressKitName";
    private static final String FOLDER_PROPERTY_PRESS_KIT_BANNER_IMAGE = "pressKitBannerImage";
    private static final String FOLDER_PROPERTY_PRESS_KIT_ID = "pressKitId";
    private static final String WORKFLOW_PRESS_KIT_PAGE_TEMPLATE_PATH = "PRESS_KIT_PAGE_TEMPLATE_PATH";
    private static final String WORKFLOW_PRESS_KIT_COMPONENT_RESOURCE_TYPE = "PRESS_KIT_COMPONENT_RESOURCE_TYPE";
    private static final String WORKFLOW_PRESS_KIT_COMPONENT_PROPERTY_NAME = "PRESS_KIT_COMPONENT_PROPERTY_NAME";
    private static final String WORKFLOW_BANNER_COMPONENT_RESOURCE_TYPE = "BANNER_COMPONENT_RESOURCE_TYPE";
    private static final String WORKFLOW_BANNER_COMPONENT_IMAGE_PROPERTY_NAME = "BANNER_COMPONENT_IMAGE_PROPERTY_NAME";
    private static final String WORKFLOW_BANNER_COMPONENT_TEXT_PROPERTY_NAME = "BANNER_COMPONENT_TEXT_PROPERTY_NAME";
    private static final String WORKFLOW_ROOT_PAGE_PATH = "ROOT_PAGE_PATH";

    public static final String WORKFLOW_PRESS_KIT_PAGE_ID = "PRESS_KIT_PAGE_ID";
    public static final String WORKFLOW_PRESS_KIT_FOLDER_PATH = "PRESS_KIT_FOLDER_PATH";


    @Reference
    private QueryBuilder queryBuilder;

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap) throws WorkflowException {
        final ResourceResolver resourceResolver = workflowSession.adaptTo(ResourceResolver.class);
        final String payload = workItem.getWorkflowData().getPayload().toString();

        if (!isAssetFolder(resourceResolver, payload)) {
            throw new WorkflowException(String.format("Payload [ %s ] is not a valid DAM asset folder.", payload));
        }

        String templatePath = metaDataMap.get(WORKFLOW_PRESS_KIT_PAGE_TEMPLATE_PATH, String.class);
        String pressKitResourceType = metaDataMap.get(WORKFLOW_PRESS_KIT_COMPONENT_RESOURCE_TYPE, String.class);
        String pressKitProperty = metaDataMap.get(WORKFLOW_PRESS_KIT_COMPONENT_PROPERTY_NAME, String.class);
        String bannerResourceType = metaDataMap.get(WORKFLOW_BANNER_COMPONENT_RESOURCE_TYPE, String.class);
        String bannerImageProperty = metaDataMap.get(WORKFLOW_BANNER_COMPONENT_IMAGE_PROPERTY_NAME, "fileReference");
        String bannerTextProperty = metaDataMap.get(WORKFLOW_BANNER_COMPONENT_TEXT_PROPERTY_NAME, "jcr:title");
        String rootPagePath = metaDataMap.get(WORKFLOW_ROOT_PAGE_PATH, "/content/press-kits");

        Resource payloadResource = resourceResolver.getResource(payload);
        if (payloadResource.getChild(JCR_CONTENT) != null) {
            payloadResource = payloadResource.getChild(JCR_CONTENT);
            if (payloadResource.getChild("metadata") == null) {
                try {
                    resourceResolver.create(payloadResource, "metadata", ImmutableMap.of(JCR_PRIMARYTYPE, NT_UNSTRUCTURED));
                } catch (PersistenceException e) {
                    throw new WorkflowException(String.format("Could not create missing metadata node for asset folder [ {} ].", payloadResource.getPath()), e);
                }
            }
        } else {
            throw new WorkflowException(String.format("Asset folder [ %s ] does not have jcr:content node.", payload));
        }

        // Get Asset Folder properties
        final String pressKitId = payloadResource.getValueMap().get("metadata/" + FOLDER_PROPERTY_PRESS_KIT_ID,
                StringUtils.removeEnd(rootPagePath, "/") + "/" + new SimpleDateFormat("yyyy/MM").format(new Date()) + "/" + UUID.randomUUID());
        final String pressKitName = payloadResource.getValueMap().get("metadata/" + FOLDER_PROPERTY_PRESS_KIT_NAME, payloadResource.getValueMap().get(JCR_TITLE, resourceResolver.getResource(payload).getName()));
        final String pressKitBannerImage = payloadResource.getValueMap().get("metadata/" + FOLDER_PROPERTY_PRESS_KIT_BANNER_IMAGE, "banner.*");


        try {
            // Get or create the press kit page
            Page page = getOrCreatePressKitPage(resourceResolver, pressKitId, templatePath, pressKitName);

            // Save the page
            payloadResource.getChild("metadata").adaptTo(ModifiableValueMap.class).put(FOLDER_PROPERTY_PRESS_KIT_ID, page.getPath());

            // Update the Banner component on the page

            // Update the image reference
            updateComponentOnPage(page, bannerResourceType, bannerImageProperty, getBannerImagePropertyValue(resourceResolver, payload, pressKitBannerImage));

            // Update the text reference
            updateComponentOnPage(page, bannerResourceType, bannerTextProperty, pressKitName);

            // Update the Press Kit component on the page
            updateComponentOnPage(page, pressKitResourceType, pressKitProperty, payload);


            resourceResolver.commit();

            // Save data for other workflows steps in the future that might need this info
            persistData(workItem, workflowSession, WORKFLOW_PRESS_KIT_PAGE_ID, page.getPath());
            persistData(workItem, workflowSession, WORKFLOW_PRESS_KIT_FOLDER_PATH, payload);

        } catch (WCMException | PersistenceException | RepositoryException e) {
            throw new WorkflowException(String.format("Could not build Press Kit page for [ %s ]", payload), e);
        }
    }

    private String getBannerImagePropertyValue(ResourceResolver resourceResolver, String payload, String bannerImageName) throws RepositoryException {
        Map<String, String> params = new HashMap<>();
        params.put("path", payload);
        params.put("type", NT_DAM_ASSET);
        params.put("nodename", bannerImageName);
        params.put("p.limit", "1");

        Query query = queryBuilder.createQuery(PredicateGroup.create(params), resourceResolver.adaptTo(Session.class));
        SearchResult result = query.getResult();

        for (Hit hit : result.getHits()) {
            return hit.getPath();
        }

        return null;
    }

    private void updateComponentOnPage(Page page, String resourceType, String propertyName, String propertyValue) throws PersistenceException, RepositoryException {
        final Resource resource = findResourceByResourceType(page, resourceType);

        if (resource != null) {
            final ModifiableValueMap properties = resource.adaptTo(ModifiableValueMap.class);
            properties.put(propertyName, propertyValue);
        }
    }

    private Resource findResourceByResourceType(Page page, String resourceType) throws RepositoryException {
        final ResourceResolver resourceResolver = page.getContentResource().getResourceResolver();
        final Map<String, String> map = new HashMap<>();

        map.put("path", page.getContentResource().getPath());
        map.put("path.self", "true");
        map.put("property", SLING_RESOURCE_TYPE_PROPERTY);
        map.put("property.value", resourceType);
        map.put("p.limit", "1");

        final Query query = queryBuilder.createQuery(PredicateGroup.create(map), resourceResolver.adaptTo(Session.class));
        final SearchResult result = query.getResult();

        if (result.getHits().size() > 0) {
            return resourceResolver.getResource(result.getHits().get(0).getPath());
        } else {
            return null;
        }
    }


    private Page getOrCreatePressKitPage(ResourceResolver resourceResolver, String pressKitId, String templatePath, String pageTitle) throws RepositoryException, WCMException {
        final PageManager pageManager = resourceResolver.adaptTo(PageManager.class);

        Page page = pageManager.getPage(pressKitId);
        if (page != null) {
            page.getContentResource().adaptTo(ModifiableValueMap.class).put("jcr:title", pageTitle);
        } else {
            final Node node = JcrUtil.createPath(StringUtils.substringBeforeLast(pressKitId, "/"), NT_SLING_ORDERED_FOLDER, NT_SLING_ORDERED_FOLDER, resourceResolver.adaptTo(Session.class), false);
            page = pageManager.create(node.getPath(), StringUtils.substringAfterLast(pressKitId, "/"), templatePath, pageTitle, true);
        }

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




