package com.adobe.aem.commons.assetshare.components.actions.downloadspanel;

import com.adobe.cq.dam.download.api.DownloadException;
import com.adobe.cq.dam.download.api.DownloadProgress;
import org.apache.commons.io.FileUtils;
import org.osgi.annotation.versioning.ProviderType;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@ProviderType
public interface DownloadsPanel {
	List<DownloadProgress> getDownloads() throws DownloadException;
}
