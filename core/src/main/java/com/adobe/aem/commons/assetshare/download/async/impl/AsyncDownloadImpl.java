package com.adobe.aem.commons.assetshare.download.async.impl;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.download.async.AsyncDownload;
import com.adobe.aem.commons.assetshare.util.MimeTypeHelper;
import com.adobe.cq.dam.download.api.DownloadApiFactory;
import com.adobe.cq.dam.download.api.DownloadException;
import com.adobe.cq.dam.download.api.DownloadManifest;
import com.adobe.cq.dam.download.api.DownloadProgress;
import com.adobe.cq.dam.download.api.DownloadService;

@Component
public class AsyncDownloadImpl implements AsyncDownload {

	private static final Logger log = LoggerFactory.getLogger(AsyncDownloadImpl.class);

	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

	public static final String ASSET_PATH = "path";
	public static final String ARCHIVE_NAME = "archiveName";
	public static final String ORIGINAL_RENDITION = "original";
	public static final String ASSET_LABEL = "asset";

	@Reference
	private DownloadService downloadService;

	@Reference
	private DownloadApiFactory apiFactory;

	@Reference
	private MimeTypeHelper mimeTypeHelper;

	public String createDownload(ResourceResolver resolver, List<AssetModel> assets) throws DownloadException {
		DownloadManifest manifest = apiFactory.createDownloadManifest();

		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String archiveName = sdf.format(timestamp).toString() + ".zip";

		try{
			for (AssetModel asset : assets) {
				manifest = addAssetRenditionsToManifest(asset, manifest, archiveName);
			}
		}catch (Exception ex) {
			log.error("Exception while adding assets to the manifest", ex);
		} 

		return downloadService.download(manifest, resolver);
	}

	private DownloadManifest addAssetRenditionsToManifest(AssetModel asset, DownloadManifest manifest,
			String archiveName) {

		Map<String, Object> renditionParameters = new HashMap<String, Object>();
		renditionParameters.put(ASSET_PATH, asset.getPath());
		renditionParameters.put(ARCHIVE_NAME, archiveName);
		manifest.addTarget(apiFactory.createDownloadTarget(ASSET_LABEL, new ValueMapDecorator(renditionParameters)));

		return manifest;
	}

	public DownloadProgress getDownloadStatus(ResourceResolver resolver, String downloadId) throws DownloadException {
		Iterable<String> getDownloadIds = downloadService.getDownloadIds(resolver);
		for (String downID : getDownloadIds) {
			if (downloadId.equalsIgnoreCase(downID)) {
				DownloadProgress progress = downloadService.getProgress(downloadId, resolver);
				return progress;
			}
		}

		return null;
	}

}