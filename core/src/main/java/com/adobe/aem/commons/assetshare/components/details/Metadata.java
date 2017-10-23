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

@ConsumerType
public interface Metadata extends EmptyTextComponent {

    /***
     *
     * @return an AssetModel representing the current Asset (suffix)
     */
    AssetModel getAsset();

    /***
     * Returns a ValueMap (from AssetModel) composed of a look up based on: 1)
     * ComputedProperties, the [dam:Asset]/jcr:content/metadata ValueMap and
     * finally the [dam:Asset] ValueMap.
     *
     * @return
     */
    ValueMap getProperties();

    /***
     * Return the expected data type of the metadata property
     *
     * @return
     */
    DataType getType();

    /***
     * Returns the format pattern for the specified metadata field. Expected to
     * be an HTL recognized Date Format or Number format.
     */
    String getFormat();

    /***
     *
     * @return String representing the raw metadata property name
     */
    String getPropertyName();

    /***
     * Get the locale for formatting purposes. Driven by the current page's
     * properties Language selection.
     *
     * @return String language code
     */
    String getLocale();

    enum DataType {
        COMPUTED("computed"), TEXT("text"), DATE("date"), NUMBER("number"), BOOLEAN("boolean");

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
