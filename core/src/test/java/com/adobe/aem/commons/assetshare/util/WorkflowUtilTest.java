/*
 * Asset Share Commons
 *
 * Copyright (C) 2024 Adobe
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

package com.adobe.aem.commons.assetshare.util;

import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.Workflow;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WorkflowUtilTest {

    @Mock
    private WorkItem workItem;

    @Mock
    private WorkflowSession workflowSession;

    @Mock
    private Workflow workflow;

    @Mock
    private WorkflowData workflowData;

    @Mock
    private MetaDataMap metaDataMap;

    @Test
    public void persistData_noMetaDataMap_returnsFalse() {
        when(workItem.getWorkflow()).thenReturn(workflow);
        when(workflow.getWorkflowData()).thenReturn(workflowData);
        when(workflowData.getMetaDataMap()).thenReturn(null);

        final boolean actual = WorkflowUtil.persistData(workItem, workflowSession, "key", "value");

        assertFalse(actual);
        verify(workflowSession, never()).updateWorkflowData(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    public void persistData_withMetaDataMap_persistsAndReturnsTrue() {
        when(workItem.getWorkflow()).thenReturn(workflow);
        when(workflow.getWorkflowData()).thenReturn(workflowData);
        when(workflowData.getMetaDataMap()).thenReturn(metaDataMap);

        final boolean actual = WorkflowUtil.persistData(workItem, workflowSession, "key", "value");

        assertTrue(actual);
        verify(metaDataMap).put("key", "value");
        verify(workflowSession).updateWorkflowData(workflow, workflowData);
    }

    @Test
    public void getPersistedData_byClass_delegatesToMetaDataMap() {
        when(workItem.getWorkflow()).thenReturn(workflow);
        when(workflow.getWorkflowData()).thenReturn(workflowData);
        when(workflowData.getMetaDataMap()).thenReturn(metaDataMap);
        when(metaDataMap.get("key", String.class)).thenReturn("stored-value");

        final String actual = WorkflowUtil.getPersistedData(workItem, "key", String.class);

        assertEquals("stored-value", actual);
    }

    @Test
    public void getPersistedData_byDefaultValue_delegatesToMetaDataMap() {
        when(workItem.getWorkflow()).thenReturn(workflow);
        when(workflow.getWorkflowData()).thenReturn(workflowData);
        when(workflowData.getMetaDataMap()).thenReturn(metaDataMap);
        when(metaDataMap.get("key", "default-value")).thenReturn("default-value");

        final String actual = WorkflowUtil.getPersistedData(workItem, "key", "default-value");

        assertEquals("default-value", actual);
    }
}
