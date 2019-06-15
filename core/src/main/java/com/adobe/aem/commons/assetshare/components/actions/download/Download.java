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

package com.adobe.aem.commons.assetshare.components.actions.download;

import com.adobe.aem.commons.assetshare.content.AssetModel;
import org.osgi.annotation.versioning.ProviderType;

import java.util.Collection;

/**
 * The model interface that represents the Download action component.
 */
@ProviderType
public interface Download {
    /**
     * @return a collection of assets that are to be downloaded.
     */
    Collection<AssetModel> getAssets();

    /**
     * @return the name of the zip file that contains the assets (and their renditions) to download.
     */
    String getZipFileName();

    /**
     * 
     * @return a long representing the maximum content size limit allowed
     */
    long getMaxContentSize();

    /**
     * @return a long representing the size of the contents about to be downloaded
     */
    long getDownloadContentSize();

    /**
     * @return a string with a human readable label of the max content size limit
     */
    String getMaxContentSizeLabel();

    /**
     * @return a string with a human readable label of the current download size
     */
    String getDownloadContentSizeLabel();

}
