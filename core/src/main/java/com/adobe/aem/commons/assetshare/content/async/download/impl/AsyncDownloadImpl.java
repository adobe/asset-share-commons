package com.adobe.aem.commons.assetshare.content.async.download.impl;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.content.async.download.AsyncDownload;
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
	public static final String DYNAMIC_RENDITION = "asset";
	public static final String IMAGE_PRESET = "imagePreset";
	public static final String ENCODING_LABEL = "encoding";
	public static final String VIDEO_ENCODING_LABEL = "videoencoding";

	@Reference
	private DownloadService downloadService;

	@Reference
	private DownloadApiFactory apiFactory;

	@Reference
	private MimeTypeHelper mimeTypeHelper;

	public String createDownload(ResourceResolver resolver, List<AssetModel> assets,
			Map<String, List<String>> renditionsMap) throws DownloadException {
		DownloadManifest manifest = apiFactory.createDownloadManifest();

		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String archiveName = sdf.format(timestamp).toString() + ".zip";

		for (AssetModel asset : assets) {
			manifest = addOrigianlRenditionToManifest(asset, manifest, archiveName);
			manifest = addDynamicRenditionToManifest(asset, manifest, archiveName, renditionsMap);
		}

		return downloadService.download(manifest, resolver);
	}

	private DownloadManifest addDynamicRenditionToManifest(AssetModel asset, DownloadManifest manifest,
			String archiveName, Map<String, List<String>> renditionsMap) {
		List<String> imageRenditionsList = renditionsMap.get(Constants.REQ_IMAGE_RENDITION_NAMES);
		List<String> videoRenditionsList = renditionsMap.get(Constants.REQ_VIDEO_RENDITION_NAMES);
		
		if (mimeTypeHelper.isDownloadSupportedImage(mimeTypeHelper.getMimeType(asset)) &&  (imageRenditionsList.size() > 1)) {
			addImageRenditionsToManifest(asset, imageRenditionsList, manifest, archiveName);
		}else if (mimeTypeHelper.isDownloadSupportedVideo(mimeTypeHelper.getMimeType(asset)) && (videoRenditionsList.size() > 1)) {
			addVideoRenditionsToManifest(asset, videoRenditionsList, manifest, archiveName);
		}

		return manifest;
	}

	private DownloadManifest addOrigianlRenditionToManifest(AssetModel asset, DownloadManifest manifest,
			String archiveName) {

		Map<String, Object> originalRenditionParameters = addBasicAssetParameters(asset, archiveName);
		manifest.addTarget(
				apiFactory.createDownloadTarget(ASSET_LABEL, new ValueMapDecorator(originalRenditionParameters)));

		return manifest;
	}

	private Map<String, Object> addBasicAssetParameters(AssetModel asset, String archiveName) {

		Map<String, Object> renditionParameters = new HashMap<String, Object>();
		renditionParameters.put(ASSET_PATH, asset.getPath());
		renditionParameters.put(ARCHIVE_NAME, archiveName);

		return renditionParameters;
	}

	private DownloadManifest addImageRenditionsToManifest(AssetModel asset, List<String> imageRenditionsList,
			DownloadManifest manifest, String archiveName) {

		for (String imageRenditionName : imageRenditionsList) {
			Map<String, Object> imageRenditionParameters = addBasicAssetParameters(asset, archiveName);
			imageRenditionParameters.put(IMAGE_PRESET, imageRenditionName);
			manifest.addTarget(apiFactory.createDownloadTarget(DYNAMIC_RENDITION,
					new ValueMapDecorator(imageRenditionParameters)));

		}

		return manifest;
	}

	private DownloadManifest addVideoRenditionsToManifest(AssetModel asset, List<String> videoRenditionsList,
			DownloadManifest manifest, String archiveName) {
		for (String videoRenditionName : videoRenditionsList) {
			Map<String, Object> imageRenditionParameters = addBasicAssetParameters(asset, archiveName);
			imageRenditionParameters.put(ENCODING_LABEL, videoRenditionName);
			manifest.addTarget(apiFactory.createDownloadTarget(VIDEO_ENCODING_LABEL,
					new ValueMapDecorator(imageRenditionParameters)));

		}

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
