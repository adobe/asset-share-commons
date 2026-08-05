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
import com.adobe.granite.workflow.exec.Workflow;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.day.cq.search.QueryBuilder;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AssetKitCreatorWorkflowProcessTest {

    @Rule
    public final AemContext ctx = new AemContext();

    @Mock
    QueryBuilder queryBuilder;

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

    private AssetKitCreatorWorkflowProcess process;

    @Before
    public void setUp() throws Exception {
        ctx.load().json("/com/adobe/aem/commons/assetshare/workflow/assetkit/impl/AssetKitCreatorWorkflowProcessTest.json",
                "/content");

        ctx.registerService(QueryBuilder.class, queryBuilder);
        ctx.registerService(AssetKitHelper.class, assetKitHelper);

        when(workflowSession.adaptTo(ResourceResolver.class)).thenReturn(ctx.resourceResolver());
        when(workItem.getWorkflowData()).thenReturn(workflowData);
        when(workItem.getWorkflow()).thenReturn(workflow);
        when(workflow.getWorkflowData()).thenReturn(workflowData);
        when(workflowData.getMetaDataMap()).thenReturn(new FakeMetaDataMap());
    }

    private void registerProcess(PagePathGenerator pagePathGenerator, ComponentUpdater componentUpdater) {
        if (pagePathGenerator != null) {
            ctx.registerService(PagePathGenerator.class, pagePathGenerator);
        }
        if (componentUpdater != null) {
            ctx.registerService(ComponentUpdater.class, componentUpdater);
        }
        process = ctx.registerInjectActivateService(new AssetKitCreatorWorkflowProcess());
    }

    private FakeMetaDataMap metaDataMap(String payloadPath) {
        when(workflowData.getPayload()).thenReturn(payloadPath);
        return new FakeMetaDataMap();
    }

    @Test
    public void execute_createsMetadataAndUpdatesExistingPage() throws Exception {
        final FakeMetaDataMap metaData = metaDataMap("/content/dam/kit-folder");
        metaData.put(AssetKitCreatorWorkflowProcess.WORKFLOW_PAGE_PATH_GENERATOR_ID, "gen1");
        metaData.put("COMPONENT_UPDATER_IDS", new String[]{"updater1"});
        metaData.put("ASSETS_KIT_PAGE_TEMPLATE_PATH", "/apps/some/template");

        when(assetKitHelper.isAssetFolder(any(Resource.class))).thenReturn(true);

        final PagePathGenerator pagePathGenerator = mock(PagePathGenerator.class);
        when(pagePathGenerator.getId()).thenReturn("gen1");
        when(pagePathGenerator.generatePagePath(eq("/content/asset-kits"), any(Resource.class)))
                .thenReturn("/content/asset-kits/existing-kit");

        final ComponentUpdater componentUpdater = mock(ComponentUpdater.class);
        when(componentUpdater.getId()).thenReturn("updater1");

        registerProcess(pagePathGenerator, componentUpdater);

        process.execute(workItem, workflowSession, metaData);

        // The tracking (metadata) resource should have been updated with the page path
        final Resource metadataResource = ctx.resourceResolver().getResource("/content/dam/kit-folder/jcr:content/metadata");
        assertEquals("/content/asset-kits/existing-kit", metadataResource.getValueMap().get("assetsKitId", String.class));

        // The page's title should be set to the assetsKitId (per the (arguably odd) production logic)
        final Resource pageContent = ctx.resourceResolver().getResource("/content/asset-kits/existing-kit/jcr:content");
        assertEquals("/content/asset-kits/existing-kit", pageContent.getValueMap().get("jcr:title", String.class));

        verify(componentUpdater, times(1)).updateComponent(any(), any(Resource.class));
        verify(workflowSession, times(2)).updateWorkflowData(eq(workflow), any(WorkflowData.class));
    }

    @Test
    public void execute_assetCollectionPayload_doesNotRequireJcrContent() throws Exception {
        final FakeMetaDataMap metaData = metaDataMap("/content/dam/kit-collection");
        metaData.put(AssetKitCreatorWorkflowProcess.WORKFLOW_PAGE_PATH_GENERATOR_ID, "gen1");
        metaData.put("COMPONENT_UPDATER_IDS", new String[]{});

        when(assetKitHelper.isAssetFolder(any(Resource.class))).thenReturn(false);
        when(assetKitHelper.isAssetCollection(any(Resource.class))).thenReturn(true);

        final PagePathGenerator pagePathGenerator = mock(PagePathGenerator.class);
        when(pagePathGenerator.getId()).thenReturn("gen1");
        when(pagePathGenerator.generatePagePath(eq("/content/asset-kits"), any(Resource.class)))
                .thenReturn("/content/asset-kits/existing-kit");

        registerProcess(pagePathGenerator, null);

        process.execute(workItem, workflowSession, metaData);

        final Resource pageContent = ctx.resourceResolver().getResource("/content/asset-kits/existing-kit/jcr:content");
        assertEquals("/content/asset-kits/existing-kit", pageContent.getValueMap().get("jcr:title", String.class));
    }

    @Test
    public void execute_trackAndUpdate_usesExistingTrackedId() throws Exception {
        final FakeMetaDataMap metaData = metaDataMap("/content/dam/kit-folder-with-metadata");
        metaData.put(AssetKitCreatorWorkflowProcess.WORKFLOW_PAGE_PATH_GENERATOR_ID, "gen1");
        metaData.put(AssetKitCreatorWorkflowProcess.WORKFLOW_TRACK_AND_UPDATE, true);
        metaData.put("COMPONENT_UPDATER_IDS", new String[]{});

        when(assetKitHelper.isAssetFolder(any(Resource.class))).thenReturn(true);

        final PagePathGenerator pagePathGenerator = mock(PagePathGenerator.class);
        when(pagePathGenerator.getId()).thenReturn("gen1");
        // This should be IGNORED since trackAndUpdate=true means the id already tracked on the resource wins
        when(pagePathGenerator.generatePagePath(eq("/content/asset-kits"), any(Resource.class)))
                .thenReturn("/content/asset-kits/some-other-path");

        registerProcess(pagePathGenerator, null);

        process.execute(workItem, workflowSession, metaData);

        final Resource pageContent = ctx.resourceResolver().getResource("/content/asset-kits/existing-kit/jcr:content");
        assertEquals("/content/asset-kits/existing-kit", pageContent.getValueMap().get("jcr:title", String.class));
    }

    @Test
    public void execute_payloadResourceDoesNotExist_throwsWorkflowException() throws Exception {
        final FakeMetaDataMap metaData = metaDataMap("/content/dam/does-not-exist");

        registerProcess(null, null);

        try {
            process.execute(workItem, workflowSession, metaData);
            assertTrue("Expected WorkflowException", false);
        } catch (WorkflowException e) {
            assertTrue(e.getMessage().contains("is not a resource"));
        }
    }

    @Test
    public void execute_payloadNotAssetFolderOrCollection_throwsWorkflowException() throws Exception {
        final FakeMetaDataMap metaData = metaDataMap("/content/dam/kit-folder");

        when(assetKitHelper.isAssetFolder(any(Resource.class))).thenReturn(false);
        when(assetKitHelper.isAssetCollection(any(Resource.class))).thenReturn(false);

        registerProcess(null, null);

        try {
            process.execute(workItem, workflowSession, metaData);
            assertTrue("Expected WorkflowException", false);
        } catch (WorkflowException e) {
            assertTrue(e.getMessage().contains("is not a valid DAM asset folder or collection"));
        }
    }

    @Test
    public void execute_noMatchingPagePathGenerator_throwsWorkflowException() throws Exception {
        final FakeMetaDataMap metaData = metaDataMap("/content/dam/kit-folder");
        metaData.put(AssetKitCreatorWorkflowProcess.WORKFLOW_PAGE_PATH_GENERATOR_ID, "does-not-exist");
        metaData.put("COMPONENT_UPDATER_IDS", new String[]{});

        when(assetKitHelper.isAssetFolder(any(Resource.class))).thenReturn(true);

        final PagePathGenerator pagePathGenerator = mock(PagePathGenerator.class);
        when(pagePathGenerator.getId()).thenReturn("gen1");

        registerProcess(pagePathGenerator, null);

        try {
            process.execute(workItem, workflowSession, metaData);
            assertTrue("Expected WorkflowException", false);
        } catch (WorkflowException e) {
            assertTrue(e.getMessage().contains("No PagePathGenerator found"));
        }
    }

    @Test
    public void execute_assetFolderMissingJcrContent_throwsWorkflowException() throws Exception {
        final FakeMetaDataMap metaData = metaDataMap("/content/dam/kit-folder-no-jcr-content");

        when(assetKitHelper.isAssetFolder(any(Resource.class))).thenReturn(true);

        registerProcess(null, null);

        try {
            process.execute(workItem, workflowSession, metaData);
            assertTrue("Expected WorkflowException", false);
        } catch (WorkflowException e) {
            assertTrue(e.getMessage().contains("does not have jcr:content node"));
        }
    }

    @Test
    public void execute_componentUpdaterThrowsPersistenceException_wrappedInWorkflowException() throws Exception {
        final FakeMetaDataMap metaData = metaDataMap("/content/dam/kit-folder");
        metaData.put(AssetKitCreatorWorkflowProcess.WORKFLOW_PAGE_PATH_GENERATOR_ID, "gen1");
        metaData.put("COMPONENT_UPDATER_IDS", new String[]{"updater1"});

        when(assetKitHelper.isAssetFolder(any(Resource.class))).thenReturn(true);

        final PagePathGenerator pagePathGenerator = mock(PagePathGenerator.class);
        when(pagePathGenerator.getId()).thenReturn("gen1");
        when(pagePathGenerator.generatePagePath(eq("/content/asset-kits"), any(Resource.class)))
                .thenReturn("/content/asset-kits/existing-kit");

        final ComponentUpdater componentUpdater = mock(ComponentUpdater.class);
        when(componentUpdater.getId()).thenReturn("updater1");
        org.mockito.Mockito.doThrow(new PersistenceException("boom")).when(componentUpdater)
                .updateComponent(any(), any(Resource.class));

        registerProcess(pagePathGenerator, componentUpdater);

        try {
            process.execute(workItem, workflowSession, metaData);
            assertTrue("Expected WorkflowException", false);
        } catch (WorkflowException e) {
            assertTrue(e.getMessage().contains("Could not build Press Kit page"));
        }
    }

    /**
     * A minimal, real (non-mocked) MetaDataMap backed by a HashMap, since MetaDataMap's generic
     * get(String, T) / get(String, Class) methods are awkward to stub individually with Mockito.
     */
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
