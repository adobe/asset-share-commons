package com.adobe.aem.commons.assetshare.content.async.download;

import java.util.List;

import org.apache.sling.api.resource.ResourceResolver;

import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.cq.dam.download.api.DownloadException;
import com.adobe.cq.dam.download.api.DownloadProgress;

public interface AsyncDownload {

	String createDownload(ResourceResolver resourceResolver, List<AssetModel> assets, List<String> renditionNames, List<String> videorenditionNames, List<String> otherrenditionNames) throws DownloadException;
	
	DownloadProgress getDownloadStatus(ResourceResolver resourceResolver, String string) throws DownloadException;

}
