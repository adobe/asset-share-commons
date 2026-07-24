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

import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.http.HttpHeaders;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.util.EntityUtils;
import org.apache.sling.api.SlingHttpServletResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DiscoveryServletTest {
    @Rule
    public final AemContext context = new AemContext();

    private CloseableHttpClient httpClient;
    private CloseableHttpResponse agentResponse;
    private DiscoveryServlet.Config config;
    private DiscoveryServlet servlet;

    @Before
    public void setUp() throws IOException {
        httpClient = mock(CloseableHttpClient.class);
        agentResponse = mock(CloseableHttpResponse.class);
        config = mock(DiscoveryServlet.Config.class);
        servlet = new DiscoveryServlet() {
            @Override
            protected CloseableHttpClient getHttpClient(final int timeout) {
                return httpClient;
            }
        };

        when(config.agent_endpoint()).thenReturn("https://agent.example/discovery");
        when(config.agent_authorization()).thenReturn("Bearer token");
        when(config.http_timeout()).thenReturn(1000);
        when(config.max_response_bytes()).thenReturn(4096);
        when(httpClient.execute(any(HttpPost.class))).thenReturn(agentResponse);
        servlet.activate(config);
    }

    @Test
    public void proxiesDiscoveryRequestAndResponse() throws IOException {
        final StringEntity entity = new StringEntity(
                "{\"query\":{\"type\":\"dam:Asset\"}}", ContentType.APPLICATION_JSON);
        when(agentResponse.getEntity()).thenReturn(entity);
        when(agentResponse.getStatusLine()).thenReturn(
                new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, "OK"));
        when(agentResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE)).thenReturn(entity.getContentType());
        context.request().addRequestParameter("prompt", "find brand products");
        context.request().addRequestParameter("context", "{\"path\":\"/content/dam\"}");

        servlet.doPost(context.request(), context.response());

        assertEquals(SlingHttpServletResponse.SC_OK, context.response().getStatus());
        assertEquals("application/json; charset=UTF-8", context.response().getContentType());
        assertEquals("{\"query\":{\"type\":\"dam:Asset\"}}", context.response().getOutputAsString());

        final ArgumentCaptor<HttpPost> requestCaptor = ArgumentCaptor.forClass(HttpPost.class);
        verify(httpClient).execute(requestCaptor.capture());
        assertEquals("Bearer token", requestCaptor.getValue().getFirstHeader(HttpHeaders.AUTHORIZATION).getValue());
        assertEquals(
                "prompt=find+brand+products&context=%7B%22path%22%3A%22%2Fcontent%2Fdam%22%7D",
                EntityUtils.toString(requestCaptor.getValue().getEntity()));
    }

    @Test
    public void returnsServiceUnavailableWhenEndpointIsNotConfigured() throws IOException {
        when(config.agent_endpoint()).thenReturn("");
        context.request().addRequestParameter("prompt", "find brand products");
        context.request().addRequestParameter("context", "{}");

        servlet.doPost(context.request(), context.response());

        assertEquals(SlingHttpServletResponse.SC_SERVICE_UNAVAILABLE, context.response().getStatus());
        verify(httpClient, never()).execute(any(HttpPost.class));
    }

    @Test
    public void returnsBadGatewayWhenAgentRequestFails() throws IOException {
        when(httpClient.execute(any(HttpPost.class))).thenThrow(new IOException("connection failed"));
        context.request().addRequestParameter("prompt", "find brand products");
        context.request().addRequestParameter("context", "{}");

        servlet.doPost(context.request(), context.response());

        assertEquals(SlingHttpServletResponse.SC_BAD_GATEWAY, context.response().getStatus());
    }
}
