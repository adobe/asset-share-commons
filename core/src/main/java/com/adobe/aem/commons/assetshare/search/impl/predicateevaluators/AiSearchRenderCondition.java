/*
 * Asset Share Commons
 *
 * Copyright (C) 2018 Adobe
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

package com.adobe.aem.commons.assetshare.search.impl.predicateevaluators;

import com.adobe.aem.commons.assetshare.util.RequireAem;
import com.adobe.granite.ui.components.rendercondition.RenderCondition;
import com.adobe.granite.ui.components.rendercondition.SimpleRenderCondition;
import com.day.cq.search.Predicate;
import com.day.cq.search.eval.EvaluationContext;
import com.day.cq.search.eval.FulltextPredicateEvaluator;
import com.day.cq.search.eval.PredicateEvaluator;
import com.day.cq.search.facets.FacetExtractor;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;

import javax.jcr.query.Row;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Comparator;

/**
 * This is a wrapper about the AEM OOTB FulltextPredicateEvaluator that adds AI Search support
 */
@Component(
        service = { Servlet.class },
        property = {
                ServletResolverConstants.SLING_SERVLET_METHODS + "=GET",
                ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES + "=asset-share-commons/authoring/renderconditions/ai-search"
        }
)
public class AiSearchRenderCondition extends SlingSafeMethodsServlet {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(AiSearchRenderCondition.class);

    public static boolean isAiSearchEnabled() {
        String inferenceEnabledStr = System.getProperty("oak.query.InferenceEnabled");
        boolean inferenceEnabled = inferenceEnabledStr != null && Boolean.parseBoolean(inferenceEnabledStr) == Boolean.TRUE;
        return inferenceEnabled;
    }

    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute(RenderCondition.class.getName(), new SimpleRenderCondition(isAiSearchEnabled()));
    }
}




