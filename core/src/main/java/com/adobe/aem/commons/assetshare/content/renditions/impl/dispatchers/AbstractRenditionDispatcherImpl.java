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
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public abstract class AbstractRenditionDispatcherImpl implements AssetRenditionDispatcher {
    private static final Logger log = LoggerFactory.getLogger(AbstractRenditionDispatcherImpl.class);

    protected static final String OSGI_PROPERTY_VALUE_DELIMITER = "=";
    public static final String QUERY_PARAM_SUGGESTED_EXTENSION = "extension";

    protected ConcurrentHashMap<String, Pattern> parseMappingsAsPatterns(final String[] renditionMappings) {
        final ConcurrentHashMap<String, Pattern> mappings = new ConcurrentHashMap<>();

        if (renditionMappings != null) {
            parseParameters(renditionMappings).forEach(segments ->
                    mappings.put(StringUtils.strip(segments[0]),
                            Pattern.compile(StringUtils.strip(segments[1]))));
        }

        return mappings;
    }

    public ConcurrentHashMap<String, String> parseMappingsAsStrings(final String[] renditionMappings) {
        final ConcurrentHashMap<String, String> mappings = new ConcurrentHashMap<>();

        if (renditionMappings != null) {
            parseParameters(renditionMappings).forEach(segments ->
                    mappings.put(StringUtils.strip(segments[0]), StringUtils.strip(segments[1])));
        }

        return mappings;
    }

    private Stream<String[]> parseParameters(final String[] renditionMappings) {
        return Arrays.stream(renditionMappings)
                .map(mapping -> StringUtils.split(mapping, OSGI_PROPERTY_VALUE_DELIMITER, 2))
                .filter(segments -> segments.length == 2)
                .filter(segments -> StringUtils.isNotBlank(segments[0]))
                .filter(segments -> StringUtils.isNotBlank(segments[1]));
    }

    public String getExtensionFromAscExtQueryParameter(String uri) throws URISyntaxException {
        String extension = new URIBuilder(uri).getQueryParams().stream()
                .filter(nvp -> QUERY_PARAM_SUGGESTED_EXTENSION.equals(nvp.getName()))
                .map(NameValuePair::getValue)
                .findFirst()
                .orElse(null);

        if (StringUtils.isBlank(extension)) {
            extension = StringUtils.substringAfterLast(URI.create(uri).getPath(), ".");
        }

        return extension;
    }

    public String cleanURI(String uri) throws URISyntaxException {
        final URIBuilder oldURI = new URIBuilder(uri);
        final URIBuilder newURI = new URIBuilder(uri).removeQuery();

        oldURI.getQueryParams().stream()
                .filter(nvp -> !QUERY_PARAM_SUGGESTED_EXTENSION.equals(nvp.getName()))
                .forEach(nvp -> newURI.addParameter(nvp.getName(), nvp.getValue()));;

        // Common pattern in DM URLS; This escaping probably is not necessary, but keeping for
        // now as it would take a closer look to remove (and it breaks tests)
        return StringUtils.replace(newURI.toString(), "%24", "$");
    }
}