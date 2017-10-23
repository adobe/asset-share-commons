# Asset Share Commons - Core Project Guidelines

## Parameter Patterns
* Avoid passing "contextResource", instead pass Config object which provides context. (normalize to Config on how context is passed)
* Sling Models should avoid exposing another Model's data
    * Especially in the context of a cq:Component, prefer to have 2 models (ex. Config and Asset) each satisfying its own use case, then trying to combine these two logical constructs into a single Model.  
    * However, if another model is needed in a Model, this can be obtained in the wrapping model, but not exposed.    
* The "AssetModel" represents an "Asset" in the this project; avoid using CQ/Granite Asset objects when possible.

## Sling Models

* Only adapt from a single adaptable, the SlingHttpServletRequest. 
    * https://github.com/apache/sling/blob/trunk/bundles/extensions/models/api/src/main/java/org/apache/sling/models/factory/ModelFactory.java#L200 
    * Note this will (likely) preclude use of the Sling Models in async operations in the future
* Avoid passing in params to Models unless 100% necessary (ie. AssetDetails takes param 'asset')
* Injectors should be as specific as possible
* ONLY use cache=true on models that are always the same per request (ie. Search, Config).    
* All models should implement an interface (vs exposing the Impl as the public abstraction)    
* When possible, Models should always be bound to a resource type (which is the ASC component sling:resourceType)    
* Avoid Getters for pass-through properties unless they are required in JavaCode.
    * Example: In `cq:Components`, the component's "label" is often displayed. This should be accessed via `${properties['label']}` rather than `${theComponentModel.label}` 
* Sling Models themselves should simply call services to perform actions. Sling Models should remain "thin".
    
## OSGi Services
* Prefer OSGi Services to static Utils.
* OSGi services should contain the bulk of the logic (Fat). Sling Models themselves should simply call services to perform actions.
* Use OSGi R6 Annotations
* Anything that might be re-useable logic, should go into an OSGi Services (and in rare circumstances into static Utils)

## Servlets

* Ensure all servlets are mapped to the appropriate METHODS (GET vs POST).


## package-info.java
* All exported packages MUST have a package-info.java, initially set to 1.0.0
    
## Copyright
* Add copyright to all files.
    
## Consumer vs ProviderType
* Add @ConsumerType and @ProviderType annotations to all public interfaces
  * @ConsumerType = other bundles are expected to implement
  * @ProviderType = only this bundle is expected to implement
* @ConsumerType needs to be strongly documented as extension points, and @ProviderTypes are strongly documented as interfaces to NOT implement.
    
    
# Datasources
* DataSources should be heavily used to provided simplified options to to Authors
* ONLY implemeent via Java Servlets (NO JSP!)
* Always bind to resource type `asset-share-commons/data-sources/<data-source-name>`
* Use the DataSourceBuilder to create them
* Only enable on AEM Author via config policy
     
     
## Interfaces / Inheritance
  
### Components

* All Sling Models that represent a cq:Component (/components/* ) must implement the com.adobe.aem.commons.assetshare.components.Component interface to guarentee use of the "isReady" pattern in the cq:Component implementation.   

#### Asset Details Components
* All Sling Models that represent Asset Details metadata components (/components/predicates/*) must implement the Predicate interface.

#### Asset Details Components
* All Sling Models that represent Asset Details metadata components (/components/details/*) must implement the EmptyTextComponent interface to support the "empty text" pattern.
