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

package com.adobe.aem.commons.assetshare.util;

import com.adobe.cq.wcm.core.components.models.form.OptionItem;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.resource.ValueMap;

/**
 * Utility class that helps with common patterns found in Predicate implementations.
 */
public final class PredicateUtil {
    private PredicateUtil() { }

    /**
     *
     * @param request the request object.
     * @param parameterName The request query parameter name.
     * @return a String representation of the query parameter parameterName. If no request parameter can be found with that name, the empty string is returned.
     */
    public static String getParamFromQueryParams(final SlingHttpServletRequest request, final String parameterName) {
        final RequestParameter requestParameter = request.getRequestParameter(parameterName);

        if (requestParameter != null) {
            return requestParameter.getString();
        } else {
            return "";
        }
    }

    /**
     * Determines if the optionItem's value exists as a value in the initialValues map.
     *
     * @param optionItem the option item.
     * @param initialValues the initial values.
     * @return true if optionItem's value is in initialValues.
     */
    public static boolean isOptionInInitialValues(OptionItem optionItem, ValueMap initialValues) {
        return isOptionInInitialValues(optionItem.getValue(), initialValues);
    }

    /**
     * Determines if the value exists as a value in the initialValues map.
     *
     * Only String and String[] intialValues values are supported.
     *
     * @param value the value to check.
     * @param initialValues the initial values.
     * @return true if value is in initialValues.
     */
    public static boolean isOptionInInitialValues(String value, ValueMap initialValues) {
        for (final String key : initialValues.keySet()) {
            if (isValueInValueMap(value, initialValues.get(key))) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param needle the value to check for.
     * @param haystack they values to check if the needle is in (must be String or String[]).
     * @return true is the needle exists in the haystack.
     */
    private static boolean isValueInValueMap(String needle, Object haystack) {
        boolean found = false;

        if (haystack instanceof String) {
            found = StringUtils.equals((String) haystack, needle);
        } else if (haystack instanceof String[]) {
            found = ArrayUtils.contains((String[])haystack, needle);
        }

        return found;
    }
}
