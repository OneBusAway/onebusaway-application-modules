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
package org.onebusaway.transit_data_federation.impl.blocks;

import static org.junit.Assert.assertEquals;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.linkBlockTrips;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stop;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stopTime;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.time;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.trip;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TripEntryImpl;
import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocation;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;

public class ScheduledBlockLocationLibraryTest {

  private List<BlockStopTimeEntry> _stopTimes;

  @Before
  public void before() {

    TripEntryImpl tripA = trip("A", "sid", 1000.0);
    TripEntryImpl tripB = trip("B", "sid", 1000.0);

    StopEntryImpl stopA = stop("stopA", 47.0, -122.0);
    StopEntryImpl stopB = stop("stopB", 47.0, -122.1);
    StopEntryImpl stopC = stop("stopC", 47.0, -122.2);

    stopTime(0, stopA, tripA, time(10, 00), time(10, 00), 200);
    stopTime(1, stopB, tripA, time(10, 10), time(10, 15), 500);
    stopTime(2, stopC, tripA, time(10, 20), time(10, 25), 800);
    // 25 minutes of slack time - 10 minutes of travel time in bewteen
    stopTime(3, stopC, tripB, time(11, 00), time(11, 05), 200);
    stopTime(4, stopB, tripB, time(11, 10), time(11, 15), 500);
    stopTime(5, stopA, tripB, time(11, 25), time(11, 25), 800);

    BlockConfigurationEntry blockConfig = linkBlockTrips("block", tripA, tripB);
    _stopTimes = blockConfig.getStopTimes();
  }

  @Test
  public void test00() {

    ScheduledBlockLocation a = new ScheduledBlockLocation();
    a.setScheduledTime(time(10, 05));
    a.setNextStop(_stopTimes.get(1));

    ScheduledBlockLocation b = new ScheduledBlockLocation();
    b.setScheduledTime(time(10, 07));
    b.setNextStop(_stopTimes.get(1));

    int t = ScheduledBlockLocationLibrary.computeTravelTimeBetweenLocations(a,
        b);

    assertEquals(time(0, 2), t);
  }

  @Test
  public void test01() {

    ScheduledBlockLocation a = new ScheduledBlockLocation();
    a.setScheduledTime(time(10, 05));
    a.setNextStop(_stopTimes.get(1));

    ScheduledBlockLocation b = new ScheduledBlockLocation();
    b.setScheduledTime(time(10, 11));
    b.setNextStop(_stopTimes.get(1));

    int t = ScheduledBlockLocationLibrary.computeTravelTimeBetweenLocations(a,
        b);

    assertEquals(time(0, 5), t);
  }

  @Test
  public void test02() {

    ScheduledBlockLocation a = new ScheduledBlockLocation();
    a.setScheduledTime(time(10, 05));
    a.setNextStop(_stopTimes.get(1));

    ScheduledBlockLocation b = new ScheduledBlockLocation();
    b.setScheduledTime(time(10, 17));
    b.setNextStop(_stopTimes.get(2));

    int t = ScheduledBlockLocationLibrary.computeTravelTimeBetweenLocations(a,
        b);

    assertEquals(time(0, 7), t);
  }

  @Test
  public void test03() {

    ScheduledBlockLocation a = new ScheduledBlockLocation();
    a.setScheduledTime(time(10, 05));
    a.setNextStop(_stopTimes.get(1));

    ScheduledBlockLocation b = new ScheduledBlockLocation();
    b.setScheduledTime(time(10, 23));
    b.setNextStop(_stopTimes.get(2));

    int t = ScheduledBlockLocationLibrary.computeTravelTimeBetweenLocations(a,
        b);

    assertEquals(time(0, 10), t);
  }

  @Test
  public void test04() {

    ScheduledBlockLocation a = new ScheduledBlockLocation();
    a.setScheduledTime(time(10, 11));
    a.setNextStop(_stopTimes.get(1));

    ScheduledBlockLocation b = new ScheduledBlockLocation();
    b.setScheduledTime(time(10, 12));
    b.setNextStop(_stopTimes.get(1));

    int t = ScheduledBlockLocationLibrary.computeTravelTimeBetweenLocations(a,
        b);

    assertEquals(time(0, 0), t);
  }

