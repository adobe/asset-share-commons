/*
 * Asset Share Commons
 *
 * Copyright (C) 2019 Adobe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.adobe.aem.commons.assetshare.content.renditions.download.impl;

import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionDispatcher;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionDispatchers;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionParameters;
import com.adobe.aem.commons.assetshare.content.renditions.download.AssetRenditionStreamer;
import com.adobe.aem.commons.assetshare.content.renditions.download.AssetRenditionsException;
import com.adobe.aem.commons.assetshare.content.renditions.impl.AssetRenditionServlet;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.osgi.services.HttpClientBuilderFactory;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;

@Component
@Designate(ocd = AssetRenditionStreamerImpl.Cfg.class)
public class AssetRenditionStreamerImpl implements AssetRenditionStreamer {
    private static final Logger log = LoggerFactory.getLogger(AssetRenditionStreamerImpl.class);

    @Reference
    private HttpClientBuilderFactory clientBuilderFactory;

    @Reference
    private AssetRenditionDispatchers assetRenditionDispatchers;

    private Cfg cfg;

    public AssetRenditionStream getAssetRendition(final SlingHttpServletRequest request,
                                                  final SlingHttpServletResponse response,
                                                  final AssetModel asset,
                                                  final String renditionName) throws AssetRenditionsException {

        AssetRenditionDownloadResponse assetRenditionDownloadResponse;

        try {
            assetRenditionDownloadResponse = sendDispatchForAssetRendition(request, response, asset, renditionName);

            if (assetRenditionDownloadResponse.getStatusCode() > 302) {
                throw new AssetRenditionsException(String.format("Response of [ %s ] from dispatched response is unacceptable. Unable to stream [ %s ].",
                        assetRenditionDownloadResponse.getStatusCode(), asset.getPath()));
            } else if (assetRenditionDownloadResponse.isRedirect()) {
                return fetchExternalRendition(assetRenditionDownloadResponse.getRedirect());
            } else {
                return new StreamImpl(assetRenditionDownloadResponse.getByteArrayOutputStream(),
                        assetRenditionDownloadResponse.getContentType());
            }
        } catch (ServletException e) {
            throw new AssetRenditionsException(String.format("Unable to fetch internal Asset Rendition output stream for [  %s @ %s ]", asset.getPath(), renditionName));
        } catch (IOException e) {
            throw new AssetRenditionsException(String.format("Unable to fetch external Asset Rendition output stream for [  %s @ %s ]", asset.getPath(), renditionName));
        }
    }

    protected AssetRenditionDownloadResponse sendDispatchForAssetRendition(final SlingHttpServletRequest realRequest,
                                                                         final SlingHttpServletResponse realResponse,
                                                                         final AssetModel asset,
                                                                         final String renditionName) throws IOException, ServletException {

        final AssetRenditionDownloadRequest assetRenditionRequest = new AssetRenditionDownloadRequest(
                realRequest,
                HttpGet.METHOD_NAME,
                asset.getResource(),
                ArrayUtils.EMPTY_STRING_ARRAY,
                AssetRenditionServlet.SERVLET_EXTENSION,
                "/" + renditionName + "/" + AssetRenditionParameters.CACHE_FILENAME);

        final AssetRenditionDownloadResponse assetRenditionResponse = new AssetRenditionDownloadResponse(
                realResponse,
                new StringWriter(),
                new ByteArrayOutputStream());

        for (final AssetRenditionDispatcher assetRenditionDispatcher : assetRenditionDispatchers.getAssetRenditionDispatchers()) {
            if (acceptedByAssetRenditionDispatcher(assetRenditionDispatcher, new AssetRenditionParameters(assetRenditionRequest))) {
                assetRenditionDispatcher.dispatch(assetRenditionRequest, assetRenditionResponse);
                break;
            }
        }

        return assetRenditionResponse;
    }

    protected boolean acceptedByAssetRenditionDispatcher(final AssetRenditionDispatcher assetRenditionDispatcher, final AssetRenditionParameters parameters) {
        if (assetRenditionDispatcher.getRenditionNames() == null ||
                assetRenditionDispatchers == null ||
                StringUtils.isBlank(parameters.getRenditionName())) {
            return false;
        } else {
            return assetRenditionDispatcher.getRenditionNames().contains(parameters.getRenditionName());
        }
    }

    protected AssetRenditionStream fetchExternalRendition(String uri) throws IOException {
        final HttpGet get = new HttpGet(uri);

        try (CloseableHttpClient httpClient = getHttpClient(cfg.http_fetch_timeout())) {
            final CloseableHttpResponse response = httpClient.execute(get);
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();

            IOUtils.copy(response.getEntity().getContent(), baos);
            response.close();

            final Header contentTypeHeader = response.getFirstHeader("Content-Type");
            return new StreamImpl(baos, contentTypeHeader.getValue());
        }
    }

    protected CloseableHttpClient getHttpClient(int timeoutInMilliSeconds) {
        RequestConfig requestConfig = RequestConfig.copy(RequestConfig.DEFAULT)
                .setSocketTimeout(timeoutInMilliSeconds)
                .setConnectTimeout(timeoutInMilliSeconds)
                .setConnectionRequestTimeout(timeoutInMilliSeconds)
                .build();
        return clientBuilderFactory.newBuilder().setDefaultRequestConfig(requestConfig).build();
    }

    public class StreamImpl implements AssetRenditionStream {
        private final ByteArrayOutputStream outputStream;
        private final String contentType;

        public StreamImpl(ByteArrayOutputStream outputStream, String contentType) {
            this.outputStream = outputStream;
            this.contentType = contentType;
        }

        @Override
        public ByteArrayOutputStream getOutputStream() {
            return outputStream;
        }

        @Override
        public String getContentType() {
            return contentType;
        }
    }

    @Activate
    protected void activate(final Cfg cfg) {
        this.cfg = cfg;
    }

    @ObjectClassDefinition(name = "Asset Share Commons - Asset Rendition Streamer")
    public @interface Cfg {
        @AttributeDefinition(
                name = "HTTP (External) Fetch Timeout",
                description = "HTTP Request timeout for external dispatching. Value is in milliseconds. Default is 600000 ms, or 10 minutes."
        )
        int http_fetch_timeout() default 600000;
    }
}
