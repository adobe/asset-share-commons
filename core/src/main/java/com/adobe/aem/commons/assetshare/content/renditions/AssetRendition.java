/*
 * Asset Share Commons
 *
 * Copyright (C) 2019 Adobe
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

package com.adobe.aem.commons.assetshare.content.renditions;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Sling Model that is used by HTL scripts to generate URLs for AssetRenditions of AssetModels.
 *<p>
 * &lt;img data-sly-use.rendition="${'com.adobe.aem.commons.assetshare.content.renditions.AssetRendition' @ asset=myAssetModel, renditionName = `web`, renditionDownload = true }<br>
 *     src="${img.url}"/&gt;
 */
@ProviderType
public interface AssetRendition {
    /**
     * Gets the URL for the specified rendition name.
     * This method is used by HTL which passes in the following parameters in via Request Attributes: asset, renditionName, renditionDownload
     *
     * @return the URL to the asset rendition.
     */
    String getUrl();

    /**
     *
     */
    final class UrlParams {
        private String renditionName;
        private boolean download;

        public UrlParams() {
        }

        public UrlParams(final String renditionName, final boolean download) {
            this.renditionName = renditionName;
            this.download = download;
        }

        public String getRenditionName() {
            return renditionName;
        }

        public void setRenditionName(String renditionName) {
            this.renditionName = renditionName;
        }

        public boolean isDownload() {
            return download;
        }

        public void setDownload(boolean download) {
            this.download = download;
        }
    }

}
