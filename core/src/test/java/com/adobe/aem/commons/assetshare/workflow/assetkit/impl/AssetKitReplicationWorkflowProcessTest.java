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

import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.util.assetkit.AssetKitHelper;
import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.Workflow;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.ReplicationStatus;
import com.day.cq.replication.Replicator;
import com.day.cq.wcm.api.reference.Reference;
import com.day.cq.wcm.api.reference.ReferenceProvider;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AssetKitReplicationWorkflowProcessTest {

    @Rule
    public final AemContext ctx = new AemContext();

    @Mock
    ReferenceProvider referenceProvider;

    @Mock
    Replicator replicator;

    @Mock
    AssetKitHelper assetKitHelper;

    @Mock
    WorkflowSession workflowSession;

    @Mock
    WorkItem workItem;

    @Mock
    Workflow workflow;

    @Mock
    WorkflowData workflowData;

    @Mock
    ReplicationStatus replicationStatus;

    private AssetKitReplicationWorkflowProcess process;

    @Before
    public void setUp() throws Exception {
        ctx.load().json("/com/adobe/aem/commons/assetshare/workflow/assetkit/impl/AssetKitReplicationWorkflowProcessTest.json",
                "/content");

        when(workflowSession.adaptTo(ResourceResolver.class)).thenReturn(ctx.resourceResolver());
        when(workItem.getWorkflow()).thenReturn(workflow);
        when(workflow.getWorkflowData()).thenReturn(workflowData);

        // ReferenceProvider/Replicator/AssetKitHelper are @Reference fields; wire them via osgi-mock
        ctx.registerService(ReferenceProvider.class, referenceProvider);
        ctx.registerService(Replicator.class, replicator);
        ctx.registerService(AssetKitHelper.class, assetKitHelper);
        process = ctx.registerInjectActivateService(new AssetKitReplicationWorkflowProcess());
    }

    private FakeMetaDataMap persistedData(String assetsKitPath, String assetsKitPagePath) {
        final FakeMetaDataMap persisted = new FakeMetaDataMap();
        if (assetsKitPath != null) {
            persisted.put(AssetKitCreatorWorkflowProcess.WORKFLOW_ASSETS_KIT_PATH, assetsKitPath);
        }
        if (assetsKitPagePath != null) {
            persisted.put(AssetKitCreatorWorkflowProcess.WORKFLOW_ASSETS_KIT_PAGE_ID, assetsKitPagePath);
        }
        when(workflowData.getMetaDataMap()).thenReturn(persisted);
        return persisted;
    }

    @Test
    public void execute_activate_fullFlow() throws Exception {
        persistedData("/content/dam/kit-folder", "/content/asset-kits/kit-page");

        final FakeMetaDataMap metaDataMap = new FakeMetaDataMap();

        when(assetKitHelper.isAssetFolder(any(Resource.class))).thenReturn(true);

        final AssetModel assetModel = mock(AssetModel.class);
        when(assetModel.getPath()).thenReturn("/content/dam/kit-folder/asset1.png");
        doReturn(Arrays.asList(assetModel)).when(assetKitHelper)
                .getAssets(eq(ctx.resourceResolver()), any(String[].class));

        when(replicator.getReplicationStatus(any(), org.mockito.ArgumentMatchers.anyString())).thenReturn(replicationStatus);
        when(replicationStatus.isActivated()).thenReturn(false);

        final Resource refResource = ctx.resourceResolver().getResource("/content/dam/other-ref.png");
        final Reference reference = new Reference("asset", "ref", refResource, 0);
        when(referenceProvider.findReferences(any(Resource.class))).thenReturn(Arrays.asList(reference));

        process.execute(workItem, workflowSession, metaDataMap);

        verify(replicator, times(1)).replicate(any(), eq(ReplicationActionType.ACTIVATE), eq("/content/dam/kit-folder"));
        verify(replicator, times(1)).replicate(any(), eq(ReplicationActionType.ACTIVATE), eq("/content/dam/kit-folder/asset1.png"));
        verify(replicator, times(1)).replicate(any(), eq(ReplicationActionType.ACTIVATE), eq("/content/asset-kits"));
        verify(replicator, times(1)).replicate(any(), eq(ReplicationActionType.ACTIVATE), eq("/content/dam/other-ref.png"));
        // The kit page itself is replicated both during the ancestor walk AND unconditionally at the end
        verify(replicator, times(2)).replicate(any(), eq(ReplicationActionType.ACTIVATE), eq("/content/asset-kits/kit-page"));
    }

    @Test
    public void execute_deactivate_skipsAncestorWalkButStillReplicatesPage() throws Exception {
        persistedData("/content/dam/kit-folder", "/content/asset-kits/kit-page");

        final FakeMetaDataMap metaDataMap = new FakeMetaDataMap();
        metaDataMap.put("replicationActionType", "DEACTIVATE");

        when(assetKitHelper.isAssetFolder(any(Resource.class))).thenReturn(true);
        when(assetKitHelper.getAssets(eq(ctx.resourceResolver()), any(String[].class)))
                .thenReturn(Collections.emptyList());
        when(referenceProvider.findReferences(any(Resource.class))).thenReturn(Collections.emptyList());

        process.execute(workItem, workflowSession, metaDataMap);

        verify(replicator, never()).getReplicationStatus(any(), any());
        verify(replicator, times(1)).replicate(any(), eq(ReplicationActionType.DEACTIVATE), eq("/content/asset-kits/kit-page"));
        verify(replicator, times(1)).replicate(any(), eq(ReplicationActionType.DEACTIVATE), eq("/content/dam/kit-folder"));
    }

    @Test
    public void execute_assetReplicationExceptionIsLoggedNotThrown() throws Exception {
        persistedData("/content/dam/kit-folder", "/content/asset-kits/kit-page");

        final FakeMetaDataMap metaDataMap = new FakeMetaDataMap();
        metaDataMap.put("replicationActionType", "DEACTIVATE");

        when(assetKitHelper.isAssetFolder(any(Resource.class))).thenReturn(true);

        final AssetModel assetModel = mock(AssetModel.class);
        when(assetModel.getPath()).thenReturn("/content/dam/kit-folder/asset1.png");
        doReturn(Arrays.asList(assetModel)).when(assetKitHelper)
                .getAssets(eq(ctx.resourceResolver()), any(String[].class));
        when(referenceProvider.findReferences(any(Resource.class))).thenReturn(Collections.emptyList());

        org.mockito.Mockito.doThrow(new ReplicationException("boom")).when(replicator)
                .replicate(any(), eq(ReplicationActionType.DEACTIVATE), eq("/content/dam/kit-folder/asset1.png"));

        // Should not throw, since per-asset replication failures are only logged
        process.execute(workItem, workflowSession, metaDataMap);

        verify(replicator, times(1)).replicate(any(), eq(ReplicationActionType.DEACTIVATE), eq("/content/asset-kits/kit-page"));
    }

    @Test
    public void execute_blankAssetsKitPath_throwsWorkflowException() throws Exception {
        persistedData(null, "/content/asset-kits/kit-page");

        try {
            process.execute(workItem, workflowSession, new FakeMetaDataMap());
            assertTrue("Expected WorkflowException", false);
        } catch (WorkflowException e) {
            assertTrue(e.getMessage().contains("Asset kit asset folder is blank"));
        }
    }

    @Test
    public void execute_blankAssetsKitPagePath_throwsWorkflowException() throws Exception {
        persistedData("/content/dam/kit-folder", null);

        try {
            process.execute(workItem, workflowSession, new FakeMetaDataMap());
            assertTrue("Expected WorkflowException", false);
        } catch (WorkflowException e) {
            assertTrue(e.getMessage().contains("Asset kit page is blank"));
        }
    }

    @Test
    public void execute_pageNotFound_throwsWorkflowException() throws Exception {
        persistedData("/content/dam/kit-folder", "/content/asset-kits/does-not-exist");

        try {
            process.execute(workItem, workflowSession, new FakeMetaDataMap());
            assertTrue("Expected WorkflowException", false);
        } catch (WorkflowException e) {
            assertTrue(e.getMessage().contains("is not a page"));
        }
    }

    @Test
    public void execute_assetResourceNotFound_throwsWorkflowException() throws Exception {
        persistedData("/content/dam/does-not-exist", "/content/asset-kits/kit-page");

        try {
            process.execute(workItem, workflowSession, new FakeMetaDataMap());
            assertTrue("Expected WorkflowException", false);
        } catch (WorkflowException e) {
            assertTrue(e.getMessage().contains("does not exist"));
        }
    }

    @Test
    public void execute_notAssetFolderOrCollection_throwsWorkflowException() throws Exception {
        persistedData("/content/dam/kit-folder", "/content/asset-kits/kit-page");

        when(assetKitHelper.isAssetFolder(any(Resource.class))).thenReturn(false);
        when(assetKitHelper.isAssetCollection(any(Resource.class))).thenReturn(false);

        try {
            process.execute(workItem, workflowSession, new FakeMetaDataMap());
            assertTrue("Expected WorkflowException", false);
        } catch (WorkflowException e) {
            assertTrue(e.getMessage().contains("is not an asset folder or a collection"));
        }
    }

    @Test
    public void execute_mainReplicationExceptionThrown_wrappedInWorkflowException() throws Exception {
        persistedData("/content/dam/kit-folder", "/content/asset-kits/kit-page");

        when(assetKitHelper.isAssetFolder(any(Resource.class))).thenReturn(true);

        org.mockito.Mockito.doThrow(new ReplicationException("boom")).when(replicator)
                .replicate(any(), eq(ReplicationActionType.ACTIVATE), eq("/content/dam/kit-folder"));

        try {
            process.execute(workItem, workflowSession, new FakeMetaDataMap());
            assertTrue("Expected WorkflowException", false);
        } catch (WorkflowException e) {
            assertTrue(e.getMessage().contains("Failed to replicate asset kit"));
        }
    }

    @SuppressWarnings("unchecked")
    private static class FakeMetaDataMap extends HashMap<String, Object> implements MetaDataMap {
        @Override
        public <T> T get(String name, Class<T> type) {
            final Object value = get(name);
            return type.isInstance(value) ? (T) value : null;
        }

        @Override
        public <T> T get(String name, T defaultValue) {
            final Object value = get(name);
            return value == null ? defaultValue : (T) value;
        }
    }
}
