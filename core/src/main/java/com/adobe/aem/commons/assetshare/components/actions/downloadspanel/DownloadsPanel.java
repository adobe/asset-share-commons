package com.adobe.aem.commons.assetshare.components.actions.downloadspanel;

import org.apache.commons.io.FileUtils;
import org.osgi.annotation.versioning.ProviderType;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@ProviderType
public interface DownloadsPanel {

    Collection<String> getPaths();
    
    default List<DownloadStatus> getDownloadStatus() {
        return Collections.EMPTY_LIST;
    }

    class DownloadStatus {
        private final String url;
        private final String downloadId;
        private final int progress;
        private final int totalCount;
        private final String status;
        private final long size;
        private final String name;
        private final Collection<String> assets;
        
        public DownloadStatus(String name, String url, long size, String downloadId, int downloadprogress,String status,int totalCount,Collection<String> assets) {
            this.name = name;
            this.url = url;
            this.status = status;
            this.size = size;
            this.progress =downloadprogress;
            this.downloadId = downloadId;
            this.totalCount = totalCount;
            this.assets = assets;
		}

    	public Collection<String> getAssets() {
            return assets;
        }
    	
		public String getName() {
            return name;
        }
		
		public int getTotalCount() {
            return totalCount;
        }
		
		public String getUrl() {
            return url;
        }
        
        public String getDownloadId() {
            return downloadId;
        }

        public int getProgress() {
            return progress;
        }

        public String getStatus() {
            return status;
        }

        public String getSize() {
            return FileUtils.byteCountToDisplaySize(size);
        }
    }
}
