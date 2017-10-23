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

package com.adobe.aem.commons.assetshare.search.providers.impl;


import com.adobe.aem.commons.assetshare.search.providers.SearchProvider;
import com.adobe.aem.commons.assetshare.search.results.AssetResult;
import com.adobe.aem.commons.assetshare.search.results.FolderResult;
import com.adobe.aem.commons.assetshare.search.results.Result;
import com.adobe.aem.commons.assetshare.search.results.Results;
import com.adobe.aem.commons.assetshare.search.results.impl.ResultsImpl;
import com.day.cq.dam.commons.util.DamUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.osgi.framework.Constants.SERVICE_RANKING;

@Component(property = {
        SERVICE_RANKING + ":Integer=0"
})
public class FolderSearchProviderImpl implements SearchProvider {
    private static final Logger log = LoggerFactory.getLogger(FolderSearchProviderImpl.class);

    private static final String NT_SLING_FOLDER = "sling:Folder";
    private static final String NT_SLING_ORDERED_FOLDER = "sling:OrderedFolder";

    private static final String MODE = "folder";
    private static final String PATH = "path";


    public boolean accepts(SlingHttpServletRequest request) {
        final String[] selectors = request.getRequestPathInfo().getSelectors();

        if (selectors.length >= 1) {
            return MODE.equalsIgnoreCase(selectors[0]);
        } else {
            return false;
        }
    }

    public Results getResults(final SlingHttpServletRequest request) {
        final long start = System.currentTimeMillis();
        final List<Resource> roots = getRoots(request);
        final Resource folder = getFolder(request, roots);
        final long timeTaken = System.currentTimeMillis() - start;

        if (folder == null) {
            return new ResultsImpl(getRootContents(roots), timeTaken);
        } else {
            return new ResultsImpl(getFolderContents(folder), timeTaken);
        }
    }

    private Resource getFolder(final SlingHttpServletRequest request, final List<Resource> roots) {
        final String path = StringUtils.trimToNull(request.getParameter(PATH));

        // Make sure the request path is covered by the configured roots
        if (path != null) {
            for (final Resource root : roots) {
                if (StringUtils.startsWithIgnoreCase(path, root.getPath())) {
                    return request.getResourceResolver().getResource(path);
                }
            }
        }

        return null;
    }

    private List<Resource> getRoots(final SlingHttpServletRequest request) {
        final List<Resource> roots = new ArrayList<Resource>();

        roots.add(request.getResourceResolver().getResource("/content/dam/we-retail"));

        return roots;
    }

    private List<Result> getFolderContents(final Resource folder) {
        return getResults(folder.getChildren());
    }

    private List<Result> getRootContents(final List<Resource> roots) {
        if (roots.size() > 1) {
            return getResults(roots);
        } else if (roots.size() == 1) {
            return getResults(roots.get(0).getChildren());
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    private List<Result> getResults(final Iterable<Resource> resources) {
        final List<Result> results = new ArrayList<Result>();

        for (final Resource resource : resources) {

            if (DamUtil.isAsset(resource)) {
                final AssetResult result = resource.adaptTo(AssetResult.class);
                if (result != null) {
                    results.add(result);
                }
            } else if (resource.isResourceType(NT_SLING_FOLDER) || resource.isResourceType(NT_SLING_ORDERED_FOLDER)) {
                final FolderResult result = resource.adaptTo(FolderResult.class);
                if (result != null) {
                    results.add(result);
                }
            }
        }

        log.debug("Collected [ {} ] results from [ {} ]", results.size(), this.getClass().getName());
        return results;
    }
}
