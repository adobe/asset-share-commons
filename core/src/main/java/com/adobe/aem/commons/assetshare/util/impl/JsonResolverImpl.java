package com.adobe.aem.commons.assetshare.util.impl;

import com.adobe.acs.commons.util.BufferedServletOutput;
import com.adobe.acs.commons.util.BufferedSlingHttpServletResponse;
import com.adobe.acs.commons.util.PathInfoUtil;
import com.adobe.aem.commons.assetshare.util.JsonResolver;
import com.adobe.aem.commons.assetshare.util.impl.requests.ExtensionOverrideRequestWrapper;
import com.day.cq.commons.PathInfo;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.commons.util.DamUtil;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestDispatcherOptions;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.apache.jackrabbit.JcrConstants.NT_FILE;
import static org.apache.jackrabbit.JcrConstants.NT_RESOURCE;
import static org.apache.jackrabbit.oak.spi.nodetype.NodeTypeConstants.NT_OAK_RESOURCE;

@Component(service = JsonResolver.class)
public class JsonResolverImpl implements JsonResolver {
    private static final Logger log = LoggerFactory.getLogger(JsonResolverImpl.class);

    @Override
    public JsonElement resolveJson(SlingHttpServletRequest request, SlingHttpServletResponse response, String path) {
        ResourceResolver resourceResolver = request.getResourceResolver();
        Resource resource = request.getResourceResolver().getResource(path);
        JsonElement result = null;
        try {
            if (resource != null && (resourceResolver.isResourceType(resource, NT_FILE) ||
                    resourceResolver.isResourceType(resource, NT_RESOURCE) ||
                    resourceResolver.isResourceType(resource, NT_OAK_RESOURCE))) {
                result = getJsonFromNtFile(resource);
            } else if (resource != null && DamUtil.resolveToAsset(resource) != null) {
                result = getJsonStringFromDamAsset(resource);
            } else if (resource == null && StringUtils.startsWithAny(path, "http://", "https://")) {
                result = getJsonFromExternalUrl(path);
            } else if (resource != null && StringUtils.startsWithAny(path, "/etc/acs-commons/lists/")) {
                PageManager pageManager = request.getResourceResolver().adaptTo(PageManager.class);
                Page page = pageManager.getContainingPage(path);
                if (page != null) {
                    path = page.getPath() + "/jcr:content.list.json";
                }
                result = getJsonAsInternalRequest(request, response, path);
            } else {
                result = getJsonAsInternalRequest(request, response, path);
            }
        } catch (IOException | InterruptedException | ServletException e) {
            log.error("Unable to resolve JSON from path: {}", path, e);
        }

        return result;
    }

    private JsonElement getJsonFromNtFile(Resource resource) {
        Resource jcrContent = resource.getChild("jcr:content");
        if (jcrContent != null && StringUtils.equalsIgnoreCase(jcrContent.getValueMap().get("jcr:mimeType", String.class), "application/json")) {
            return getJsonElement(jcrContent.adaptTo(InputStream.class));
        } else {
            return null;
        }
    }

    private JsonElement getJsonStringFromDamAsset(Resource resource) {
        Asset asset = DamUtil.resolveToAsset(resource);

        if (StringUtils.equalsIgnoreCase(asset.getMimeType(), "application/json")) {
            return getJsonElement(asset.getOriginal().getStream());
        } else {
            return null;
        }
    }

    private JsonElement getJsonAsInternalRequest(SlingHttpServletRequest request, SlingHttpServletResponse response, String path) throws ServletException, IOException {
        BufferedSlingHttpServletResponse wrappedResponse = new BufferedSlingHttpServletResponse(response);
        wrappedResponse.getBufferedServletOutput().setFlushBufferOnClose(true);

        PathInfo pathInfo = new PathInfo(path);
        String extension = StringUtils.defaultIfEmpty(pathInfo.getExtension(), "json");

        final RequestDispatcherOptions options = new RequestDispatcherOptions();
        options.setReplaceSelectors(pathInfo.getSelectorString());
        options.setReplaceSuffix("");

        request.getRequestDispatcher(pathInfo.getResourcePath(), options)
                .forward(new ExtensionOverrideRequestWrapper(request, extension), wrappedResponse);

        byte[] bytes = null;
        if (wrappedResponse.getBufferedServletOutput().getWriteMethod() == BufferedServletOutput.ResponseWriteMethod.WRITER) {
            bytes = wrappedResponse.getBufferedServletOutput().getBufferedString().getBytes(StandardCharsets.UTF_8);
        } else if (wrappedResponse.getBufferedServletOutput().getWriteMethod() == BufferedServletOutput.ResponseWriteMethod.OUTPUTSTREAM){
           // Output stream
            bytes = wrappedResponse.getBufferedServletOutput().getBufferedBytes();
        }

        if (bytes != null) {
            return getJsonElement(new ByteArrayInputStream(bytes));
        } else {
            log.warn("Unable to resolve JSON from path: {}", path);
            return null;
        }
    }

    private JsonElement getJsonFromExternalUrl(String path) throws IOException, InterruptedException {
        URL url = new URL(path);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return getJsonElement(new ByteArrayInputStream(response.toString().getBytes(StandardCharsets.UTF_8)));
            }
        } else {
            return null;
        }
    }

    private JsonElement getJsonElement(InputStream inputStream) {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        return new Gson().fromJson(reader, JsonElement.class);
    }
}
