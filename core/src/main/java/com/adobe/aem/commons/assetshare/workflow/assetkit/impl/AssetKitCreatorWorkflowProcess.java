/*
 * Asset Share Commons
 *
 * Copyright (C) 2023 Adobe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.adobe.aem.commons.assetshare.workflow.assetkit.impl;

import com.adobe.aem.commons.assetshare.util.assetkit.AssetKitHelper;
import com.adobe.aem.commons.assetshare.util.assetkit.ComponentUpdater;
import com.adobe.aem.commons.assetshare.util.assetkit.PagePathGenerator;
import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.search.QueryBuilder;
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
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.*;

import static com.day.cq.commons.jcr.JcrConstants.*;
import static org.apache.sling.jcr.resource.api.JcrResourceConstants.*;

@Component(service = WorkflowProcess.class,
        property = {
            "process.label=Asset kit creator",
        }
)
public class AssetKitCreatorWorkflowProcess implements WorkflowProcess {
    private static final Logger log = LoggerFactory.getLogger(AssetKitCreatorWorkflowProcess.class);

    private static final String TRACKING_PROPERTY_ASSETS_KIT_ID = "assetsKitId";

    private static final String WORKFLOW_ASSETS_KIT_PAGE_COMPONENT_UPDATERS = "COMPONENT_UPDATER_IDS";
    private static final String WORKFLOW_ASSETS_KIT_PAGE_TEMPLATE_PATH = "ASSETS_KIT_PAGE_TEMPLATE_PATH";
    private static final String WORKFLOW_ROOT_PAGE_PATH = "ROOT_PAGE_PATH";
    public static final String WORKFLOW_ASSETS_KIT_PAGE_ID = "ASSETS_KIT_PAGE_ID";
    public static final String WORKFLOW_ASSETS_KIT_PATH = "ASSETS_KIT_PATH";
    public static final String WORKFLOW_TRACK_AND_UPDATE = "TRACK_AND_UPDATE";

    public static final String WORKFLOW_PAGE_PATH_GENERATOR_ID = "PAGE_PATH_GENERATOR_ID";

    @Reference
    private transient QueryBuilder queryBuilder;

    @Reference
    private transient AssetKitHelper assetKitHelper;

    @Reference(
            policyOption = ReferencePolicyOption.GREEDY,
            cardinality = ReferenceCardinality.MULTIPLE
    )
    private transient Collection<ComponentUpdater> componentUpdaters;

    @Reference(
            policyOption = ReferencePolicyOption.GREEDY,
            cardinality = ReferenceCardinality.MULTIPLE
    )
    private transient Collection<PagePathGenerator> pagePathGenerators;

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap) throws WorkflowException {
        final ResourceResolver resourceResolver = workflowSession.adaptTo(ResourceResolver.class);
        final String payload = workItem.getWorkflowData().getPayload().toString();

        final Resource payloadResource = resourceResolver.getResource(payload);

        if (payloadResource == null) {
            throw new WorkflowException(String.format("Payload [ %s ] is not a resource.", payload));
        } else if (!(assetKitHelper.isAssetFolder(payloadResource) || assetKitHelper.isAssetCollection(payloadResource))) {
            throw new WorkflowException(String.format("Payload [ %s ] is not a valid DAM asset folder or collection.", payload));
        }

        final boolean trackAndUpdate = metaDataMap.get(WORKFLOW_TRACK_AND_UPDATE, false);
        final String templatePath = metaDataMap.get(WORKFLOW_ASSETS_KIT_PAGE_TEMPLATE_PATH, String.class);
        final String rootPagePath = metaDataMap.get(WORKFLOW_ROOT_PAGE_PATH, "/content/asset-kits");
        final String pagePathGeneratorId = metaDataMap.get(WORKFLOW_PAGE_PATH_GENERATOR_ID, String.class);
        final String[] componentUpdaterIds = metaDataMap.get(WORKFLOW_ASSETS_KIT_PAGE_COMPONENT_UPDATERS, String[].class);

        final Resource trackingResource = getOrCreateTrackingResource(payloadResource);

        String assetsKitId = pagePathGenerators.stream().filter(pagePathGenerator -> StringUtils.equals(pagePathGeneratorId, pagePathGenerator.getId())).findFirst().orElseThrow(() -> new WorkflowException(String.format("No PagePathGenerator found for ID [ %s ]", pagePathGeneratorId))).generatePagePath(rootPagePath, payloadResource);
        if (trackAndUpdate) {
            assetsKitId = trackingResource.getValueMap().get(TRACKING_PROPERTY_ASSETS_KIT_ID, assetsKitId);
        }

        try {
            // Get or create the press kit page
            final Page page = getOrCreateAssetsKitPage(resourceResolver, assetsKitId, templatePath, assetsKitId);

            trackingResource.adaptTo(ModifiableValueMap.class).put(TRACKING_PROPERTY_ASSETS_KIT_ID, page.getPath());

            // Update components on page
            for (String componentUpdaterId : componentUpdaterIds) {
                for (ComponentUpdater componentUpdater : componentUpdaters) {
                    if (StringUtils.equals(componentUpdaterId, componentUpdater.getId())) {
                        componentUpdater.updateComponent(page, payloadResource);
                    }
                }
            }

            resourceResolver.commit();

            // Save data for other workflows steps in the future that might need this info
            persistData(workItem, workflowSession, WORKFLOW_ASSETS_KIT_PAGE_ID, page.getPath());
            persistData(workItem, workflowSession, WORKFLOW_ASSETS_KIT_PATH, payload);
        } catch (WCMException | PersistenceException | RepositoryException e) {
            throw new WorkflowException(String.format("Could not build Press Kit page for [ %s ]", payload), e);
        }
    }

    private Page getOrCreateAssetsKitPage(ResourceResolver resourceResolver, String assetsKitId, String templatePath, String pageTitle) throws RepositoryException, WCMException {
        final PageManager pageManager = resourceResolver.adaptTo(PageManager.class);

        Page page = pageManager.getPage(assetsKitId);
        if (page != null) {
            page.getContentResource().adaptTo(ModifiableValueMap.class).put("jcr:title", pageTitle);
        } else {
            final Node node = JcrUtil.createPath(StringUtils.substringBeforeLast(assetsKitId, "/"), NT_SLING_ORDERED_FOLDER, NT_SLING_ORDERED_FOLDER, resourceResolver.adaptTo(Session.class), false);
            page = pageManager.create(node.getPath(), StringUtils.substringAfterLast(assetsKitId, "/"), templatePath, pageTitle, true);
        }

        return page;
    }

    private Resource getOrCreateTrackingResource(Resource resource) throws WorkflowException {
        if (assetKitHelper.isAssetFolder(resource)) {
            if (resource.getChild(JCR_CONTENT) != null) {
                resource = resource.getChild(JCR_CONTENT);
                if (resource.getChild("metadata") == null) {
                    try {
                        resource = resource.getResourceResolver().create(resource, "metadata", ImmutableMap.of(JCR_PRIMARYTYPE, NT_UNSTRUCTURED));
                    } catch (PersistenceException e) {
                        throw new WorkflowException(String.format("Could not create missing metadata node for asset folder [ {} ].", resource.getPath()), e);
                    }
                } else {
                    resource = resource.getChild("metadata");
                }
            } else {
                throw new WorkflowException(String.format("Asset folder [ %s ] does not have jcr:content node.", resource.getPath()));
            }
        }

        // Everything other than a folder should just be the payload resource
        return resource;
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




