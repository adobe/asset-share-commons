/*
 * Asset Share Commons
 *
 * Copyright (C) 2017 Adobe
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

package com.adobe.aem.commons.assetshare.search.results;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SizeTest {

    @Test
    public void getCount() {
        final Size size = new Size(10, false);

        assertEquals(10, size.getCount());
    }

    @Test
    public void isMore_False() {
        final Size size = new Size(10, false);

        assertFalse(size.isMore());
    }

    @Test
    public void isMore_True() {
        final Size size = new Size(10, true);

        assertTrue(size.isMore());
    }

    @Test
    public void toString_WithoutMore() {
        final Size size = new Size(42, false);

        assertEquals("42", size.toString());
    }

    @Test
    public void toString_WithMore() {
        final Size size = new Size(42, true);

        assertEquals("42+", size.toString());
    }

    @Test
    public void toString_WithZeroCount() {
        final Size size = new Size(0, false);

        assertEquals("0", size.toString());
    }
}
