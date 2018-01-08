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
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.factory.ModelFactory;
import org.apache.sling.settings.SlingSettingsService;
import org.apache.sling.xss.XSSAPI;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Designate(ocd = EmailShareServiceImpl.Cfg.class)
public class EmailShareServiceImpl implements ShareService {
    private static final Logger log = LoggerFactory.getLogger(EmailShareServiceImpl.class);

    /**
     * Share Parameters
     */
    public static final String SIGNATURE = "signature";
    private static final String ASSET_PATHS = "path";
    private static final String EMAIL_ADDRESSES = "email";
    private static final String EMAIL_ASSET_LINK_LIST_HTML = "assetLinksHTML";

    private Cfg cfg;

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
    private XSSAPI xssAPi;

    @Override
    public final void share(final SlingHttpServletRequest request, final SlingHttpServletResponse response, final ValueMap shareParameters) throws ShareException {
        final EmailShare emailShare = request.adaptTo(EmailShare.class);

        shareParameters.putAll(xssProtectUserData(emailShare.getUserData()));

        // Configured data supercedes user data
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
        final String[] assetPaths = shareParameters.get(ASSET_PATHS, String[].class);

        // Check to ensure the minimum set of e-mail parameters are provided; Throw exception if not.
        if (emailAddresses == null || emailAddresses.length == 0) {
            throw new ShareException("At least one e-mail address is required to share");
        } else if (assetPaths == null || assetPaths.length == 0) {
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

                String url = assetDetailsResolver.getUrl(config, asset);

                if (StringUtils.isBlank(url)) {
                    log.warn("Could not determine an Asset Details page path for asset at [ {} ]", assetPath);
                    continue;
                }

                url += asset.getPath();

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
     * @param userData the Map of data provided by the end-user (usually derived from the Request) to use in the email.
     * @return the protected Map; all String's are xss protected for HTML.
     */
    private Map<String, Object> xssProtectUserData(Map<String, Object> userData) {
        for(final Map.Entry<String, Object> entry : userData.entrySet()) {
            if (entry.getValue() instanceof String) {
                userData.put(entry.getKey(), xssAPi.encodeForHTML((String)entry.getValue()));
            }
        }

        return userData;
    }


    @Activate
    protected final void activate(final Cfg config) throws Exception {
        this.cfg = config;
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
