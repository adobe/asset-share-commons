# Unit Testing Guide

## Testing Tools

* Unit tests are broken up by the class being tested under `core/src/test/java`.
* Unit tests are written using the dependency provided in the core/pom.xml:
    * [JUnit 4.12](https://junit.org/junit4/)
    * [Mockito 2](https://site.mockito.org/)
    * [AEM Mocks](http://wcm.io/testing/aem-mock/usage.html)

## Picking the right tool for the job

* Prefer not depending on any mocks (AEM Mocks or Mockito) when possible (however, this is rarely possible unless classes are specifically written with this in mind and/or are utility classes).
* Prefer existing AEM Mocks over Mockito mocks.
    * The exception to this rule is if using AEM Mocks requires creating/registering many mocks, where Mockito's mocks (or spies) allow you to do it in fewer.
* TBD if Sling Mocks dependencies will have to be introduced so supplement Sling Mocks.

## General test writing style

* Use @Before for initializations common to MOST tests (ideally all).
* It's ok to repeat small, simple sets of test "setup" code in each test method vs. breaking it out into a "helper" method since the repetition can add clarity and ease-of-reading.
    * This also avoids having to write tests FOR your test code.
    * TBD: Suppress rule for CodeClimate which will complain about this.
* For explicit values (ex. paths, property values, expected values, etc.), prefer enumerating the value as a constant in the test rather than abstracting their value into shared class variables. Use your best judgment! The tests should be easy to read and understand!


## Test Cases

Each class being tested has a corresponding test case class, sharing the package and filename of the class being tested, however, the filename is post-fixed with "Test".

For example, to test:

   `core/src/main/java/com/adobe/aem.commons/assetshare/components/impl/HelloWorldImpl.java`

this test is created:

    `core/src/test/java/com/adobe/aem.commons/assetshare/components/impl/HelloWorldImplTest.java`

### Test Methods

Each public method on the class being tested should have at least one corresponding test method. If multiple conditions (working/not-working) required testing, multiple test methods should be defined.

Test method naming:
* Shares the name of method being tested, ex: `getTitle()`
* Prefixed with `test`, ex: the test method is `testGetTitle()`
* Optionally post-fixed with an underscore-prefixed, capitalized condition name, ex: `testGetTitle__EmptyTitle()`

For example, to test the method `getTitle()` in `HelloWorld.java`:

   `String getTitle()`

The following test methods are created in `HelloWorldImplTest.java`:

    `@Test void testGetTitle()` which tests for the normal, populated title condition
    `@Test void testGetTitle_EmptyTitle()`, which tests for the condition where the title is missing

### Resource files (JSON)

AEM Mocks provide support to load JSON into mock AEM context and write tests against that, avoiding manual mocking of resources, OSGi services, etc. using AEM-agnostic tools like Mockito.

The following convention for defining JSON resource files should be followed:

* JSON files are created under test/resources under a folder structure that follows the test case class's package and file name, for example:

    `core/src/test/resources/com/adobe/aem/commons/assetshare/components/impl/HelloWorldImplTest.json`

provides the JSON required by the test case `core/src/test/java/com/adobe/aem.commons/assetshare/components/impl/HelloWorldImplTest.java`.

Each test case's JSON file can define multiple, well-named JSON definition roots that represent different states and can, in turn, be used across one or many test methods in the corresponding test case.

JSON definitions should contain the MINIMUM definition to satisfy the test-cases. This means NO unnecessary junk data from JCR exports, for example, jcr:lastModifiedBy, cq:lastReplicated, cq:responsive, etc. nodes/properties unless they are required by a test.

For example, the HelloWorldImplTest.java may need to test for: a fully configured ("default")  component, a wholly un-configured component ("empty") and a component with ONLY the title populated ("title") using the JSON file defined below:

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
import org.mockito.junit.MockitoJUnitRunner;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import io.wcm.testing.mock.aem.junit.AemContext;

public class HelloWorldImplTest {

    @Rule
    public AemContext ctx = new AemContext();

    @Before
    public void setUp() throws Exception {
        // If using Sling Models, register them into the test context
        ctx.addModelsForClasses(HelloWorldImpl.class);

        // Load the test JSON as mock resources
        ctx.load().json("/com/adobe/aem/commons/assetshare/components/impl/HelloWorldImplTest.json", "/");
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
