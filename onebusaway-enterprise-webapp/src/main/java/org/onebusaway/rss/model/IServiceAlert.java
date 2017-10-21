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

import com.rometools.rome.feed.module.Module;

import java.util.List;

public interface IServiceAlert extends Module {

    String URI = "https://github.com/camsys/onebusaway-application-modules/wiki/service-alerts-1.0";

    String getId();
    void setId(String id);

    String getDescription();

    void setDescription(String description);

    String getReason();

    void setReason(String reason);

    String getSummary();

    void setSummary(String summary);

    String getSeverity();

    void setSeverity(String severity);

    List<TimeRangeRssBean> getPublicationWindows();

    void setPublicationWindows(List<TimeRangeRssBean> publicationWindows);

    List<AffectsClauseRssBean> getAffectsClauses();

    void setAffectsClauses(List<AffectsClauseRssBean> affectsClauses);
}
