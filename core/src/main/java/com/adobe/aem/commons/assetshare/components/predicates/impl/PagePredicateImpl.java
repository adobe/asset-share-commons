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
import com.adobe.aem.commons.assetshare.components.predicates.PagePredicate;
import com.adobe.aem.commons.assetshare.util.ComponentModelVisitor;
import com.adobe.aem.commons.assetshare.util.PredicateUtil;
import com.day.cq.dam.api.DamConstants;
import com.day.cq.search.eval.PathPredicateEvaluator;
import com.day.cq.wcm.api.Page;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.factory.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;

@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {PagePredicate.class},
        resourceType = {PagePredicateImpl.RESOURCE_TYPE},
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class PagePredicateImpl extends AbstractPredicate implements PagePredicate {
    protected static final String RESOURCE_TYPE = "asset-share-commons/components/search/results";

    private static final int MAX_GUESS_TOTAL = 2000;
    private static final int MAX_LIMIT = 1000;
    private static final int DEFAULT_LIMIT = 50;
    private static final String DEFAULT_GUESS_TOTAL = "250";
    private static final String DEFAULT_ORDER_BY = "@jcr:score";
    private static final String DEFAULT_ORDER_BY_SORT = "desc";
    private final String[] DEFAULT_PATHS = {"/content/dam"};

    private String PN_ORDERBY = "orderBy";
    private String PN_ORDERBY_SORT = "orderBySort";
    private String PN_LIMIT = "limit";
    private String PN_PATHS = "paths";

    @Self
    @Required
    SlingHttpServletRequest request;

    @Inject
    @Required
    private Page currentPage;

    @SlingObject
    @Required
    private Resource resource;

    @OSGiService
    private ModelFactory modelFactory;

    private ValueMap properties;

    @PostConstruct
    protected void init() {
        initPredicate(request, null);
        properties = resource.getValueMap();
    }

    @Override
    public String getName() {
        return "p";
    }

    @Override
    public boolean isReady() {
        return true;
    }


    public String getOrderBy() {
        final String value = PredicateUtil.getParamFromQueryParams(request, "orderby");
        return StringUtils.defaultIfBlank(value, properties.get(PN_ORDERBY, DEFAULT_ORDER_BY));
    }

    public String getOrderBySort() {
        final String value = PredicateUtil.getParamFromQueryParams(request, "orderby.sort");
        return StringUtils.defaultIfBlank(value, properties.get(PN_ORDERBY_SORT, DEFAULT_ORDER_BY_SORT));
    }

    public int getLimit() {
        final RequestParameter requestParameter = request.getRequestParameter("p.limit");
        int limit;

        if (requestParameter != null) {
            try {
                limit = Integer.parseInt(requestParameter.getString());
            } catch (NumberFormatException e) {
                limit = properties.get(PN_LIMIT, DEFAULT_LIMIT);
            }
        } else {
            limit = properties.get(PN_LIMIT, DEFAULT_LIMIT);
        }

        if (limit > MAX_LIMIT) {
            return MAX_LIMIT;
        } else if (limit < 1) {
            return DEFAULT_LIMIT;
        } else {
            return limit;
        }
    }

    public String getGuessTotal() {
        final String guessTotal = properties.get("guessTotal", DEFAULT_GUESS_TOTAL);

        if ("true".equalsIgnoreCase(guessTotal)) {
            return guessTotal;
        } else {
            try {
                int tmp = Integer.parseInt(guessTotal);

                if (tmp < 1 || tmp > MAX_GUESS_TOTAL) {
                    return DEFAULT_GUESS_TOTAL;
                } else {
                    return String.valueOf(tmp);
                }
            } catch (NumberFormatException e) {
                return DEFAULT_GUESS_TOTAL;
            }
        }
    }

    public List<String> getPaths() {
        final String[] uncheckedPaths = properties.get(PN_PATHS, DEFAULT_PATHS);
        final List<String> paths = new ArrayList<>();

        for (final String path : uncheckedPaths) {
            if (StringUtils.equals(path, DamConstants.MOUNTPOINT_ASSETS) || StringUtils.startsWith(path, DamConstants.MOUNTPOINT_ASSETS)) {
                paths.add(path);
            }
        }

        if (paths.size() < 1) {
            return Arrays.asList(DEFAULT_PATHS);
        } else {
            return paths;
        }
    }

    @Override
    public Map<String, String> getParams() {
        return getParams(new ParamTypes[]{});
    }


    public Map<String, String> getParams(ParamTypes... excludeParamTypes) {
        int systemGroupId = Integer.MAX_VALUE;
        final Map<String, String> params = new HashMap<>();

        int i = 0;

        if (!ArrayUtils.contains(excludeParamTypes, ParamTypes.NODE_TYPE)) {
            params.put("type", DamConstants.NT_DAM_ASSET);
        }

        if (!ArrayUtils.contains(excludeParamTypes, ParamTypes.PATH)) {
            final String pathGroup = String.valueOf(systemGroupId--) + "_group";
            params.put(pathGroup + ".p.or", "true");
            for (final String path : getPaths()) {
                params.put(pathGroup + "." + i++ + "_path", path);
            }
        }

        if (!ArrayUtils.contains(excludeParamTypes, ParamTypes.HIDDEN_PREDICATES)) {
            // Start large to avoid any conflicts w parameterized
            for (final HiddenPredicate hiddenPredicate : getHiddenPredicates(currentPage)) {
                params.putAll(hiddenPredicate.getParams(systemGroupId--));
            }
        }

        if (!ArrayUtils.contains(excludeParamTypes, ParamTypes.LIMIT)) {
            params.put("p.limit", String.valueOf(getLimit()));
        }

        if (!ArrayUtils.contains(excludeParamTypes, ParamTypes.GUESS_TOTAL)) {
            params.put("p.guessTotal", getGuessTotal());
        }

        return params;
    }

    private Collection<HiddenPredicate> getHiddenPredicates(final Page page) {
        final ComponentModelVisitor<HiddenPredicate> visitor = new ComponentModelVisitor<HiddenPredicate>(request,
                modelFactory,
                new String[]{HiddenPredicateImpl.RESOURCE_TYPE},
                HiddenPredicate.class);

        visitor.accept(page.getContentResource());
        return visitor.getModels();
    }
}