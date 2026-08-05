package com.adobe.aem.commons.assetshare.util.impl;

import com.adobe.aem.commons.assetshare.util.EmailService;
import com.day.cq.mailer.MessageGateway;
import com.day.cq.mailer.MessageGatewayService;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.commons.mail.ByteArrayDataSource;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.HtmlEmail;
import org.apache.sling.commons.html.HtmlParser;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.xml.sax.ContentHandler;

import javax.activation.DataSource;
import javax.mail.internet.InternetAddress;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EmailServiceImplTest {

    private static final String HTML_TEMPLATE_PATH = "/etc/notification/email/test.html";
    private static final String TEXT_TEMPLATE_PATH = "/etc/notification/email/test.txt";

    @Rule
    public final AemContext ctx = new AemContext(ResourceResolverType.JCR_MOCK);

    @Mock
    private MessageGatewayService messageGatewayService;

    @Mock
    private MessageGateway<Email> messageGateway;

    @Mock
    private HtmlParser htmlParser;

    @Before
    public void setUp() {
        ctx.registerService(MessageGatewayService.class, messageGatewayService);
        ctx.registerService(HtmlParser.class, htmlParser);
    }

    @SuppressWarnings("unchecked")
    private EmailServiceImpl registerService(Object... properties) {
        mockGateway();
        final EmailServiceImpl emailService = new EmailServiceImpl();
        ctx.registerInjectActivateService(emailService, properties);
        return emailService;
    }

    @SuppressWarnings("unchecked")
    private void mockGateway() {
        when(messageGatewayService.getGateway(any(Class.class))).thenReturn((MessageGateway) messageGateway);
    }

    private void createTemplate(String path, String content) {
        ctx.load().binaryFile(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), path);
    }

    // -------------------
    // Argument validation
    // -------------------

    @Test(expected = IllegalArgumentException.class)
    public void sendEmail_stringRecipients_nullRecipients_throws() {
        final EmailService emailService = registerService();
        emailService.sendEmail(HTML_TEMPLATE_PATH, new HashMap<>(), (String[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendEmail_stringRecipients_emptyRecipients_throws() {
        final EmailService emailService = registerService();
        emailService.sendEmail(HTML_TEMPLATE_PATH, new HashMap<>(), new String[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendEmail_internetAddressRecipients_nullRecipients_throws() {
        final EmailService emailService = registerService();
        emailService.sendEmail(HTML_TEMPLATE_PATH, new HashMap<>(), (InternetAddress[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendEmail_internetAddressRecipients_emptyRecipients_throws() {
        final EmailService emailService = registerService();
        emailService.sendEmail(HTML_TEMPLATE_PATH, new HashMap<>(), new InternetAddress[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendEmail_allInvalidStringRecipients_throws() {
        final EmailService emailService = registerService();
        emailService.sendEmail(HTML_TEMPLATE_PATH, new HashMap<>(), "not-a-valid-email-address-@-@-@");
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendEmail_withAttachments_internetAddressRecipients_nullRecipients_throws() {
        final EmailService emailService = registerService();
        emailService.sendEmail(HTML_TEMPLATE_PATH, new HashMap<>(), new HashMap<>(), (InternetAddress[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendEmail_withAttachments_stringRecipients_emptyRecipients_throws() {
        final EmailService emailService = registerService();
        emailService.sendEmail(HTML_TEMPLATE_PATH, new HashMap<>(), new HashMap<>(), new String[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendEmail_withAttachments_allInvalidStringRecipients_throws() {
        final EmailService emailService = registerService();
        emailService.sendEmail(HTML_TEMPLATE_PATH, new HashMap<>(), new HashMap<>(), "not-a-valid-email-address-@-@-@");
    }

    // -------------------
    // Template resolution failure
    // -------------------

    @Test(expected = IllegalArgumentException.class)
    public void sendEmail_templateDoesNotExist_throws() throws Exception {
        final EmailService emailService = registerService();
        emailService.sendEmail("/does/not/exist.html", new HashMap<>(), new InternetAddress("to@test.com"));
    }

    // -------------------
    // Successful sends
    // -------------------

    @Test
    public void sendEmail_simpleTemplate_success() throws Exception {
        createTemplate(TEXT_TEMPLATE_PATH, "Subject: Test\n\nHello ${name}");

        final EmailService emailService = registerService();
        final Map<String, String> params = new HashMap<>();
        params.put("name", "World");

        final List<InternetAddress> failures = emailService.sendEmail(TEXT_TEMPLATE_PATH, params, new InternetAddress("to@test.com"));

        assertTrue(failures.isEmpty());
        verify(messageGateway, times(1)).send(any(Email.class));
    }

    @Test
    public void sendEmail_stringRecipients_success() throws Exception {
        createTemplate(TEXT_TEMPLATE_PATH, "Subject: Test\n\nHello ${name}");

        final EmailService emailService = registerService();
        final Map<String, String> params = new HashMap<>();
        params.put("name", "World");

        final List<String> failures = emailService.sendEmail(TEXT_TEMPLATE_PATH, params, "to@test.com");

        assertTrue(failures.isEmpty());
        verify(messageGateway, times(1)).send(any(Email.class));
    }

    @Test
    public void sendEmail_stringRecipients_mixedValidAndInvalid() throws Exception {
        createTemplate(TEXT_TEMPLATE_PATH, "Subject: Test\n\nHello ${name}");

        final EmailService emailService = registerService();
        final Map<String, String> params = new HashMap<>();
        params.put("name", "World");

        // one invalid address is skipped (logged and ignored), the valid one is still processed
        final List<String> failures = emailService.sendEmail(TEXT_TEMPLATE_PATH, params, "to@test.com", "not-a-valid-@-@-address");

        assertTrue(failures.isEmpty());
        verify(messageGateway, times(1)).send(any(Email.class));
    }

    @Test
    public void sendEmail_multipleRecipients_success() throws Exception {
        createTemplate(TEXT_TEMPLATE_PATH, "Subject: Test\n\nHello ${name}");

        final EmailService emailService = registerService();
        final Map<String, String> params = new HashMap<>();
        params.put("name", "World");

        final List<InternetAddress> failures = emailService.sendEmail(TEXT_TEMPLATE_PATH, params,
                new InternetAddress("to1@test.com"), new InternetAddress("to2@test.com"));

        assertTrue(failures.isEmpty());
        verify(messageGateway, times(2)).send(any(Email.class));
    }

    @Test
    public void sendEmail_withSenderEmailAndName() throws Exception {
        createTemplate(TEXT_TEMPLATE_PATH, "Subject: Test\n\nHello");

        final EmailService emailService = registerService();
        final Map<String, String> params = new HashMap<>();
        params.put(EmailService.SENDER_EMAIL_ADDRESS, "sender@test.com");
        params.put(EmailService.SENDER_NAME, "Sender Name");

        final ArgumentCaptor<Email> captor = ArgumentCaptor.forClass(Email.class);

        final List<InternetAddress> failures = emailService.sendEmail(TEXT_TEMPLATE_PATH, params, new InternetAddress("to@test.com"));

        assertTrue(failures.isEmpty());
        verify(messageGateway, times(1)).send(captor.capture());
        assertEquals("sender@test.com", captor.getValue().getFromAddress().getAddress());
        assertEquals("Sender Name", captor.getValue().getFromAddress().getPersonal());
    }

    @Test
    public void sendEmail_withSenderEmailOnly() throws Exception {
        createTemplate(TEXT_TEMPLATE_PATH, "Subject: Test\n\nHello");

        final EmailService emailService = registerService();
        final Map<String, String> params = new HashMap<>();
        params.put(EmailService.SENDER_EMAIL_ADDRESS, "sender@test.com");

        final ArgumentCaptor<Email> captor = ArgumentCaptor.forClass(Email.class);

        final List<InternetAddress> failures = emailService.sendEmail(TEXT_TEMPLATE_PATH, params, new InternetAddress("to@test.com"));

        assertTrue(failures.isEmpty());
        verify(messageGateway, times(1)).send(captor.capture());
        assertEquals("sender@test.com", captor.getValue().getFromAddress().getAddress());
    }

    @Test
    public void sendEmail_withSubjectAndReplyToParams() throws Exception {
        createTemplate(TEXT_TEMPLATE_PATH, "Subject: Original Subject\n\nHello");

        final EmailService emailService = registerService();
        final Map<String, String> params = new HashMap<>();
        params.put(EmailService.SUBJECT, "Overridden Subject");
        params.put(EmailService.REPLY_TO, "replyto@test.com");

        final ArgumentCaptor<Email> captor = ArgumentCaptor.forClass(Email.class);

        final List<InternetAddress> failures = emailService.sendEmail(TEXT_TEMPLATE_PATH, params, new InternetAddress("to@test.com"));

        assertTrue(failures.isEmpty());
        verify(messageGateway, times(1)).send(captor.capture());
        assertEquals("Overridden Subject", captor.getValue().getSubject());
        assertEquals(1, captor.getValue().getReplyToAddresses().size());
        assertEquals("replyto@test.com", captor.getValue().getReplyToAddresses().get(0).getAddress());
    }

    @Test
    public void sendEmail_withCustomTimeoutConfig() throws Exception {
        createTemplate(TEXT_TEMPLATE_PATH, "Subject: Test\n\nHello");

        // Explicitly configure timeouts to non-default (but still positive) values to exercise
        // the cfg.connectTimeout() > 0 / cfg.socketTimeout() > 0 branches with custom config.
        final EmailService emailService = registerService("connectTimeout", 5000, "socketTimeout", 5000);
        final Map<String, String> params = new HashMap<>();

        final ArgumentCaptor<Email> captor = ArgumentCaptor.forClass(Email.class);
        final List<InternetAddress> failures = emailService.sendEmail(TEXT_TEMPLATE_PATH, params, new InternetAddress("to@test.com"));

        assertTrue(failures.isEmpty());
        verify(messageGateway, times(1)).send(captor.capture());
        assertEquals(5000, captor.getValue().getSocketConnectionTimeout());
        assertEquals(5000, captor.getValue().getSocketTimeout());
    }

    @Test
    public void sendEmail_withZeroTimeoutConfig_doesNotSetTimeouts() throws Exception {
        createTemplate(TEXT_TEMPLATE_PATH, "Subject: Test\n\nHello");

        // Zero/negative timeouts should skip the cfg.connectTimeout()/socketTimeout() calls entirely.
        final EmailService emailService = registerService("connectTimeout", 0, "socketTimeout", 0);
        final Map<String, String> params = new HashMap<>();

        final List<InternetAddress> failures = emailService.sendEmail(TEXT_TEMPLATE_PATH, params, new InternetAddress("to@test.com"));

        assertTrue(failures.isEmpty());
        verify(messageGateway, times(1)).send(any(Email.class));
    }

    @Test
    public void sendEmail_htmlTemplate_withHtmlParser_success() throws Exception {
        createTemplate(HTML_TEMPLATE_PATH, "Subject: Test\n\n<html><body><p>Hello ${name}</p></body></html>");

        // Simulate the HtmlParser invoking its ContentHandler so the resulting plain-text
        // extraction is non-empty (HtmlEmail#setTextMsg(..) rejects blank/empty messages).
        doAnswer(invocation -> {
            final ContentHandler handler = invocation.getArgument(2);
            handler.startElement(null, "body", "body", org.mockito.Mockito.mock(org.xml.sax.Attributes.class));
            handler.characters("Hello World".toCharArray(), 0, "Hello World".length());
            handler.endElement(null, "body", "body");
            return null;
        }).when(htmlParser).parse(any(java.io.InputStream.class), org.mockito.ArgumentMatchers.anyString(), any(ContentHandler.class));

        final EmailService emailService = registerService();
        final Map<String, String> params = new HashMap<>();
        params.put("name", "World");

        final ArgumentCaptor<Email> captor = ArgumentCaptor.forClass(Email.class);
        final List<InternetAddress> failures = emailService.sendEmail(HTML_TEMPLATE_PATH, params, new InternetAddress("to@test.com"));

        assertTrue(failures.isEmpty());
        verify(messageGateway, times(1)).send(captor.capture());
        assertTrue(captor.getValue() instanceof HtmlEmail);
    }

    // -------------------
    // Attachments
    // -------------------

    @Test
    public void sendEmail_withAttachments_internetAddressRecipients_success() throws Exception {
        createTemplate(TEXT_TEMPLATE_PATH, "Subject: Test\n\nHello");

        final EmailService emailService = registerService();
        final Map<String, String> params = new HashMap<>();
        final Map<String, DataSource> attachments = new HashMap<>();
        attachments.put("attachment.txt", new ByteArrayDataSource("some content", "text/plain"));

        final ArgumentCaptor<Email> captor = ArgumentCaptor.forClass(Email.class);
        final List<InternetAddress> failures = emailService.sendEmail(TEXT_TEMPLATE_PATH, params, attachments, new InternetAddress("to@test.com"));

        assertTrue(failures.isEmpty());
        verify(messageGateway, times(1)).send(captor.capture());
        // Attachments force the mail type to HtmlEmail regardless of the template's own extension.
        assertTrue(captor.getValue() instanceof HtmlEmail);
    }

    @Test
    public void sendEmail_withAttachments_stringRecipients_success() throws Exception {
        createTemplate(TEXT_TEMPLATE_PATH, "Subject: Test\n\nHello");

        final EmailService emailService = registerService();
        final Map<String, String> params = new HashMap<>();
        final Map<String, DataSource> attachments = new HashMap<>();
        attachments.put("attachment.txt", new ByteArrayDataSource("some content", "text/plain"));

        final List<String> failures = emailService.sendEmail(TEXT_TEMPLATE_PATH, params, attachments, "to@test.com");

        assertTrue(failures.isEmpty());
        verify(messageGateway, times(1)).send(any(Email.class));
    }

    @Test
    public void sendEmail_withEmptyAttachmentsMap_usesTemplateMailType() throws Exception {
        createTemplate(TEXT_TEMPLATE_PATH, "Subject: Test\n\nHello");

        final EmailService emailService = registerService();
        final Map<String, String> params = new HashMap<>();
        final Map<String, DataSource> attachments = new HashMap<>();

        final ArgumentCaptor<Email> captor = ArgumentCaptor.forClass(Email.class);
        final List<InternetAddress> failures = emailService.sendEmail(TEXT_TEMPLATE_PATH, params, attachments, new InternetAddress("to@test.com"));

        assertTrue(failures.isEmpty());
        verify(messageGateway, times(1)).send(captor.capture());
        assertTrue(captor.getValue() instanceof org.apache.commons.mail.SimpleEmail);
    }

    @Test
    public void sendEmail_withNullAttachmentsMap_usesTemplateMailType() throws Exception {
        createTemplate(TEXT_TEMPLATE_PATH, "Subject: Test\n\nHello");

        final EmailService emailService = registerService();
        final Map<String, String> params = new HashMap<>();

        final List<InternetAddress> failures = emailService.sendEmail(TEXT_TEMPLATE_PATH, params, (Map<String, DataSource>) null, new InternetAddress("to@test.com"));

        assertTrue(failures.isEmpty());
        verify(messageGateway, times(1)).send(any(Email.class));
    }

    // -------------------
    // Per-recipient failure handling
    // -------------------

    @Test
    public void sendEmail_invalidSenderAddress_addsToFailureList() throws Exception {
        createTemplate(TEXT_TEMPLATE_PATH, "Subject: Test\n\nHello");

        final EmailService emailService = registerService();
        final Map<String, String> params = new HashMap<>();
        // An address containing illegal characters triggers an EmailException from Email#setFrom(..)
        params.put(EmailService.SENDER_EMAIL_ADDRESS, "not a valid address");

        final List<InternetAddress> failures = emailService.sendEmail(TEXT_TEMPLATE_PATH, params, new InternetAddress("to@test.com"));

        assertEquals(1, failures.size());
        assertEquals("to@test.com", failures.get(0).getAddress());
    }

    @Test
    public void sendEmail_stringRecipients_invalidSenderAddress_addsToFailureList() throws Exception {
        createTemplate(TEXT_TEMPLATE_PATH, "Subject: Test\n\nHello");

        final EmailService emailService = registerService();
        final Map<String, String> params = new HashMap<>();
        params.put(EmailService.SENDER_EMAIL_ADDRESS, "not a valid address");

        final List<String> failures = emailService.sendEmail(TEXT_TEMPLATE_PATH, params, "to@test.com");

        assertEquals(1, failures.size());
        assertEquals("to@test.com", failures.get(0));
    }

    @Test
    public void sendEmail_withAttachments_internetAddressRecipients_invalidSenderAddress_addsToFailureList() throws Exception {
        createTemplate(TEXT_TEMPLATE_PATH, "Subject: Test\n\nHello");

        final EmailService emailService = registerService();
        final Map<String, String> params = new HashMap<>();
        params.put(EmailService.SENDER_EMAIL_ADDRESS, "not a valid address");
        final Map<String, DataSource> attachments = new HashMap<>();
        attachments.put("attachment.txt", new ByteArrayDataSource("some content", "text/plain"));

        final List<InternetAddress> failures = emailService.sendEmail(TEXT_TEMPLATE_PATH, params, attachments, new InternetAddress("to@test.com"));

        assertEquals(1, failures.size());
        assertEquals("to@test.com", failures.get(0).getAddress());
    }

    @Test
    public void sendEmail_withAttachments_stringRecipients_invalidSenderAddress_addsToFailureList() throws Exception {
        createTemplate(TEXT_TEMPLATE_PATH, "Subject: Test\n\nHello");

        final EmailService emailService = registerService();
        final Map<String, String> params = new HashMap<>();
        params.put(EmailService.SENDER_EMAIL_ADDRESS, "not a valid address");
        final Map<String, DataSource> attachments = new HashMap<>();
        attachments.put("attachment.txt", new ByteArrayDataSource("some content", "text/plain"));

        final List<String> failures = emailService.sendEmail(TEXT_TEMPLATE_PATH, params, attachments, "to@test.com");

        assertEquals(1, failures.size());
        assertEquals("to@test.com", failures.get(0));
    }

    @Test
    public void sendEmail_resourceResolverFactoryLoginException_propagatesAsNullPointerException() throws Exception {
        // NOTE (see final report): getMailTemplate(..) swallows LoginException and returns null
        // instead of raising a caller-facing error. That null EmailTemplate is then dereferenced in
        // getEmail(..) (mailTemplate.getEmail(...)), so the actual observed failure mode is a raw
        // NullPointerException rather than a clean/handled error. This test documents that current
        // (arguably buggy) behavior rather than asserting a "nicer" outcome that the code doesn't
        // actually provide.
        final org.apache.sling.api.resource.ResourceResolverFactory throwingFactory =
                mock(org.apache.sling.api.resource.ResourceResolverFactory.class);
        when(throwingFactory.getServiceResourceResolver(org.mockito.ArgumentMatchers.anyMap()))
                .thenThrow(new org.apache.sling.api.resource.LoginException("no login for you"));
        ctx.registerService(org.apache.sling.api.resource.ResourceResolverFactory.class, throwingFactory,
                org.osgi.framework.Constants.SERVICE_RANKING, Integer.MAX_VALUE);

        createTemplate(TEXT_TEMPLATE_PATH, "Subject: Test\n\nHello");

        final EmailService emailService = registerService();
        final Map<String, String> params = new HashMap<>();

        try {
            emailService.sendEmail(TEXT_TEMPLATE_PATH, params, new InternetAddress("to@test.com"));
            org.junit.Assert.fail("Expected a NullPointerException due to the swallowed LoginException (see note above)");
        } catch (NullPointerException expected) {
            // documents current behavior; see note above.
        }
    }
}