  @Test
  public void test05() {

    ScheduledBlockLocation a = new ScheduledBlockLocation();
    a.setScheduledTime(time(10, 11));
    a.setNextStop(_stopTimes.get(1));

    ScheduledBlockLocation b = new ScheduledBlockLocation();
    b.setScheduledTime(time(10, 17));
    b.setNextStop(_stopTimes.get(2));

    int t = ScheduledBlockLocationLibrary.computeTravelTimeBetweenLocations(a,
        b);

    assertEquals(time(0, 2), t);
  }

  @Test
  public void test06() {

    ScheduledBlockLocation a = new ScheduledBlockLocation();
    a.setScheduledTime(time(10, 11));
    a.setNextStop(_stopTimes.get(1));

    ScheduledBlockLocation b = new ScheduledBlockLocation();
    b.setScheduledTime(time(10, 21));
    b.setNextStop(_stopTimes.get(2));

    int t = ScheduledBlockLocationLibrary.computeTravelTimeBetweenLocations(a,
        b);

    assertEquals(time(0, 5), t);
  }

  @Test
  public void test07() {

    ScheduledBlockLocation a = new ScheduledBlockLocation();
    a.setScheduledTime(time(10, 11));
    a.setNextStop(_stopTimes.get(1));

    ScheduledBlockLocation b = new ScheduledBlockLocation();
    b.setScheduledTime(time(10, 27));
    b.setNextStop(_stopTimes.get(3));

    int t = ScheduledBlockLocationLibrary.computeTravelTimeBetweenLocations(a,
        b);

    assertEquals(time(0, 7), t);
  }

  @Test
  public void test08() {

    ScheduledBlockLocation a = new ScheduledBlockLocation();
    a.setScheduledTime(time(10, 11));
    a.setNextStop(_stopTimes.get(1));

    ScheduledBlockLocation b = new ScheduledBlockLocation();
    b.setScheduledTime(time(10, 27));
    b.setNextStop(_stopTimes.get(3));

    int t = ScheduledBlockLocationLibrary.computeTravelTimeBetweenLocations(a,
        b);

    assertEquals(time(0, 7), t);
  }
  
  @Test
  public void test09() {

    ScheduledBlockLocation a = new ScheduledBlockLocation();
    a.setScheduledTime(time(10, 11));
    a.setNextStop(_stopTimes.get(1));

    ScheduledBlockLocation b = new ScheduledBlockLocation();
    b.setScheduledTime(time(11, 06));
    b.setNextStop(_stopTimes.get(4));

    int t = ScheduledBlockLocationLibrary.computeTravelTimeBetweenLocations(a,
        b);

    assertEquals(time(0, 16), t);
  }
  
  @Test
  public void test10() {

    ScheduledBlockLocation a = new ScheduledBlockLocation();
    a.setScheduledTime(time(10, 35));
    a.setNextStop(_stopTimes.get(3));

    ScheduledBlockLocation b = new ScheduledBlockLocation();
    b.setScheduledTime(time(11, 06));
    b.setNextStop(_stopTimes.get(4));

    int t = ScheduledBlockLocationLibrary.computeTravelTimeBetweenLocations(a,
        b);

    assertEquals(time(0, 1), t);
  }
  
  @Test
  public void test11() {

    ScheduledBlockLocation a = new ScheduledBlockLocation();
    a.setScheduledTime(time(10, 55));
    a.setNextStop(_stopTimes.get(3));

    ScheduledBlockLocation b = new ScheduledBlockLocation();
    b.setScheduledTime(time(11, 06));
    b.setNextStop(_stopTimes.get(4));

    int t = ScheduledBlockLocationLibrary.computeTravelTimeBetweenLocations(a,
        b);

    assertEquals(time(0, 1), t);
  }
}
