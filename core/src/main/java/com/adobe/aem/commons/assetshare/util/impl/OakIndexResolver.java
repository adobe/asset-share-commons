package com.adobe.aem.commons.assetshare.util.impl;

import com.day.cq.commons.Version;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

public class OakIndexResolver {
    private static final Logger log = LoggerFactory.getLogger(OakIndexResolver.class);

    private static final String PN_TYPE = "type";
    private static final String VALUE_TYPE_LUCENE = "lucene";
    private static final String PATH_OAK_INDEX = "/oak:index";

    private OakIndexResolver() {
        // Static utility class
    }

    public static final String resolveRankingOakIndex(final ResourceResolver resourceResolver, final String oakIndexRootName) {
        final Resource oakIndexes = resourceResolver.getResource(PATH_OAK_INDEX);

        final Map<Version, String> map = new TreeMap<>(Collections.reverseOrder());

        StreamSupport.stream(oakIndexes.getChildren().spliterator(), false)
                // Only look at Lucene indexes
                .filter(oakIndex -> VALUE_TYPE_LUCENE.equals(oakIndex.getValueMap().get(PN_TYPE, String.class)))
                .forEach(oakIndex -> {
                    final Version version = getOakIndexVersion(oakIndexRootName, oakIndex.getName());

                    if (version != null) {
                        log.debug("Resolved Oak Index [ {} ] -> [ {} ]", oakIndex.getPath(), version.toString());
                        map.put(version, oakIndex.getName());
                    } else {
                        log.warn("Unable to parse version for Oak Index [ {} ]", oakIndex.getPath());
                    }
                }
        );

        if (map.isEmpty()) {
            log.warn("Unable to collect any versions for the provided Oak Index Root Name [ {} ]. This probably means there is a type-o in this parameter.", oakIndexRootName);

            return null;
        } else {
            return map.entrySet().iterator().next().getValue();
        }
    }

    /**
     * Parse a version from the Oak Index node name.
     *
     * @param oakIndexRootName the root index node name; ie. damAssetLucene
     * @param oakIndexName the actual index node name being processed; ie. damAssetLucene-2-custom-5
     * @return the version of the index, with the Product version being the "Major" and the Customer "custom" being the Minor
     */
    protected static final Version getOakIndexVersion(final String oakIndexRootName, final String oakIndexName) {
        final Pattern pattern = Pattern.compile(oakIndexRootName + "(-(\\d+)(-custom-(\\d+))?)?");
        final Matcher matcher = pattern.matcher(oakIndexName);

        if (matcher.matches()) {
            final String product = StringUtils.defaultIfEmpty(matcher.group(2), "0");
            final String custom = StringUtils.defaultIfEmpty(matcher.group(4), "0");

            return Version.create(new String[]{product, custom});
        } else {
            return null;
        }
    }
}
