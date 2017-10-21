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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Index;
import org.onebusaway.gtfs_realtime.interfaces.HasStopId;

@Entity
@Table(name = "stop_time_update")
@org.hibernate.annotations.Table(appliesTo = "stop_time_update", indexes = {
    @Index(name = "stu_id_idx", columnNames = {"id"}),
    @Index(name = "stu_stop_sequence_idx", columnNames = {"stop_sequence"}),
    @Index(name = "stu_stop_id_idx", columnNames = {"stop_id"}),
    @Index(name = "stu_trip_update_id_idx", columnNames = {"trip_update_id"})})
@org.hibernate.annotations.Entity(mutable = false)

public class StopTimeUpdateModel implements HasStopId {

  @Id
  @GeneratedValue(generator = "increment")
  @GenericGenerator(name = "increment", strategy = "increment")
  private long id;
  @Column(nullable = true, name = "stop_sequence")
  private Long stopSequence;
  @Column(nullable = true, name = "stop_id", length = 20)
  private String stopId;
  @Column(nullable = true, name = "arrival_delay")
  private Integer arrivalDelay;
  @Column(nullable = true, name = "arrival_time")
  private Date arrivalTime;
  @Column(nullable = true, name = "arrival_uncertainty")
  private Integer arrivalUncertainty;
  @Column(nullable = true, name = "departure_delay")
  private Integer departureDelay;
  @Column(nullable = true, name = "departure_time")
  private Date departureTime;
  @Column(nullable = true, name = "departure_uncertainty")
  private Integer departureUncertainty;
  @Column(nullable = false, name = "schedule_relationship")
  private int scheduleRelationship;
  @ManyToOne
  @JoinColumn(name = "trip_update_id", nullable = false)
  private TripUpdateModel tripUpdate;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public Long getStopSequence() {
    return stopSequence;
  }

  public void setStopSequence(long stopSequence) {
    this.stopSequence = stopSequence;
  }

  public String getStopId() {
    return stopId;
  }

  public void setStopId(String stopId) {
    this.stopId = stopId;
  }

  public Integer getArrivalDelay() {
    return arrivalDelay;
  }

  public void setArrivalDelay(Integer arrivalDelay) {
    this.arrivalDelay = arrivalDelay;
  }

  public Integer getArrivalUncertainty() {
    return arrivalUncertainty;
  }

  public void setArrivalUncertainty(Integer arrivalUncertainty) {
    this.arrivalUncertainty = arrivalUncertainty;
  }

  public Integer getDepartureDelay() {
    return departureDelay;
  }

  public void setDepartureDelay(Integer departureDelay) {
    this.departureDelay = departureDelay;
  }

  public Integer getDepartureUncertainty() {
    return departureUncertainty;
  }

  public void setDepartureUncertainty(Integer departureUncertainty) {
    this.departureUncertainty = departureUncertainty;
  }

  public int getScheduleRelationship() {
    return scheduleRelationship;
  }

  public void setScheduleRelationship(int scheduleRelationship) {
    this.scheduleRelationship = scheduleRelationship;
  }

  public TripUpdateModel getTripUpdate() {
    return tripUpdate;
  }

  public void setTripUpdateModel(TripUpdateModel tripUpdate) {
    this.tripUpdate = tripUpdate;
  }

  public Date getArrivalTime() {
    return arrivalTime;
  }

  public void setArrivalTime(Date arrivalTime) {
    this.arrivalTime = arrivalTime;
  }

  public Date getDepartureTime() {
    return departureTime;
  }

  public void setDepartureTime(Date departureTime) {
    this.departureTime = departureTime;
  }

}
