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

package com.adobe.aem.commons.assetshare.search;

import org.osgi.annotation.versioning.ProviderType;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * OSGi Service interface to composed lists (usually for Granite DataSources) of JCR properties that have/have not been optimized for AEM search..
 */
@ProviderType
public interface FastProperties {
    String SLOW = "\uD83D\uDC22"; // Turtle in Unicode
    String FAST = "\u26A1"; // Lightning bolt in Unicode

    /**
     * Checks if the /oak:index/damAssetLucene index (or whatever may be overridden via FastPropertiesImpl OSGi Config) has a indexRule property config with the a property named indexConfigFlagPropertyName set to true.
     * If it does, this propertyName (ie. jcr:content/metadata/dc:title) is added to the return list.
     * @param indexConfigFlagPropertyName the oak index property name that acts as the true/false flag to check.
     * @return a list of property paths as who are configured with @{param indexConfigFlagPropertyName} set to `true`
     */
    List<String> getFastProperties(String indexConfigFlagPropertyName);

    /**
     * @param fastProperties a list relative property paths that are considered to be fast.
     * @param otherProperties a list of other relative property paths.
     * @return the delta between the fastProperties and the otherProperties.
     */
    List<String> getDeltaProperties(Collection<String> fastProperties, Collection<String> otherProperties);

    /**
     * @param label the label text
     * @return the FASE icon suffixed with the label.
     */
    String getFastLabel(String label);

    /**
     * @param label the label text
     * @return the SLOW icon suffixed with the label.
     */
    String getSlowLabel(String label);
}