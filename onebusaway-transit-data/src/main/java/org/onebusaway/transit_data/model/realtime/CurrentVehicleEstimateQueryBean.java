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
package org.onebusaway.transit_data.model.realtime;

import java.io.Serializable;
import java.util.List;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.transit_data.model.QueryBean;
import org.onebusaway.util.SystemTime;

@QueryBean
public final class CurrentVehicleEstimateQueryBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private long time = SystemTime.currentTimeMillis();

  private CoordinatePoint mostRecentLocation;

  private List<Record> records;

  /**
   * These are optional, but used to quickly narrow down an estimate if given
   */
  private String blockId;

  private long serviceDate;

  private String vehicleId;

  private double minProbability = 0.25;

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }

  public CoordinatePoint getMostRecentLocation() {
    return mostRecentLocation;
  }

  public void setMostRecentLocation(CoordinatePoint mostRecentLocation) {
    this.mostRecentLocation = mostRecentLocation;
  }

  public List<Record> getRecords() {
    return records;
  }

  public void setRecords(List<Record> records) {
    this.records = records;
  }

  public String getBlockId() {
    return blockId;
  }

  public void setBlockId(String blockId) {
    this.blockId = blockId;
  }

  public long getServiceDate() {
    return serviceDate;
  }

  public void setServiceDate(long serviceDate) {
    this.serviceDate = serviceDate;
  }

  public String getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(String vehicleId) {
    this.vehicleId = vehicleId;
  }

  public double getMinProbability() {
    return minProbability;
  }

  public void setMinProbability(double minProbability) {
    this.minProbability = minProbability;
  }

  public static class Record implements Comparable<Record>, Serializable {

    private static final long serialVersionUID = 1L;

    private long timestamp;

    private CoordinatePoint location;

    private double accuracy;

    public long getTimestamp() {
      return timestamp;
    }

    public void setTimestamp(long timestamp) {
      this.timestamp = timestamp;
    }

    public CoordinatePoint getLocation() {
      return location;
    }

    public void setLocation(CoordinatePoint location) {
      this.location = location;
    }

    public double getAccuracy() {
      return accuracy;
    }

    public void setAccuracy(double accuracy) {
      this.accuracy = accuracy;
    }

    @Override
    public int compareTo(Record o) {
      long t1 = this.timestamp;
      long t2 = o.timestamp;
      return t1 == t2 ? 0 : (t1 < t2 ? -1 : 1);
    }
  }
}
