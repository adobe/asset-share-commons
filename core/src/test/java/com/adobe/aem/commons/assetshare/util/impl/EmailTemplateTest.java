package com.adobe.aem.commons.assetshare.util.impl;

import com.day.cq.commons.jcr.JcrConstants;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.SimpleEmail;
import org.apache.sling.commons.html.HtmlParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.nodetype.NodeType;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EmailTemplateTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    private Object getFieldValue(Object target, Class<?> declaringClass, String fieldName) throws Exception {
        final Field field = declaringClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    @Test
    public void testReplace_singleVariable() {
        Map<String, String> vars = new HashMap<>();
        vars.put("name", "World");
        String result = EmailTemplate.SimpleSubstitutor.replace("Hello, ${name}!", vars);
        assertEquals("Hello, World!", result);
    }

    @Test
    public void testReplace_multipleVariables() {
        Map<String, String> vars = new HashMap<>();
        vars.put("greeting", "Hi");
        vars.put("name", "Alice");
        String result = EmailTemplate.SimpleSubstitutor.replace("${greeting}, ${name}!", vars);
        assertEquals("Hi, Alice!", result);
    }

    @Test
    public void testReplace_missingVariable() {
        Map<String, String> vars = new HashMap<>();
        vars.put("foo", "bar");
        String result = EmailTemplate.SimpleSubstitutor.replace("Value: ${missing}", vars);
        assertEquals("Value: ${missing}", result);
    }

    @Test
    public void testReplace_noVariables() {
        Map<String, String> vars = Collections.emptyMap();
        String result = EmailTemplate.SimpleSubstitutor.replace("No variables here.", vars);
        assertEquals("No variables here.", result);
    }

    @Test
    public void testReplace_nullTemplate() {
        Map<String, String> vars = new HashMap<>();
        String result = EmailTemplate.SimpleSubstitutor.replace(null, vars);
        assertEquals(null, result);
    }

    @Test
    public void testReplace_emptyTemplate() {
        Map<String, String> vars = new HashMap<>();
        String result = EmailTemplate.SimpleSubstitutor.replace("", vars);
        assertEquals("", result);
    }

    @Test
    public void testReplace_nullVars() {
        String result = EmailTemplate.SimpleSubstitutor.replace("Hello, ${name}!", null);
        assertEquals("Hello, ${name}!", result);
    }

    @Test
    public void testReplace_unclosedVariable() {
        Map<String, String> vars = new HashMap<>();
        vars.put("foo", "bar");
        String result = EmailTemplate.SimpleSubstitutor.replace("Test ${foo", vars);
        assertEquals("Test ${foo", result);
    }

    @Test
    public void testReplace_adjacentVariables() {
        Map<String, String> vars = new HashMap<>();
        vars.put("a", "1");
        vars.put("b", "2");
        String result = EmailTemplate.SimpleSubstitutor.replace("${a}${b}", vars);
        assertEquals("12", result);
    }

    // ------------------------
    // Constructor tests
    // ------------------------

    @Test(expected = IllegalArgumentException.class)
    public void constructor_nullInputStream_throws() throws IOException {
        new EmailTemplate(null, "utf-8");
    }

    @Test
    public void constructor_blankEncoding_usesDefaultCharset() throws IOException {
        final EmailTemplate template = new EmailTemplate(toStream("Subject: Test\n\nHello"), "");
        assertNotNull(template);
    }

    @Test
    public void constructor_invalidEncoding_fallsBackToUtf8() throws IOException {
        final EmailTemplate template = new EmailTemplate(toStream("Subject: Test\n\nHello"), "not-a-real-charset");
        assertNotNull(template);
    }

    @Test
    public void constructor_twoArg_delegatesToThreeArg() throws IOException, EmailException, javax.mail.MessagingException {
        final EmailTemplate template = new EmailTemplate(toStream("Subject: Test\n\nHello, plain"), "utf-8");
        final Email email = template.getEmail(new HashMap<>(), SimpleEmail.class);
        assertEquals("Test", email.getSubject());
    }

    // ------------------------
    // getEmail(..) tests
    // ------------------------

    @Test(expected = IllegalArgumentException.class)
    public void getEmail_nullType_throws() throws IOException, EmailException, javax.mail.MessagingException {
        final EmailTemplate template = new EmailTemplate(toStream("Subject: Test\n\nHello"), "utf-8");
        template.getEmail(new HashMap<>(), null);
    }

    @Test
    public void getEmail_appliesPrimaryHeaders() throws Exception {
        final String rawTemplate = "To: to@test.com\n"
                + "CC: cc@test.com\n"
                + "BCC: bcc@test.com\n"
                + "Reply-To: reply@test.com\n"
                + "From: from@test.com\n"
                + "Subject: ${subject}\n"
                + "Bounce-To: bounce@test.com\n"
                + "\n"
                + "This is a plain text body with ${name}.";

        final Map<String, String> vars = new HashMap<>();
        vars.put("subject", "Hello Subject");
        vars.put("name", "World");

        final EmailTemplate template = new EmailTemplate(toStream(rawTemplate), "utf-8");
        final SimpleEmail email = template.getEmail(vars, SimpleEmail.class);

        assertEquals(1, email.getToAddresses().size());
        assertEquals("to@test.com", email.getToAddresses().get(0).getAddress());

        assertEquals(1, email.getCcAddresses().size());
        assertEquals("cc@test.com", email.getCcAddresses().get(0).getAddress());

        assertEquals(1, email.getBccAddresses().size());
        assertEquals("bcc@test.com", email.getBccAddresses().get(0).getAddress());

        assertEquals(1, email.getReplyToAddresses().size());
        assertEquals("reply@test.com", email.getReplyToAddresses().get(0).getAddress());

        assertEquals("from@test.com", email.getFromAddress().getAddress());
        assertEquals("Hello Subject", email.getSubject());
        assertEquals("bounce@test.com", email.getBounceAddress());

        final Object content = getFieldValue(email, Email.class, "content");
        assertEquals("This is a plain text body with World.", content);
    }

    @Test
    public void getEmail_appliesSecondaryHeaders() throws Exception {
        final String rawTemplate = "Subject: Test\n"
                + "X-Custom-Header: custom-value\n"
                + "\n"
                + "Body content";

        final EmailTemplate template = new EmailTemplate(toStream(rawTemplate), "utf-8");
        final SimpleEmail email = template.getEmail(new HashMap<>(), SimpleEmail.class);

        assertEquals("custom-value", email.getHeader("X-Custom-Header"));
    }

    @Test
    public void getEmail_nonHtmlBody_setsPlainMessage() throws Exception {
        final EmailTemplate template = new EmailTemplate(toStream("Subject: Test\n\nJust plain text, no html tag here."), "utf-8");
        final SimpleEmail email = template.getEmail(new HashMap<>(), SimpleEmail.class);

        final Object content = getFieldValue(email, Email.class, "content");
        assertEquals("Just plain text, no html tag here.", content);
    }

    @Test
    public void getEmail_htmlEmailType_htmlBody_noHtmlParser_fallsBackToSetMsg() throws Exception {
        final String body = "<html><body><p>Hello</p></body></html>";
        final EmailTemplate template = new EmailTemplate(toStream("Subject: Test\n\n" + body), "utf-8", null);
        final HtmlEmail email = template.getEmail(new HashMap<>(), HtmlEmail.class);

        // HtmlEmail#setMsg(..) (invoked via the fallback else branch) stores the raw message in the "text" field.
        final Object text = getFieldValue(email, HtmlEmail.class, "text");
        assertEquals(body, text);
    }

    @Test
    public void getEmail_simpleEmailType_htmlLikeBody_treatedAsPlainMessage() throws Exception {
        final String body = "<html><body>Not really parsed since not a HtmlEmail</body></html>";
        final HtmlParser htmlParser = mock(HtmlParser.class);
        final EmailTemplate template = new EmailTemplate(toStream("Subject: Test\n\n" + body), "utf-8", htmlParser);
        final SimpleEmail email = template.getEmail(new HashMap<>(), SimpleEmail.class);

        final Object content = getFieldValue(email, Email.class, "content");
        assertEquals(body, content);
        Mockito.verifyNoInteractions(htmlParser);
    }

    @Test
    public void getEmail_htmlEmailType_withHtmlParser_extractsPlainText() throws Exception {
        final String body = "<html><body><h1>Title</h1><p>Paragraph <a href=\"http://example.com\">link</a></p>"
                + "<ul><li>Item1</li></ul><dl><dt>Term</dt></dl><table><tr><td>row</td></tr></table></body></html>";

        final HtmlParser htmlParser = mock(HtmlParser.class);
        doAnswer(invocation -> {
            final ContentHandler handler = invocation.getArgument(2);
            simulateHtmlDocument(handler);
            return null;
        }).when(htmlParser).parse(any(InputStream.class), anyString(), any(ContentHandler.class));

        final EmailTemplate template = new EmailTemplate(toStream("Subject: Test\n\n" + body), "utf-8", htmlParser);
        final HtmlEmail email = template.getEmail(new HashMap<>(), HtmlEmail.class);

        final Object html = getFieldValue(email, HtmlEmail.class, "html");
        final Object text = getFieldValue(email, HtmlEmail.class, "text");

        assertEquals(body, html);
        assertNotNull(text);
        final String textStr = (String) text;
        assertTrue(textStr.contains("Title"));
        assertTrue(textStr.contains("Paragraph"));
        assertTrue(textStr.contains(" <http://example.com>"));
        assertTrue(textStr.contains("Item1"));
        assertTrue(textStr.contains("Term"));
        assertTrue(textStr.contains("row"));
        // no leading/trailing whitespace due to trim() in toString()
        assertEquals(textStr.trim(), textStr);
    }

    @Test
    public void getEmail_htmlEmailType_htmlParserThrowsSaxException_fallsBackToSetMsg() throws Exception {
        final String body = "<html><body><p>Hello</p></body></html>";

        final HtmlParser htmlParser = mock(HtmlParser.class);
        doThrow(new SAXException("boom")).when(htmlParser).parse(any(InputStream.class), anyString(), any(ContentHandler.class));

        final EmailTemplate template = new EmailTemplate(toStream("Subject: Test\n\n" + body), "utf-8", htmlParser);
        final HtmlEmail email = template.getEmail(new HashMap<>(), HtmlEmail.class);

        // HtmlEmail#setMsg(..) (invoked via the fallback else branch) stores the raw message in the "text" field.
        final Object text = getFieldValue(email, HtmlEmail.class, "text");
        assertEquals(body, text);
    }

    @Test(expected = EmailException.class)
    public void getEmail_noPublicNoArgConstructor_throwsEmailException() throws Exception {
        final EmailTemplate template = new EmailTemplate(toStream("Subject: Test\n\nBody"), "utf-8");
        template.getEmail(new HashMap<>(), NoPublicConstructorEmail.class);
    }

    /**
     * Test double that intentionally has no public no-arg constructor so that
     * EmailTemplate#newEmailInstance(..) is forced down its ReflectiveOperationException path.
     */
    public static final class NoPublicConstructorEmail extends Email {
        private NoPublicConstructorEmail() {
        }

        @Override
        public Email setMsg(String msg) {
            return this;
        }
    }

    private static void simulateHtmlDocument(final ContentHandler handler) throws SAXException {
        final Attributes noAttrs = mock(Attributes.class);
        final Attributes hrefAttrs = mock(Attributes.class);
        when(hrefAttrs.getValue("href")).thenReturn("http://example.com");

        handler.setDocumentLocator(null);
        handler.startDocument();
        handler.startPrefixMapping("", "");
        handler.endPrefixMapping("");

        handler.startElement(null, "html", "html", noAttrs);
        handler.characters("before body".toCharArray(), 0, "before body".length());

        handler.startElement(null, "body", "body", noAttrs);

        handler.startElement(null, "h1", "h1", noAttrs);
        handler.characters("Title".toCharArray(), 0, "Title".length());
        handler.endElement(null, "h1", "h1");

        handler.startElement(null, "p", "p", noAttrs);
        handler.characters("Paragraph ".toCharArray(), 0, "Paragraph ".length());

        handler.startElement(null, "a", "a", hrefAttrs);
        handler.characters("link".toCharArray(), 0, "link".length());
        handler.endElement(null, "a", "a");

        handler.endElement(null, "p", "p");

        handler.startElement(null, "ul", "ul", noAttrs);
        handler.startElement(null, "li", "li", noAttrs);
        handler.characters("Item1".toCharArray(), 0, "Item1".length());
        handler.endElement(null, "li", "li");
        handler.endElement(null, "ul", "ul");

        handler.startElement(null, "dl", "dl", noAttrs);
        handler.startElement(null, "dt", "dt", noAttrs);
        handler.characters("Term".toCharArray(), 0, "Term".length());
        handler.endElement(null, "dt", "dt");
        handler.endElement(null, "dl", "dl");

        handler.startElement(null, "table", "table", noAttrs);
        handler.startElement(null, "tr", "tr", noAttrs);
        handler.characters("row".toCharArray(), 0, "row".length());
        handler.endElement(null, "tr", "tr");
        handler.endElement(null, "table", "table");

        // an anchor tag with no href attribute - the href-null branch
        handler.startElement(null, "a", "a", noAttrs);
        handler.characters("no href link".toCharArray(), 0, "no href link".length());
        handler.endElement(null, "a", "a");

        // a br element to hit the endElement br branch
        handler.startElement(null, "br", "br", noAttrs);
        handler.endElement(null, "br", "br");

        handler.endElement(null, "body", "body");

        handler.characters("after body".toCharArray(), 0, "after body".length());

        handler.ignorableWhitespace(new char[]{' '}, 0, 1);
        handler.processingInstruction("target", "data");
        handler.skippedEntity("entity");

        handler.endElement(null, "html", "html");
        handler.endDocument();
    }

    private InputStream toStream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    // ------------------------
    // create(path, session, htmlParser) tests
    // ------------------------

    @Test(expected = IllegalArgumentException.class)
    public void create_blankPath_throws() {
        EmailTemplate.create("", mock(Session.class), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void create_nullSession_throws() {
        EmailTemplate.create("/content/test.html", null, null);
    }

    @Test
    public void create_pathDoesNotExist_returnsNull() throws RepositoryException {
        final Session session = mock(Session.class);
        when(session.itemExists("/content/test.html")).thenReturn(false);

        final EmailTemplate template = EmailTemplate.create("/content/test.html", session, null);

        assertNull(template);
    }

    @Test(expected = IllegalArgumentException.class)
    public void create_nodeNotNtFile_throws() throws RepositoryException {
        final Session session = mock(Session.class);
        final Node node = mock(Node.class);
        final NodeType nodeType = mock(NodeType.class);

        when(session.itemExists("/content/test.html")).thenReturn(true);
        when(session.getNode("/content/test.html")).thenReturn(node);
        when(node.getPrimaryNodeType()).thenReturn(nodeType);
        when(nodeType.getName()).thenReturn("nt:unstructured");

        EmailTemplate.create("/content/test.html", session, null);
    }

    @Test
    public void create_ntFileNoEncoding_usesDefaultCharset() throws RepositoryException, IOException {
        final EmailTemplate template = createTemplateFromMockedNode(null, "Subject: Test\n\nHello Default Charset");

        assertNotNull(template);
    }

    @Test
    public void create_ntFileWithEncoding_usesSpecifiedCharset() throws RepositoryException, IOException {
        final EmailTemplate template = createTemplateFromMockedNode("UTF-8", "Subject: Test\n\nHello Specified Charset");

        assertNotNull(template);
    }

    @Test
    public void create_repositoryExceptionThrown_returnsNull() throws RepositoryException {
        final Session session = mock(Session.class);
        when(session.itemExists("/content/test.html")).thenThrow(new RepositoryException("boom"));

        final EmailTemplate template = EmailTemplate.create("/content/test.html", session, null);

        assertNull(template);
    }

    @Test
    public void create_ioExceptionThrown_returnsNull() throws RepositoryException, IOException {
        final Session session = mock(Session.class);
        final Node node = mock(Node.class);
        final NodeType nodeType = mock(NodeType.class);
        final Node contentNode = mock(Node.class);
        final Property dataProperty = mock(Property.class);
        final Binary binary = mock(Binary.class);

        when(session.itemExists("/content/test.html")).thenReturn(true);
        when(session.getNode("/content/test.html")).thenReturn(node);
        when(node.getPrimaryNodeType()).thenReturn(nodeType);
        when(nodeType.getName()).thenReturn(JcrConstants.NT_FILE);
        when(node.getNode(JcrConstants.JCR_CONTENT)).thenReturn(contentNode);
        when(contentNode.hasProperty(JcrConstants.JCR_ENCODING)).thenReturn(false);
        when(contentNode.getProperty(JcrConstants.JCR_DATA)).thenReturn(dataProperty);
        when(dataProperty.getBinary()).thenReturn(binary);

        final InputStream throwingStream = mock(InputStream.class);
        when(throwingStream.read()).thenThrow(new IOException("read failure"));
        when(throwingStream.read(any(byte[].class))).thenThrow(new IOException("read failure"));
        when(throwingStream.read(any(byte[].class), org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyInt())).thenThrow(new IOException("read failure"));
        when(binary.getStream()).thenReturn(throwingStream);

        final EmailTemplate template = EmailTemplate.create("/content/test.html", session, null);

        assertNull(template);
    }

    @Test
    public void create_backwardsCompatibleOverload_returnsNullWhenNotFound() throws RepositoryException {
        final Session session = mock(Session.class);
        when(session.itemExists("/content/test.html")).thenReturn(false);

        final EmailTemplate template = EmailTemplate.create("/content/test.html", session);

        assertNull(template);
    }

    private EmailTemplate createTemplateFromMockedNode(final String encoding, final String content) throws RepositoryException, IOException {
        final Session session = mock(Session.class);
        final Node node = mock(Node.class);
        final NodeType nodeType = mock(NodeType.class);
        final Node contentNode = mock(Node.class);
        final Property dataProperty = mock(Property.class);
        final Binary binary = mock(Binary.class);

        when(session.itemExists("/content/test.html")).thenReturn(true);
        when(session.getNode("/content/test.html")).thenReturn(node);
        when(node.getPrimaryNodeType()).thenReturn(nodeType);
        when(nodeType.getName()).thenReturn(JcrConstants.NT_FILE);
        when(node.getNode(JcrConstants.JCR_CONTENT)).thenReturn(contentNode);

        if (encoding != null) {
            when(contentNode.hasProperty(JcrConstants.JCR_ENCODING)).thenReturn(true);
            final Property encodingProperty = mock(Property.class);
            when(encodingProperty.getString()).thenReturn(encoding);
            when(contentNode.getProperty(JcrConstants.JCR_ENCODING)).thenReturn(encodingProperty);
        } else {
            when(contentNode.hasProperty(JcrConstants.JCR_ENCODING)).thenReturn(false);
        }

        when(contentNode.getProperty(JcrConstants.JCR_DATA)).thenReturn(dataProperty);
        when(dataProperty.getBinary()).thenReturn(binary);
        when(binary.getStream()).thenReturn(toStream(content));

        return EmailTemplate.create("/content/test.html", session, null);
    }
}
