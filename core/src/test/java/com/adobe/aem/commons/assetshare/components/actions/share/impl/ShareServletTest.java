/*
 * Asset Share Commons
 *
 * Copyright (C) 2017 Adobe
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

package com.adobe.aem.commons.assetshare.components.actions.share.impl;

import com.adobe.aem.commons.assetshare.components.actions.share.ShareException;
import com.adobe.aem.commons.assetshare.components.actions.share.ShareService;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ShareServletTest {

    private static final String EMAIL_SHARE_SERVICE_COMPONENT_NAME =
            "com.adobe.aem.commons.assetshare.components.actions.share.impl.EmailShareServiceImpl";

    @Rule
    public final AemContext ctx = new AemContext();

    @Mock
    private ShareService defaultShareService;

    @Mock
    private ShareService acceptingShareService;

    @Mock
    private ShareService nonAcceptingShareService;

    private Servlet servlet;

    @Before
    public void setUp() {
        ctx.request().setMethod("POST");

        final Map<String, Object> defaultServiceProps = new HashMap<>();
        defaultServiceProps.put("component.name", EMAIL_SHARE_SERVICE_COMPONENT_NAME);
        ctx.registerService(ShareService.class, defaultShareService, defaultServiceProps);
    }

    private Servlet registerServlet() {
        return ctx.registerInjectActivateService(new ShareServlet());
    }

    @Test
    public void doPost_acceptingServiceHandlesShare_defaultNotInvoked() throws Exception {
        doReturn(true).when(acceptingShareService).accepts(ctx.request());
        ctx.registerService(ShareService.class, acceptingShareService);

        doReturn(false).when(nonAcceptingShareService).accepts(ctx.request());
        ctx.registerService(ShareService.class, nonAcceptingShareService);

        servlet = registerServlet();
        servlet.service(ctx.request(), ctx.response());

        verify(acceptingShareService).share(any(), any(), any());
        verify(nonAcceptingShareService, never()).share(any(), any(), any());
        verify(defaultShareService, never()).share(any(), any(), any());
    }

    @Test
    public void doPost_noAcceptingService_usesDefault() throws Exception {
        doReturn(false).when(nonAcceptingShareService).accepts(ctx.request());
        ctx.registerService(ShareService.class, nonAcceptingShareService);

        servlet = registerServlet();
        servlet.service(ctx.request(), ctx.response());

        verify(defaultShareService).share(any(), any(), any());
        assertEquals(200, ctx.response().getStatus());
    }

    @Test
    public void doPost_acceptingServiceThrowsShareException_stillInvokesDefaultAndSwallowsError() throws Exception {
        doReturn(true).when(acceptingShareService).accepts(ctx.request());
        doThrow(new ShareException("boom")).when(acceptingShareService).share(any(), any(), any());
        ctx.registerService(ShareService.class, acceptingShareService);

        servlet = registerServlet();
        servlet.service(ctx.request(), ctx.response());

        verify(acceptingShareService).share(any(), any(), any());
        // since the accepting service threw, the counter never incremented so the default is still invoked
        verify(defaultShareService).share(any(), any(), any());
    }

    @Test
    public void doPost_defaultServiceThrowsShareException_setsInternalServerError() throws Exception {
        doThrow(new ShareException("boom")).when(defaultShareService).share(any(), any(), any());

        servlet = registerServlet();
        servlet.service(ctx.request(), ctx.response());

        assertEquals(500, ctx.response().getStatus());
    }

    @Test
    public void doPost_acceptingServiceSucceeds_defaultNotInvokedEvenOnEmptyCollection() throws Exception {
        doReturn(true).when(acceptingShareService).accepts(ctx.request());
        ctx.registerService(ShareService.class, acceptingShareService);

        servlet = registerServlet();
        servlet.service(ctx.request(), ctx.response());

        verify(acceptingShareService, times(1)).share(any(), any(), any());
        verify(defaultShareService, never()).share(any(), any(), any());
    }
}
