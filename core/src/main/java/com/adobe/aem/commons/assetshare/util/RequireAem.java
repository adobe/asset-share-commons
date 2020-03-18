package com.adobe.aem.commons.assetshare.util;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface RequireAem {
    /**
     * Determines if the AEM service is running in the true, Adobe Cloud, or not (ie. local Quickstart, AMS or Onprem)
     *
     * @return true if running in the Adobe Cloud, else false.
     */
    boolean isRunningInAdobeCloud();
}
