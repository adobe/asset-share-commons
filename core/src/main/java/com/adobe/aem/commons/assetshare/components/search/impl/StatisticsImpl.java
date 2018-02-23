package com.adobe.aem.commons.assetshare.components.search.impl;

import com.adobe.aem.commons.assetshare.components.search.Statistics;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;

@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {Statistics.class},
        resourceType = {StatisticsImpl.RESOURCE_TYPE},
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class StatisticsImpl implements Statistics {
    public static final String RESOURCE_TYPE = "asset-share-commons/components/search/statistics";

    @Self
    private SlingHttpServletRequest request;

    private String id;

    @Override
    public String getId() {
        if (id == null) {
            id = "cmp-statistics--" + String.valueOf(Math.abs(request.getResource().getPath().hashCode() - 1));
        }

        return id;
    }
}
