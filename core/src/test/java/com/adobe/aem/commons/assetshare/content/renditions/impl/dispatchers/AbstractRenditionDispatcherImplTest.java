package com.adobe.aem.commons.assetshare.content.renditions.impl.dispatchers;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.junit.Test;

import javax.servlet.ServletException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.adobe.aem.commons.assetshare.content.renditions.impl.dispatchers.AbstractRenditionDispatcherImpl.QUERY_PARAM_SUGGESTED_EXTENSION;
import static org.junit.Assert.assertEquals;

public class AbstractRenditionDispatcherImplTest {

    String smartCropURI ="https://s7d2.scene7.com/is/image/test/my%20asset:Large?" + QUERY_PARAM_SUGGESTED_EXTENSION + "=jpeg";

    String imagePresetURI = "https://s7d2.scene7.com/is/image/test/asset?$grayscale$&" + QUERY_PARAM_SUGGESTED_EXTENSION + "=png";

    String miscURI = "https://www.adobe.com?foo=bar&" + QUERY_PARAM_SUGGESTED_EXTENSION + "=gif&zip=zap";

    String conflictURI = "https://www.adobe.com/asset.docx?" + QUERY_PARAM_SUGGESTED_EXTENSION + "=doc";

    String noConflictURI = "https://www.adobe.com/asset.ppt";


    TestRenditionDispatcher trd = new TestRenditionDispatcher();

    @Test
    public void parseMappingsAsStrings()  {

        ConcurrentHashMap<String, String> map = trd.parseMappingsAsStrings(new String[] {
                "simple=test",
                "complex=${dm.domain}/is/image/${dm.file}:Small?" + QUERY_PARAM_SUGGESTED_EXTENSION + "=jpeg",
        });

        assertEquals("test", map.get("simple"));
        assertEquals("${dm.domain}/is/image/${dm.file}:Small?" + QUERY_PARAM_SUGGESTED_EXTENSION + "=jpeg", map.get("complex"));
    }

    @Test
    public void getExtensionFromAscExtQueryParameter() throws URISyntaxException {
        assertEquals("jpeg", trd.getExtensionFromAscExtQueryParameter(smartCropURI));
        assertEquals("png", trd.getExtensionFromAscExtQueryParameter(imagePresetURI));
        assertEquals("gif", trd.getExtensionFromAscExtQueryParameter(miscURI));
        assertEquals("doc", trd.getExtensionFromAscExtQueryParameter(conflictURI));
        assertEquals("ppt", trd.getExtensionFromAscExtQueryParameter(noConflictURI));
    }

    @Test
    public void cleanURI() throws URISyntaxException {
        assertEquals("https://s7d2.scene7.com/is/image/test/my%20asset:Large", trd.cleanURI(smartCropURI));
        assertEquals("https://s7d2.scene7.com/is/image/test/asset?$grayscale$", trd.cleanURI(imagePresetURI));
        assertEquals("https://www.adobe.com?foo=bar&zip=zap", trd.cleanURI(miscURI));
    }

    class TestRenditionDispatcher extends AbstractRenditionDispatcherImpl {

        @Override
        public String getLabel() {
            return null;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public Map<String, String> getOptions() {
            return null;
        }

        @Override
        public Set<String> getRenditionNames() {
            return null;
        }

        @Override
        public void dispatch(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException, ServletException {

        }
    }
}