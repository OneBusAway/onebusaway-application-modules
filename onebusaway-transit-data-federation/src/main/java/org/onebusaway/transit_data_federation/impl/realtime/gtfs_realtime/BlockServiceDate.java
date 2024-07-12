/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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

import java.util.Objects;

/**
 * A Block + Service Date model + Trip Start time model.
 */
public class BlockServiceDate {
  private final ServiceDate serviceDate;

  private BlockInstance instance;

  private final Integer tripStartTime;

  public BlockServiceDate(ServiceDate serviceDate, BlockInstance instance, Integer tripStartTime) {
    this.serviceDate = serviceDate;
    this.instance = instance;
    this.tripStartTime = tripStartTime;
  }

  public ServiceDate getServiceDate() {
    return serviceDate;
  }

  public BlockInstance getBlockInstance() {
    return instance;
  }

  public Integer getTripStartTime() {
    return tripStartTime;
  }

  @Override
  public int hashCode() {
    return serviceDate.hashCode() + instance.hashCode()
            + (tripStartTime==null?0:tripStartTime.hashCode());
  }
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    BlockServiceDate other = (BlockServiceDate) obj;
    if (!serviceDate.equals(other.serviceDate))
      return false;
    if (!instance.equals(other.instance))
      return false;
    if (!Objects.equals(tripStartTime, other.tripStartTime))
      return false;
    return true;
  }
}