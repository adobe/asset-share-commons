package com.adobe.aem.commons.assetshare.util.impl;

import com.adobe.aem.commons.assetshare.util.RequireAem;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Designate(ocd = RequireAemImpl.Cfg.class)
public class RequireAemImpl implements RequireAem {
    private static final Logger log = LoggerFactory.getLogger(RequireAemImpl.class);

    private static final String CLOUD_ONLY_OSGI_PROPERTY = "$[env:programId]";

    private boolean runningInAdobeCloud = false;

    public boolean isRunningInAdobeCloud() {
        return runningInAdobeCloud;
    }

    @Activate
    protected void activate(RequireAemImpl.Cfg cfg) {
       this.runningInAdobeCloud = !(StringUtils.isBlank(cfg.cloud_only_variable()) ||
               StringUtils.equals(CLOUD_ONLY_OSGI_PROPERTY, cfg.cloud_only_variable()));
    }

    @ObjectClassDefinition(name = "Asset Share Commons - Require AEM")
    public @interface Cfg {
        @AttributeDefinition
        String cloud_only_variable() default "";
    }
}
