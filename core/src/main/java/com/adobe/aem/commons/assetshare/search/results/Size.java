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

public final class Size {
    private long count = 0;
    private boolean more = false;

    public Size(int count, boolean more) {
        this.count = count;
        this.more = more;
    }

    public long getCount() {
        return count;
    }

    public boolean isMore() {
        return more;
    }

    public String toString() {
        if (more) {
            return String.valueOf(count) + "+";
        } else {
            return String.valueOf(count);
        }
    }
}