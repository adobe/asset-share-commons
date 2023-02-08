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
import com.adobe.aem.commons.assetshare.util.WorkflowUtil;
import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.Replicator;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.reference.ReferenceProvider;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import java.util.List;

import static com.adobe.aem.commons.assetshare.workflow.assetkit.impl.AssetKitCreatorWorkflowProcess.WORKFLOW_ASSETS_KIT_PAGE_ID;
import static com.adobe.aem.commons.assetshare.workflow.assetkit.impl.AssetKitCreatorWorkflowProcess.WORKFLOW_ASSETS_KIT_PATH;

@Component(service = WorkflowProcess.class, property = {
        "process.label=Asset kit activator",
})
public class AssetKitReplicationWorkflowProcess implements WorkflowProcess {
    private static final Logger log = LoggerFactory.getLogger(AssetKitReplicationWorkflowProcess.class);
    private static final String WORKFLOW_REPLICATION_ACTIVATION_TYPE = "replicationActionType";

    @Reference
    private transient ReferenceProvider referenceProvider;

    @Reference
    private transient Replicator replicator;

    @Reference
    private transient AssetKitHelper assetKitHelper;

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap) throws WorkflowException {
        final ResourceResolver resourceResolver = workflowSession.adaptTo(ResourceResolver.class);
        final Session session = resourceResolver.adaptTo(Session.class);

        final String assetsKitPath = WorkflowUtil.getPersistedData(workItem, WORKFLOW_ASSETS_KIT_PATH, String.class);
        final String assetsKitPagePath = WorkflowUtil.getPersistedData(workItem, WORKFLOW_ASSETS_KIT_PAGE_ID, String.class);
        final ReplicationActionType replicationActionType = ReplicationActionType.valueOf(metaDataMap.get(WORKFLOW_REPLICATION_ACTIVATION_TYPE,  "ACTIVATE"));

        if (StringUtils.isBlank(assetsKitPath)) {
            throw new WorkflowException("Asset kit asset folder is blank");
        } else if (StringUtils.isBlank(assetsKitPagePath)) {
            throw new WorkflowException("Asset kit page is blank");
        } else if (resourceResolver.adaptTo(PageManager.class).getPage(assetsKitPagePath) == null) {
            throw new WorkflowException(String.format("Asset kit page [ %s ] is not a page.", assetsKitPagePath));
        } else if (resourceResolver.getResource(assetsKitPath) == null) {
            throw new WorkflowException(String.format("Asset kit [ %s ] does not exist.", assetsKitPath));
        } else if (!assetKitHelper.isAssetFolder(resourceResolver.getResource(assetsKitPath)) &&
                !assetKitHelper.isAssetCollection(resourceResolver.getResource(assetsKitPath))) {
            throw new WorkflowException(String.format("Asset kit asset [ %s ] is not an asset folder or a collection.", assetsKitPath));
        }
        final Page assetsKitPage = resourceResolver.adaptTo(PageManager.class).getPage(assetsKitPagePath);

        try {
            // Replicate the asset kit and the first level assets
            replicator.replicate(resourceResolver.adaptTo(Session.class), replicationActionType, assetsKitPath);

            /// Replicate all the assets in the kit
            assetKitHelper.getAssets(resourceResolver, new String[]{assetsKitPath}).forEach(asset -> {
                try {
                    replicator.replicate(resourceResolver.adaptTo(Session.class), replicationActionType, asset.getPath());
                } catch (ReplicationException e) {
                    log.error("Error replicating asset [ {} ]", asset.getPath(), e);
                }
            });

            if (ReplicationActionType.ACTIVATE.equals(replicationActionType)) {
                Resource pageResource = assetsKitPage.adaptTo(Resource.class);

                while(!"/content".equals(pageResource.getPath())) {
                    if (ReplicationActionType.ACTIVATE.equals(replicationActionType) && !replicator.getReplicationStatus(session, pageResource.getPath()).isActivated()) {
                        replicator.replicate(session, replicationActionType, pageResource.getPath());
                    }
                    pageResource = pageResource.getParent();
                }
            }

            // Replicate the asset kit page and any references
            final List<com.day.cq.wcm.api.reference.Reference> references = referenceProvider.findReferences(resourceResolver.getResource(assetsKitPage.getPath()));
            for (com.day.cq.wcm.api.reference.Reference reference : references) {
            	replicator.replicate(session, replicationActionType, reference.getResource().getPath());
            }
            replicator.replicate(session, replicationActionType, assetsKitPage.getPath());

        } catch (ReplicationException e) {
            throw new WorkflowException(String.format("Failed to replicate asset kit [ %s ]", assetsKitPath), e);
        }
    }
}




