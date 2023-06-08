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

package com.adobe.aem.commons.assetshare.components.details;

import com.adobe.aem.commons.assetshare.content.AssetModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.annotation.versioning.ConsumerType;

import java.io.IOException;
import java.util.List;

@ConsumerType
public interface Metadata extends EmptyTextComponent {

    /***
     * @return an AssetModel representing either the request's resource (if it is resolvable to an Asset) OR the request's suffix path (if that resolved to an Asset)
     */
    AssetModel getAsset();

    /***
     * Returns a ValueMap (from AssetModel) composed of a look up based on:
     * 1) ComputedProperties
     * 2) [dam:Asset]'s ValueMap
     * 3) [dam:Asset]/jcr:content/metadata's ValueMap
     *
     * @return a value map of asset properties. See method description for more details.
     */
    ValueMap getProperties();

    /***
     * @return the expected data type of the metadata property.
     */
    DataType getType();

    /***
     * @return the format pattern for the specified metadata field. Expected to be an HTL recognized Date Format or Number format.
     */
    String getFormat();

    /***
     *
     * @return String representing the raw metadata property name.
     */
    String getPropertyName();

    /***
     * Get the locale for formatting purposes. Driven by the current page's properties Language selection.

     * @return String language code.
     */
    String getLocale();

    /**
     * @return the values for the selected property adn type
     */
    List<String> getValues() throws IOException;

    /**
     * The DataType are the types of data supported by the Metadata Component that support special formatting.
     */
    enum DataType {
        COMPUTED("computed"), TEXT("text"), DATE("date"), NUMBER("number"), BOOLEAN("boolean"), JSON("json");

        private String value;

        DataType(String value) {
            this.value = value;
        }

        public static DataType fromString(String value) {
            for (DataType type : DataType.values()) {
                if (StringUtils.equals(value, type.value)) {
                    return type;
                }
            }
            return null;
        }

        public String getValue() {
            return value;
        }
    }
}