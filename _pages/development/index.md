---
layout: section-page
title: Development
---

## Model Cache

Asset Share Commons implements a Model Cache that allows an instance of model to be cached for the life of the request. This is convenient when using Sling Models across comments when the models are always the same in the context of hte Sling Request (such as the ASC Config or PagePredicate) or when a model should only be instaniated once per request ( such as the Search model).

The way the ModelCache is typically used is in an HTL script and the ModelCache model is retrieved normally via the `data-sly-use.modelCache` call such as `data-sly-use.modelCache="com.adobe.aem.commons.assetshare.util.ModelCache"`.

Cacheable models are then requestable using the Sling Model's adapter class name, by passing it as a String to the modelCache object's get method via the `[..]` operator, such as `data-sly-test.config="${modelCache['com.adobe.aem.commons.assetshare.configuration.Config']}"`.

If the requested model does not exist in the ModelCache, it will be adapted, cached and returned to the requesting code.

When engaging w the ModelCache in HTL, `data-sly-test.xxx` must be used to set the model object to the HTL variable since it is does not follow the usual adaptation model.

Cacheable models MUST be adaptable from SlingHttpServletRequest.

If in some circumstance a "fresh" (not from cache) instance of a Sling Model is desired, simply use `data-sly-use.myModel="com...SomeModel"` as usual, and simply do no engage the ModelCache.

```
<sly    data-sly-use.modelCache="com.adobe.aem.commons.assetshare.util.ModelCache"
        data-sly-test.config="${modelCache['com.adobe.aem.commons.assetshare.configuration.Config']}"
        data-sly-test.search="${modelCache['com.adobe.aem.commons.assetshare.search.Search']}"
        data-sly-test.pagePredicate="${modelCache['com.adobe.aem.commons.assetshare.components.predicates.PagePredicate']}">
      
      <!-- use the cached model as you would any other model -->
      <p>${config.locale()}</p>
        
</sly>        
```        
        
