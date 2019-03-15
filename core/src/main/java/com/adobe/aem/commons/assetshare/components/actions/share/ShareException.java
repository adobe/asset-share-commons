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

/**
 * Exception thrown when an issue with sharing from Asset Share Commons occurs.
 */
public class ShareException extends Exception {
    /**
     * Creates a new ShareException with a custom message.
     *
     * @param message the exception message.
     */
    public ShareException(String message) {
        super(message);
    }

    /**
     * Method used to re-throw an internal exception as a ShareException.
     *
     * @param e the exception to re-throw
     */
    public ShareException(Exception e) {
        super(e);
    }

    /**
     * Creates a new ShareException with a custom message.
     *
     * @param message the exception message.
     * @param ex the cause exception
     */
    public ShareException(String message, Throwable ex) {
        super(message, ex);
    }

}
