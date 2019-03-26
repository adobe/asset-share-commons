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

package com.adobe.aem.commons.assetshare.content.properties;

import com.day.cq.dam.api.Asset;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.annotation.versioning.ConsumerType;

@ConsumerType
public interface ComputedProperty<T> {

    /**
     * Asset Share Common's Computed Properties Default Service Ranking
     */
    int DEFAULT_ASC_COMPUTED_PROPERTY_SERVICE_RANKING = -1;

    /**
     * This return value may NOT have a ?, & or = in it, as this will conflict with parameters.
     *
     * @return the computed property's name.
     */
    String getName();

    /**
     * @return the human-friendly label for this Computed Property.
     */
    String getLabel();

    /**
     * This is primarily used to select ComputedProperties for DataSources which drive Dropdown lists in the AEM Authoring UI.
     *
     * @return the types this ComputedProperty applies to.
     */
    String[] getTypes();

    /**
     * Method that indicates if the result via (get(..)) is cacheable by the ComputedPropertyAccessor.
     * This is highly encouraged if the same computed property is called many times in the context of the same Accessor (ie Model) and the resulting value will not/should not change.
     * <p>
     * The AbstractComputedProperty's implementation of this method returns true as this is the common use case.
     *
     * @return true if the value is cachable by the consumer of the ComputedProperty.
     */
    boolean isCachable();

    /**
     * @param asset the asset
     * @param request the request object
     * @param propertyName the computed property name
     * @return true if this ComputedProperty should accept the handling of this invocation.
     */
    boolean accepts(Asset asset, SlingHttpServletRequest request, String propertyName);

    /**
     *
     * @param asset the asset
     * @param propertyName the computed property name
     * @return true if this ComputedProperty should accept the handling of this invocation.
     */
    boolean accepts(Asset asset, String propertyName);

    /**
     *
     * @param asset the asset
     * @param request the request
     * @param parameters any parameters. If this method is implemented, it should handle the case where no parameters
     * @return the computed value.
     */
    T get(Asset asset, SlingHttpServletRequest request, ValueMap parameters);

    /**
     *
     * @param asset
     * @param request
     * @return the computed value.
     */
    T get(Asset asset, SlingHttpServletRequest request);

    /**
     *
     * @param asset
     * @param parameters
     * @return the computed value.
     */
    T get(Asset asset, ValueMap parameters);

    static final class Types {
        public static final String METADATA = "metadata";
        public static final String RENDITION = "rendition";
        public static final String URL = "url";
        public static final String VIDEO_RENDITION = "video-rendition";
    }
}
