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

import com.adobe.granite.auth.oauth.AccessTokenProvider;
import com.adobe.granite.crypto.CryptoException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.osgi.services.HttpClientBuilderFactory;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
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
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    private static final String ACCESS_TOKEN_PROVIDER_NAME = "name";
    private static final String DISCOVERY_IMS_CLIENT = "discovery-ims-client";
    private static final Map<String, Object> SERVICE_AUTH_INFO = Collections.<String, Object>singletonMap(
            ResourceResolverFactory.SUBSERVICE, DISCOVERY_IMS_CLIENT);

    @Reference
    private transient HttpClientBuilderFactory clientBuilderFactory;

    @Reference
    private transient ResourceResolverFactory resourceResolverFactory;

    private final transient Map<String, AccessTokenProvider> availableTokenProviders = new ConcurrentHashMap<>();

    private transient Config config;
    private transient String providerName;

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

        final String endpointHost = hostOf(endpoint);
        LOG.debug("Discovery agent request -> host [{}], prompt [{}], context [{}]",
                endpointHost, preview(prompt), preview(context));

        final long startedAt = System.currentTimeMillis();

        try {
            final HttpPost agentRequest = new HttpPost(endpoint);
            agentRequest.setEntity(new StringEntity(toDiscoveryRequestJson(prompt, context), ContentType.APPLICATION_JSON));
            agentRequest.setHeader(HttpHeaders.ACCEPT, ContentType.TEXT_PLAIN.getMimeType());
            applyConfiguredHeaders(agentRequest);

            final String authorization = getAuthorizationHeaderValue();
            if (StringUtils.isNotBlank(authorization)) {
                agentRequest.setHeader(HttpHeaders.AUTHORIZATION, authorization);
            }

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
            }
        } catch (IOException e) {
            LOG.error("Unable to complete a discovery agent request to host [{}] after [{}] ms",
                    endpointHost, System.currentTimeMillis() - startedAt, e);
            sendError(response, SlingHttpServletResponse.SC_BAD_GATEWAY,
                    "The discovery agent request failed.");
        }
    }

    private void applyConfiguredHeaders(final HttpPost request) {
        final String[] configuredHeaders = config.agent_headers();
        if (configuredHeaders == null) {
            return;
        }

        for (String configuredHeader : configuredHeaders) {
            final int separator = StringUtils.indexOf(configuredHeader, ':');
            if (separator > 0 && separator < configuredHeader.length() - 1) {
                request.setHeader(
                        StringUtils.trim(configuredHeader.substring(0, separator)),
                        StringUtils.trim(configuredHeader.substring(separator + 1)));
            }
        }
    }

    private String toDiscoveryRequestJson(final String prompt, final String context) {
        return "{\"prompt\":" + toJsonString(prompt) + ",\"context\":" + context + "}";
    }

    private String toJsonString(final String value) {
        final StringBuilder json = new StringBuilder("\"");
        for (int index = 0; index < value.length(); index++) {
            final char character = value.charAt(index);
            switch (character) {
                case '"':
                    json.append("\\\"");
                    break;
                case '\\':
                    json.append("\\\\");
                    break;
                case '\b':
                    json.append("\\b");
                    break;
                case '\f':
                    json.append("\\f");
                    break;
                case '\n':
                    json.append("\\n");
                    break;
                case '\r':
                    json.append("\\r");
                    break;
                case '\t':
                    json.append("\\t");
                    break;
                default:
                    if (character < 0x20) {
                        json.append(String.format("\\u%04x", (int) character));
                    } else {
                        json.append(character);
                    }
                    break;
            }
        }
        return json.append('"').toString();
    }

    private String getAuthorizationHeaderValue() throws IOException {
        final AccessTokenProvider provider = resolveAccessTokenProvider();
        if (provider == null) {
            return config.agent_authorization();
        }

        try (ResourceResolver resourceResolver =
                     resourceResolverFactory.getServiceResourceResolver(SERVICE_AUTH_INFO)) {
            final String token = provider.getAccessToken(
                    resourceResolver, resourceResolver.getUserID(), null);
            if (StringUtils.isBlank(token)) {
                throw new IOException("AccessTokenProvider returned an empty token.");
            }
            return "Bearer " + token;
        } catch (CryptoException | LoginException e) {
            throw new IOException("Unable to obtain IMS access token.", e);
        }
    }

    private AccessTokenProvider resolveAccessTokenProvider() {
        if (StringUtils.isBlank(providerName)) {
            return null;
        }

        final AccessTokenProvider exactProvider = availableTokenProviders.get(providerName);
        if (exactProvider != null) {
            return exactProvider;
        }

        for (Map.Entry<String, AccessTokenProvider> entry : availableTokenProviders.entrySet()) {
            if (entry.getKey().startsWith(providerName)) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Reference(
            service = AccessTokenProvider.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC
    )
    protected void bindAccessTokenProvider(
            final AccessTokenProvider accessTokenProvider, final Map<String, Object> properties) {
        final Object name = properties.get(ACCESS_TOKEN_PROVIDER_NAME);
        if (name instanceof String && StringUtils.isNotBlank((String) name)) {
            availableTokenProviders.put((String) name, accessTokenProvider);
            LOG.info("AccessTokenProvider (name: {}) added", name);
        }
    }

    protected void unbindAccessTokenProvider(
            final AccessTokenProvider accessTokenProvider, final Map<String, Object> properties) {
        final Object name = properties.get(ACCESS_TOKEN_PROVIDER_NAME);
        if (name instanceof String && StringUtils.isNotBlank((String) name)) {
            availableTokenProviders.remove(name, accessTokenProvider);
            LOG.info("AccessTokenProvider (name: {}) removed", name);
        }
    }

    @Deactivate
    protected void deactivate() {
        availableTokenProviders.clear();
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
    @Modified
    protected void activate(final Config config) {
        this.config = config;
        this.providerName = config.ims_provider_name();
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
                name = "Agent request headers",
                description = "Optional HTTP request headers in Name: Value format."
        )
        String[] agent_headers() default {};

        @AttributeDefinition(
                name = "IMS provider name",
                description = "Name of the IMS AccessTokenProvider used to obtain a discovery-agent bearer token."
        )
        String ims_provider_name() default "Asset Compute";

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
