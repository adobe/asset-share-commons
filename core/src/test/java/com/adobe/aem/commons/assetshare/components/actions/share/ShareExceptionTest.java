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

package com.adobe.aem.commons.assetshare.components.actions.share;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class ShareExceptionTest {

    @Test
    public void constructor_message() {
        final ShareException exception = new ShareException("something went wrong");

        assertEquals("something went wrong", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    public void constructor_exception() {
        final Exception cause = new IllegalStateException("root cause");

        final ShareException exception = new ShareException(cause);

        assertSame(cause, exception.getCause());
    }

    @Test
    public void constructor_messageAndThrowable() {
        final Throwable cause = new RuntimeException("root cause");

        final ShareException exception = new ShareException("something went wrong", cause);

        assertEquals("something went wrong", exception.getMessage());
        assertSame(cause, exception.getCause());
    }
}
