package com.adobe.aem.commons.assetshare.components.search.impl;

import com.adobe.aem.commons.assetshare.components.search.Statistics;
import com.adobe.aem.commons.assetshare.search.Search;
import com.adobe.aem.commons.assetshare.util.ModelCache;
import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ExporterConstants;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;

@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {Statistics.class, ComponentExporter.class},
        resourceType = {StatisticsImpl.RESOURCE_TYPE},
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
@Exporter(name = ExporterConstants.SLING_MODEL_EXPORTER_NAME, extensions = ExporterConstants.SLING_MODEL_EXTENSION)
public class StatisticsImpl implements Statistics {
    public static final String RESOURCE_TYPE = "asset-share-commons/components/search/statistics";

    @Self
    private SlingHttpServletRequest request;

    @Self
    private ModelCache modelCache;

    private Search search;

    private String id;

    @PostConstruct
    protected void init() {
        search = modelCache.get(Search.class);
    }

    @Override
    public String getId() {
        if (id == null) {
            id = "cmp-statistics--" + String.valueOf(Math.abs(request.getResource().getPath().hashCode() - 1));
        }

        return id;
    }

    @Override
    public long getRunningTotal() {
        return search.getResults().getRunningTotal();
    }

    @Override
    public long getTotal() {
        return search.getResults().getTotal();
    }

    @Override
    public boolean hasMore() {
        return search.getResults().isMoreThanTotal();
    }

    @Override
    public long getTimeTaken() {
        return search.getResults().getTimeTaken();
    }

    @Override
    public boolean isReady() {
        return search != null;
    }

    @Nonnull
    @Override
    public String getExportedType() {
        return RESOURCE_TYPE;
    }
}
