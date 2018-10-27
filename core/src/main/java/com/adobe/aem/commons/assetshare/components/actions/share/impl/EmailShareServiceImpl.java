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

/**
 * This file is a copy-down of the ACS AEM Commons Email Service feature with slight modifications.
 * This copy-down was necessary due to issues with OSGi service resolution when the service was
 * provided by the ACS AEM Commons project.
 *
 * > https://adobe-consulting-services.github.io/acs-aem-commons/features/e-mail/email-api/index.html
 *
 * Please see the above ACS AEM Commons copyright for copyright details.
 **/

package com.adobe.aem.commons.assetshare.components.actions.share.impl;

import com.adobe.aem.commons.assetshare.components.actions.share.EmailShare;
import com.adobe.aem.commons.assetshare.components.actions.share.ShareException;
import com.adobe.aem.commons.assetshare.components.actions.share.ShareService;
import com.adobe.aem.commons.assetshare.configuration.AssetDetailsResolver;
import com.adobe.aem.commons.assetshare.configuration.Config;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.util.EmailService;
import com.adobe.granite.security.user.UserProperties;
import com.adobe.granite.security.user.UserPropertiesManager;
import com.day.cq.commons.Externalizer;
import com.day.cq.dam.commons.util.DamUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.models.factory.ModelFactory;
import org.apache.sling.scripting.core.ScriptHelper;
import org.apache.sling.settings.SlingSettingsService;
import org.apache.sling.xss.XSSAPI;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.jcr.RepositoryException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component(service = ShareService.class)
@Designate(ocd = EmailShareServiceImpl.Cfg.class)
public class EmailShareServiceImpl implements ShareService {
    private static final Logger log = LoggerFactory.getLogger(EmailShareServiceImpl.class);

    public static final String SHARE_SERVICE_ACCEPTANCE_KEY = "asset-share-commons__share--email";

    /**
     * Share Parameters
     */
    public static final String SIGNATURE = "signature";
    private static final String ASSET_PATHS = "path";
    private static final String EMAIL_ADDRESSES = "email";
    private static final String EMAIL_ASSET_LINK_LIST_HTML = "assetLinksHTML";

    private Cfg cfg;
    private BundleContext bundleContext;

    @Reference
    private EmailService emailService;

    @Reference
    private Externalizer externalizer;

    @Reference
    private AssetDetailsResolver assetDetailsResolver;

    @Reference
    private SlingSettingsService slingSettingsService;

    @Reference
    private ModelFactory modelFactory;

    @Reference
    private XSSAPI xssAPI;

    @Override
    public boolean accepts(final SlingHttpServletRequest request) {
        return "true".equals(request.getParameter(SHARE_SERVICE_ACCEPTANCE_KEY));
    }

    @Override
    public final void share(final SlingHttpServletRequest request, final SlingHttpServletResponse response, final ValueMap shareParameters) throws ShareException {
    	
        /** Work around for regression issue introduced in AEM 6.4 **/
        SlingBindings bindings = new SlingBindings();
        //intentionally setting the second argument to 'null' since there is no SlingScript to pass in
        bindings.setSling( new ScriptHelper(bundleContext, null, request, response));
        request.setAttribute(SlingBindings.class.getName(), bindings);
        
        final EmailShare emailShare = request.adaptTo(EmailShare.class);

        shareParameters.putAll(xssProtectUserData(emailShare.getUserData()));

        // Configured data supersedes user data
        shareParameters.putAll(emailShare.getConfiguredData());

        // Except for signature which we may or may  not want to use from configured data, depending on flags in configured data
        try {
            shareParameters.put(SIGNATURE, getSignature(request));
        } catch (RepositoryException e) {
            throw new ShareException("Could not obtain user display name for " + request.getResourceResolver().getUserID());
        }

        share(request.adaptTo(Config.class), shareParameters, StringUtils.defaultIfBlank(emailShare.getEmailTemplatePath(), cfg.emailTemplate()));
    }

    private final void share(final Config config, final ValueMap shareParameters, final String emailTemplatePath) throws ShareException {
        final String[] emailAddresses = StringUtils.split(shareParameters.get(EMAIL_ADDRESSES, ""), ",");
        final String[] assetPaths = Arrays.stream(shareParameters.get(ASSET_PATHS, ArrayUtils.EMPTY_STRING_ARRAY))
                .map(path -> {
                    try {
                        return URLDecoder.decode(path, StandardCharsets.UTF_8.name());
                    } catch (UnsupportedEncodingException e) {
                        log.warn("Could not decode path [ {} ] as UTF-8; Using path as is,.", path);
                        return path;
                    }
                }).filter(StringUtils::isNotBlank)
                .toArray(String[]::new);

        // Check to ensure the minimum set of e-mail parameters are provided; Throw exception if not.
        if (emailAddresses == null || emailAddresses.length == 0) {
            throw new ShareException("At least one e-mail address is required to share");
        } else if (ArrayUtils.isEmpty(assetPaths)) {
            throw new ShareException("At least one asset is required to share");
        }
       
        // Convert provided params to <String, String>; anything that needs to be accessed in its native type should be accessed and manipulated via shareParameters.get(..)
        final Map<String, String> emailParameters = new HashMap<String, String>();
        for (final String key : shareParameters.keySet()) {
            emailParameters.put(key, shareParameters.get(key, String.class));
        }

        // Generate the HTML list of Assets and their links
        emailParameters.put(EMAIL_ASSET_LINK_LIST_HTML, getAssetLinkListHtml(config, assetPaths));

        // Send e-mail
        final List<String> failureList = emailService.sendEmail(emailTemplatePath, emailParameters, emailAddresses);
        if (failureList.size() > 0) {
            throw new ShareException(String.format("Unable to send share e-mail too [ %s ]", StringUtils.join(emailAddresses)));
        }
    }

