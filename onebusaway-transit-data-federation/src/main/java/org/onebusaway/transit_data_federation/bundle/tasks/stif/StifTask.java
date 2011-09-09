/**
 * Copyright (c) 2011 Metropolitan Transportation Authority
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.transit_data_federation.bundle.tasks.stif;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.model.RunData;
import org.onebusaway.utility.ObjectSerializationLibrary;
import org.opentripplanner.graph_builder.services.DisjointSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Load STIF data, including the mapping between destination sign codes and trip
 * ids, into the database
 * 
 * @author bdferris
 * 
 */
public class StifTask implements Runnable {

  private Logger _log = LoggerFactory.getLogger(StifTask.class);

  private GtfsMutableRelationalDao _gtfsMutableRelationalDao;

  private List<File> _stifPaths = new ArrayList<File>();

  private Set<String> _notInServiceDscs = new HashSet<String>();

  private File _notInServiceDscPath;

  @Autowired 
  private FederatedTransitDataBundle _bundle;
  
  @Autowired
  public void setGtfsMutableRelationalDao(
      GtfsMutableRelationalDao gtfsMutableRelationalDao) {
    _gtfsMutableRelationalDao = gtfsMutableRelationalDao;
  }

  /**
   * The path of the directory containing STIF files to process
   */
  public void setStifPath(File path) {
    _stifPaths.add(path);
  }

  public void setStifPaths(List<File> paths) {
    _stifPaths.addAll(paths);
  }

  public void setNotInServiceDsc(String notInServiceDsc) {
    _notInServiceDscs.add(notInServiceDsc);
  }

  public void setNotInServiceDscs(List<String> notInServiceDscs) {
    _notInServiceDscs.addAll(notInServiceDscs);
  }

  public void setNotInServiceDscPath(File notInServiceDscPath) {
    _notInServiceDscPath = notInServiceDscPath;
  }

  public void run() {

    StifTripLoader loader = new StifTripLoader();
    loader.setGtfsDao(_gtfsMutableRelationalDao);

    for (File path : _stifPaths) {
      loadStif(path, loader);
    }
    Map<Trip, BlockAndRuns> BlockAndRunsByTrip = loader.getBlockAndRunsByTrip();
    DisjointSet<String> tripGroups = loader.getTripGroups();
    for (Map.Entry<Trip, BlockAndRuns> entry : BlockAndRunsByTrip.entrySet()) {
      Trip trip = entry.getKey();
      BlockAndRuns data = entry.getValue();

      String newBlockId = data.getBlockId() + "_group_"
          + tripGroups.find(data.getRun1());
      trip.setBlockId(newBlockId);
      _gtfsMutableRelationalDao.updateEntity(trip);
    }

    Map<AgencyAndId, RunData> runsForTrip = loader.getRunsForTrip();
    try {
      ObjectSerializationLibrary.writeObject(_bundle.getTripRunDataPath(),
          runsForTrip);
    } catch (IOException e) {
          throw new IllegalStateException(e);
    }
        
    Map<String, List<AgencyAndId>> dscToTripMap = loader.getTripMapping();
    Map<AgencyAndId, String> tripToDscMap = new HashMap<AgencyAndId, String>();
    		
    Set<String> inServiceDscs = new HashSet<String>();

    for (Map.Entry<String, List<AgencyAndId>> entry : dscToTripMap.entrySet()) {
      String destinationSignCode = entry.getKey();
      List<AgencyAndId> tripIds = entry.getValue();

      for (AgencyAndId tripId : tripIds) {
    	 tripToDscMap.put(tripId, destinationSignCode);
      }
    }

    int withoutMatch = loader.getTripsWithoutMatchCount();
    int total = loader.getTripsCount();

    _log.info("stif trips without match: " + withoutMatch + " / " + total);

    readNotInServiceDscs();

    for (String notInServiceDsc : _notInServiceDscs) {
      if (inServiceDscs.contains(notInServiceDsc))
        _log.warn("overlap between in-service and not-in-service dscs: "
            + notInServiceDsc);

      // clear out trip mappings for out of service DSCs
      dscToTripMap.put(notInServiceDsc, new ArrayList<AgencyAndId>());
    }

    try {
        ObjectSerializationLibrary.writeObject(_bundle.getNotInServiceDSCs(), 
        		_notInServiceDscs);

        ObjectSerializationLibrary.writeObject(_bundle.getTripsForDSCIndex(), 
        		tripToDscMap);

        ObjectSerializationLibrary.writeObject(_bundle.getDSCForTripIndex(), 
        		dscToTripMap);
    } catch (IOException e) {
        throw new IllegalStateException(
            "error serializing DSC/STIF data", e);
      }    
  }

  public void loadStif(File path, StifTripLoader loader) {
    // Exclude files and directories like .svn
    if (path.getName().startsWith("."))
      return;

    if (path.isDirectory()) {
      for (String filename : path.list()) {
        File contained = new File(path, filename);
        loadStif(contained, loader);
      }
    } else {
      loader.run(path);
    }
  }

  private void readNotInServiceDscs() {
    if (_notInServiceDscPath != null) {
      try {
        BufferedReader reader = new BufferedReader(new FileReader(
            _notInServiceDscPath));
        String line = null;
        while ((line = reader.readLine()) != null)
          _notInServiceDscs.add(line);
      } catch (IOException ex) {
        throw new IllegalStateException("unable to read nonInServiceDscPath: "
            + _notInServiceDscPath);
      }
    }
  }
}
