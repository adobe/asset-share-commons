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

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class UrlUtilTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void escape_WithDoubleEscaping() {
        // allow double escaping
        assertEquals("/content/dam/test.png", UrlUtil.escape("/content/dam/test.png", false));
        assertEquals("/content/dam/test%20asset.png", UrlUtil.escape("/content/dam/test asset.png", false));
        assertEquals("/content/dam/test%20asset.png/_jcr_content", UrlUtil.escape("/content/dam/test asset.png/jcr:content", false));
        assertEquals("/content/dam/test%20folder/ir%c4%81%2bpu%20p%c3%b6%20%26p%c3%aep%e2%98%83.jpeg/_jcr_content/test",
                UrlUtil.escape("/content/dam/test folder/irā+pu pö &pîp☃.jpeg/jcr:content/test", false));
    }



    @Test
    public void escape_WithNoDoubleEscaping() {
        // No double escaping
        assertEquals("/content/dam/test.png", UrlUtil.escape("/content/dam/test.png"));
        assertEquals("/content/dam/test%20asset.png", UrlUtil.escape("/content/dam/test asset.png"));
        assertEquals("/content/dam/test%20asset.png/_jcr_content", UrlUtil.escape("/content/dam/test asset.png/jcr:content"));
        assertEquals("/content/dam/test%20folder/ir%c4%81%2bpu%20p%c3%b6%20%26p%c3%aep%e2%98%83.jpeg/_jcr_content/test",
                UrlUtil.escape("/content/dam/test folder/irā+pu pö &pîp☃.jpeg/jcr:content/test"));
    }

    @Test
    public void isEscaped() {
        assertTrue(UrlUtil.isEscaped("/content/dam/test.png"));
        assertTrue(UrlUtil.isEscaped("/content/dam/test%20asset.png"));
        assertTrue(UrlUtil.isEscaped("/content/dam/test%20asset.png/_jcr_content"));
        assertTrue(UrlUtil.isEscaped("/content/dam/test%20folder/ir%c4%81%2bpu%20p%c3%b6%20%26p%c3%aep%e2%98%83.jpeg/_jcr_content/test"));

        assertFalse(UrlUtil.isEscaped("/content/dam/test asset.png"));
        assertFalse(UrlUtil.isEscaped("/content/dam/test asset.png/jcr:content"));
        assertFalse(UrlUtil.isEscaped("/content/dam/test folder/irā+pu pö &pîp☃.jpeg/jcr:content/test"));
    }
}