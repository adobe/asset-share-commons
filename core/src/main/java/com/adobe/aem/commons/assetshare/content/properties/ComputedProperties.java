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

package com.adobe.aem.commons.assetshare.content.properties;

import org.osgi.annotation.versioning.ProviderType;

import java.util.List;

/**
 * This OSGi Service exposes the active list of Computed Properties.
 * <p>
 * This service takes into account the Computed Property's service ranking (OSGi Property) and ensures the Computed Property with the highest service ranking (per name) is used.
 * By default, service rankings are 0 if not specified. (All the Asset Share Commons provided service rankings are 0);
 */
@ProviderType
public interface ComputedProperties {
    /**
     * @return a list of the highest ranking (OSGi Service Ranking) Computed Properties by name (ComputedProperty.getName()).
     */
    List<ComputedProperty> getComputedProperties();
}
