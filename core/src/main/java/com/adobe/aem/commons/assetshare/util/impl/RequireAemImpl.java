/*
 * #%L
 * ACS AEM Commons Bundle
 * %%
 * Copyright (C) 2020 Adobe
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

/* Copied from: https://github.com/Adobe-Consulting-Services/acs-aem-commons/blob/master/bundle/src/main/java/com/adobe/acs/commons/util/impl/RequireAemImpl.java */
package com.adobe.aem.commons.assetshare.util.impl;

import com.adobe.aem.commons.assetshare.util.RequireAem;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Hashtable;

import static com.adobe.aem.commons.assetshare.util.impl.RequireAemImpl.PN_SERVICE_TYPE;

@Component(
        immediate = true,
        service = {},
        property = {
                PN_SERVICE_TYPE + "=" + RequireAemImpl.PUBLISH_SERVICE_TYPE_VALUE
        }
)
@Designate(ocd = RequireAemImpl.Config.class)
public class RequireAemImpl implements RequireAem {
    private static final Logger log = LoggerFactory.getLogger(RequireAemImpl.class);

    static final String PN_DISTRIBUTION = "distribution";
    static final String PN_SERVICE_TYPE = "service.type";

    protected static final String PUBLISH_SERVICE_TYPE_VALUE = "publish";

    private ServiceRegistration<?> serviceRegistration;

    private RequireAemImpl.Config config;

    @Reference(
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.STATIC,
            policyOption = ReferencePolicyOption.GREEDY
    )
    private volatile RequireAemCanary requireAemCanary;

    private Distribution distribution;
    @ObjectClassDefinition(
            name = "Asset Share Commons - AEM Service",
            description = "Describes the AEM Service being operated on."
    )
    @interface Config {
        @AttributeDefinition(
                name = "Service type name",
                description = "Defines the which AEM service type (author or publish) the application is running under. Allowed values are: author or publish. Defaults to: publish."
        )
        String service_type() default PUBLISH_SERVICE_TYPE_VALUE;
    }

    @Override
    public Distribution getDistribution() {
        if (Distribution.CLOUD_READY.equals(distribution)) {
            return Distribution.CLOUD_READY;
        } else {
            return Distribution.CLASSIC;
        }
    }

    @Override
    public ServiceType getServiceType() {
        if (StringUtils.equalsIgnoreCase(PUBLISH_SERVICE_TYPE_VALUE, config.service_type())) {
            return ServiceType.PUBLISH;
        } else {
            return ServiceType.AUTHOR;
        }
    }

    @Activate
    protected void activate(final RequireAemImpl.Config config, final BundleContext bundleContext) {
        this.config = config;

        @SuppressWarnings("squid:java:S1149")
        final Dictionary<String, Object> properties = new Hashtable<>();

        if (isCloudService()) {
            this.distribution = Distribution.CLOUD_READY;
        } else {
            this.distribution = Distribution.CLASSIC;
        }

        properties.put(PN_DISTRIBUTION, this.distribution.getValue());
        properties.put(PN_SERVICE_TYPE, this.config.service_type());

        serviceRegistration = bundleContext.registerService(RequireAem.class.getName(), this, properties);

        if (log.isInfoEnabled()) {
            log.info("Registering [ RequireAem.class ] as an OSGi Service with OSGi properties [ distribution = {}, serviceType = {} ] so it can be used to enable/disable other OSGi Components",
                    properties.get(PN_DISTRIBUTION), properties.get(PN_SERVICE_TYPE));
        }
    }

    protected boolean isCloudService() {
        return requireAemCanary != null;
    }

    @Deactivate
    protected void deactivate() {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
            serviceRegistration = null;
        }
    }
}