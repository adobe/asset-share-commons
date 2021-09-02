/*
 * Asset Share Commons
 *
 * Copyright (C) 2021 Adobe
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

package com.adobe.aem.commons.assetshare.content.renditions.download.async.impl;

import com.adobe.aem.commons.assetshare.content.renditions.download.async.DownloadEntry;
import com.adobe.cq.dam.download.api.DownloadArtifact;
import com.adobe.cq.dam.download.api.DownloadProgress;
import com.day.cq.dam.commons.util.UIHelper;
import org.apache.sling.api.SlingHttpServletRequest;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;

public class DownloadEntryImpl implements DownloadEntry {

    private final String id;
    private final DownloadProgress downloadProgress;
    private final DownloadArtifact downloadArtifact;
    private final SlingHttpServletRequest request;

    public DownloadEntryImpl(final SlingHttpServletRequest request, final String id, final DownloadProgress downloadProgress) {
        this.request = request;
        this.id = id;
        this.downloadProgress = downloadProgress;

        if (downloadProgress.getArtifacts().iterator().hasNext()) {
            this.downloadArtifact = downloadProgress.getArtifacts().iterator().next();
        } else {
            this.downloadArtifact = null;
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Calendar getStarted() {
        return downloadProgress.getStarted();
    }

    @Override
    public Calendar getFinished() {
        return downloadProgress.getFinished();
    }

    @Override
    public DownloadProgress.Status getStatus() {
        return downloadProgress.getStatus();
    }

    @Override
    public int getProcessedCount() {
        return downloadProgress.getProcessedCount();
    }

    @Override
    public int getSuccessCount() {
        return downloadProgress.getSuccessCount();
    }

    @Override
    public int getFailureCount() {
        return downloadProgress.getFailureCount();
    }

    @Override
    public int getTotalCount() {
        return downloadProgress.getTotalCount();
    }

    @Override
    public long getTotalSize() {
        return downloadProgress.getTotalSize();
    }

    @Override
    public String getFormattedTotalSize() {
        return UIHelper.getSizeLabel(getTotalSize(), request);
    }

    @Override
    public boolean isComplete() {
        return downloadProgress.isComplete();
    }

    @Override
    public int getProgress() {
        return downloadProgress.getProgress();
    }

    @Override
    public String getName() {
        if (downloadArtifact != null) {
            return downloadArtifact.getName();
        } else {
            return null;
        }
    }

    @Override
    public Collection<String> getSuccesses() {
        if (downloadArtifact != null) {
            return downloadArtifact.getSuccesses();
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Collection<String> getFailures() {
        if (downloadArtifact != null) {
            return downloadArtifact.getFailures().keySet();
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public String getURI() {
        if (downloadArtifact != null && downloadArtifact.getBinaryURI() != null) {
            return downloadArtifact.getBinaryURI().toString();
        } else {
            return null;
        }
    }
}
