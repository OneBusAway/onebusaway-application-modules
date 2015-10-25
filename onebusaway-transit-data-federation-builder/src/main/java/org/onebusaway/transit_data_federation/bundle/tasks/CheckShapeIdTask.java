/**
 * Copyright (C) 2012 Metropolitan Transportation Authority
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
package org.onebusaway.transit_data_federation.bundle.tasks;

import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsDao;
import org.springframework.beans.factory.annotation.Autowired;

public class CheckShapeIdTask implements Runnable {

  @Autowired
  private MultiCSVLogger logger;
  
  private GtfsDao _dao;

  @Autowired
  public void setGtfsDao(GtfsDao dao) {
    _dao = dao;
  }

  public void setLogger(MultiCSVLogger logger) {
    this.logger = logger;
  }
      
  @Override
  public void run() {
    logger.header("gtfs_trips_with_missing_shape_ids.csv", "agency_id,trip_id");
    for (Trip trip : _dao.getAllTrips()) {
      if (trip.getShapeId() == null) {
        logger.log("gtfs_trips_with_missing_shape_ids.csv", trip.getId().getAgencyId(), trip.getId().getId());
      }
    }
  }

}
