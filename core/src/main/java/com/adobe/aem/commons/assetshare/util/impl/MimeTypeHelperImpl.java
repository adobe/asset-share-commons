package com.adobe.aem.commons.assetshare.util.impl;

import com.adobe.aem.commons.assetshare.util.MimeTypeHelper;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import java.util.Arrays;

@Component(service = MimeTypeHelper.class)
@Designate(ocd = MimeTypeHelperImpl.Cfg.class)
public class MimeTypeHelperImpl implements MimeTypeHelper {

    private Cfg cfg;

    public boolean isBrowserSupportedImage(String mimeType) {
        return Arrays.stream(cfg.browserSupportedImageMimeTypes()).anyMatch(browserSupportedImageMimeType -> {
           return StringUtils.equals(browserSupportedImageMimeType, mimeType);
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
    }

}
