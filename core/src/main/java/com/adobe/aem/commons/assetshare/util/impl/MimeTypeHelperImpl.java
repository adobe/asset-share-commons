package com.adobe.aem.commons.assetshare.util.impl;

import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.content.async.download.impl.AsyncDownloadImpl;
import com.adobe.aem.commons.assetshare.util.MimeTypeHelper;
import com.day.cq.dam.api.Rendition;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

@Component(service = MimeTypeHelper.class)
@Designate(ocd = MimeTypeHelperImpl.Cfg.class)
public class MimeTypeHelperImpl implements MimeTypeHelper {

	
	 private static final Logger log = LoggerFactory.getLogger(MimeTypeHelper.class);
    private Cfg cfg;

    public boolean isBrowserSupportedImage(String mimeType) {
        return Arrays.stream(cfg.browserSupportedImageMimeTypes()).anyMatch(browserSupportedImageMimeType -> {
           return StringUtils.equals(browserSupportedImageMimeType, mimeType);
        });
    }
    
    public boolean isDownloadSupportedImage(String mimeType) {
        return Arrays.stream(cfg.CSDownloadSupportedImageMimeTypes()).anyMatch(CSDownloadSupportedImageMimeType -> {
           return StringUtils.startsWith(CSDownloadSupportedImageMimeType, mimeType);
        });
    }
    
    public boolean isDownloadSupportedVideo(String mimeType) {
        return Arrays.stream(cfg.CSDownloadSupportedVideoMimeTypes()).anyMatch(CSDownloadSupportedVideoMimeType -> {
           return StringUtils.startsWith(CSDownloadSupportedVideoMimeType, mimeType);
        });
    }
    
    public boolean isDownloadSupportedOther(String mimeType) {
        return Arrays.stream(cfg.CSDownloadSupportedOtherMimeTypes()).anyMatch(CSDownloadSupportedOtherMimeType -> {
           return StringUtils.startsWith(CSDownloadSupportedOtherMimeType, mimeType);
        });
    }

    @Activate
    protected void activate(Cfg cfg) {
        this.cfg = cfg;
    }

    @ObjectClassDefinition(name = "Asset Share Commons - MimeTypeHelper")
    public @interface Cfg {

        @AttributeDefinition(
                name = "Browser Supported Image MIME Types",
                description = "List of MIME types that are considered images supported by the browser (can be displayed via img tag)."
        )
        
        

        // Default list taken from: https://en.wikipedia.org/wiki/Comparison_of_web_browsers#Image_format_support
        String[] browserSupportedImageMimeTypes() default {
                "image/jpg",
                "image/jpeg",
                "image/png",
                "image/apng",
                "image/gif",
                "image/webp",
                "image/tiff",
                "image/svg+xml",
                "image/bmp",
                "image/x-xbitmap"
        };
        
        @AttributeDefinition(
                name = "Supported Image MIME Types for Downloads",
                description = "List of MIME types that are considered images supported by the AEM cloud services asset compute serive."
        )
        
        

        // Default list taken from: https://en.wikipedia.org/wiki/Comparison_of_web_browsers#Image_format_support
        String[] CSDownloadSupportedImageMimeTypes() default {
                "image/"
        };
        
        @AttributeDefinition(
                name = "Supported Video MIME Types for Download",
                description = "List of MIME types that are considered video supported by the AEM cloud services asset compute serive."
        )
        
        

        // Default list taken from: https://en.wikipedia.org/wiki/Comparison_of_web_browsers#Image_format_support
        String[] CSDownloadSupportedVideoMimeTypes() default {
                "video/"
        };
        
        
        @AttributeDefinition(
                name = "Supported Application MIME Types for Download",
                description = "List of MIME types that are considered other supported by the AEM cloud services asset compute serive."
        )
        
        

        // Default list taken from: https://en.wikipedia.org/wiki/Comparison_of_web_browsers#Image_format_support
        String[] CSDownloadSupportedOtherMimeTypes() default {
                "application/"
        };
    }

	@Override
	public String getMimeType(AssetModel asset) {
		String mimeType = null;
    	for(Rendition rendition : asset.getRenditions()){
    		if(rendition.getName().contentEquals("original")){
    			mimeType = rendition.getMimeType();
    			break;
    		}
    	}
		return mimeType;
	}

}
