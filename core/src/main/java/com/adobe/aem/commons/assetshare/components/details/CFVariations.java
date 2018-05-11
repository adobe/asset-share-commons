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

import com.adobe.aem.commons.assetshare.components.Component;
import com.adobe.cq.dam.cfm.VariationDef;
import org.osgi.annotation.versioning.ProviderType;

import java.util.Collection;

@ProviderType
public interface CFVariations extends Component {

    /**
     * Returns all variations of a content fragment using Asset API
     * @return a collection of objects of type CFVariation
     */
    Collection<VariationDef> getVariations();

}
