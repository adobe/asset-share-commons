package com.adobe.aem.commons.assetshare.content.async.download;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.sling.api.resource.ResourceResolver;

import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.cq.dam.download.api.DownloadException;
import com.adobe.cq.dam.download.api.DownloadProgress;

public interface AsyncDownload {

	DownloadProgress getDownloadStatus(ResourceResolver resourceResolver, String string) throws DownloadException;

	String createDownload(ResourceResolver resourceResolver, List<AssetModel> assets) throws DownloadException;

	class ManifestPayload {
		private final String archiveName;
		private final String type;
		private final String target;

		public ManifestPayload(String archiveName, String type,String target) {
			this.archiveName = archiveName;
			this.type = type;
			this.target = target;

		}

		public String getArchiveName() {
			return archiveName;
		}

		public String getType() {
			return type;
		}

		public String getTarget() {
			return target;
		}

	}

}
