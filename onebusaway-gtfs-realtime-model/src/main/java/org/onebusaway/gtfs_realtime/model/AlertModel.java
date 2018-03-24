/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
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
package org.onebusaway.gtfs_realtime.model;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Index;
import org.onebusaway.gtfs_realtime.interfaces.FeedEntityModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "alert")
@org.hibernate.annotations.Table(appliesTo = "alert", indexes = {
    @Index(name = "alrt_id_idx", columnNames = {"id"}),
    @Index(name = "alrt_timestamp_idx", columnNames = {"timestamp"})})
@org.hibernate.annotations.Entity(mutable = false)

public class AlertModel implements FeedEntityModel {

  /* Sound Transit constants */

  private static final int CAUSE_LENGTH = 20;
  private static final int EFFECT_LENGTH = 20;
  private static final int URL_LENGTH = 300;
  private static final int HEADER_LENGTH = 80;
  private static final int DESCRIPTION_LENGTH = 4000;

  /* Test values */
  /*
   * private static final int CAUSE_LENGTH = 20; private static final int
   * EFFECT_LENGTH = 20; private static final int URL_LENGTH = 300; private
   * static final int HEADER_LENGTH = 150; private static final int
   * DESCRIPTION_LENGTH = 4000;
   */

  @Id
  @GeneratedValue(generator = "increment")
  @GenericGenerator(name = "increment", strategy = "increment")
  private long id;
  @Column(nullable = true, name = "cause", length = CAUSE_LENGTH)
  private String cause;
  @Column(nullable = true, name = "effect", length = EFFECT_LENGTH)
  private String effect;
  @Column(nullable = true, name = "url", length = URL_LENGTH)
  private String url;
  @Column(nullable = true, name = "header_text", length = HEADER_LENGTH)
  private String headerText;
  @Column(nullable = true, name = "description_text", length = DESCRIPTION_LENGTH)
  private String descriptionText;
  @Column(nullable = true, name = "timestamp")
  private Date timestamp;

  @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "alert")
  private List<TimeRangeModel> timeRanges = new ArrayList<TimeRangeModel>();
  @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "alert")
  private List<EntitySelectorModel> entitySelectors = new ArrayList<EntitySelectorModel>();

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getCause() {
    return cause;
  }

  public void setCause(String cause) {
    this.cause = cause;
  }

  public String getEffect() {
    return effect;
  }

  public void setEffect(String effect) {
    this.effect = effect;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getHeaderText() {
    return headerText;
  }

  public void setHeaderText(String headerText) {
    this.headerText = headerText;
  }

  public String getDescriptionText() {
    return descriptionText;
  }

  public void setDescriptionText(String descriptionText) {
    this.descriptionText = descriptionText;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public List<TimeRangeModel> getTimeRanges() {
    return timeRanges;
  }

  public void setTimeRanges(List<TimeRangeModel> timeRanges) {
    this.timeRanges = timeRanges;
  }

  public void addTimeRangeModel(TimeRangeModel timeRange) {
    if (timeRange != null) {
      timeRanges.add(timeRange);
    }
  }

  public List<EntitySelectorModel> getEntitySelectors() {
    return entitySelectors;
  }

  public void setEntitySelectors(List<EntitySelectorModel> entitySelectors) {
    this.entitySelectors = entitySelectors;
  }

  public void addEntitySelectorModel(EntitySelectorModel entitySelector) {
    if (entitySelector != null) {
      entitySelectors.add(entitySelector);
    }
  }

  public String toString() {
    return "{alert=" + headerText + " @" + timestamp + "}";
  }
}
