package com.adobe.aem.commons.assetshare.content.async.download;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.mime.MimeTypeService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.scene7.api.S7ConfigResolver;
import com.day.cq.dam.scene7.api.Scene7Service;
import com.day.cq.dam.scene7.api.constants.Scene7Constants;
import com.adobe.cq.dam.download.api.DownloadApiFactory;
import com.adobe.cq.dam.download.api.DownloadException;
import com.adobe.cq.dam.download.api.DownloadFile;
import com.adobe.cq.dam.download.api.DownloadTarget;
import com.adobe.cq.dam.download.spi.DownloadTargetProcessor;

@Component(service = DownloadTargetProcessor.class)
public class VideoEncodingDownloadTargetProcessor implements DownloadTargetProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(VideoEncodingDownloadTargetProcessor.class);
    private static final String PARAM_PATH = "path";
    private static final String PARAM_RENDITIONNAME = "encoding";
    
    private static final String PARAM_ARCHIVENAME = "archiveName";
    
    private static final Map<String, Object> SERVICE_USER_AUTH_INFO = Collections.<String, Object>singletonMap(ResourceResolverFactory.SUBSERVICE, Scene7Constants.S7_ASSET_READER_SERVICE);

    @Reference
    private ResourceResolverFactory resolverFactory;
    
    @Reference
    private DownloadApiFactory apiFactory;
    
    @Reference
    private MimeTypeService mimeService;
    
    @Reference
    private Scene7Service scene7Service;
    
    @Reference
    private S7ConfigResolver s7ConfigResolver;
    
    @Override
    public Collection<DownloadFile> processTarget(DownloadTarget target, ResourceResolver resourceResolver) throws DownloadException  {
        List<DownloadFile> answer = new ArrayList<>();
        
        String path = target.getParameter(PARAM_PATH, String.class);
        String renditionName = target.getParameter(PARAM_RENDITIONNAME, String.class);
        String archiveName = target.getParameter(PARAM_ARCHIVENAME, String.class);
        
        Resource assetResource = resourceResolver.getResource(path);
        Asset asset = assetResource.adaptTo(Asset.class);
        String domain = asset.getMetadataValue("dam:scene7Domain"); 
        String folder = asset.getMetadataValue("dam:scene7Folder");
       
        Map<String, Object> fileParams = new HashMap<String, Object>();
        
        fileParams.put("archivePath", getArchiveFileName(asset, renditionName, asset.getMimeType()));
        fileParams.put("archiveName", archiveName);
        String assetName = getAssetName(asset.getName(),asset.getMimeType());
        String assetExtension = getAssetExtension(asset.getName());
   
        String pathofasset = domain+"/is/content/"+folder+"/"+assetName+"-"+renditionName+assetExtension;
        
        URI binaryURL = null;
		try {
			binaryURL = new URI(pathofasset);
		} catch (URISyntaxException e) {
			LOG.error("Exception while fetching binary URL ",e);
		}
		

        answer.add(apiFactory.createDownloadFile(Optional.of((long) 0), binaryURL, fileParams));
       
     
        return answer;
    }
    
    private String getAssetExtension(String name) {
        return name.substring(name.lastIndexOf("."),name.length());
	}

	private String getAssetName(String filename, String mimeType){
        return  filename.replace("."+mimeService.getExtension(mimeType), "");
    }
	
    private ResourceResolver getConfigServiceResolver() throws DownloadException {
        try {
            return resolverFactory.getServiceResourceResolver(SERVICE_USER_AUTH_INFO);
        } catch (LoginException e) {
            throw new DownloadException("Unable to retrieve service user resolver", e);
        }
    }
    
    private String getArchiveFileName(Asset asset, String renditionName, String mimeType) {
        return asset.getName()+"-"+renditionName+"."+mimeService.getExtension(mimeType);
    }

    @Override
    public String getTargetType() {
        return "videoencoding";
    }

    @Override
    public Map<String, Boolean> getValidParameters() {
        Map<String, Boolean> answer = new HashMap<String, Boolean>();
        answer.put(PARAM_PATH, true);
        answer.put(PARAM_RENDITIONNAME, true);
        return answer;
    }

}

