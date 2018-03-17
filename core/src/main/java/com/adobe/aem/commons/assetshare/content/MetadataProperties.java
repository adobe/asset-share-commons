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

package com.adobe.aem.commons.assetshare.content;

import org.apache.sling.api.SlingHttpServletRequest;
import org.osgi.annotation.versioning.ProviderType;

import java.util.List;
import java.util.Map;

@ProviderType
public interface MetadataProperties {
    /**
     * Collect the Metadata fields across Assets Metadata Schemas. To be collected the following must be true:
     * - The metadata field must be of resource type: metadataFieldResourceTypes
     * - Only the top-most matching field is collected (ie. in multi-value fields, the top level field is used).
     * - The field bust have a non-blank Label AND PropertyName defined.
     *
     * @param request the request object.
     * @param metadataFieldResourceTypes the sling:resourceTypes that identify candidate metadata schema widget resources.
     * @return a map, indexed by propertyName of propertyName: labels[] defined in all metadata schemas that meet the metadataFieldResourceTypes acceptance check.
     */
    Map<String, List<String>> getMetadataProperties(SlingHttpServletRequest request, List<String> metadataFieldResourceTypes);

    /**
     * Collect the Metadata fields across Assets Metadata Schemas. To be collected the following must be true:
     * - The metadata field must be of resource type: GraniteUI Field resource type.
     * - Only the top-most matching field is collected (ie. in multi-value fields, the top level field is used).
     * - The field bust have a non-blank Label AND PropertyName defined.
     *
     * @param request the request object.
     * @return a map, indexed by propertyName of propertyName: labels[] defined in all metadata schemas that meet the metadataFieldResourceTypes acceptance check.
     */
    Map<String, List<String>> getMetadataProperties(SlingHttpServletRequest request);
}
