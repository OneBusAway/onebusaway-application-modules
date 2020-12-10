/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.transit_data.model.service_alerts;

import java.io.Serializable;
import java.util.List;

public class ServiceAlertBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String id;

  private long creationTime;

  private List<TimeRangeBean> activeWindows;

  private List<TimeRangeBean> publicationWindows;

  private String reason;

  private List<NaturalLanguageStringBean> summaries;

  private List<NaturalLanguageStringBean> descriptions;

  private List<NaturalLanguageStringBean> urls;

  private List<SituationAffectsBean> allAffects;

  private List<SituationConsequenceBean> consequences;

  private ESeverity severity;

  private String source;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public long getCreationTime() {
    return creationTime;
  }

  public void setCreationTime(long creationTime) {
    this.creationTime = creationTime;
  }

  public List<TimeRangeBean> getActiveWindows() {
    return activeWindows;
  }

  public void setActiveWindows(List<TimeRangeBean> activeWindows) {
    this.activeWindows = activeWindows;
  }

  public List<TimeRangeBean> getPublicationWindows() {
    return publicationWindows;
  }

  public void setPublicationWindows(List<TimeRangeBean> publicationWindows) {
    this.publicationWindows = publicationWindows;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public List<NaturalLanguageStringBean> getSummaries() {
    return summaries;
  }

  public void setSummaries(List<NaturalLanguageStringBean> summaries) {
    this.summaries = summaries;
  }

  public List<NaturalLanguageStringBean> getDescriptions() {
    return descriptions;
  }

  public void setDescriptions(List<NaturalLanguageStringBean> descriptions) {
    this.descriptions = descriptions;
  }

  public List<NaturalLanguageStringBean> getUrls() {
    return urls;
  }

  public void setUrls(List<NaturalLanguageStringBean> urls) {
    this.urls = urls;
  }

  public List<SituationAffectsBean> getAllAffects() {
    return allAffects;
  }

  public void setAllAffects(List<SituationAffectsBean> allAffects) {
    this.allAffects = allAffects;
  }

  public List<SituationConsequenceBean> getConsequences() {
    return consequences;
  }

  public void setConsequences(List<SituationConsequenceBean> consequences) {
    this.consequences = consequences;
  }

  public ESeverity getSeverity() {
    return severity;
  }

  public void setSeverity(ESeverity severity) {
    this.severity = severity;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public void combineAffectsIds() {
    for (SituationAffectsBean affect : getAllAffects()) {
      affect.combineIds();
    }
  }
}
