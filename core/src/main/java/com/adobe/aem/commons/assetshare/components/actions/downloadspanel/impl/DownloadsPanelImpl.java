package com.adobe.aem.commons.assetshare.components.actions.downloadspanel.impl;

import com.adobe.aem.commons.assetshare.components.actions.ActionHelper;
import com.adobe.aem.commons.assetshare.components.actions.downloadspanel.DownloadsPanel;
import com.adobe.aem.commons.assetshare.content.async.download.AsyncDownload;
import com.adobe.cq.dam.download.api.DownloadArtifact;
import com.adobe.cq.dam.download.api.DownloadProgress;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adobe.acs.commons.util.CookieUtil;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {DownloadsPanel.class}
)
public class DownloadsPanelImpl implements DownloadsPanel {
	
    private static final Logger log = LoggerFactory.getLogger(DownloadsPanelImpl.class);
    private static final String ASC_DOWNLOAD_COOKIE = "ADC";
    
	@Self
    @Required
    private SlingHttpServletRequest request;

    @OSGiService
    private ActionHelper actionHelper;
    
    @OSGiService
    private AsyncDownload asyncDownload;

    private Collection<DownloadProgress> downloadProgress = new ArrayList<>();
    private Collection<String> paths = new ArrayList<>();
    private String[] downloadIds = null;
    protected List<DownloadStatus> downloadStatus = null;

    @PostConstruct
    protected void init() { 	

    	try{

    	    String requestDLs = request.getParameter("downloadIds");
    		if(requestDLs != null){
    			downloadIds = requestDLs.split(",");
    		}
    		
    	}
    	catch (Exception e) {
        	log.error("Error While processing Async download ",e);
        }
    }
        
    @Override
    public List<DownloadStatus> getDownloadStatus() {
        if (downloadStatus == null  && downloadIds !=null) {
        	downloadStatus = new ArrayList<>();
        	
        	try{
        		for(String downloadId : downloadIds){
            		DownloadProgress progress = asyncDownload.getDownloadStatus(request.getResourceResolver(),downloadId);
            		
            		long size = progress.getTotalSize();
            		int downloadprogress = progress.getProgress();
            		int totalCount = progress.getTotalCount();
            		String status = progress.getStatus().toString();
            		String name = null;
            		String url = null;
            		Collection<String> assetsList = new ArrayList<>();
            		
            		if(null != progress){
                        // retrieve successfully generated URIs
                        for (DownloadArtifact artifact : progress.getArtifacts()) {
                          if (artifact.getBinaryURI() != null) {
                        	  name = artifact.getName();
                        	  url = artifact.getBinaryURI().toString();
                        	  assetsList = artifact.getSuccesses();
                          } else {
                             // handleFailure(artifact.getFailureReason());
                          }
                        }
                        
                        downloadStatus.add(new DownloadStatus( name, url,  size,  downloadId, downloadprogress,status,totalCount,assetsList));
                		downloadProgress.add(progress);
            		}
            	}
        	} catch (Exception e){
        		log.error("Error while processing downloads info");
        	}
        }
        
        return Collections.unmodifiableList(downloadStatus);
    }

    public Collection<String> getPaths() {
        return new ArrayList<>(paths);
    }
}