package com.adobe.aem.commons.assetshare.util.impl;

import com.day.cq.commons.jcr.JcrConstants;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.sling.commons.html.HtmlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Modernized EmailTemplate implementation:
 * - Uses Apache Commons Text for ${var} substitution
 * - Accepts HtmlParser via constructor (no HtmlParserAccessor)
 * - No deprecated Commons Lang 2 classes
 */
public class EmailTemplate {

    private static final String HEADER_TO = "To";
    private static final String HEADER_CC = "CC";
    private static final String HEADER_BCC = "BCC";
    private static final String HEADER_REPLYTO = "Reply-To";
    private static final String HEADER_FROM = "From";
    private static final String HEADER_SUBJECT = "Subject";
    private static final String HEADER_BOUNCETO = "Bounce-To";

    private static final String[] PRIMARY_HEADERS = new String[]{
            HEADER_TO,
            HEADER_CC,
            HEADER_BCC,
            HEADER_REPLYTO,
            HEADER_FROM,
            HEADER_SUBJECT,
            HEADER_BOUNCETO
    };

    private static final String DEFAULT_CHARSET = "utf-8";
    private static final Logger log = LoggerFactory.getLogger(EmailTemplate.class);

    private final String message;
    private final Charset charset;
    private final HtmlParser htmlParser; // may be null

    /**
     * Backwards-friendly constructor (no HTML parsing).
     */
    public EmailTemplate(final InputStream inputStream, final String encoding) throws IOException {
        this(inputStream, encoding, null);
    }

    /**
     * Preferred constructor: provide HtmlParser (OSGi service) for HTML->text extraction.
     */
    public EmailTemplate(final InputStream inputStream, final String encoding, final HtmlParser htmlParser)
            throws IOException {

        if (inputStream == null) {
            throw new IllegalArgumentException("input stream may not be null");
        }

        final String cs = StringUtils.defaultIfEmpty(encoding, DEFAULT_CHARSET);
        this.charset = safeCharset(cs);
        this.htmlParser = htmlParser;

        try (InputStreamReader reader = new InputStreamReader(inputStream, this.charset);
             StringWriter writer = new StringWriter()) {
            IOUtils.copy(reader, writer);
            this.message = writer.toString();
        }
    }

    /**
     * Create an Email based on the template text and variable substitution.
     */
    public <T extends Email> T getEmail(final Map<String, String> variables, final Class<T> type)
            throws IOException, MessagingException, EmailException {

        if (type == null) {
            throw new IllegalArgumentException("type may not be null");
        }

        // Apache Commons Text substitution (replaces deprecated StrSubstitutor/StrLookup)
        final String rendered = substitute(message, variables);

        try (ByteArrayInputStream in = new ByteArrayInputStream(rendered.getBytes(charset))) {

            final InternetHeaders headers = new InternetHeaders(in);

            final T email = newEmailInstance(type);
            email.setCharset(charset.name());

            // Remaining bytes after InternetHeaders parsing are the body
            final String body = readRemainder(in, charset);

            applyBody(email, body);
            applyHeaders(email, headers);

            return email;
        }
    }

    /**
     * Convenience method to create a EmailTemplate based on a repository path.
     * You requested: pass HtmlParser instance (no HtmlParserAccessor), so this overload accepts it.
     */
    public static EmailTemplate create(final String path, final Session session, final HtmlParser htmlParser) {

        if (StringUtils.isBlank(path)) {
            throw new IllegalArgumentException("path may not be null or empty");
        }
        if (session == null) {
            throw new IllegalArgumentException("session may not be null");
        }

        try {
            if (!session.itemExists(path)) {
                return null;
            }

            final Node node = session.getNode(path);
            if (!JcrConstants.NT_FILE.equals(node.getPrimaryNodeType().getName())) {
                throw new IllegalArgumentException("provided path does not point to a nt:file node");
            }

            final Node content = node.getNode(JcrConstants.JCR_CONTENT);
            final String encoding = content.hasProperty(JcrConstants.JCR_ENCODING)
                    ? content.getProperty(JcrConstants.JCR_ENCODING).getString()
                    : DEFAULT_CHARSET;

            try (InputStream is = content.getProperty(JcrConstants.JCR_DATA).getBinary().getStream()) {
                log.debug("loaded template [{}].", path);
                return new EmailTemplate(is, encoding, htmlParser);
            }

        } catch (RepositoryException e) {
            log.error("error creating message template: ", e);
        } catch (IOException e) {
            log.error("error creating message template: ", e);
        }

        return null;
    }

    /**
     * Backwards-compatible signature (no HtmlParser available).
     */
    public static EmailTemplate create(final String path, final Session session) {
        return create(path, session, null);
    }

    // ------------------------
    // Internals
    // ------------------------

    private static Charset safeCharset(String cs) {
        try {
            return Charset.forName(cs);
        } catch (Exception e) {
            return StandardCharsets.UTF_8;
        }
    }

    private static String substitute(final String template, final Map<String, String> variables) {
        return SimpleSubstitutor.replace(template, variables);
    }

    private static <T extends Email> T newEmailInstance(final Class<T> type) throws EmailException {
        try {
            final Constructor<T> ctor = type.getConstructor();
            return ctor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new EmailException("Unable to instantiate email type: " + type.getName(), e);
        }
    }

    private static String readRemainder(final InputStream in, final Charset charset) throws IOException {
        final StringWriter writer = new StringWriter();
        IOUtils.copy(in, writer, charset);
        return writer.toString();
    }

