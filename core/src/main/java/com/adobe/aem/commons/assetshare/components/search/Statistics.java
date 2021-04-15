package com.adobe.aem.commons.assetshare.components.search;

import com.adobe.aem.commons.assetshare.components.Component;
import org.osgi.annotation.versioning.ConsumerType;

/**
 * Sling Model for the Search Statistics Component.
 */
@ConsumerType
public interface Statistics extends Component {
    /**
     * @return the component id; unique to this instance of the component.
     */
    String getId();

    /**
     * @return the number of results returned so far.
     */
    long getRunningTotal();

    /**
     * @return the total number of results for this query (this may be a guess).
     */
    long getTotal();

    /**
     * @return true if there are more results to display for this search (using offset and limit)
     */
    boolean hasMore();

    /**
     * @return the time taken in milliseconds for this specific search. This is NOT an aggregate of all "loads" for this search.
     */
    long getTimeTaken();
}

