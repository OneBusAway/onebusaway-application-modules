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
package org.onebusaway.admin.service.bundle.task;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.transit_data_federation.bundle.tasks.MultiCSVLogger;
import org.onebusaway.transit_data_federation.services.FederatedTransitDataBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.conveyal.gtfs.model.InvalidValue;
import com.conveyal.gtfs.model.ValidationResult;
import com.conveyal.gtfs.service.GtfsValidationService;

public class GtfsValidationTask implements Runnable {
  private Logger _log = LoggerFactory.getLogger(GtfsStatisticsTask.class);
  private GtfsRelationalDaoImpl _dao;
  private FederatedTransitDataBundle _bundle;
  private String filename;
  
  @Autowired
  public void setFilename(String filename) {
	this.filename = filename;
  }

  @Autowired
  private MultiCSVLogger logger;

  
  public void setLogger(MultiCSVLogger logger) {
    this.logger = logger;
  }

  
  @Autowired
  public void setGtfsDao(GtfsRelationalDaoImpl dao) {
    _dao = dao;
  }

  @Autowired
  public void setBundle(FederatedTransitDataBundle bundle) {
    _bundle = bundle;
  }
  @Override
  public void run() {
    try {
      File basePath = _bundle.getPath();
      _log.info("Starting GTFS valdiation to basePath=" + basePath);
      logger.header(filename, InvalidValueHelper.getCsvHeader());
      GtfsValidationService service = new GtfsValidationService(_dao);
      ValidationResult vr = service.validateRoutes();
      log(vr, filename);
      // TODO GTFSValidationService needs to be updated to use latest JTS
      // Until then these methods conflict with geospatial's JTS.
      //vr = service.validateTrips();
      //log(vr, filename);
      //vr = service.duplicateStops();
      //log(vr, filename);
      //vr = service.listReversedTripShapes();
      //log(vr, filename);
      
    } catch (Exception any) {
      // don't let validation issues break the build
      _log.error("validation issue:", any);
    } finally {
      _log.info("Exiting");
    }
  }


  private void log(ValidationResult vr, String file) {
    Set<InvalidValue> invalidValues = vr.invalidValues;
    for (InvalidValue iv : invalidValues) {
      logger.logCSV(file, InvalidValueHelper.getCsv(iv));
    }

    
  }

}