    private void applyBody(final Email email, final String msg) throws EmailException, IOException {
        if (email instanceof HtmlEmail && htmlParser != null && isHtmlMessage(msg)) {
            final HtmlEmail htmlEmail = (HtmlEmail) email;
            try {
                final PlainTextExtractor extractor = new PlainTextExtractor();
                htmlParser.parse(new ByteArrayInputStream(msg.getBytes(charset)), charset.name(), extractor);
                htmlEmail.setTextMsg(extractor.toString());
                htmlEmail.setHtmlMsg(msg);
            } catch (SAXException e) {
                // If HTML parsing fails, preserve original behavior: fall back to plain message
                email.setMsg(msg);
            }
        } else {
            email.setMsg(msg);
        }
    }

    @SuppressWarnings("unchecked")
    private static void applyHeaders(final Email email, final InternetHeaders headers) throws EmailException {
        // Primary headers
        final Enumeration<Header> primaryHeaders = headers.getMatchingHeaders(PRIMARY_HEADERS);
        while (primaryHeaders.hasMoreElements()) {
            final Header header = primaryHeaders.nextElement();
            final String name = header.getName();
            final String value = header.getValue();

            if (value == null) {
                log.warn("got empty primary header [{}].", name);
                continue;
            }

            if (HEADER_TO.equalsIgnoreCase(name)) {
                email.addTo(value);
            } else if (HEADER_CC.equalsIgnoreCase(name)) {
                email.addCc(value);
            } else if (HEADER_BCC.equalsIgnoreCase(name)) {
                email.addBcc(value);
            } else if (HEADER_REPLYTO.equalsIgnoreCase(name)) {
                email.addReplyTo(value);
            } else if (HEADER_FROM.equalsIgnoreCase(name)) {
                email.setFrom(value);
            } else if (HEADER_SUBJECT.equalsIgnoreCase(name)) {
                email.setSubject(value);
            } else if (HEADER_BOUNCETO.equalsIgnoreCase(name)) {
                email.setBounceAddress(value);
            }
        }

        // Secondary headers
        final Enumeration<Header> secondaryHeaders = headers.getNonMatchingHeaders(PRIMARY_HEADERS);
        while (secondaryHeaders.hasMoreElements()) {
            final Header header = secondaryHeaders.nextElement();
            final String name = header.getName();
            final String value = header.getValue();

            if (value == null) {
                log.warn("got empty secondary header [{}].", name);
                continue;
            }

            email.addHeader(name, value);
        }
    }

    /*
     * Sling's Commons HTML parser is too lenient and can create false positives.
     */
    private static boolean isHtmlMessage(final String msg) {
        final Pattern p = Pattern.compile("<\\s*html[^>]*>");
        final Matcher m = p.matcher(msg.toLowerCase());
        return m.find();
    }

    private static class PlainTextExtractor implements ContentHandler {

        private boolean append = false;
        private final StringBuilder buffer = new StringBuilder();
        private String linkText;

        @Override public void setDocumentLocator(Locator locator) { }
        @Override public void startDocument() throws SAXException { }
        @Override public void endDocument() throws SAXException { }
        @Override public void startPrefixMapping(String prefix, String uri) throws SAXException { }
        @Override public void endPrefixMapping(String prefix) throws SAXException { }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            if ("body".equals(localName)) {
                append = true;
            } else if ("li".equals(localName)) {
                buffer.append("\n * ");
            } else if ("dt".equals(localName)) {
                buffer.append("  ");
            } else if ("p".equals(localName) || "tr".equals(localName) || localName.matches("h[1-5]")) {
                buffer.append("\n");
            } else if ("a".equals(localName)) {
                final String href = atts.getValue("href");
                if (href != null) {
                    linkText = String.format(" <%s>", href);
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if ("body".equals(localName)) {
                append = false;
            } else if ("br".equals(localName) || "p".equals(localName) || "tr".equals(localName) || localName.matches("h[1-5]")) {
                buffer.append("\n");
            } else if ("a".equals(localName) && linkText != null) {
                buffer.append(linkText);
                linkText = null;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (append) {
                buffer.append(ch, start, length);
            }
        }

        @Override public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException { }
        @Override public void processingInstruction(String target, String data) throws SAXException { }
        @Override public void skippedEntity(String name) throws SAXException { }

        @Override
        public String toString() {
            return buffer.toString().trim();
        }
    }


    public static final class SimpleSubstitutor {

        private SimpleSubstitutor() {}

        public static String replace(String template, Map<String, String> vars) {
            if (template == null || template.isEmpty()) return template;
            if (vars == null || vars.isEmpty()) return template;

            StringBuilder out = new StringBuilder(template.length());
            int i = 0;
            int n = template.length();

            while (i < n) {
                char c = template.charAt(i);

                // look for ${...}
                if (c == '$' && i + 1 < n && template.charAt(i + 1) == '{') {
                    int start = i;          // '$'
                    int j = i + 2;          // first char after '{'

                    // find closing '}'
                    while (j < n && template.charAt(j) != '}') {
                        j++;
                    }

                    if (j < n && template.charAt(j) == '}') {
                        String key = template.substring(i + 2, j);
                        String val = vars.get(key);

                        if (val != null) {
                            out.append(val);           // replace
                        } else {
                            out.append(template, start, j + 1); // keep ${key} as-is
                        }

                        i = j + 1;
                        continue;
                    }
                    // no closing brace -> treat literally
                }

                out.append(c);
                i++;
            }

            return out.toString();
        }
    }
}
