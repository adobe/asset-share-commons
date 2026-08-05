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

import com.adobe.aem.commons.assetshare.components.actions.share.EmailShare;
import com.adobe.aem.commons.assetshare.components.actions.share.ShareException;
import com.adobe.aem.commons.assetshare.components.actions.share.ShareService;
import com.adobe.aem.commons.assetshare.configuration.AssetDetailsResolver;
import com.adobe.aem.commons.assetshare.configuration.Config;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.testing.RequireAemMock;
import com.adobe.aem.commons.assetshare.util.EmailService;
import com.adobe.aem.commons.assetshare.util.RequireAem;
import com.adobe.granite.security.user.UserProperties;
import com.adobe.granite.security.user.UserPropertiesManager;
import com.day.cq.commons.Externalizer;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.wrappers.SlingHttpServletRequestWrapper;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.models.factory.ModelFactory;
import org.apache.sling.xss.XSSAPI;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.jcr.RepositoryException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class EmailShareServiceImplTest {

    @Rule
    public final AemContext ctx = new AemContext();

    @Mock
    private EmailService emailService;

    @Mock
    private Externalizer externalizer;

    @Mock
    private AssetDetailsResolver assetDetailsResolver;

    @Mock
    private ModelFactory modelFactory;

    @Mock
    private XSSAPI xssAPI;

    @Mock
    private UserPropertiesManager userPropertiesManager;

    @Mock
    private Authorizable authorizable;

    @Mock
    private UserProperties userProperties;

    @Mock
    private Config config;

    @Mock
    private EmailShare emailShare;

    @Mock
    private AssetModel assetModel1;

    @Mock
    private AssetModel assetModel2;

    private ShareService shareService;

    @Before
    public void setUp() throws RepositoryException {
        ctx.load().json("/com/adobe/aem/commons/assetshare/components/actions/share/impl/EmailShareServiceImplTest.json", "/content/dam");

        RequireAemMock.setAem(ctx, RequireAem.Distribution.CLOUD_READY, RequireAem.ServiceType.AUTHOR);

        ctx.registerService(EmailService.class, emailService, org.osgi.framework.Constants.SERVICE_RANKING, Integer.MAX_VALUE);
        ctx.registerService(Externalizer.class, externalizer, org.osgi.framework.Constants.SERVICE_RANKING, Integer.MAX_VALUE);
        ctx.registerService(AssetDetailsResolver.class, assetDetailsResolver, org.osgi.framework.Constants.SERVICE_RANKING, Integer.MAX_VALUE);
        ctx.registerService(ModelFactory.class, modelFactory, org.osgi.framework.Constants.SERVICE_RANKING, Integer.MAX_VALUE);
        ctx.registerService(XSSAPI.class, xssAPI, org.osgi.framework.Constants.SERVICE_RANKING, Integer.MAX_VALUE);

        // Valid (non-anonymous, non-admin) user by default; wire up UserPropertiesManager/Authorizable so
        // getUserProperties(...) doesn't NPE.
        ctx.registerAdapter(ResourceResolver.class, UserPropertiesManager.class, userPropertiesManager);
        ctx.registerAdapter(ResourceResolver.class, Authorizable.class, authorizable);
        doReturn(userProperties).when(userPropertiesManager).getUserProperties(eq(authorizable), eq("profile"));
        doReturn("jsmith").when(userProperties).getAuthorizableID();

        ctx.registerAdapter(org.apache.sling.api.SlingHttpServletRequest.class, Config.class, config);
        doReturn(ctx.resourceResolver()).when(config).getResourceResolver();
        doReturn(ctx.request()).when(config).getRequest();
        doReturn(new ValueMapDecorator(new HashMap<>())).when(config).getProperties();

        ctx.registerAdapter(org.apache.sling.api.SlingHttpServletRequest.class, EmailShare.class, emailShare);
        doReturn(new ValueMapDecorator(new HashMap<>())).when(emailShare).getProperties();
        doReturn(new ValueMapDecorator(new HashMap<>())).when(emailShare).getConfiguredData();
        doReturn(new ValueMapDecorator(new HashMap<>())).when(emailShare).getUserData();
        doReturn(null).when(emailShare).getEmailTemplatePath();

        doReturn("Asset One").when(assetModel1).getTitle();
        doReturn("Asset Two").when(assetModel2).getTitle();

        doReturn(assetModel1).when(modelFactory).getModelFromWrappedRequest(any(), argThatPath("/content/dam/asset-1.jpg"), eq(AssetModel.class));
        doReturn(assetModel2).when(modelFactory).getModelFromWrappedRequest(any(), argThatPath("/content/dam/asset-2.jpg"), eq(AssetModel.class));

        doReturn("http://author.example.com/details/asset-1.jpg").when(assetDetailsResolver).getFullUrl(eq(config), eq(assetModel1));
        doReturn("http://author.example.com/details/asset-2.jpg").when(assetDetailsResolver).getFullUrl(eq(config), eq(assetModel2));

        doReturn("http://author.example.com/details/asset-1.jpg").when(externalizer).authorLink(any(), anyString());
        doReturn("http://publish.example.com/details/asset-1.jpg").when(externalizer).externalLink(any(), anyString(), anyString());

        doReturn(Collections.emptyList()).when(emailService).sendEmail(anyString(), any(Map.class), any(String[].class));

        shareService = ctx.registerInjectActivateService(new EmailShareServiceImpl(),
                new HashMap<String, Object>() {{
                    put("emailTemplate", "/etc/notification/email/asset-share-commons/share/default.html");
                    put("signature", "Your Assets Team");
                    put("externalizerDomain", "publish");
                }});
    }

    private Resource argThatPath(final String path) {
        return org.mockito.ArgumentMatchers.argThat(resource -> resource != null && path.equals(resource.getPath()));
    }

    @Test
    public void accepts_true() {
        ctx.request().setParameterMap(Collections.singletonMap(EmailShareServiceImpl.SHARE_SERVICE_ACCEPTANCE_KEY, "true"));
        assertTrue(shareService.accepts(ctx.request()));
    }

    @Test
    public void accepts_false() {
        assertFalse(shareService.accepts(ctx.request()));
    }

    @Test
    public void accepts_falseWhenParamNotTrue() {
        ctx.request().setParameterMap(Collections.singletonMap(EmailShareServiceImpl.SHARE_SERVICE_ACCEPTANCE_KEY, "false"));
        assertFalse(shareService.accepts(ctx.request()));
    }

    @Test
    public void share_missingEmailAddresses_throwsShareException() {
        final Map<String, Object> params = new HashMap<>();
        params.put("path", new String[]{"/content/dam/asset-1.jpg"});

        try {
            shareService.share(ctx.request(), ctx.response(), new ValueMapDecorator(params));
            fail("Expected ShareException");
        } catch (ShareException e) {
            assertEquals("At least one e-mail address is required to share", e.getMessage());
        }
    }

    @Test
    public void share_missingAssetPaths_throwsShareException() {
        final Map<String, Object> params = new HashMap<>();
        params.put("email", "someone@example.com");

        try {
            shareService.share(ctx.request(), ctx.response(), new ValueMapDecorator(params));
            fail("Expected ShareException");
        } catch (ShareException e) {
            assertEquals("At least one asset is required to share", e.getMessage());
        }
    }

    @Test
    public void share_success_sendsEmailWithAssetLinksHtml_authorMode() throws ShareException {
        final Map<String, Object> params = new HashMap<>();
        params.put("email", "one@example.com,two@example.com");
        params.put("path", new String[]{"/content/dam/asset-1.jpg", "/content/dam/asset-2.jpg"});

        shareService.share(ctx.request(), ctx.response(), new ValueMapDecorator(params));

        final ArgumentCaptor<Map> emailParamsCaptor = ArgumentCaptor.forClass(Map.class);
        final ArgumentCaptor<String[]> addressesCaptor = ArgumentCaptor.forClass(String[].class);
        verify(emailService).sendEmail(eq("/etc/notification/email/asset-share-commons/share/default.html"),
                emailParamsCaptor.capture(), addressesCaptor.capture());

        assertEquals(2, addressesCaptor.getValue().length);
        assertTrue(((String) emailParamsCaptor.getValue().get("assetLinksHTML")).contains("Asset One"));
        assertTrue(((String) emailParamsCaptor.getValue().get("assetLinksHTML")).contains("Asset Two"));
        verify(externalizer, org.mockito.Mockito.times(2)).authorLink(any(), anyString());
        verify(externalizer, org.mockito.Mockito.times(0)).externalLink(any(), anyString(), anyString());
    }

    @Test
    public void share_success_publishMode_usesExternalLink() throws ShareException {
        // Override the AUTHOR RequireAem registered in setUp() with a higher-ranked PUBLISH one.
        ctx.registerService(RequireAem.class, new RequireAem() {
            @Override
            public Distribution getDistribution() {
                return Distribution.CLOUD_READY;
            }

            @Override
            public ServiceType getServiceType() {
                return ServiceType.PUBLISH;
            }
        }, org.osgi.framework.Constants.SERVICE_RANKING, 2);

        // re-register the service so it picks up the higher-ranked (PUBLISH) RequireAem service
        shareService = ctx.registerInjectActivateService(new EmailShareServiceImpl(),
                new HashMap<String, Object>() {{
                    put("emailTemplate", "/etc/notification/email/asset-share-commons/share/default.html");
                    put("signature", "Your Assets Team");
                    put("externalizerDomain", "publish");
                }});

        final Map<String, Object> params = new HashMap<>();
        params.put("email", "one@example.com");
        params.put("path", new String[]{"/content/dam/asset-1.jpg"});

        shareService.share(ctx.request(), ctx.response(), new ValueMapDecorator(params));

        verify(externalizer).externalLink(any(), eq("publish"), anyString());
    }

    @Test
    public void share_sendEmailFailure_throwsShareException() {
        doReturn(Collections.singletonList("bad@example.com")).when(emailService).sendEmail(anyString(), any(Map.class), any(String[].class));

        final Map<String, Object> params = new HashMap<>();
        params.put("email", "bad@example.com");
        params.put("path", new String[]{"/content/dam/asset-1.jpg"});

        try {
            shareService.share(ctx.request(), ctx.response(), new ValueMapDecorator(params));
            fail("Expected ShareException");
        } catch (ShareException e) {
            assertTrue(e.getMessage().contains("bad@example.com"));
        }
    }

    @Test
    public void share_assetDetailsUrlBlank_assetSkippedInHtml() throws ShareException {
        doReturn("").when(assetDetailsResolver).getFullUrl(eq(config), eq(assetModel1));

        final Map<String, Object> params = new HashMap<>();
        params.put("email", "one@example.com");
        params.put("path", new String[]{"/content/dam/asset-1.jpg"});

        shareService.share(ctx.request(), ctx.response(), new ValueMapDecorator(params));

        final ArgumentCaptor<Map> emailParamsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(emailService).sendEmail(anyString(), emailParamsCaptor.capture(), any(String[].class));

        assertFalse(((String) emailParamsCaptor.getValue().get("assetLinksHTML")).contains("<li>"));
    }

    @Test
    public void share_unresolvableAssetPathsAreFilteredOut_throwsWhenNoneRemain() {
        final Map<String, Object> params = new HashMap<>();
        params.put("email", "one@example.com");
        params.put("path", new String[]{"/content/dam/does-not-exist.jpg"});

        try {
            shareService.share(ctx.request(), ctx.response(), new ValueMapDecorator(params));
            fail("Expected ShareException");
        } catch (ShareException e) {
            assertEquals("At least one asset is required to share", e.getMessage());
        }
    }

    @Test
    public void share_signatureFromConfiguredData() throws ShareException {
        doReturn(new ValueMapDecorator(Collections.singletonMap(EmailShareImpl.PN_SIGNATURE, "Configured Signature")))
                .when(emailShare).getConfiguredData();

        final Map<String, Object> params = new HashMap<>();
        params.put("email", "one@example.com");
        params.put("path", new String[]{"/content/dam/asset-1.jpg"});

        shareService.share(ctx.request(), ctx.response(), new ValueMapDecorator(params));

        final ArgumentCaptor<Map> emailParamsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(emailService).sendEmail(anyString(), emailParamsCaptor.capture(), any(String[].class));
        assertEquals("Configured Signature", emailParamsCaptor.getValue().get("signature"));
    }

    @Test
    public void share_signatureDefaultsToCfgWhenNoConfiguredData() throws ShareException {
        final Map<String, Object> params = new HashMap<>();
        params.put("email", "one@example.com");
        params.put("path", new String[]{"/content/dam/asset-1.jpg"});

        shareService.share(ctx.request(), ctx.response(), new ValueMapDecorator(params));

        final ArgumentCaptor<Map> emailParamsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(emailService).sendEmail(anyString(), emailParamsCaptor.capture(), any(String[].class));
        assertEquals("Your Assets Team", emailParamsCaptor.getValue().get("signature"));
    }

    @Test
    public void share_signatureFromSharerDisplayName() throws Exception {
        doReturn(new ValueMapDecorator(Collections.singletonMap(EmailShareImpl.PN_USE_SHARER_NAME_AS_SIGNATURE, (Object) true)))
                .when(emailShare).getProperties();
        doReturn("Jane Smith").when(userProperties).getDisplayName();

        final Map<String, Object> params = new HashMap<>();
        params.put("email", "one@example.com");
        params.put("path", new String[]{"/content/dam/asset-1.jpg"});

        shareService.share(ctx.request(), ctx.response(), new ValueMapDecorator(params));

        final ArgumentCaptor<Map> emailParamsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(emailService).sendEmail(anyString(), emailParamsCaptor.capture(), any(String[].class));
        assertEquals("Jane Smith", emailParamsCaptor.getValue().get("signature"));
    }

    @Test
    public void share_signatureRepositoryExceptionWrapped() throws Exception {
        doReturn(new ValueMapDecorator(Collections.singletonMap(EmailShareImpl.PN_USE_SHARER_NAME_AS_SIGNATURE, (Object) true)))
                .when(emailShare).getProperties();
        doThrow(new RepositoryException("boom")).when(userProperties).getDisplayName();

        final Map<String, Object> params = new HashMap<>();
        params.put("email", "one@example.com");
        params.put("path", new String[]{"/content/dam/asset-1.jpg"});

        try {
            shareService.share(ctx.request(), ctx.response(), new ValueMapDecorator(params));
            fail("Expected ShareException");
        } catch (ShareException e) {
            assertTrue(e.getMessage().contains("jsmith"));
        }
    }

    @Test
    public void share_replyToFromSharer() throws Exception {
        doReturn(new ValueMapDecorator(Collections.singletonMap(EmailShare.PN_USE_SHARER_EMAIL_AS_REPLY_TO, (Object) true)))
                .when(emailShare).getProperties();
        doReturn("jane@example.com").when(userProperties).getProperty(UserProperties.EMAIL);

        final Map<String, Object> params = new HashMap<>();
        params.put("email", "one@example.com");
        params.put("path", new String[]{"/content/dam/asset-1.jpg"});

        shareService.share(ctx.request(), ctx.response(), new ValueMapDecorator(params));

        final ArgumentCaptor<Map> emailParamsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(emailService).sendEmail(anyString(), emailParamsCaptor.capture(), any(String[].class));
        assertEquals("jane@example.com", emailParamsCaptor.getValue().get(EmailService.REPLY_TO));
    }

    @Test
    public void share_replyToRepositoryExceptionWrapped() throws Exception {
        doReturn(new ValueMapDecorator(Collections.singletonMap(EmailShare.PN_USE_SHARER_EMAIL_AS_REPLY_TO, (Object) true)))
                .when(emailShare).getProperties();
        doThrow(new RepositoryException("boom")).when(userProperties).getProperty(UserProperties.EMAIL);

        final Map<String, Object> params = new HashMap<>();
        params.put("email", "one@example.com");
        params.put("path", new String[]{"/content/dam/asset-1.jpg"});

        try {
            shareService.share(ctx.request(), ctx.response(), new ValueMapDecorator(params));
            fail("Expected ShareException");
        } catch (ShareException e) {
            assertTrue(e.getMessage().contains("jsmith"));
        }
    }

    @Test
    public void share_trackingParametersAppendedToUrl() throws ShareException {
        final Map<String, Object> configProps = new HashMap<>();
        configProps.put("trackingName", "utm_source");
        configProps.put("trackingValue", "email-share");
        doReturn(new ValueMapDecorator(configProps)).when(config).getProperties();

        final Map<String, Object> params = new HashMap<>();
        params.put("email", "one@example.com");
        params.put("path", new String[]{"/content/dam/asset-1.jpg"});

        shareService.share(ctx.request(), ctx.response(), new ValueMapDecorator(params));

        final ArgumentCaptor<Map> emailParamsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(emailService).sendEmail(anyString(), emailParamsCaptor.capture(), any(String[].class));
        assertTrue(((String) emailParamsCaptor.getValue().get("assetLinksHTML")).contains("utm_source=email-share"));
    }

    @Test
    public void share_customEmailTemplatePathFromEmailShare() throws ShareException {
        doReturn("/etc/notification/email/custom.html").when(emailShare).getEmailTemplatePath();

        final Map<String, Object> params = new HashMap<>();
        params.put("email", "one@example.com");
        params.put("path", new String[]{"/content/dam/asset-1.jpg"});

        shareService.share(ctx.request(), ctx.response(), new ValueMapDecorator(params));

        verify(emailService).sendEmail(eq("/etc/notification/email/custom.html"), any(Map.class), any(String[].class));
    }

    @Test
    public void share_userDataIsXssProtected() throws ShareException {
        doReturn("cleaned-message").when(xssAPI).encodeForHTML("<script>hi</script>");
        doReturn("cleaned-tag").when(xssAPI).encodeForHTML("<b>tag</b>");

        final Map<String, Object> userData = new HashMap<>();
        userData.put("message", "<script>hi</script>");
        userData.put("tags", new String[]{"<b>tag</b>"});
        doReturn(new ValueMapDecorator(userData)).when(emailShare).getUserData();

        final Map<String, Object> params = new HashMap<>();
        params.put("email", "one@example.com");
        params.put("path", new String[]{"/content/dam/asset-1.jpg"});

        shareService.share(ctx.request(), ctx.response(), new ValueMapDecorator(params));

        final ArgumentCaptor<Map> emailParamsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(emailService).sendEmail(anyString(), emailParamsCaptor.capture(), any(String[].class));
        assertEquals("cleaned-message", emailParamsCaptor.getValue().get("message"));

        verify(xssAPI).encodeForHTML("<script>hi</script>");
        verify(xssAPI).encodeForHTML("<b>tag</b>");
    }

    @Test
    public void share_adminResourceResolverUser_skipsUserPropertiesLookup() throws Exception {
        final ResourceResolver spiedResolver = org.mockito.Mockito.spy(ctx.resourceResolver());
        doReturn("admin").when(spiedResolver).getUserID();

        final SlingHttpServletRequest adminRequest = new SlingHttpServletRequestWrapper(ctx.request()) {
            @Override
            public ResourceResolver getResourceResolver() {
                return spiedResolver;
            }
        };

        // Even with the "use sharer name as signature" flag on, since the user is "admin" the
        // UserProperties lookup is skipped entirely and the configured/default signature is used.
        doReturn(new ValueMapDecorator(Collections.singletonMap(EmailShareImpl.PN_USE_SHARER_NAME_AS_SIGNATURE, (Object) true)))
                .when(emailShare).getProperties();

        final Map<String, Object> params = new HashMap<>();
        params.put("email", "one@example.com");
        params.put("path", new String[]{"/content/dam/asset-1.jpg"});

        shareService.share(adminRequest, ctx.response(), new ValueMapDecorator(params));

        verify(userPropertiesManager, org.mockito.Mockito.never()).getUserProperties(any(Authorizable.class), anyString());

        final ArgumentCaptor<Map> emailParamsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(emailService).sendEmail(anyString(), emailParamsCaptor.capture(), any(String[].class));
        assertEquals("Your Assets Team", emailParamsCaptor.getValue().get("signature"));
    }

    @Test
    public void share_getUserPropertiesRepositoryException_isCaughtAndTreatedAsNoProfile() throws Exception {
        doThrow(new RepositoryException("boom")).when(userPropertiesManager).getUserProperties(eq(authorizable), eq("profile"));

        final Map<String, Object> params = new HashMap<>();
        params.put("email", "one@example.com");
        params.put("path", new String[]{"/content/dam/asset-1.jpg"});

        // Should not throw; the RepositoryException is caught internally and treated as "no profile".
        shareService.share(ctx.request(), ctx.response(), new ValueMapDecorator(params));

        final ArgumentCaptor<Map> emailParamsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(emailService).sendEmail(anyString(), emailParamsCaptor.capture(), any(String[].class));
        assertEquals("Your Assets Team", emailParamsCaptor.getValue().get("signature"));
    }

    @Test
    public void share_uriSyntaxExceptionBuildingTrackingUrl_isCaughtAndUrlUsedAsIs() throws ShareException {
        final Map<String, Object> configProps = new HashMap<>();
        configProps.put("trackingName", "utm_source");
        configProps.put("trackingValue", "email-share");
        doReturn(new ValueMapDecorator(configProps)).when(config).getProperties();

        // A URL with an unescaped space is not a valid URI and will cause URIBuilder to throw
        // a URISyntaxException, which the production code should catch and log, leaving the
        // original (unmodified) url in place.
        doReturn("http://author.example.com/details/asset one.jpg").when(externalizer).authorLink(any(), anyString());

        final Map<String, Object> params = new HashMap<>();
        params.put("email", "one@example.com");
        params.put("path", new String[]{"/content/dam/asset-1.jpg"});

        shareService.share(ctx.request(), ctx.response(), new ValueMapDecorator(params));

        final ArgumentCaptor<Map> emailParamsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(emailService).sendEmail(anyString(), emailParamsCaptor.capture(), any(String[].class));
        assertTrue(((String) emailParamsCaptor.getValue().get("assetLinksHTML")).contains("asset one.jpg"));
    }
}
