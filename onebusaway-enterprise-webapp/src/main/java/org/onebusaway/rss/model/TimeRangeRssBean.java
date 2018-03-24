/**
 * Copyright (C) 2017 Cambridge Systematics, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.rss.model;

import com.rometools.rome.feed.CopyFrom;
import com.rometools.rome.feed.module.ModuleImpl;

public class TimeRangeRssBean extends ModuleImpl implements ITimeRange {

    private long from;
    private long to;

    @Override
    public long getFrom() { return from; }
    @Override
    public void setFrom(long from) { this.from = from; }
    @Override
    public long getTo() { return to; }
    @Override
    public void setTo(long to) { this.to = to; }

    public TimeRangeRssBean() {
        super(TimeRangeRssBean.class, IServiceAlert.URI);
    }

    @Override
    public Class<? extends CopyFrom> getInterface() {
        return TimeRangeRssBean.class;
    }

    @Override
    public void copyFrom(CopyFrom copyFrom) {
        // not used
    }
}
