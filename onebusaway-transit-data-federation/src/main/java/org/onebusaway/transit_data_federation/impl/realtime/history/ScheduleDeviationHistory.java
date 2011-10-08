/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.transit_data_federation.impl.realtime.history;

import java.io.Serializable;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.onebusaway.gtfs.model.AgencyAndId;

@Entity
@Table(name = "oba_schedule_deviation_history")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class ScheduleDeviationHistory implements Serializable {

  private static final long serialVersionUID = 1L;

  @EmbeddedId
  @AttributeOverrides({
      @AttributeOverride(name = "agencyId", column = @Column(name = "trip_agencyId", length = 50)),
      @AttributeOverride(name = "id", column = @Column(name = "trip_id"))})
  private final AgencyAndId tripId;

  //@Column(columnDefinition = "longblob")
  private double[] scheduleTimes;

  //@Column(columnDefinition = "longblob")
  private final double[][] scheduleDeviations;

  public ScheduleDeviationHistory(AgencyAndId tripId,
      double[] scheduleTimes, double[][] scheduleDeviations) {
    this.tripId = tripId;
    this.scheduleTimes = scheduleTimes;
    this.scheduleDeviations = scheduleDeviations;
  }

  ScheduleDeviationHistory() {
    this.tripId = null;
    this.scheduleTimes = null;
    this.scheduleDeviations = null;
  }

  public AgencyAndId getTripId() {
    return tripId;
  }

  public int getNumberOfSamples() {
    return scheduleDeviations.length;
  }

  public double[] getScheduleTimes() {
    return scheduleTimes;
  }

  public double[][] getScheduleDeviations() {
    return scheduleDeviations;
  }
}
