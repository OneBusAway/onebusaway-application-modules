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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GtfsDao;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.onebusaway.utility.IOLibrary;
import org.onebusaway.utility.collections.TreeUnionFind;
import org.onebusaway.utility.collections.TreeUnionFind.Sentry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class StopConsolidationSuggestionsTask implements Runnable {

  private static final String CONSOLIDATED_URL = "https://raw.githubusercontent.com/wiki/camsys/onebusaway-application-modules/PugetSoundStopConsolidation.md";
  protected static Logger _log = LoggerFactory.getLogger(StopConsolidationSuggestionsTask.class);
  protected MultiCSVLogger _logger;
  protected GtfsDao _dao;

  @Autowired
  public void setGtfsDao(GtfsDao dao) {
    _dao = dao;
  }
  
  @Autowired
  public void setLogger(MultiCSVLogger logger) {
    this._logger = logger;
  }

  @Override
  public void run() {
    try {
      double distanceThreshold = 20;
      _log.info("begin stop consolidation suggestions with distanceThreshold=" + distanceThreshold);
      TreeUnionFind<AgencyAndId> existingConsolidatedStops = new TreeUnionFind<AgencyAndId>();
      existingConsolidatedStops = loadExistingConsolidatedStops(CONSOLIDATED_URL);

      TreeUnionFind<AgencyAndId> union = new TreeUnionFind<AgencyAndId>();
      List<Collection<Stop>> allStops = new ArrayList<Collection<Stop>>();
      for (Agency agency : _dao.getAllAgencies()) {
        Collection<Stop> stops = getStopsForAgency(_dao, agency);

        for (Collection<Stop> previousStops : allStops) {
          for (Stop stopA : previousStops) {
            for (Stop stopB : stops) {
              double d = SphericalGeometryLibrary.distance(stopA.getLat(),
                  stopA.getLon(), stopB.getLat(), stopB.getLon());
              if (d < distanceThreshold
                  && !existingConsolidatedStops.isSameSet(stopA.getId(),
                      stopB.getId())) {
                union.union(stopA.getId(), stopB.getId());
              }
            }
          }
        }

        allStops.add(stops);
      }
      
      
      PrintWriter writer = new PrintWriter(new OutputStreamWriter(System.out));
      _logger.header("stop_consolidation_suggestions.csv", "new_stops,existing_stops");
          
          
      for (Set<AgencyAndId> set : union.getSetMembers()) {
        StringBuffer newStopBuffer = new StringBuffer();
        StringBuffer existingStopBuffer = new StringBuffer();
        Set<Sentry> existing = new HashSet<Sentry>();
        boolean first = true;
        for (AgencyAndId stopId : set) {
          if (first)
            first = false;
          else {
            writer.print(' ');
            newStopBuffer.append(" ");
          }
          if (existingConsolidatedStops.contains(stopId)) {
            existing.add(existingConsolidatedStops.find(stopId));
          }
          writer.print(AgencyAndIdLibrary.convertToString(stopId));
          newStopBuffer.append(AgencyAndIdLibrary.convertToString(stopId));
        }
        writer.println();
        for (Sentry sentry : existing) {
          writer.println("  => " + existingConsolidatedStops.members(sentry));
          existingStopBuffer.append(existingConsolidatedStops.members(sentry));
        }
        _logger.log("stop_consolidation_suggestions.csv", newStopBuffer.toString(), existingStopBuffer.toString());
        writer.flush();
      }
      
      _log.info("end stop consolidation suggestions");
    } catch (Exception any) {
      _log.error("exception:", any);
    }

  }

  private Collection<Stop> getStopsForAgency(GtfsDao dao, Agency agency) {
    List<Stop> stops = new ArrayList<Stop>();
    
    for (Stop stop : dao.getAllStops()) {
      if (agency.getId().equals(stop.getId().getAgencyId())) {
        stops.add(stop);
      }
    }
    
    return stops;
  }

  private TreeUnionFind<AgencyAndId> loadExistingConsolidatedStops(String path)
      throws IOException {

    InputStream in = IOLibrary.getPathAsInputStream(path);
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));

    TreeUnionFind<AgencyAndId> consolidated = new TreeUnionFind<AgencyAndId>();

    String line = null;

    while ((line = reader.readLine()) != null) {
      if (line.startsWith("#") || line.startsWith("{{{")
          || line.startsWith("}}}") || line.length() == 0)
        continue;
      String[] tokens = line.split("\\s+");
      if (tokens == null || tokens.length == 0) continue;
      AgencyAndId stopId = AgencyAndIdLibrary.convertFromString(tokens[0]);
      for (int i = 1; i < tokens.length; i++) {
        AgencyAndId otherStopId = AgencyAndIdLibrary.convertFromString(tokens[i]);
        consolidated.union(stopId, otherStopId);
      }
    }

    reader.close();

    return consolidated;
  }
}
