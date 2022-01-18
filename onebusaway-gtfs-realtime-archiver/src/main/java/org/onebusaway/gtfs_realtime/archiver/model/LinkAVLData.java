/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
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
package org.onebusaway.gtfs_realtime.archiver.model;

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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "link_avl_feed")
@org.hibernate.annotations.Entity(mutable = false)

public class LinkAVLData {

  @Id
  @GeneratedValue(generator = "increment")
  @GenericGenerator(name = "increment", strategy = "increment")
  @JsonIgnore
  private long id;

  @Column(nullable = true, name = "avl_source")
  @JsonIgnore
  private String avlSource;

  @OneToMany(cascade = {
      CascadeType.ALL}, mappedBy = "linkAVLData", fetch = FetchType.EAGER)
  @JsonIgnore
  List<TripInfo> trips;

  @Column(nullable = true, name = "env_message")
  @JsonIgnore
  private String envMessage;

  @Column(nullable = true, name = "timestamp")
  private Date timestamp;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getAvlSource() {
    return avlSource;
  }

  public void setAvlSource(String avlSource) {
    this.avlSource = avlSource;
  }

  public List<TripInfo> getTrips() {
    return trips;
  }

  @JsonProperty("Trips")
  public void setTrips(TripInfoList tripInfoList) {
    //this.stopUpdates = stopUpdates;
    if (trips == null) {
      trips = new ArrayList<TripInfo>();
    }
    if (tripInfoList != null) {
      if (tripInfoList.getTrips() != null) {
        for (TripInfo tripInfo: tripInfoList.getTrips()) {
          trips.add(tripInfo);
        }
      }
      this.setEnvMessage(tripInfoList.getEnvMessage());
    }
  }

  public String getEnvMessage() {
    return envMessage;
  }

  public void setEnvMessage(String envMessage) {
    this.envMessage = envMessage;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }
}