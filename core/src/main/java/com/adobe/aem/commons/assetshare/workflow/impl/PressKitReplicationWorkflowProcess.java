package com.adobe.aem.commons.assetshare.workflow.impl;

import com.adobe.aem.commons.assetshare.util.WorkflowUtil;
import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.day.cq.dam.commons.util.DamUtil;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.ReplicationStatus;
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
import java.util.*;

import static com.adobe.aem.commons.assetshare.workflow.impl.PressKitWorkflowProcess.WORKFLOW_PRESS_KIT_FOLDER_PATH;
import static com.adobe.aem.commons.assetshare.workflow.impl.PressKitWorkflowProcess.WORKFLOW_PRESS_KIT_PAGE_ID;

@Component(service = WorkflowProcess.class, property = {
        "process.label=Press Kit activator",
})
public class PressKitReplicationWorkflowProcess implements WorkflowProcess {
    private static final Logger log = LoggerFactory.getLogger(PressKitReplicationWorkflowProcess.class);
    private static final String WORKFLOW_REPLICATION_ACTIVATION_TYPE = "replicationActionType";

    @Reference
    private ReferenceProvider referenceProvider;

    @Reference
    private Replicator replicator;

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap) throws WorkflowException {
        final ResourceResolver resourceResolver = workflowSession.adaptTo(ResourceResolver.class);
        final Session session = resourceResolver.adaptTo(Session.class);

        final String pressKitFolderPath = WorkflowUtil.getPersistedData(workItem, WORKFLOW_PRESS_KIT_FOLDER_PATH, String.class);
        final String pressKitPagePath = WorkflowUtil.getPersistedData(workItem, WORKFLOW_PRESS_KIT_PAGE_ID, String.class);
        final ReplicationActionType replicationActionType = ReplicationActionType.valueOf(metaDataMap.get(WORKFLOW_REPLICATION_ACTIVATION_TYPE,  "ACTIVATE"));

        if (StringUtils.isBlank(pressKitFolderPath)) {
            throw new WorkflowException("Press kit asset folder is blank");
        } else if (StringUtils.isBlank(pressKitPagePath)) {
            throw new WorkflowException("Press kit page is blank");
        } else if (resourceResolver.adaptTo(PageManager.class).getPage(pressKitPagePath) == null) {
            throw new WorkflowException(String.format("Press kit page [ %s ] is not a page.", pressKitPagePath));
        } else if (!com.adobe.aem.commons.assetshare.util.DamUtil.isAssetFolder(resourceResolver, pressKitFolderPath)) {
            throw new WorkflowException(String.format("Press kit asset folder [ %s ] is not an asset folder.", pressKitFolderPath));
        }

        final Page pressKitPage = resourceResolver.adaptTo(PageManager.class).getPage(pressKitPagePath);

        try {
            // Replicate the press kit folder and the first level assets
            replicator.replicate(resourceResolver.adaptTo(Session.class), replicationActionType, pressKitFolderPath);
            Iterator<Resource> resources =  resourceResolver.getResource(pressKitFolderPath).listChildren();
            while(resources.hasNext()) {
            	Resource resource = resources.next();
                if (DamUtil.isAsset(resource)) {
                    replicator.replicate(resourceResolver.adaptTo(Session.class), replicationActionType, resource.getPath());
                }
            }

            if (ReplicationActionType.ACTIVATE.equals(replicationActionType)) {
                Page page = pressKitPage;

                while(!"/content".equals(page.getPath())) {
                    if (ReplicationActionType.ACTIVATE.equals(replicationActionType) && !replicator.getReplicationStatus(session, page.getPath()).isActivated()) {
                        replicator.replicate(session, replicationActionType, page.getPath());
                    }
                    page = page.getParent();
                }
            }

            // Replicate the press kit page and any references
            final List<com.day.cq.wcm.api.reference.Reference> references = referenceProvider.findReferences(resourceResolver.getResource(pressKitPage.getPath()));
            for (com.day.cq.wcm.api.reference.Reference reference : references) {
            	replicator.replicate(session, replicationActionType, reference.getResource().getPath());
            }
            replicator.replicate(session, replicationActionType, pressKitPage.getPath());

        } catch (ReplicationException e) {
            throw new WorkflowException("Failed to replicate press kit", e);
        }
    }
}




