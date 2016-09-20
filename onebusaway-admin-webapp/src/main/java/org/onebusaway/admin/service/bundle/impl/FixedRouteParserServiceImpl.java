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
package org.onebusaway.admin.service.bundle.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.onebusaway.admin.model.ui.DataValidationMode;
import org.onebusaway.admin.model.ui.DataValidationRouteCounts;
import org.onebusaway.admin.model.ui.DataValidationStopCt;
import org.onebusaway.admin.service.FixedRouteParserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Default implementation of {@link BundleCheckParserService} for parsing
 * a csv file representing a Fixed Route Data Validation report. The report 
 * data will be parsed into a list of  DataValidationMode objects containing
 * the information on modes, routes per mode, stop counts per route, and number
 * of trips per stop count.
 * 
 * @author jpearson
 *
 */
@Component
public class FixedRouteParserServiceImpl implements FixedRouteParserService {
  private static Logger _log = LoggerFactory.getLogger(FixedRouteParserServiceImpl.class);

  /**
   * Parses a FixedRouteDataValidation report file into a List of 
   * DataValidationMode objects.  
   * 
   * @param fixedRouteReportFile name of the file containing the report
   *                             in csv format.
   * @return a List of DataValidationMode objects containing the report data.
   */
  @Override
  public List<DataValidationMode> parseFixedRouteReportFile(File fixedRouteReportFile) {

    List<DataValidationMode> parsedModes = new ArrayList<>();
    if (fixedRouteReportFile != null && fixedRouteReportFile.exists()) {
      DataValidationMode currentMode = null;
      try {
        Reader in = new FileReader(fixedRouteReportFile);
        int i = 0;
        for (CSVRecord record : CSVFormat.DEFAULT.parse(in)) {
          if (i==0) {
            i++;
            continue;   // Skip the first record, which is just the column headers
          }
          currentMode = parseRecord(record, currentMode, parsedModes);
          i++;
        }
      }  catch (FileNotFoundException e) {
        _log.info("Exception parsing csv file " + fixedRouteReportFile, e);
        e.printStackTrace();
      } catch (IOException e) {
        _log.info("Exception parsing csv file " + fixedRouteReportFile, e);
        e.printStackTrace();
      }
      parsedModes.add(currentMode); // Add in the last mode processed.
    }
    return parsedModes;
  }
  
  /**
   * Parses a csv record representing one line of a FixedRouteDataValidation
   * report.  If the line is part of the mode currently being processed, it
   * is added to that DataValidationMode object. If it is for a new mode, the
   * current mode is added to the parsedModes list and the record being 
   * parsed becomes the new current mode.
   *
   * @param record the record to be parsed
   * @param currentMode the DataValidationMode currently being built
   * @param parsedModes the list of modes already created.
   * @return the DataValidationMode currently being built
   */
  private DataValidationMode parseRecord(CSVRecord record, 
      DataValidationMode currentMode, List<DataValidationMode> parsedModes) {
    if (record.size() < 6 || record.get(2).isEmpty()
        || !record.get(2).matches("^\\d+$")) {  //Stop count should be numeric
      return currentMode;  
    }
    // Create the StopCt for this line (every line should have a stop count)
    DataValidationStopCt currentStopCt = new  DataValidationStopCt();
    currentStopCt.setStopCt(Integer.parseInt(record.get(2)));
    int[] stopCtTrips = {0,0,0};
    for (int i=0; i<3; i++) {
      try {
        int tripCt = Integer.parseInt(record.get(3+i));
        stopCtTrips[i] = tripCt;
      } catch (NumberFormatException ex) {
        // Do nothing, leave array value at 0.
      }
    }
    currentStopCt.setTripCts(stopCtTrips);
    
    if (!record.get(0).isEmpty()) {  // new mode
      if (record.get(1).isEmpty()) {
        return currentMode;  // this shouldn't happen.  Any line with a mode
                             // name should also have a route name.
      }
      if (currentMode != null) {
        parsedModes.add(currentMode);
      }
      currentMode = new DataValidationMode();
      currentMode.setModeName(record.get(0));
      DataValidationRouteCounts currentRoute = new DataValidationRouteCounts();
      currentRoute.setRouteName(record.get(1));
      List<DataValidationStopCt> stopCountsList = new ArrayList<>();
      stopCountsList.add(currentStopCt);
      currentRoute.setStopCounts(stopCountsList);
      List<DataValidationRouteCounts> routesForMode = new ArrayList<>();
      routesForMode.add(currentRoute);
      currentMode.setRoutes(routesForMode);
    } else if (record.get(0).isEmpty() && !record.get(1).isEmpty()) {
      // New route for current mode
      DataValidationRouteCounts currentRoute = new DataValidationRouteCounts();
      currentRoute.setRouteName(record.get(1));
      List<DataValidationStopCt> stopCountsList = new ArrayList<>();
      stopCountsList.add(currentStopCt);
      currentRoute.setStopCounts(stopCountsList);
      List<DataValidationRouteCounts> routesForMode = currentMode.getRoutes();
      routesForMode.add(currentRoute);
      currentMode.setRoutes(routesForMode);
    } else if (record.get(0).isEmpty() && record.get(1).isEmpty()) {
      // Additional stop count for existing route
      List<DataValidationRouteCounts> routesForMode = currentMode.getRoutes();
      DataValidationRouteCounts currentRoute
        = routesForMode.get(routesForMode.size()-1);
      List<DataValidationStopCt> stopCountsList = currentRoute.getStopCounts();
      stopCountsList.add(currentStopCt);
      currentRoute.setStopCounts(stopCountsList);
      routesForMode.set(routesForMode.size()-1, currentRoute);
      currentMode.setRoutes(routesForMode);
    } 
    return currentMode;  
  }

}
