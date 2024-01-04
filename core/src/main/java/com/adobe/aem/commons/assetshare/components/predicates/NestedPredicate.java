package com.adobe.aem.commons.assetshare.components.predicates;

import com.adobe.aem.commons.assetshare.components.predicates.impl.options.NestedOptionItem;
import org.osgi.annotation.versioning.ConsumerType;

import java.util.List;

@ConsumerType
public interface NestedPredicate extends Predicate {
    
    List<NestedOptionItem> getItems();
    
    String getValuesKey();
    String getProperty();
    String getInputName();
    // boolean getPropagate();
    
    boolean hasOr();
    
}