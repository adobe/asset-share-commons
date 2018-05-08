package com.adobe.aem.commons.assetshare.components.details;

import com.adobe.aem.commons.assetshare.components.Component;
import com.adobe.cq.dam.cfm.VariationDef;
import org.osgi.annotation.versioning.ProviderType;

import java.util.Collection;

@ProviderType
public interface CFVariations extends Component {

    /**
     * Returns all variations of a content fragment using Asset API
     * @return a collection of objects of type CFVariation
     */
    Collection<VariationDef> getVariations();

}
