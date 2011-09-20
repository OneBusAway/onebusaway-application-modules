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
package org.onebusaway.api.model.transit.service_alerts;

import java.io.Serializable;
import java.util.List;

import org.onebusaway.api.model.transit.HasId;

public class SituationV2Bean implements HasId, Serializable {

  private static final long serialVersionUID = 1L;

  private String id;

  private long creationTime;

  private List<TimeRangeV2Bean> activeWindows;

  private List<TimeRangeV2Bean> publicationWindows;

  private String reason;

  private NaturalLanguageStringV2Bean summary;

  private NaturalLanguageStringV2Bean description;

  private NaturalLanguageStringV2Bean url;

  private List<SituationAffectsV2Bean> allAffects;

  private List<SituationConsequenceV2Bean> consequences;

  private String severity;

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

  public List<TimeRangeV2Bean> getActiveWindows() {
    return activeWindows;
  }

  public void setActiveWindows(List<TimeRangeV2Bean> activeWindows) {
    this.activeWindows = activeWindows;
  }

  public List<TimeRangeV2Bean> getPublicationWindows() {
    return publicationWindows;
  }

  public void setPublicationWindows(List<TimeRangeV2Bean> publicationWindows) {
    this.publicationWindows = publicationWindows;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public NaturalLanguageStringV2Bean getSummary() {
    return summary;
  }

  public void setSummary(NaturalLanguageStringV2Bean summary) {
    this.summary = summary;
  }

  public NaturalLanguageStringV2Bean getDescription() {
    return description;
  }

  public void setDescription(NaturalLanguageStringV2Bean description) {
    this.description = description;
  }

  public NaturalLanguageStringV2Bean getUrl() {
    return url;
  }

  public void setUrl(NaturalLanguageStringV2Bean url) {
    this.url = url;
  }

  public List<SituationAffectsV2Bean> getAllAffects() {
    return allAffects;
  }

  public void setAllAffects(List<SituationAffectsV2Bean> affects) {
    this.allAffects = affects;
  }

  public List<SituationConsequenceV2Bean> getConsequences() {
    return consequences;
  }

  public void setConsequences(List<SituationConsequenceV2Bean> consequences) {
    this.consequences = consequences;
  }

  public String getSeverity() {
    return severity;
  }

  public void setSeverity(String severity) {
    this.severity = severity;
  }
}
