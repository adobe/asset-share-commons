package com.adobe.aem.commons.assetshare.components.actions.downloads.impl;

import com.adobe.aem.commons.assetshare.content.renditions.download.async.DownloadEntry;
import com.adobe.cq.dam.download.api.DownloadProgress;
import com.day.cq.dam.commons.util.UIHelper;
import org.apache.sling.api.SlingHttpServletRequest;

import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

public class PlaceholderDownloadEntry implements DownloadEntry {
    private final SlingHttpServletRequest request;
    private final String id;
    private final int numSuccesses;
    private final int numFailures;
    private DownloadProgress.Status status;

    public PlaceholderDownloadEntry(SlingHttpServletRequest request, String id, DownloadProgress.Status status, int totalFiles) {
        this.id = id;
        this.status = status;
        this.request = request;

        if (DownloadProgress.Status.SUCCESSFUL.equals(status)) {
            numSuccesses = totalFiles;
            numFailures = 0;
        } else if (DownloadProgress.Status.FAILED.equals(status)) {
            numSuccesses = 0;
            numFailures = totalFiles;
        } else if (DownloadProgress.Status.PARTIALLY_SUCCESSFUL.equals(status)) {
            numSuccesses = (totalFiles / 2);
            numFailures = totalFiles - numSuccesses;
        } else if (DownloadProgress.Status.PROCESSING.equals(status)) {
            numSuccesses = 0;
            numFailures = 0;
        } else {
            numSuccesses = 0;
            numFailures = 0;
        }
    }

    @Override
    public String getId() {
        return"0000-0000-0000-" + String.valueOf(id);
    }

    @Override
    public DownloadProgress.Status getStatus() {
        return status;
    }

    @Override
    public int getTotalCount() {
        return getSuccessCount() + getFailureCount();
    }

    @Override
    public long getTotalSize() {
        if (DownloadProgress.Status.SUCCESSFUL.equals(status) || DownloadProgress.Status.PARTIALLY_SUCCESSFUL.equals(status)) {
            return 314159265L;
        }

        return 0L;
    }

    @Override
    public String getFormattedTotalSize() {
        return UIHelper.getSizeLabel(getTotalSize(), request);
    }

    @Override
    public int getProcessedCount() {
        return getSuccessCount() + getFailureCount();
    }

    @Override
    public int getProgress() {
        if (DownloadProgress.Status.PROCESSING.equals(status)) {
            return 83;
        }

        return 100;
    }

    @Override
    public String getName() {
        return "Placeholder download " + id + ".zip";
    }

    @Override
    public int getFailureCount() { return getFailures().size(); }

    @Override
    public int getSuccessCount() {
        return getSuccesses().size();
    }

    @Override
    public boolean isComplete() {
        return !DownloadProgress.Status.PROCESSING.equals(status);
    }

    @Override
    public Calendar getStarted() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -10);

        return calendar;
    }
    @Override
    public Calendar getFinished() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -1);

        return calendar;
    }

    @Override
    public String getURI() {
        return URI.create(String.format("/content/dam.downloadbinaries.json?downloadId=%s&artifactId=%s", String.valueOf(id), getId())).toString();
    }

    @Override
    public Collection<String> getSuccesses() {
        final List<String> successes = new ArrayList<>();

        for (int i = 0; i < numSuccesses; i++) {
            successes.add(String.format("Successful asset #%s (rendition-name).jpeg", String.valueOf(i)));
        }

        return successes;
    }

    @Override
    public Collection<String> getFailures() {

        final List<String> failures = new ArrayList<>();

        for (int i = 0; i < numFailures; i++) {
            failures.add(String.format("Failed asset #%s (rendition-name)", String.valueOf(i)));
        }

        return failures;
    }
}

