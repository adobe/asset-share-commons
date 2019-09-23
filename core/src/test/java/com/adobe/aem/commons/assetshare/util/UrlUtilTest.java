/*
 * Asset Share Commons
 *
 * Copyright (C) 2019 Adobe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.adobe.aem.commons.assetshare.util;

import org.junit.Assert;
import org.junit.Test;

public class UrlUtilTest {

    @Test
    public void escape_WithDoubleEscaping() {
        // allow double escaping
        Assert.assertNull(UrlUtil.escape(null, false));

        Assert.assertEquals("/content/dam/test.png", UrlUtil.escape("/content/dam/test.png", false));
        Assert.assertEquals("/content/dam/test%20asset.png", UrlUtil.escape("/content/dam/test asset.png", false));
        Assert.assertEquals("/content/dam/test%20asset.png/_jcr_content", UrlUtil.escape("/content/dam/test asset.png/jcr:content", false));
        Assert.assertEquals("/content/dam/test%20folder/ir%c4%81%2bpu%20p%c3%b6%20%26p%c3%aep%e2%98%83.jpeg/_jcr_content/test",
                UrlUtil.escape("/content/dam/test folder/irā+pu pö &pîp☃.jpeg/jcr:content/test", false));
    }

    @Test
    public void escape_WithNoDoubleEscaping() {
        // No double escaping
        Assert.assertNull(UrlUtil.escape(null));

        Assert.assertEquals("/content/dam/test.png", UrlUtil.escape("/content/dam/test.png"));
        Assert.assertEquals("/content/dam/test%20asset.png", UrlUtil.escape("/content/dam/test asset.png"));
        Assert.assertEquals("/content/dam/test%20asset.png/_jcr_content", UrlUtil.escape("/content/dam/test asset.png/jcr:content"));
        Assert.assertEquals("/content/dam/test%20folder/ir%c4%81%2bpu%20p%c3%b6%20%26p%c3%aep%e2%98%83.jpeg/_jcr_content/test",
                UrlUtil.escape("/content/dam/test folder/irā+pu pö &pîp☃.jpeg/jcr:content/test"));
    }

    @Test
    public void isEscaped() {
        Assert.assertFalse(UrlUtil.isEscaped(null));

        Assert.assertTrue(UrlUtil.isEscaped("/content/dam/test.png"));
        Assert.assertTrue(UrlUtil.isEscaped("/content/dam/test%20asset.png"));
        Assert.assertTrue(UrlUtil.isEscaped("/content/dam/test%20asset.png/_jcr_content"));
        Assert.assertTrue(UrlUtil.isEscaped("/content/dam/testasset.png/_jcr_content"));
        Assert.assertTrue(UrlUtil.isEscaped("/content/dam/test%20folder/ir%c4%81%2bpu%20p%c3%b6%20%26p%c3%aep%e2%98%83.jpeg/_jcr_content/test"));

        Assert.assertFalse(UrlUtil.isEscaped("/content/dam/test asset.png"));
        Assert.assertFalse(UrlUtil.isEscaped("/content/dam/test asset.png/jcr:content"));
        Assert.assertFalse(UrlUtil.isEscaped("/content/dam/testasset.png/jcr:content"));
        Assert.assertFalse(UrlUtil.isEscaped("/content/dam/test folder/irā+pu pö &pîp☃.jpeg/jcr:content/test"));
    }

    @Test
    public void escape_WithHost() {
        Assert.assertEquals("http://www.test.com/content/dam/test.png", UrlUtil.escape("http://www.test.com/content/dam/test.png", false));
        Assert.assertEquals("http://www.test.com/content/dam/test%20asset.png", UrlUtil.escape("http://www.test.com/content/dam/test asset.png", false));
        Assert.assertEquals("https://www.test.com/content/dam/test%20asset.png/_jcr_content", UrlUtil.escape("https://www.test.com/content/dam/test asset.png/jcr:content", false));
        Assert.assertEquals("https://www.test.com/content/dam/test%20folder/ir%c4%81%2bpu%20p%c3%b6%20%26p%c3%aep%e2%98%83.jpeg/_jcr_content/test",
                UrlUtil.escape("https://www.test.com/content/dam/test folder/irā+pu pö &pîp☃.jpeg/jcr:content/test", false));
    }

    @Test
    public void isEscaped_WithHost() {
        Assert.assertTrue(UrlUtil.isEscaped("http://www.test.com/content/dam/test.png"));
        Assert.assertTrue(UrlUtil.isEscaped("http://www.test.com/content/dam/test%20asset.png"));
        Assert.assertTrue(UrlUtil.isEscaped("http://www.test.com/content/dam/test%20asset.png/_jcr_content"));
        Assert.assertTrue(UrlUtil.isEscaped("https://www.test.com/content/dam/testasset.png/_jcr_content"));
        Assert.assertTrue(UrlUtil.isEscaped("https://www.test.com/content/dam/test%20folder/ir%c4%81%2bpu%20p%c3%b6%20%26p%c3%aep%e2%98%83.jpeg/_jcr_content/test"));

        Assert.assertFalse(UrlUtil.isEscaped("https://www.test.com/content/dam/test asset.png"));
        Assert.assertFalse(UrlUtil.isEscaped("https://www.test.com/content/dam/test asset.png/jcr:content"));
        Assert.assertFalse(UrlUtil.isEscaped("http://www.test.com/content/dam/testasset.png/jcr:content"));
        Assert.assertFalse(UrlUtil.isEscaped("http://www.test.com/content/dam/test folder/irā+pu pö &pîp☃.jpeg/jcr:content/test"));
    }

    @Test
    public void escape_WithQueryParams() {
        Assert.assertEquals("http://www.test.com/content/dam/test.png?$testing$",
                UrlUtil.escape("http://www.test.com/content/dam/test.png?$testing$", false));
        Assert.assertEquals("http://www.test.com/content/dam/test.png?foo=$bar$&zip=zap!&crazy=irā+pu%20pö%20&pîp☃",
                UrlUtil.escape("http://www.test.com/content/dam/test.png?foo=$bar$&zip=zap!&crazy=irā+pu pö &pîp☃", false));
    }
}