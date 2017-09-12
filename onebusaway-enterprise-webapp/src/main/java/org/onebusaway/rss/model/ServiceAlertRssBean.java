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
import org.onebusaway.transit_data.model.service_alerts.NaturalLanguageStringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ServiceAlertRssBean extends ModuleImpl implements IServiceAlert {

    private static Logger _log = LoggerFactory.getLogger(ServiceAlertRssBean.class);

    private String id;
    private String description;
    private String reason;
    private String summary;
    private String severity;
    private List<TimeRangeRssBean> publicationWindows = new ArrayList<>();
    private List<AffectsClauseRssBean> affectsClauses = new ArrayList<>();

    public String getId() { return id;}
    public void setId(String id) { this.id = id;}
    @Override
    public String getDescription() {return description;}
    @Override
    public void setDescription(String description) { this.description = description; }
    @Override
    public String getReason() { return reason; }
    @Override
    public void setReason(String reason) { this.reason = reason; }
    @Override
    public String getSummary() { return summary; }
    @Override
    public void setSummary(String summary) { this.summary = summary; }
    @Override
    public String getSeverity() { return severity; }
    @Override
    public void setSeverity(String severity) { this.severity = severity; }
    @Override
    public List<TimeRangeRssBean> getPublicationWindows() {
        return publicationWindows;
    }
    @Override
    public void setPublicationWindows(List<TimeRangeRssBean> publicationWindows) {
        this.publicationWindows = publicationWindows;
    }
    public ServiceAlertRssBean() {
        super(ServiceAlertRssBean.class, IServiceAlert.URI);
    }
    @Override
    public List<AffectsClauseRssBean> getAffectsClauses() {
        return affectsClauses;
    }
    @Override
    public void setAffectsClauses(List<AffectsClauseRssBean> affectsClauses) {
        this.affectsClauses = affectsClauses;
    }

    @Override
    public Class<? extends CopyFrom> getInterface() {
        return IServiceAlert.class;
    }

    @Override
    public void copyFrom(CopyFrom copyFrom) {
        // not used
    }

    public static String getLocalString(List<NaturalLanguageStringBean> beans) {

        if (beans == null)
            return null;
        if (beans.isEmpty())
            return null;

        return beans.get(0).getValue();
    }
}
