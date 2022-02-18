package com.adobe.aem.commons.assetshare.components.actions.downloads;

import com.adobe.aem.commons.assetshare.content.renditions.download.async.DownloadEntry;
import com.adobe.cq.dam.download.api.DownloadException;
import org.osgi.annotation.versioning.ConsumerType;

import java.util.List;

@ConsumerType
public interface Downloads {

	/**
	 * This model is adapted from a SlingHttpServletRequest object that must have a RequestParameters to indicate the requested DownloadProgresses to return.
	 *
	 * - downloadId: 1 or more request parameters of this name whose values are the allowed downloadIds to include in the result.
	 *
	 * @return a list of the DownloadEntries the user has requested and also has access to.
	 * @throws DownloadException if unable to get a list of downloads, likely due to an issue getting the DownloadProgress
	 */
	List<DownloadEntry> getDownloads() throws DownloadException;
}