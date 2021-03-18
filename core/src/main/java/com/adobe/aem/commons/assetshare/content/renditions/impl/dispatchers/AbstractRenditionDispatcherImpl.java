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

package com.adobe.aem.commons.assetshare.content.renditions.impl.dispatchers;

import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionDispatcher;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.apache.jackrabbit.JcrConstants.*;

public abstract class AbstractRenditionDispatcherImpl implements AssetRenditionDispatcher {
    private static Logger log = LoggerFactory.getLogger(AbstractRenditionDispatcherImpl.class);

    protected static final String OSGI_PROPERTY_VALUE_DELIMITER = "=";

    protected ConcurrentHashMap<String, Pattern> parseMappingsAsPatterns(final String[] renditionMappings) {
        final ConcurrentHashMap<String, Pattern> mappings = new ConcurrentHashMap<>();

        if (renditionMappings != null) {
            parseParameters(renditionMappings).forEach(segments ->
                    mappings.put(StringUtils.strip(segments[0]),
                            Pattern.compile(StringUtils.strip(segments[1]))));
        }

        return mappings;
    }

    protected ConcurrentHashMap<String, String> parseMappingsAsStrings(final String[] renditionMappings) {
        final ConcurrentHashMap<String, String> mappings = new ConcurrentHashMap<>();

        if (renditionMappings != null) {
            parseParameters(renditionMappings).forEach(segments ->
                    mappings.put(StringUtils.strip(segments[0]), StringUtils.strip(segments[1])));
        }

        return mappings;
    }

    private Stream<String[]> parseParameters(final String[] renditionMappings) {
        return Arrays.stream(renditionMappings)
                .map(mapping -> StringUtils.split(mapping, OSGI_PROPERTY_VALUE_DELIMITER))
                .filter(segments -> segments.length == 2)
                .filter(segments -> StringUtils.isNotBlank(segments[0]))
                .filter(segments -> StringUtils.isNotBlank(segments[1]));
    }

    protected long getBinaryResourceSizeInBytes(final Resource resource) {
        if (resource != null && !ResourceUtil.isNonExistingResource(resource)) {

            Node node = resource.adaptTo(Node.class);

            try {
                if (node == null) {
                    // Do nothing, return 0L below
                } else if (node.hasProperty(JCR_DATA)) {
                    return node.getProperty(JCR_DATA).getBinary().getSize();
                } else if (node.hasNode(JCR_CONTENT)) {
                    node = node.getNode(JCR_CONTENT);
                    if (node.hasProperty(JCR_DATA)) {
                        return node.getProperty(JCR_DATA).getBinary().getSize();
                    }
                }
            } catch (RepositoryException e) {
                log.error("Error obtaining binary size for node [ {} ] - returning a size of 0 bytes.", resource.getPath());
            }
        }

        return 0L;
    }


    protected String getBinaryResourceMimeType(final Resource resource) {
        if (resource == null || ResourceUtil.isNonExistingResource(resource)) {
            return null;
        }

        return resource.getValueMap().get(JCR_MIMETYPE, resource.getValueMap().get(JCR_CONTENT + "/" + JCR_MIMETYPE, String.class));
    }
}