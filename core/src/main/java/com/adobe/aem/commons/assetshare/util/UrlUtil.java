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

package com.adobe.aem.commons.assetshare.util;

import com.day.text.Text;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;

public class UrlUtil {
    private static final Logger log = LoggerFactory.getLogger(UrlUtil.class);

    /**
     * This variant is the default behavior and prevents double escaping.
     *
     * @param unescaped the unescaped URL representation to escape.
     *
     * @return the escaped URL representation, or null if the unescaped parameter is null.
     */
    public static final String escape(final String unescaped) {
        if (unescaped == null) {
            return null;
        }

        return escape(unescaped, true);
    }

    /**
     * @param unescaped the unescaped URL representation to escape.
     * @param preventDoubleEscaping true to check if unescaped is already escaped before attempting to re-escape.
     * @return the escaped URL representation, or null if the unescaped parameter is null.
     */
    public static final String escape(final String unescaped, final boolean preventDoubleEscaping) {
        if (unescaped == null) {
            return null;
        }  else if (preventDoubleEscaping && isEscaped(unescaped)) {
            return unescaped;
        }

        String tmp = unescaped;

        // URL cannot handle spaces, so convert them to %20; escapePath(..) will handle converting them back for later escaping
        tmp = StringUtils.replace(tmp, " ", "%20");

        if (!isPath(unescaped)) {
            try {
                final URL url = new URL(tmp);
                String path = escapePath(url.getPath());

                if (StringUtils.isNotBlank(url.getQuery())) {
                    path += "?" + url.getQuery();
                }

                // Reconstruct the URL using the escaped path
                return new URL(url.getProtocol(), url.getHost(), url.getPort(), path).toString();

            } catch (MalformedURLException e) {
                // Treat as internal path
                log.warn("Could not evaluate unescaped string [ {} ] as a URL. Falling back to escape as path.", unescaped, e);
            }
        }

        return escapePath(tmp);
    }

    /**
     * Checks if the candidate url appears to already be escaped.
     * A null candidate parameter returns false;
     *
     * @param candidate the url to check if is already escaped.
     * @return true if already escaped, else false. A null candidate also returns false.
     */
    public static final boolean isEscaped(final String candidate) {
        if (candidate == null) { return false; }

        String unescaped = Text.unescape(StringUtils.stripToEmpty(candidate));

        unescaped = StringUtils.replace(unescaped, "/_jcr_content", "/jcr:content");

        if (!StringUtils.equals(unescaped, candidate)) {
            return true;
        }

        if (StringUtils.equals(escape(candidate, false), candidate)) {
            return true;
        }

        return false;
    }

    /**
     * Escapes the path portion of the URL.
     *
     * @param path the path segment to escape
     * @return an escaped version of the path parameter.
     */
    private static String escapePath(String path) {
        path = StringUtils.replace(path, "%20", " ");
        path = StringUtils.replace(path, "/jcr:content", "/_jcr_content");
        path = Text.escapePath(path);
        return path;
    }

    /**
     * @param candidate the candidate URI to path to check
     * @return true if it looks like a path, and false it looks like a URL
     */
    private static boolean isPath(String candidate) {
        if (StringUtils.isNotBlank(candidate)) {
            if (candidate.matches("^[a-zA-Z0-9]+://.+")) {
                return false;
            } else if (StringUtils.startsWith(candidate, "//")) {
                return false;
            }
        }

        return true;
    }
}
