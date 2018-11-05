# Unit Testing Guide

## Testing Tools

* Unit tests are broken up by the class being tested under `core/src/test/java`.
* Unit tests are written using the dependency provides in the core/pom.xml:
    * [JUnit 4.12](https://junit.org/junit4/)
    * [Mockito 2](https://site.mockito.org/)
    * [AEM Mocks](http://wcm.io/testing/aem-mock/usage.html)

## Test Cases

Each class being test has a corresponding test case class, sharing the package and filename of the class being test, however the filename is post-fixed with "Test".

For example, to test:

   `core/src/main/java/com.adobe.aem.commons.assetshare.components.impl.HelloWorldImpl.java`

this test is created:

    `core/src/test/java/com.adobe.aem.commons.assetshare.components.impl.HelloWorldImplTest.java`

### Test Methods

Each public method on the class being test should have at least one corresponding test method. If multiple conditions (working/not-working) required testing, multiple test methods should be defined.

Test method naming:
* Shares the name of method being tested, ex: `getTitle()`
* Prefixed with `test`, ex: the test method is `testGetTitle()`
* Optionally post-fixed with an underscore-prefixed, capitalized condition name, ex: `testGetTitle__EmptyTitle()`


post-fixed with a clear identifier `_ConditionToTest`

For example, to test in `HelloWorld.java`:

   `String getTitle()`

these test methods created in `HelloWorldImplTest.java`:

    `@Test void testGetTitle()` which tests for the normal, populated title condition
    `@Test void testGetTitle_EmptyTitle()`, which tests for the condition where the title is missing

### Resource files (JSON)

AEM Mocks provide support to load JSON into mock AEM context and write tests against that, avoiding manual mocking of resources, OSGi services, etc. using AEM-agnostic tools like Mockito.

The following convention for defining JSON resource files should be followed:

* JSON files are created under test/resources under a folder structure that follows the test case class's package and file name, for example:

    `core/src/test/resources/com/adobe/aem/commons/assetshare/components/impl/HelloWorldImplTest.json`

provides the JSON required by the test case `core/src/test/java/com.adobe.aem.commons.assetshare.components.impl.HelloWorldImplTest.java`.

Each test case's JSON file can define multiple well-named root in the JSON definition that represent different states, which in turn can be used across 1 or many test methods in the corresponding test case.

For example, the HelloWorldImplTest.java may need to test for: a fully configured ("default")  component, an wholly un-configured component ("empty") and a component with ONLY the title populated ("title") using the JSON file defined below:

```
{
  "default": {
      jcr:primaryType: "nt:unstructured",
      sling:resourceType: "asset-share-commons/components/search/hello-world"
      title: "hello world!",
      propX: "foo"
  },
  "empty": {
      jcr:primaryType: "nt:unstructured",
      sling:resourceType: "asset-share-commons/components/search/hello-world"
  },
  "title": {
      jcr:primaryType: "nt:unstructured",
      sling:resourceType: "asset-share-commons/components/search/hello-world"
      title: "hello world!",
  }
```

#### Example consumption of JSON

```
@Before
public void setup() {
   ctx.load().json("HelloWorldImplTest.json");
}

...

@Test
public void testGetTitle__EmptyTitle()  {
    context.currentResource("/empty");
    ...
}
```

## A sample is worth 1000 words


### Sample test case

`core/src/test/java/com/adobe/aem/commons/assetshare/components/impl/HelloWorldImplTest.java`

```
public class HelloWorldImplTest {

@Rule
	public AemContext ctx = new AemContext();

	@Before
	public void setUp() throws Exception {
	    // If using Sling Models, register them into the test context
		ctx.addModelsForClasses(HelloWorldImpl.class);

        // Load the test JSON as mock resources
		ctx.load().json("HelloWorldImplTest.json", "/");
	}

	@Test
	public void testGetTitle() {
		final String expected = "hello world!";

		ctx.currentResource("/default");
		HelloWorld helloWorld = ctx.request().adaptTo(HelloWorld.class);

		String actual = helloWorld.getTitle();

		assertEquals(expected, actual);
	}

	@Test
	public void testGetTitle_EmptyTitle() {
		ctx.currentResource("/empty");
		HelloWorld helloWorld = ctx.request().adaptTo(HelloWorld.class);

		String actual = helloWorld.getTitle();

		assertNull(actual);
	}
}

```








