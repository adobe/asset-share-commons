package com.adobe.aem.commons.assetshare.content.async.download.impl;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
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
  
  
  public String createDownload(ResourceResolver resolver,List<AssetModel> assets,List<String> imageRenditionsList,List<String> videoRenditionsList,List<String> otherRenditionsList) throws DownloadException {
	    // add target parameters needed for an asset. See DownloadTargetProcessor
	    // documentation for additional details.
	    // create a manifest for specifying the targets to download
	    DownloadManifest manifest = apiFactory.createDownloadManifest();
	    
	    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
	    String archiveName = sdf.format(timestamp).toString()+".zip";
	    
	    for(AssetModel asset : assets){
	    	log.error("mime type is "+asset.getName()+mimeTypeHelper.getMimeType(asset));
	    	    	
	    	if(mimeTypeHelper.isDownloadSupportedImage(mimeTypeHelper.getMimeType(asset))){
				for(String imageRenditionName:imageRenditionsList){
					   Map<String, Object> imageRenditionParameters = new HashMap<String, Object>();
					   imageRenditionParameters.put(ASSET_PATH, asset.getPath());
					   imageRenditionParameters.put(ARCHIVE_NAME, archiveName);
					   if(imageRenditionName.equalsIgnoreCase(ORIGINAL_RENDITION)){
						   manifest.addTarget(apiFactory.createDownloadTarget(ASSET_LABEL, new ValueMapDecorator(imageRenditionParameters)));
					   }else{
						   imageRenditionParameters.put(IMAGE_PRESET, imageRenditionName);
						   manifest.addTarget(apiFactory.createDownloadTarget(DYNAMIC_RENDITION, new ValueMapDecorator(imageRenditionParameters)));
					   }
				}
	    	}
	    	
	    	if(mimeTypeHelper.isDownloadSupportedVideo(mimeTypeHelper.getMimeType(asset))){
	    		log.error("@@@@@in if routine to download "+mimeTypeHelper.getMimeType(asset) + asset.getName());
				for(String videorenditionName:videoRenditionsList){
					log.error("$$$$$$rendition name "+videorenditionName + asset.getName());
					Map<String, Object> videorenditionParameters = new HashMap<String, Object>();
					videorenditionParameters.put(ASSET_PATH, asset.getPath());
					videorenditionParameters.put(ARCHIVE_NAME, archiveName);
					if(videorenditionName.equalsIgnoreCase(ORIGINAL_RENDITION)){
					    manifest.addTarget(apiFactory.createDownloadTarget(ASSET_LABEL, new ValueMapDecorator(videorenditionParameters)));
					}else{
						videorenditionParameters.put(ENCODING_LABEL, videorenditionName);
					    manifest.addTarget(apiFactory.createDownloadTarget(VIDEO_ENCODING_LABEL, new ValueMapDecorator(videorenditionParameters)));
					}
				 }
	    	}
	    	
	    	if(mimeTypeHelper.isDownloadSupportedOther(mimeTypeHelper.getMimeType(asset))){
				for(String otherRenditionName:otherRenditionsList){
					Map<String, Object> videorenditionParameters = new HashMap<String, Object>();
					videorenditionParameters.put(ASSET_PATH, asset.getPath());
					videorenditionParameters.put(ARCHIVE_NAME, archiveName);
					if(otherRenditionName.equalsIgnoreCase(ORIGINAL_RENDITION)){
					    manifest.addTarget(apiFactory.createDownloadTarget(ASSET_LABEL, new ValueMapDecorator(videorenditionParameters)));
					}
				 }
	    	}  
	   }

	    // request the download and remember the ID
	    String downloadId = downloadService.download(manifest, resolver);

	    return downloadId;
}

  public DownloadProgress getDownloadStatus(ResourceResolver resolver,String downloadId) throws DownloadException {

	  
	  log.debug("retrieve the current status of the download");
	  // retrieve the current status of the download
	  Iterable<String> getDownloadIds = downloadService.getDownloadIds(resolver);
	  
	  for(String downID : getDownloadIds){
		  
		  if(downloadId.equalsIgnoreCase(downID)){
			  DownloadProgress progress = downloadService.getProgress(downloadId, resolver);
		      return progress;
		  }
	  }

      return null;
     
  }


}


