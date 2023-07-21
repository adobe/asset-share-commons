package com.adobe.aem.commons.assetshare.util.impl.responses;

import com.adobe.acs.commons.util.BufferedSlingHttpServletResponse;
import org.apache.sling.api.SlingHttpServletResponse;

public class IncludableResponseWrapper extends BufferedSlingHttpServletResponse {
    private String contentType;

    public IncludableResponseWrapper(SlingHttpServletResponse wrappedResponse) {
        super(wrappedResponse);
        super.getBufferedServletOutput().setFlushBufferOnClose(true);
    }

    @Override
    public void setContentType(String type) {
        contentType = type;
    }

    @Override
    public String getContentType() {
        return contentType;
    }
}
