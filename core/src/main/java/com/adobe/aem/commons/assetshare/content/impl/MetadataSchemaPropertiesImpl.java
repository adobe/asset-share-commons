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

package com.adobe.aem.commons.assetshare.content.impl;

import com.adobe.aem.commons.assetshare.content.MetadataProperties;
import com.day.cq.dam.commons.util.SchemaFormHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.AbstractResourceVisitor;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Component(service = MetadataProperties.class)
public class MetadataSchemaPropertiesImpl implements MetadataProperties {
    private static final Logger log = LoggerFactory.getLogger(MetadataSchemaPropertiesImpl.class);

    private static final String PN_FIELD_LABEL = "fieldLabel";
    private static final String PN_NAME = "name";

    private static final String NN_FIELD = "field";

    private static final String[] RT_FIELDS = { "granite/ui/components/foundation/form/field",
        "granite/ui/components/coral/foundation/form/field", "dam/gui/components/admin/schemafield" };

    private static final String[] METADATA_TYPES_PROPERTIES = {"metaType", "type"};

    @Override
    public Map<String, List<String>> getMetadataProperties(final SlingHttpServletRequest request) {
        return getMetadataProperties(request, Collections.EMPTY_LIST);
    }

    @Override
    public Map<String, List<String>> getMetadataProperties(final SlingHttpServletRequest request, final List<String> metadataFieldResourceTypes) {
        Map<String, List<String>> collectedMetadata = new HashMap<>();

        final Iterator<Resource> resourceIterator = SchemaFormHelper.getSchemaFormsIterator(request.getResourceResolver(),
            "/conf/global/settings/dam/adminui-extension/metadataschema", 0, 0);

        while (resourceIterator.hasNext()){
            final Resource resource = resourceIterator.next();

            if (resource.getValueMap().get("allowCustomization", true)) {
                final MetadataSchemaResourceVisitor visitor = new MetadataSchemaResourceVisitor(collectedMetadata, metadataFieldResourceTypes);
                visitor.accept(resource);
                collectedMetadata = visitor.getMetadata();
            }
        }

        return collectedMetadata;
    }



    private class MetadataSchemaResourceVisitor extends AbstractResourceVisitor {
        // propertyName : fieldLabels
        private final Map<String, List<String>> metadata;
        private final List<String> metadataFieldResourceTypes;

        private boolean widget = false;

        public MetadataSchemaResourceVisitor(Map<String, List<String>> metadata, List<String> metadataFieldResourceTypes) {
            this.metadata = metadata;
            this.metadataFieldResourceTypes = metadataFieldResourceTypes;
        }

        public final Map<String, List<String>> getMetadata() {
            return metadata;
        }

        @Override
        public void accept(final Resource resource) {
            visit(resource);

            if (!widget) {
                this.traverseChildren(resource.listChildren());
            }

            widget = false;
        }

        @Override
        protected void visit(final Resource resource) {
            widget = isWidget(resource);

            if (!widget) {
                return;
            }

            final ValueMap properties = resource.getValueMap();
            final String fieldLabel = properties.get(PN_FIELD_LABEL, String.class);
            final String propertyName = properties.get(PN_NAME, properties.get(NN_FIELD + "/" + PN_NAME, String.class));

            if (StringUtils.isNotBlank(fieldLabel) && StringUtils.isNotBlank(propertyName)) {
                if (metadata.containsKey(propertyName)) {
                    final List<String> tmp = metadata.get(propertyName);
                    if (!tmp.contains(fieldLabel)) {
                        tmp.add(fieldLabel);
                    }
                    metadata.put(propertyName, tmp);
                } else {
                    final ArrayList<String> tmp = new ArrayList<>();
                    tmp.add(fieldLabel);
                    metadata.put(propertyName, tmp);
                }
            }
        }


        private boolean isWidget(Resource resource) {
            if (resource == null) {
                return false;
            }

            if (metadataFieldResourceTypes != null && metadataFieldResourceTypes.size() > 0) {
                // Overriding the allowed field resource types; only match these.
                return metadataFieldResourceTypes.stream().anyMatch(metaType -> checkMetaDataType(resource, metaType));
            } else {
                // Default use the Granite UI (foundation and coral) Field resourceTypes
                return Arrays.stream(RT_FIELDS).anyMatch(resourceType -> resource.isResourceType(resourceType));
            }
        }

        private boolean checkMetaDataType(Resource resource, String metaType) {
            return Arrays.stream(METADATA_TYPES_PROPERTIES).anyMatch(metaTypeProperty-> resource.getValueMap().containsKey(metaTypeProperty)
                && resource.getValueMap().get(metaTypeProperty).equals(metaType));
        }
    }
}
