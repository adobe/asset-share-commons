package com.adobe.aem.commons.assetshare.components.predicates.impl;

import com.adobe.aem.commons.assetshare.components.predicates.AbstractNestedPredicate;
import com.adobe.aem.commons.assetshare.components.predicates.NestedPathPredicate;
import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ExporterConstants;
import com.day.cq.search.eval.PathPredicateEvaluator;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import javax.annotation.Nonnull;

@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {NestedPathPredicate.class, ComponentExporter.class},
        resourceType = {NestedPathPredicateImpl.RESOURCE_TYPE},
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
@Exporter(name = ExporterConstants.SLING_MODEL_EXPORTER_NAME, extensions = ExporterConstants.SLING_MODEL_EXTENSION)

public class NestedPathPredicateImpl extends AbstractNestedPredicate implements NestedPathPredicate {
    public static final String RESOURCE_TYPE = "asset-share-commons/components/search/nested-path";    

    @Override
    public String getName() {
        return PathPredicateEvaluator.PATH;
    }
    @Nonnull
    @Override
    public String getExportedType() {
        return RESOURCE_TYPE;
    }
}