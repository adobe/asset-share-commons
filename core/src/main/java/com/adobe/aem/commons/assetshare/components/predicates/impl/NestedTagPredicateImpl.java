package com.adobe.aem.commons.assetshare.components.predicates.impl;

import com.adobe.aem.commons.assetshare.components.predicates.AbstractNestedPredicate;
import com.adobe.aem.commons.assetshare.components.predicates.NestedTagPredicate;
import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ExporterConstants;
import com.day.cq.tagging.TagConstants;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import javax.annotation.Nonnull;

@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {NestedTagPredicate.class, ComponentExporter.class},
        resourceType = {NestedTagPredicateImpl.RESOURCE_TYPE},
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
@Exporter(name = ExporterConstants.SLING_MODEL_EXPORTER_NAME, extensions = ExporterConstants.SLING_MODEL_EXTENSION)

public class NestedTagPredicateImpl extends AbstractNestedPredicate implements NestedTagPredicate {
    
    public static final String RESOURCE_TYPE = "asset-share-commons/components/search/nested-tag";    
    public static final String TAG_ID = "tagid";
    public static final String VALUES_KEY = "value";
    public static final String PROPERTY = "jcr:content/metadata/cq:tags";

    @Override
    public String getDescendantsType(Resource ancestor) {
        return TagConstants.NT_TAG;
    }
    @Override
    public String getName() {
        return TAG_ID;
    }    
    @Override
    public String getValuesKey() {
        return VALUES_KEY;
    }
    @Override
    public String getProperty() {
        return PROPERTY;
    }
    @Override
    public boolean hasOr() {
        return false;
    }
    @Nonnull
    @Override
    public String getExportedType() {
        return RESOURCE_TYPE;
    }
}