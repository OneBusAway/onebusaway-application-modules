/**
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
package org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime;

import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;

class BlockDescriptor {

  private BlockInstance blockInstance;

  private ServiceDate startDate;

  private Integer startTime;

  private String vehicleId;

  private ScheduleRelationship scheduleRelationship = ScheduleRelationship.SCHEDULED;
  
  public enum ScheduleRelationship {
    SCHEDULED,
    ADDED,
    UNSCHEDULED,
    CANCELED
  };

  public BlockInstance getBlockInstance() {
    return blockInstance;
  }

  public void setBlockInstance(BlockInstance blockInstance) {
    this.blockInstance = blockInstance;
  }

  public ServiceDate getStartDate() {
    return startDate;
  }

  public void setStartDate(ServiceDate startDate) {
    this.startDate = startDate;
  }

  public Integer getStartTime() {
    return startTime;
  }

  public void setStartTime(Integer startTime) {
    this.startTime = startTime;
  }

  public String getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(String vehicleId) {
    this.vehicleId = vehicleId;
  }
  
  public void setScheduleRelationshipValue(String value) {
    this.scheduleRelationship = ScheduleRelationship.valueOf(value);
  }

  public void setScheduleRelationship(ScheduleRelationship schedule) {
    this.scheduleRelationship = schedule;
  }
  
  public ScheduleRelationship getScheduleRelationship() {
    return scheduleRelationship;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((blockInstance == null) ? 0 : blockInstance.hashCode());
    result = prime * result + ((startDate == null) ? 0 : startDate.hashCode());
    result = prime * result + ((startTime == null) ? 0 : startTime.hashCode());
    result = prime * result + ((vehicleId == null) ? 0 : vehicleId.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    BlockDescriptor other = (BlockDescriptor) obj;
    if (blockInstance == null) {
      if (other.blockInstance != null)
        return false;
    } else if (!blockInstance.equals(other.blockInstance))
      return false;
    if (startDate == null) {
      if (other.startDate != null)
        return false;
    } else if (!startDate.equals(other.startDate))
      return false;
    if (startTime == null) {
      if (other.startTime != null)
        return false;
    } else if (!startTime.equals(other.startTime))
      return false;
    if (vehicleId == null) {
      if (other.vehicleId != null)
        return false;
    } else if (!vehicleId.equals(other.vehicleId))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return blockInstance.toString();
  }
}