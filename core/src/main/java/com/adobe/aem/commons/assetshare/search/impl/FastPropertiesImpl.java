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

package com.adobe.aem.commons.assetshare.search.impl;

import com.adobe.aem.commons.assetshare.search.FastProperties;
import com.adobe.aem.commons.assetshare.util.impl.OakIndexResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.*;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Component(service = FastProperties.class)
@Designate(ocd = FastPropertiesImpl.Cfg.class)
public class FastPropertiesImpl implements FastProperties {
    private static final Logger log = LoggerFactory.getLogger(FastPropertiesImpl.class);

    private static final String PN_NAME = "name";

    private static final String SERVICE_NAME = "oak-index-definition-reader";

    protected static final String NN_OAK_INDEX = "/oak:index";

    protected static final String NN_DAM_ASSET_LUCENE_INDEX = "damAssetLucene";

    protected static final String INDEX_DEFINITION_RULES_SUB_PATH = "indexRules/dam:Asset/properties";

    protected static final Map<String, Object> AUTH_INFO = Collections.singletonMap(ResourceResolverFactory.SUBSERVICE, SERVICE_NAME);

    protected String oakIndexName = NN_DAM_ASSET_LUCENE_INDEX;

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Override
    public final List<String> getFastProperties() {
        return getFastProperties(Collections.EMPTY_LIST);
    }

    @Override
    public final List<String> getFastProperties(final String indexConfigFlagPropertyName) {
        return getFastProperties(Arrays.asList(indexConfigFlagPropertyName));
    }

    @Override
    public final List<String> getFastProperties(final List<String> indexConfigFlagPropertyNames) {
        final Set<String> fastProperties = new TreeSet<>();

        ResourceResolver resourceResolver = null;
        try {
            resourceResolver = resourceResolverFactory.getServiceResourceResolver(AUTH_INFO);

            final String resolvedOakIndexName = StringUtils.defaultIfBlank(OakIndexResolver.resolveRankingOakIndex(resourceResolver, oakIndexName), oakIndexName);
            final String[] indexDefinitionRulesPaths = new String[] { NN_OAK_INDEX + "/" + resolvedOakIndexName + "/" + INDEX_DEFINITION_RULES_SUB_PATH};

            for (final String indexDefinitionRulesPath : indexDefinitionRulesPaths) {
                final Resource damAssetIndexRulesResource = resourceResolver.getResource(indexDefinitionRulesPath);
                if (damAssetIndexRulesResource == null) {
                    log.warn("Could not locate Oak Index Definition Index Rules for dam:Asset at [ {} ]", indexDefinitionRulesPath);
                    continue;
                }

                final Iterator<Resource> indexRules = damAssetIndexRulesResource.listChildren();

                while (indexRules.hasNext()) {
                    final Resource indexRule = indexRules.next();
                    final ValueMap properties = indexRule.getValueMap();

                    final String relPath = StringUtils.stripToNull(properties.get(PN_NAME, String.class));

                    if (relPath != null
                            && (indexConfigFlagPropertyNames == null
                            || indexConfigFlagPropertyNames.isEmpty()
                            || indexConfigFlagPropertyNames.stream().allMatch(propertyName -> properties.get(propertyName, false)))) {
                        fastProperties.add(relPath);
                    }
                }
            }
        } catch (LoginException e) {
            log.error("Could not obtain the Asset Share Commons service user [ {} ]", SERVICE_NAME, e);
        } finally {
            if (resourceResolver != null) {
                resourceResolver.close();
            }
        }

        return new ArrayList<>(fastProperties);
    }

    public List<String> getDeltaProperties(final Collection<String> fastProperties, final Collection<String> otherProperties) {
        final List<String> delta = new ArrayList<>();

        for (final String fastProperty : fastProperties) {
            boolean found = false;
            for (String otherProperty : otherProperties) {
                if (StringUtils.equals(
                        StringUtils.removeStart(fastProperty, "./"),
                        StringUtils.removeStart(otherProperty, "./"))) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                delta.add(fastProperty);
            }
        }

        return delta;
    }

    @Override
    public String getFastLabel(final String label) {
        return FAST + "  " + label;
    }

    @Override
    public String getSlowLabel(final String label) {
        return SLOW + "  " + label;
    }

    @Activate
    protected void activate(Cfg cfg) {
        oakIndexName = cfg.oakIndexName();
    }

    @ObjectClassDefinition(name = "Asset Share Commons - Fast Properties")
    public @interface Cfg {

        @AttributeDefinition(name = "Oak Index Definition (Root) Name",
                             description = "The 'root' name of the index definitions rules paths to inspect to determine fast properties. These must be readable by the oak-index-definition-reader service user. Defaults to [ " + NN_DAM_ASSET_LUCENE_INDEX + " ].")
        String oakIndexName() default NN_DAM_ASSET_LUCENE_INDEX;

    }
}