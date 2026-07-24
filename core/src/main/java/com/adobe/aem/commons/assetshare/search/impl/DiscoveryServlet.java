/*
 * Asset Share Commons
 *
 * Copyright [2026] Adobe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.adobe.aem.commons.assetshare.search.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.osgi.services.HttpClientBuilderFactory;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.methods=POST",
                "sling.servlet.paths=/bin/asset-share-commons/discovery"
        }
)
@Designate(ocd = DiscoveryServlet.Config.class)
public class DiscoveryServlet extends SlingAllMethodsServlet {
    private static final Logger LOG = LoggerFactory.getLogger(DiscoveryServlet.class);
    private static final int BUFFER_SIZE = 8192;
    private static final int LOG_PREVIEW_MAX = 2000;

    @Reference
    private transient HttpClientBuilderFactory clientBuilderFactory;

    private transient Config config;

    @Override
    protected void doPost(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
            throws IOException {
        final String endpoint = config.agent_endpoint();
        final String prompt = request.getParameter("prompt");
        final String context = request.getParameter("context");

        if (!isValidEndpoint(endpoint)) {
            LOG.warn("Discovery agent endpoint is not configured or is invalid; returning 503");
            sendError(response, SlingHttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Discovery search is not configured.");
            return;
        }

        if (StringUtils.isBlank(prompt) || StringUtils.isBlank(context)) {
            LOG.debug("Discovery request rejected (400): promptBlank={}, contextBlank={}",
                    StringUtils.isBlank(prompt), StringUtils.isBlank(context));
            sendError(response, SlingHttpServletResponse.SC_BAD_REQUEST,
                    "The prompt and context parameters are required.");
            return;
        }

        final HttpPost agentRequest = new HttpPost(endpoint);
        final List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("prompt", prompt));
        parameters.add(new BasicNameValuePair("context", context));
        agentRequest.setEntity(new UrlEncodedFormEntity(parameters, StandardCharsets.UTF_8));

        if (StringUtils.isNotBlank(config.agent_authorization())) {
            agentRequest.setHeader(HttpHeaders.AUTHORIZATION, config.agent_authorization());
        }

        final String endpointHost = hostOf(endpoint);
        LOG.debug("Discovery agent request -> host [{}], prompt [{}], context [{}]",
                endpointHost, preview(prompt), preview(context));

        final long startedAt = System.currentTimeMillis();

        try (CloseableHttpClient client = getHttpClient(config.http_timeout());
             CloseableHttpResponse agentResponse = client.execute(agentRequest)) {
            final HttpEntity entity = agentResponse.getEntity();
            final byte[] responseBody = entity == null
                    ? new byte[0]
                    : readResponseBody(entity.getContent(), Math.max(1, config.max_response_bytes()));

            final int agentStatus = agentResponse.getStatusLine().getStatusCode();
            final String agentContentType = agentResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE) != null
                    ? agentResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue()
                    : null;

            LOG.debug("Discovery agent response <- host [{}], status [{}], contentType [{}], bytes [{}], elapsedMs [{}], body [{}]",
                    endpointHost, agentStatus, agentContentType, responseBody.length,
                    System.currentTimeMillis() - startedAt,
                    preview(new String(responseBody, StandardCharsets.UTF_8)));

            response.setStatus(agentStatus);
            if (agentContentType != null) {
                response.setContentType(agentContentType);
            }
            response.getOutputStream().write(responseBody);
        } catch (IOException e) {
            LOG.error("Unable to complete a discovery agent request to host [{}] after [{}] ms",
                    endpointHost, System.currentTimeMillis() - startedAt, e);
            sendError(response, SlingHttpServletResponse.SC_BAD_GATEWAY,
                    "The discovery agent request failed.");
        }
    }

    protected CloseableHttpClient getHttpClient(final int timeout) {
        final RequestConfig requestConfig = RequestConfig.copy(RequestConfig.DEFAULT)
                .setSocketTimeout(Math.max(1, timeout))
                .setConnectTimeout(Math.max(1, timeout))
                .setConnectionRequestTimeout(Math.max(1, timeout))
                .build();
        return clientBuilderFactory.newBuilder().setDefaultRequestConfig(requestConfig).build();
    }

    private byte[] readResponseBody(final InputStream inputStream, final int maximumBytes) throws IOException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final byte[] buffer = new byte[BUFFER_SIZE];
        int totalBytes = 0;
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) > -1) {
            totalBytes += bytesRead;
            if (totalBytes > maximumBytes) {
                throw new IOException("Discovery agent response exceeded the configured maximum size.");
            }
            outputStream.write(buffer, 0, bytesRead);
        }

        return outputStream.toByteArray();
    }

    private static String preview(final String value) {
        if (value == null) {
            return "";
        }
        return StringUtils.abbreviate(value, LOG_PREVIEW_MAX);
    }

    private String hostOf(final String endpoint) {
        try {
            return URI.create(endpoint).getHost();
        } catch (IllegalArgumentException e) {
            return "unknown";
        }
    }

    private boolean isValidEndpoint(final String endpoint) {
        if (StringUtils.isBlank(endpoint)) {
            return false;
        }

        try {
            final URI uri = new URI(endpoint);
            return ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                    && StringUtils.isNotBlank(uri.getHost())
                    && uri.getUserInfo() == null;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private void sendError(final SlingHttpServletResponse response, final int status, final String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }

    @Activate
    protected void activate(final Config config) {
        this.config = config;
    }

    @ObjectClassDefinition(name = "Asset Share Commons - Discovery Agent Proxy")
    public @interface Config {
        @AttributeDefinition(
                name = "Agent endpoint",
                description = "Absolute HTTP endpoint that accepts prompt and context form fields."
        )
        String agent_endpoint() default "";

        @AttributeDefinition(
                name = "Agent authorization header",
                description = "Optional complete Authorization header value sent only to the configured endpoint.",
                type = AttributeType.PASSWORD
        )
        String agent_authorization() default "";

        @AttributeDefinition(
                name = "HTTP timeout",
                description = "Connection and response timeout in milliseconds."
        )
        int http_timeout() default 30000;

        @AttributeDefinition(
                name = "Maximum response size",
                description = "Maximum accepted discovery response size in bytes."
        )
        int max_response_bytes() default 1048576;
    }
}
