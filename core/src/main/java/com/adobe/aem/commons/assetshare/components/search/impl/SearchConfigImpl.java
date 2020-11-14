package com.adobe.aem.commons.assetshare.components.search.impl;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;

import com.adobe.aem.commons.assetshare.components.search.SearchConfig;
import com.adobe.aem.commons.assetshare.util.ResourceTypeVisitor;
import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ExporterConstants;
import com.day.cq.dam.api.DamConstants;
import com.day.cq.search.Predicate;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.ScriptVariable;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.factory.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {SearchConfig.class, ComponentExporter.class},
        resourceType = {SearchConfigImpl.RESOURCE_TYPE}
)
@Exporter(name = ExporterConstants.SLING_MODEL_EXPORTER_NAME, extensions = ExporterConstants.SLING_MODEL_EXTENSION)
public class SearchConfigImpl implements SearchConfig, ComponentExporter {
    private static final Logger log = LoggerFactory.getLogger(SearchConfigImpl.class);

    public static final String RESOURCE_TYPE = "asset-share-commons/components/search/results";

    private static final int DEFAULT_LIMIT = 50;

    private static final String DEFAULT_GUESS_TOTAL = "250";
    private static final String DEFAULT_ORDER_BY = "@jcr:score";
    private static final String DEFAULT_ORDER_BY_SORT = Predicate.SORT_DESCENDING;
    private static final boolean DEFAULT_ORDER_BY_CASE = true;

    private static final String DEFAULT_LAYOUT = "card";
    private static final String DEFAULT_SPID = "search";

    private final String[] DEFAULT_PATHS = {"/content/dam"};

    private String PN_ORDER_BY = "orderBy";
    private String PN_ORDER_BY_SORT = "orderBySort";
    private String PN_ORDER_BY_CASE = "orderByCase";
    private String PN_LIMIT = Predicate.PARAM_LIMIT;
    private String PN_PATHS = "paths";
    private String PN_LAYOUT = "layout";
    private String PN_GUESS_TOTAL = Predicate.PARAM_GUESS_TOTAL;
    private String PN_SPID = "searchProviderId";
    private String PN_SEARCH_PREDICATES = "searchPredicates";

    @Self
    private SlingHttpServletRequest request;

    @ScriptVariable
    private Page currentPage;

    @OSGiService
    private ModelFactory modelFactory;

    @SlingObject
    @Optional
    private Resource resource;

    private ValueMap properties;

    List<String> paths;

    @PostConstruct
    protected void init() {
        resource = resolveSearchConfigResource(request.getResourceResolver().adaptTo(PageManager.class),
                request.getResource());

        if (resource == null) {
            throw new IllegalArgumentException("Adaptable must resolve a search results component.");
        }

        properties = resource.getValueMap();
    }

    @Override
    public ValueMap getProperties() {
        return properties;
    }

    @Override
    public String getMode() {
        return properties.get(PN_SPID, DEFAULT_SPID);
    }

    @Override
    public String getLayout() {
        return properties.get(PN_LAYOUT, DEFAULT_LAYOUT);
    }

    @Override
    public String getGuessTotal() {
        final String guessTotal = properties.get(PN_GUESS_TOTAL, DEFAULT_GUESS_TOTAL);

        if (Boolean.TRUE.toString().equalsIgnoreCase(guessTotal)) {
            return Boolean.TRUE.toString();
        }

        try {
            int guessTotalAsNumber = Integer.parseInt(guessTotal);
            return guessTotalAsNumber < -1 ? DEFAULT_GUESS_TOTAL : String.valueOf(guessTotalAsNumber);
        } catch (NumberFormatException e) {
            return DEFAULT_GUESS_TOTAL;
        }
    }

    @Override
    public String getSearchProviderId() {
        return properties.get(PN_SPID, DEFAULT_SPID);
    }

    @Override
    public int getLimit() {
        return properties.get(PN_LIMIT, DEFAULT_LIMIT);
    }

    @Override
    public String getOrderBy() {
        return properties.get(PN_ORDER_BY, DEFAULT_ORDER_BY);
    }

    @Override
    public String getOrderBySort() {
        return properties.get(PN_ORDER_BY_SORT, DEFAULT_ORDER_BY_SORT);
    }

    @Override
    public boolean isOrderByCase() {
        return properties.get(PN_ORDER_BY_CASE, DEFAULT_ORDER_BY_CASE);
    }

    @Override
    public List<String> getPaths() {
        final List<String> paths  = Arrays.stream(properties.get(PN_PATHS, DEFAULT_PATHS)).filter(path ->
          StringUtils.equals(path, DamConstants.MOUNTPOINT_ASSETS) || StringUtils.startsWith(path, DamConstants.MOUNTPOINT_ASSETS)
        ).collect(Collectors.toList());

        return (paths.size() < 1) ? Arrays.asList(DEFAULT_PATHS) : paths;
    }

    @Override
    public List<String> getSearchPredicatesNames() {
        return Arrays.asList(properties.get(PN_SEARCH_PREDICATES, new String[]{}));
    }

    private Resource resolveSearchConfigResource(final PageManager pageManager, final Resource currentResource) {
        if (!isValidResource(currentResource)) {
            // Hit the sites tree root; stop looking!
            return null;
        } else if (currentResource.isResourceType(RESOURCE_TYPE)) {
            // We won the powerball! this passed in resource is the right resource!
            // Ok, not really the powerball, this happens when the Search Results component uses this model.
            return currentResource;
        }

        // Go look under each page, up the tree for the component.
        final ResourceTypeVisitor visitor = new ResourceTypeVisitor(new String[]{RESOURCE_TYPE});
        final Page page = pageManager.getContainingPage(currentResource);

        visitor.accept(page.getContentResource());

        if (visitor.getResources().size() > 0) {
            return visitor.getResources().iterator().next();
        } else if (page.getParent() != null) {
            return resolveSearchConfigResource(pageManager, page.getParent().getContentResource());
        } else {
            log.warn("Unable to locate a Search Results component resource that can represent the Search Config. It is likely the Search Results component has not been added to the Search page yet!");
            return null;
        }
    }


    private boolean isValidResource(Resource resource) {
        return resource != null && StringUtils.startsWith(resource.getPath(), "/content/");
    }

    @Nonnull
    @Override
    public String getExportedType() {
        return RESOURCE_TYPE;
    }
}