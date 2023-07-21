package com.adobe.aem.commons.assetshare.util.impl.requests;

import org.apache.sling.api.SlingHttpServletRequest;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

public class IncludableRequestWrapper extends ExtensionOverrideRequestWrapper{
    private HashMap<String, Object> attributes = new HashMap<>();
    private String contentType;

    /**
     * @param wrappedRequest the request to wrap;
     * @param extension      the extension to force. Set to null for no extension;
     */
    public IncludableRequestWrapper(SlingHttpServletRequest wrappedRequest, String extension) {
        super(wrappedRequest, extension);
        contentType = wrappedRequest.getContentType();
    }


    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public void setAttribute(String name, Object o) {
        attributes.put(name, o);
    }

    @Override
    public Object getAttribute(String name) {
        if (attributes.containsKey(name)) {
            return attributes.get(name);
        }
        return super.getAttribute(name);
    }

    @Override
    public void removeAttribute(String name) {
        if (attributes.containsKey(name)) {
            attributes.remove(name);
            return;
        }
        super.removeAttribute(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        List<String> attributeNames = Collections.list(super.getAttributeNames());
        attributeNames.addAll(attributes.keySet());

        return Collections.enumeration(attributeNames);
    }
}
