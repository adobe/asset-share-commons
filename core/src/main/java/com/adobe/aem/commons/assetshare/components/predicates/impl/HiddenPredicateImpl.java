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

package com.adobe.aem.commons.assetshare.components.predicates.impl;

import com.adobe.aem.commons.assetshare.components.predicates.AbstractPredicate;
import com.adobe.aem.commons.assetshare.components.predicates.HiddenPredicate;
import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ExporterConstants;
import com.day.cq.search.PredicateConverter;
import com.day.cq.search.PredicateGroup;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.AbstractResourceVisitor;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.factory.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.*;

@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {HiddenPredicate.class, ComponentExporter.class},
        resourceType = {HiddenPredicateImpl.RESOURCE_TYPE}
)
@Exporter(name = ExporterConstants.SLING_MODEL_EXPORTER_NAME, extensions = ExporterConstants.SLING_MODEL_EXTENSION)
public class HiddenPredicateImpl extends AbstractPredicate implements HiddenPredicate {
    protected static final String RESOURCE_TYPE = "asset-share-commons/components/search/hidden";

    private static final String NN_PREDICATES = "predicates";
    private static final String PN_PREDICATE = "predicate";
    private static final String PN_VALUE = "value";

    @Self
    @Required
    private SlingHttpServletRequest request;

    @SlingObject
    @Required
    private Resource resource;

    @OSGiService
    private ModelFactory modelFactory;

    @Override
    public boolean isReady() {
        // Hidden properties should never display to the end-user
        return false;
    }

    @Override
    public PredicateGroup getPredicateGroup() {

        final PredicateGroup hiddenPredicateGroup = new PredicateGroup("hiddenPredicate");

        final Map<String, String> params = new HashMap<>();

        if (resource == null) {
            return hiddenPredicateGroup;
        }

        final Resource predicates = resource.getChild(NN_PREDICATES);

        if (predicates == null) {
            return hiddenPredicateGroup;
        }

        final Iterator<Resource> iterator = predicates.listChildren();

        while (iterator.hasNext()) {
            final Resource predicateResource = iterator.next();
            final ValueMap predicateProperties = predicateResource.getValueMap();

            final String predicate = predicateProperties.get(PN_PREDICATE, String.class);
            final String value = predicateProperties.get(PN_VALUE, "");

            if (StringUtils.isNotBlank(predicate)) {
                params.put(predicate, value);
            }
        }

        hiddenPredicateGroup.addAll(PredicateConverter.createPredicates(params));

        return hiddenPredicateGroup;
    }

    @Override
    public String getGroup() {
        throw new UnsupportedOperationException("Hidden predicate groupIds are managed in the PagePredicateImpl automatically");
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Hidden predicates have no name");
    }


    /** Deprecated Methods **/

    @Override
    @Deprecated
    public Map<String, String> getParams(final int groupId) {
        final Map<String, String> params = new HashMap<>();

        if (resource == null) {
            return params;
        }

        final Resource predicates = resource.getChild(NN_PREDICATES);

        if (predicates == null) {
            return params;
        }

        final Iterator<Resource> iterator = predicates.listChildren();

        while (iterator.hasNext()) {
            final Resource predicateResource = iterator.next();
            final ValueMap predicateProperties = predicateResource.getValueMap();

            final String predicate = predicateProperties.get(PN_PREDICATE, String.class);
            final String value = predicateProperties.get(PN_VALUE, "");

            if (StringUtils.isNotBlank(predicate)) {
                params.put(groupId + "_group." + predicate, value);
            }
        }

        return params;
    }

    @Nonnull
    @Override
    public String getExportedType() {
        return RESOURCE_TYPE;
    }
}