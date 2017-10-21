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
import java.util.SortedSet;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.onebusaway.admin.model.ui.DataValidationDirectionCts;
import org.onebusaway.admin.model.ui.DataValidationHeadsignCts;
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
  private DataValidationRouteCounts currentRoute;
  private DataValidationHeadsignCts currentHeadsign;
  private DataValidationDirectionCts currentDirection;

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
          // When the record being parsed is for a new mode, the parseRecord
          // method will add the previous mode to the parsedModes List.
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
    if (record.size() < 8 || record.get(4).isEmpty()
        || !record.get(4).matches("^\\d+$")) {  //Stop count should be numeric
      return currentMode;  
    }
    // Create the StopCt for this line (every line should have a stop count)
    DataValidationStopCt currentStopCt = new  DataValidationStopCt();
    currentStopCt.setStopCt(Integer.parseInt(record.get(4)));
    int[] stopCtTrips = {0,0,0};
    for (int i=0; i<3; i++) {
      try {
        int tripCt = Integer.parseInt(record.get(5+i));
        stopCtTrips[i] = tripCt;
      } catch (NumberFormatException ex) {
        // Do nothing, leave array value at 0.
      }
    }
    currentStopCt.setTripCts(stopCtTrips);
    String modeName = record.get(0);
    String routeName = record.get(1);
    String headsign = record.get(2);
    String dirName = record.get(3);

    // If routeName is prefixed with the route number, extract the route number
    String routeNum = "";
    if (routeName.length() > 0) {
    int idx = routeName.substring(0,Math.min(5, routeName.length())).indexOf("-");
      if (idx > 0) {
        routeNum = routeName.substring(0,idx).trim();
        routeName = routeName.substring(idx+1);
      }
    }

    if (modeName.length()>0) {  // new mode
      if (routeName.isEmpty()) {
        return currentMode;  // this shouldn't happen.  Any line with a mode
                             // name should also have a route name.
      }
      if (currentMode != null) {
        parsedModes.add(currentMode);
      }
      currentMode = new DataValidationMode(modeName, routeNum, routeName, headsign, dirName);
      currentRoute = currentMode.getRoutes().first();
      currentHeadsign = currentRoute.getHeadsignCounts().first();
      currentDirection = currentHeadsign.getDirCounts().first();
      SortedSet<DataValidationStopCt> stopCountsList = currentDirection.getStopCounts();
      stopCountsList.add(currentStopCt);
    } else if (routeName.length()>0) {
      // New route for current mode
      currentRoute = new DataValidationRouteCounts(routeNum, routeName, headsign, dirName);
      currentMode.getRoutes().add(currentRoute);
      currentHeadsign = currentRoute.getHeadsignCounts().first();
      currentDirection = currentHeadsign.getDirCounts().first();
      SortedSet<DataValidationStopCt> stopCountsList = currentDirection.getStopCounts();
      stopCountsList.add(currentStopCt);
    } else if (headsign.length()>0) {
      currentHeadsign = new DataValidationHeadsignCts(headsign, dirName);
      currentRoute.getHeadsignCounts().add(currentHeadsign);
      currentDirection = currentHeadsign.getDirCounts().first();
      SortedSet<DataValidationStopCt> stopCountsList = currentDirection.getStopCounts();
      stopCountsList.add(currentStopCt);
    } else if (dirName.length()>0) {
      currentDirection = new DataValidationDirectionCts(dirName);
      currentHeadsign.getDirCounts().add(currentDirection);
      SortedSet<DataValidationStopCt> stopCountsList = currentDirection.getStopCounts();
      stopCountsList.add(currentStopCt);
    } else if (dirName.isEmpty()) {
      SortedSet<DataValidationStopCt> stopCountsList = currentDirection.getStopCounts();
      stopCountsList.add(currentStopCt);
    } 
    return currentMode;  
  }
}
