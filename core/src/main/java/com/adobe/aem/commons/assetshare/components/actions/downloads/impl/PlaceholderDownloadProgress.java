package com.adobe.aem.commons.assetshare.components.actions.downloads.impl;

import com.adobe.cq.dam.download.api.DownloadArtifact;
import com.adobe.cq.dam.download.api.DownloadProgress;
import com.google.common.collect.ImmutableList;
import org.apache.http.entity.mime.MIME;

import java.net.URI;
import java.util.*;

public class PlaceholderDownloadProgress implements DownloadProgress {
    @Override
    public String getRequestingUser() {
        return "Jane Doe";
    }

    @Override
    public Status getStatus() {
        return Status.SUCCESSFUL;
    }

    @Override
    public int getTotalCount() {
        return getSuccessCount() + getFailureCount();
    }

    @Override
    public long getTotalSize() {
        return 314159265L;
    }

    @Override
    public int getProcessedCount() {
        return getSuccessCount() + getFailureCount();
    }

    @Override
    public int getProgress() {
        return 80;
    }

    @Override
    public int getFailureCount() {
        return (int) getArtifacts().stream().map(a -> a.getFailures()).count();
    }

    @Override
    public int getSuccessCount() {
        return (int) getArtifacts().stream().map(a -> a.getSuccesses()).count();
    }

    @Override
    public boolean isComplete() {
        return true;
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
    public Collection<DownloadArtifact> getArtifacts() {
        final List<DownloadArtifact> downloadArtifacts = new ArrayList<>();

        downloadArtifacts.add(new PlaceholderDownloadArtifact(1, 4, 1));

        return downloadArtifacts;
    }

    private class PlaceholderDownloadArtifact implements DownloadArtifact {
        private final int id;
        private final List<String> successes = new ArrayList<>();
        private final Map<String, String> failures = new HashMap<>();

        public PlaceholderDownloadArtifact(int id, int numSuccesses, int numFailures) {
            this.id = id;

            for (int i = 0; i < numSuccesses; i++) {
                successes.add(String.format("Successful asset #%d (rendition-name).jpeg", i));
            }

            for (int i = 0; i < numFailures; i++) {
                failures.put(String.format("Failed asset #%d (rendition-name).jpeg", i), "Failure reason");
            }
        }

        @Override
        public String getId() {
            return String.format("0000-0000-0000-000%d", id);
        }

        @Override
        public String getName() {
            return String.format("Assets %d (mm-dd hh-mmaa).zip", id);
        }

        @Override
        public String getMimeType() {
            return  "application/zip";
        }

        @Override
        public URI getBinaryURI() {
            return URI.create(String.format("/content/dam.downloadbinaries.json?downloadId=%d&artifactId=%s", id, getId()));
        }

        @Override
        public String getFailureReason() {
            if (!failures.isEmpty()) {
                return "Example failure reason";
            }
            return null;
        }

        @Override
        public Collection<String> getSuccesses() {
            return ImmutableList.copyOf(successes);
        }

        @Override
        public Map<String, String> getFailures() {
            return failures;
        }
    }
}