    private final String getAssetLinkListHtml(final Config config, final String[] assetPaths) {
        final StringBuilder sb = new StringBuilder();

        for (final String assetPath : assetPaths) {
            final Resource assetResource = config.getResourceResolver().getResource(assetPath);
            if (assetResource != null && DamUtil.isAsset(assetResource)) {
                final AssetModel asset = modelFactory.getModelFromWrappedRequest(config.getRequest(), assetResource, AssetModel.class);

                String url = assetDetailsResolver.getFullUrl(config, asset);

                if (StringUtils.isBlank(url)) {
                    log.warn("Could not determine an Asset Details page path for asset at [ {} ]", assetPath);
                    continue;
                }

                if (isAuthor()) {
                    url = externalizer.authorLink(config.getResourceResolver(), url);
                } else {
                    url = externalizer.publishLink(config.getResourceResolver(), url);
                }

                sb.append("<li><a href=\"");
                sb.append(url);
                sb.append("\">");
                sb.append(asset.getTitle());
                sb.append("</a></li>");
            }
        }

        sb.append("</ul>");

        return sb.toString();
    }

    public boolean isAuthor() {
        return slingSettingsService.getRunModes().contains("author");
    }

    private String getSignature(final SlingHttpServletRequest request) throws RepositoryException {
        if (request == null) {
            return cfg.signature();
        }

        final EmailShare emailShare = request.adaptTo(EmailShare.class);

        boolean useSharerDisplayNameAsSignature = emailShare.getProperties().get(EmailShareImpl.PN_USE_SHARER_NAME_AS_SIGNATURE, false);

        if (useSharerDisplayNameAsSignature) {
            final String currentUser = request.getResourceResolver().getUserID();
            if (!"anonymous".equalsIgnoreCase(currentUser) && !"admin".equalsIgnoreCase(currentUser)) {
                final UserPropertiesManager upm = request.getResourceResolver().adaptTo(UserPropertiesManager.class);
                final Authorizable authorizable = request.getResourceResolver().adaptTo(Authorizable.class);
                final UserProperties userProperties = upm.getUserProperties(authorizable, "profile");

                if (userProperties != null) {
                    return StringUtils.trimToNull(userProperties.getDisplayName());
                }
            }
        }

        return StringUtils.defaultIfBlank(emailShare.getConfiguredData().get(EmailShareImpl.PN_SIGNATURE, String.class), cfg.signature());
    }

    /**
     * Since all emails are expected to be HTML emails in this implementation, we must XSS Protect all values that may end up in the HTML email.
     *
     * @param dirtyUserData the Map of data provided by the end-user (usually derived from the Request) to use in the email.
     * @return the protected Map; all String's are xss protected for HTML.
     */
    private Map<String, Object> xssProtectUserData(Map<String, Object> dirtyUserData) {
        Map<String, Object> cleanUserData = new HashMap<String, Object>();
        for (final Map.Entry<String, Object> entry : dirtyUserData.entrySet()) {

            if (entry.getValue() instanceof String[]) {
                cleanUserData.put(entry.getKey(), xssCleanData((String[]) entry.getValue()));
            } else if (entry.getValue() instanceof String) {
                cleanUserData.put(entry.getKey(), xssCleanData((String) entry.getValue()));
            }   
        }

        return cleanUserData;
    }

    private String[] xssCleanData(String[] dirtyData) {
        List<String> cleanValues = new ArrayList<String>();
        for (String val : dirtyData) {
            cleanValues.add(xssAPI.encodeForHTML(xssAPI.filterHTML(val)));
        }
        return cleanValues.toArray(new String[0]);
    }

    private String xssCleanData(String dirtyData) {
        return xssAPI.encodeForHTML(xssAPI.filterHTML(dirtyData));
    }

    @Activate
    protected final void activate(final Cfg config, final BundleContext bundleContext) throws Exception {
        this.cfg = config;
        this.bundleContext = bundleContext;
    }

    @ObjectClassDefinition(name = "Asset Share Commons - E-mail Share Service")
    public @interface Cfg {

        @AttributeDefinition(
                name = "Default E-mail Template",
                description = "Absolute path to email template in the repository"
        )
        String emailTemplate() default "/etc/notification/email/asset-share-commons/share/default.html";

        @AttributeDefinition(
                name = "Default Signature",
                description = "The default value to use is no signature can be derived."
        )
        String signature() default "Your Assets Team";
    }
}
