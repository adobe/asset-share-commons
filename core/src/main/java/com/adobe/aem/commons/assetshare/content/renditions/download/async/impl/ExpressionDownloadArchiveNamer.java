/*
 * Asset Share Commons
 *
 * Copyright (C) 2021 Adobe
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

package com.adobe.aem.commons.assetshare.content.renditions.download.async.impl;

import com.adobe.aem.commons.assetshare.components.actions.download.impl.DownloadImpl;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRendition;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditions;
import com.adobe.aem.commons.assetshare.content.renditions.download.DownloadExtensionResolver;
import com.adobe.aem.commons.assetshare.content.renditions.download.async.DownloadArchiveNamer;
import com.adobe.aem.commons.assetshare.content.renditions.download.async.DownloadTargetParameters;
import com.adobe.cq.dam.download.api.DownloadTarget;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.commons.mime.MimeTypeService;
import org.osgi.service.component.annotations.*;

import static org.osgi.framework.Constants.SERVICE_RANKING;

@Component(
        property = {
                SERVICE_RANKING + ":Integer=" + -10000
        }
)
public class ExpressionDownloadArchiveNamer implements DownloadArchiveNamer {
    @Reference
    private AssetRenditions assetRenditions;

    @Reference
    private MimeTypeService mimeTypeService;

    @Reference(policy = ReferencePolicy.DYNAMIC,
            policyOption = ReferencePolicyOption.GREEDY,
            cardinality = ReferenceCardinality.OPTIONAL
    )
    private volatile DownloadExtensionResolver downloadExtensionResolver;

    @Override
    public String getArchiveFilePath(final AssetModel assetModel, final AssetRendition assetRendition, final DownloadTarget downloadTarget) {
        final ResourceResolver resourceResolver = assetModel.getResource().getResourceResolver();
        final Resource downloadComponentResource = resourceResolver.getResource(downloadTarget.getParameter(DownloadTargetParameters.DOWNLOAD_COMPONENT_PATH.toString(), String.class));
        final String renditionName = downloadTarget.getParameter(DownloadTargetParameters.RENDITION_NAME.toString(), String.class);

        if (downloadComponentResource == null || renditionName == null) {
            return null;
        }

        // Attempt to use custom extension logic if implemented first
        String extension = resolveDownloadExtension(assetModel, assetRendition);

        // If extension is null that means the custom extension logic doesnt exist or its deferring to default behavior provided by Sling
        // Since Sling requires a mimeType to derive and extension, make sure its not blank
        if (extension == null && StringUtils.isNotBlank(assetRendition.getMimeType())) {
            extension = mimeTypeService.getExtension(assetRendition.getMimeType());
        }

        // Add extension's dot if necessary
        if (StringUtils.isNotBlank(extension) && !StringUtils.startsWith(extension, ".")) {
            extension = "." + extension;
        }

        // This gets the value from the Download Modal component definition; TBD if this is better moved to DownloadImpl, and then retrieved by adapting that to a SlingModel and using a getter.
        // The main problem is this context does not have access to the request object required for getting the model.
        final String expression = downloadComponentResource.getValueMap().get(DownloadImpl.PN_ARCHIVE_FILE_NAME_EXPRESSION, DownloadImpl.DEFAULT_ARCHIVE_FILE_NAME_EXPRESSION);

        if (StringUtils.isNotBlank(expression)) {
           return assetRenditions.evaluateExpression(assetModel, renditionName, expression) + StringUtils.defaultIfBlank(extension, "");
        } else {
            return null;
        }
    }

    private String resolveDownloadExtension(AssetModel assetModel, AssetRendition assetRendition) {
        if (downloadExtensionResolver != null) {
            return downloadExtensionResolver.resolve(assetModel, assetRendition);
        } else {
            return null;
        }
    }
}




