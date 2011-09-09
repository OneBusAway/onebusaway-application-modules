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

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.opentripplanner.graph_builder.services.DisjointSet;

public class TestStifTripLoader {
  @Test
  public void testLoader() throws IOException {
    InputStream in = getClass().getResourceAsStream("stif.m_0014__.210186.sun");
    String gtfs = getClass().getResource("m14.zip").getFile();

    GtfsReader reader = new GtfsReader();
    GtfsRelationalDaoImpl dao = new GtfsRelationalDaoImpl();
    reader.setEntityStore(dao);
    reader.setInputLocation(new File(gtfs));
    reader.run();

    StifTripLoader loader = new StifTripLoader();
    loader.setGtfsDao(dao);
    loader.run(in);
    Map<String, List<AgencyAndId>> mapping = loader.getTripMapping();
    assertTrue(mapping.containsKey("1140"));
    List<AgencyAndId> trips = mapping.get("1140");
    AgencyAndId tripId = trips.get(0);
    Trip trip = dao.getTripForId(tripId);
    assertEquals(new AgencyAndId("MTA NYCT",
        "20100627DA_003000_M14AD_0001_M14AD_1"), trip.getId());

    
    Map<Trip, BlockAndRuns> blockAndRunsByTrip = loader.getBlockAndRunsByTrip();
    BlockAndRuns blockAndRuns = blockAndRunsByTrip.get(trip);
    
    /* All runs in this STIF file must be part of the same group */
    DisjointSet<String> groups = loader.getTripGroups();
    int runGroup = groups.find(blockAndRuns.getRun1());
    for (BlockAndRuns tripData : blockAndRunsByTrip.values()) {
      assertEquals(runGroup, groups.find(tripData.getRun1()));      
    }    
  }
}
