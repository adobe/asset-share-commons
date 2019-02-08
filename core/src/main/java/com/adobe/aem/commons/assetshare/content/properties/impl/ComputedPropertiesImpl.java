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

package com.adobe.aem.commons.assetshare.content.properties.impl;

import com.adobe.aem.commons.assetshare.content.properties.ComputedProperties;
import com.adobe.aem.commons.assetshare.content.properties.ComputedProperty;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component(
        reference = {
                @Reference(
                        name = "computedProperty",
                        bind = "bindComputedProperty",
                        unbind = "unbindComputedProperty",
                        service = ComputedProperty.class,
                        policy = ReferencePolicy.DYNAMIC,
                        policyOption = ReferencePolicyOption.GREEDY,
                        cardinality = ReferenceCardinality.MULTIPLE
                )
        })
public final class ComputedPropertiesImpl implements ComputedProperties {
    private static final Logger log = LoggerFactory.getLogger(ComputedPropertiesImpl.class);

    private final Object lock = new Object();
    private transient Map<ComputedProperty, RankedComputedProperty> allComputedProperties = new ConcurrentHashMap<>();
    private transient List<ComputedProperty> rankedComputedProperties = new CopyOnWriteArrayList();

    public List<ComputedProperty> getComputedProperties() {
        if (log.isDebugEnabled()) {
            log.debug("Returning this list of highest ranking Computed Prooerties by label");
            rankedComputedProperties.stream().forEach(computedProperty -> {
                log.debug("Computed property: [ name: {} ] - [ label: {} ]", computedProperty.getName(), computedProperty.getLabel());
            });
        }

        return rankedComputedProperties;
    }

    void bindComputedProperty(ComputedProperty computedProperty, Map<String, Object> props) {
        final RankedComputedProperty rankedComputedProperty = new RankedComputedProperty(computedProperty, props);

        log.debug("Binding Computed Property: [ name: {} ] - [ rank: {} ] - [ label: {} ]",
                new String[]{ rankedComputedProperty.getName(), String.valueOf(rankedComputedProperty.getRank()), rankedComputedProperty.getComputedProperty().getLabel()});

        allComputedProperties.put(computedProperty, rankedComputedProperty);

        synchronized (lock) {
            rankedComputedProperties = getHighestRankingByLabel();
        }
    }

    void unbindComputedProperty(ComputedProperty computedProperty, Map<String, Object> props) {
        final RankedComputedProperty rankedComputedProperty = new RankedComputedProperty(computedProperty, props);

        log.debug("Unbinding Computed Property: [ name: {} ] - [ rank: {} ] - [ label: {} ]",
                new String[]{ rankedComputedProperty.getName(), String.valueOf(rankedComputedProperty.getRank()), rankedComputedProperty.getComputedProperty().getLabel()});

        allComputedProperties.remove(computedProperty);

        synchronized (lock) {
            rankedComputedProperties = getHighestRankingByLabel();
        }
    }

    private CopyOnWriteArrayList<ComputedProperty> getHighestRankingByLabel() {
        return new CopyOnWriteArrayList<>(allComputedProperties.values().stream()
                .sorted(Comparator.comparing(RankedComputedProperty::getRank).reversed())
                .peek(rankedComputedProperty -> log.debug("Computed Property by Rank: [ name: {} ] - [ rank: {} ] - [ label: {} ]",
                        new String[]{ rankedComputedProperty.getName(), String.valueOf(rankedComputedProperty.getRank()), rankedComputedProperty.getComputedProperty().getLabel()}))
                .filter(distinctByKey(RankedComputedProperty::getName))
                .peek(rankedComputedProperty -> log.debug("Highest ranking Computed Property: [ name: {} ] - [ rank: {} ] - [ label: {} ]",
                        new String[]{ rankedComputedProperty.getName(), String.valueOf(rankedComputedProperty.getRank()), rankedComputedProperty.getComputedProperty().getLabel()}))
                .map(RankedComputedProperty::getComputedProperty)
                .sorted(Comparator.comparing(ComputedProperty::getLabel))
                .collect(Collectors.toList()));
    }

    private class RankedComputedProperty {
        private final String name;
        private final ComputedProperty computedProperty;
        private final Integer rank;

        public RankedComputedProperty(ComputedProperty computedProperty, Map<String, Object> props) {
            this.rank = new ValueMapDecorator(props).get(Constants.SERVICE_RANKING, 0);
            this.name = computedProperty.getName();
            this.computedProperty = computedProperty;
        }

        public String getName() {
            return name;
        }

        public ComputedProperty getComputedProperty() {
            return computedProperty;
        }

        public int getRank() {
            return rank;
        }
    }

    private <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        final Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
}
