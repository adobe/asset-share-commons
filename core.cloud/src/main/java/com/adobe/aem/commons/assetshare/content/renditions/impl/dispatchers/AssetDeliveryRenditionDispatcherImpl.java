/*
 * Asset Share Commons
 *
 * Copyright (C) 2023 Adobe
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

package com.adobe.aem.commons.assetshare.content.renditions.impl.dispatchers;

import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.content.renditions.*;
import com.adobe.aem.commons.assetshare.util.ExpressionEvaluator;
import com.adobe.aem.commons.assetshare.util.RequireAem;
import com.adobe.cq.wcm.spi.AssetDelivery;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.DamConstants;
import com.google.common.base.Splitter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.commons.mime.MimeTypeService;
import org.apache.sling.models.factory.ModelFactory;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.osgi.framework.Constants.SERVICE_RANKING;

@Component(
        property = {
                SERVICE_RANKING + ":Integer=" + -10000,
                "webconsole.configurationFactory.nameHint={name} [ {label} ] @ {service.ranking}"
        }
)
@Designate(
        ocd = AssetDeliveryRenditionDispatcherImpl.Cfg.class,
        factory = true
)
public class AssetDeliveryRenditionDispatcherImpl extends AbstractRenditionDispatcherImpl implements AssetRenditionDispatcher {
    private static final Logger log = LoggerFactory.getLogger(AssetDeliveryRenditionDispatcherImpl.class);

    private static Long PLACEHOLDER_SIZE_IN_BYTES = 104857600L; // 100MB

    public static final String[] ALLOWED_FORMATS = new String[] {  "gif", "png", "png8", "jpg", "jpeg", "jpe", "pjpg", "bjpg", "webp", "webpll", "webply" };
    public static final String PN_PATH = "path";
    public static final String PN_FORMAT = "format";
    public static final String PN_SEONAME = "seoname";
    public static final String DEFAULT_FORMAT = "webp";
    final String[] ACCEPTED_MIME_TYPES = {
            "image/.*",
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",    // PPT
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",      // DOC
    };

    private Cfg cfg;

    private ConcurrentHashMap<String, String> mappings;

    @Reference
    private RequireAem requireAem;

    @Reference
    private AssetDelivery assetDelivery;

    @Reference
    private ModelFactory modelFactory;

    @Reference
    private ExpressionEvaluator expressionEvaluator;

    @Reference
    private AssetRenditions assetRenditions;

    @Reference
    private MimeTypeService mimeTypeService;

    @Reference(
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            policyOption = ReferencePolicyOption.GREEDY
    )
    private volatile AssetRenditionTracker assetRenditionTracker;

    @Override
    public String getLabel() {
        return cfg.label();
    }

    @Override
    public String getName() {
        return cfg.name();
    }

    @Override
    public Map<String, String> getOptions() {
        return assetRenditions.getOptions(mappings);
    }

    @Override
    public boolean isHidden() {
        return cfg.hidden();
    }

    @Override
    public Set<String> getRenditionNames() {
        if (mappings == null) {
            return Collections.EMPTY_SET;
        } else {
            return mappings.keySet();
        }
    }

    @Override
    public List<String> getTypes() {
        if (cfg.types() != null) {
            return Arrays.asList(cfg.types());
        }

        return Collections.EMPTY_LIST;
    }

    @Override
    public void dispatch(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        final AssetRenditionParameters parameters = new AssetRenditionParameters(request);

        final String expression = mappings.get(parameters.getRenditionName());
        // Special case where we use a limited set of expression evaluation since Asset Delivery is limited in its configurations
        final String renditionRedirect = getDeliveryURL(evaluateExpression(request, expression), parameters.getAsset());

        if (StringUtils.isNotBlank(renditionRedirect)) {
            if (log.isDebugEnabled()) {
                log.debug("Serving Asset Delivery redirect rendition [ {} ] for resolved rendition name [ {} ]",
                        renditionRedirect,
                        parameters.getRenditionName());
            }

            if (assetRenditionTracker != null) {
                assetRenditionTracker.track(this, request, parameters, renditionRedirect);
            }

            if (cfg.redirect() == HttpServletResponse.SC_MOVED_TEMPORARILY) {
                response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
            } else {
                response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
            }

            response.setHeader("Location", renditionRedirect);

        } else {
            log.error("Could not convert [ {} ] into a valid URI", renditionRedirect);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Could not serve asset rendition.");
        }
    }

    @Override
    public AssetRendition getRendition(AssetModel assetModel, AssetRenditionParameters parameters) {
        final String expression = mappings.get(parameters.getRenditionName());
        // Special case where we use a limited set of expression evaluation since Asset Delivery is limited in its configurations
        final String renditionRedirect = getDeliveryURL(evaluateExpression(parameters.getAsset(), expression), parameters.getAsset());

        if (StringUtils.isNotBlank(renditionRedirect)) {
            try {
                List<NameValuePair> params = URLEncodedUtils.parse(new URI(renditionRedirect), Charset.forName("UTF-8"));
                String extension = StringUtils.defaultIfBlank(params.stream().filter(p -> p.getName().equals(PN_FORMAT)).findFirst().map(NameValuePair::getValue).orElse(null), DEFAULT_FORMAT);

                if (log.isDebugEnabled()) {
                    log.debug("Downloading Asset Delivery rendition [ {} ] for resolved rendition name [ {} ]",
                            renditionRedirect,
                            parameters.getRenditionName());
                }

                if (assetRenditionTracker != null) {
                    assetRenditionTracker.track(this, assetModel, parameters, renditionRedirect);
                }

                return new AssetRendition(renditionRedirect, PLACEHOLDER_SIZE_IN_BYTES, mimeTypeService.getMimeType(extension));
            } catch (URISyntaxException e) {
                log.warn("Unable to create a valid URI for rendition redirect [ {} ]", renditionRedirect, e);
                // Still sending to Async Download Framework so we can get a failure
                return new AssetRendition("failed://to.create.valid.uri.from.asset.delivery.rendition.redirect", 0L, "invalid/uri");
            }
        }

        return null;
    }

    /**
     * Only accept if this is running on AEM as a Cloud Service cloud, the asset is of type image or document, and the renditionName is one of the configured mappings.
     *
     * @param assetModel the asset to dispatch
     * @param renditionName the renditionName of the asset to dispatcher
     * @return if this rendition dispatcher should be used.
     */
    @Override
    public boolean accepts(AssetModel assetModel, String renditionName) {
        if (assetDelivery == null) {
            // the AssetDelivery OSGi service is not available, so we can't use this dispatcher; this means we're running on an AEM SDK (non-Cloud)
            return false;
        } else if (!getRenditionNames().contains(renditionName)) {
            // the renditionName is not one of the configured mappings
            return false;
        }

        // Asset Delivery only supports image and document formats; it cannot generate renditions for video, audio, etc. at this time.
        final String assetFormat = StringUtils.lowerCase(assetModel.getProperties().get(DamConstants.DC_FORMAT, String.class));

        // Return true if assetFormat matches any regex in ALLOWED_MIME_TYPES
        if (StringUtils.isBlank(assetFormat)) {
            // If the asset format is not set, we can't determine if it's an image or document, so we can't use Asset Delivery
            return false;
        } else {
            // Otherwise, check if the asset format is an image or document
            return Arrays.stream(ACCEPTED_MIME_TYPES).anyMatch(regex -> assetFormat.matches(regex));
        }
    }

    protected String evaluateExpression(final SlingHttpServletRequest request, String expression) {
        final
        Asset asset = request.getResource().adaptTo(Asset.class);
        return evaluateExpression(asset, expression);
    }

    protected String evaluateExpression(final Asset asset, String expression) {
        final AssetModel assetModel = modelFactory.createModel(asset.adaptTo(Resource.class), AssetModel.class);

        expression = expressionEvaluator.evaluateAssetExpression(expression, assetModel);
        expression = expressionEvaluator.evaluateProperties(expression, assetModel);

        return expression;
    }

    protected String getDeliveryURL(String expression, Asset asset) {
        final Map<String, String> map = Splitter.on("&").withKeyValueSeparator("=").split(expression);
        final Map<String, Object> params = map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()));

        // The following properties are REQUIRED by the AssetDelivery service, so add them with defaults if they do not exist
        if (StringUtils.isBlank((String) params.get(PN_PATH))) {
            params.put(PN_PATH, asset.getPath());
        }

        if (StringUtils.isBlank((String) params.get(PN_FORMAT)) || !ArrayUtils.contains(ALLOWED_FORMATS, params.get(PN_FORMAT))) {
            params.put(PN_FORMAT, DEFAULT_FORMAT);
        }

        if (StringUtils.isBlank((String) params.get(PN_SEONAME))) {
            if (StringUtils.isNotBlank(asset.getMetadataValue(DamConstants.DC_TITLE))) {
                params.put(PN_SEONAME, asset.getMetadataValue(DamConstants.DC_TITLE).replaceAll("[^a-zA-Z0-9]", "-"));
            } else if (StringUtils.isNotBlank(asset.getName())) {
                params.put(PN_SEONAME, asset.getName().replaceAll("[^a-zA-Z0-9]", "-"));
            } else {
                params.put(PN_SEONAME, "asset");
            }
        }

        return assetDelivery.getDeliveryURL(asset.adaptTo(Resource.class), params);
    }

    @Activate
    protected void activate(Cfg cfg) {
        this.cfg = cfg;

        this.mappings = super.parseMappingsAsStrings(cfg.rendition_mappings());
    }

    @ObjectClassDefinition(name = "Asset Share Commons - Rendition Dispatcher - Asset Delivery Renditions")
    public @interface Cfg {
        @AttributeDefinition
        String webconsole_configurationFactory_nameHint() default "{name} [ {label} ] @ {service.ranking}";

        @AttributeDefinition(
                name = "Name",
                description = "The system name of this Rendition Dispatcher. This should be unique across all AssetRenditionDispatcher instances."
        )
        String name() default "aem-asset-delivery-redirect";

        @AttributeDefinition(
                name = "Label",
                description = "The human-friendly name of this AssetRenditionDispatcher and may be displayed to authors."
        )
        String label() default "Web-optimized Renditions";

        @AttributeDefinition(
                name = "Rendition types",
                description = "The types of renditions this configuration will return. Ideally all renditions in this configuration apply types specified here. This is used to drive and scope the Asset Renditions displays in Authoring datasources. OOTB types are: `image` and `video`"
        )
        String[] types() default {};

        @AttributeDefinition(
                name = "Hide renditions",
                description = "Hide if this AssetRenditionDispatcher configuration is not intended to be exposed to AEM authors for selection in dialogs.",
                type = AttributeType.BOOLEAN
        )
        boolean hidden() default false;

        @AttributeDefinition(
                name = "Redirect",
                description = "Select the type of redirect that should be made: Moved Permanently (301) or Moved Temporarily (302). Defaults to 301.",
                options = {
                        @Option(label = "Moved Permanently (301)", value = "301"),
                        @Option(label = "Moved Temporarily (302)", value = "302"),
                }
        )
        int redirect() default HttpServletResponse.SC_MOVED_PERMANENTLY;

        @AttributeDefinition(
                name = "Rendition mappings",
                description = "In the form: <rendition name>" + OSGI_PROPERTY_VALUE_DELIMITER + "<delivery parameters> -- Example delivery parameters: format=webp&preferwebp=true&width=98&quality=50"
        )
        String[] rendition_mappings() default {};

        @AttributeDefinition(
                name = "Service ranking",
                description = "The larger the number, the higher the precedence.",
                type = AttributeType.INTEGER
        )
        int service_ranking() default 0;
    }
}