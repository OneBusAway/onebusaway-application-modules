package org.onebusaway.transit_data_federation.impl.realtime;

import static org.junit.Assert.assertEquals;
import static org.onebusaway.transit_data_federation.impl.MockEntryFactory.block;
import static org.onebusaway.transit_data_federation.impl.MockEntryFactory.linkBlockTrips;
import static org.onebusaway.transit_data_federation.impl.MockEntryFactory.stopTime;
import static org.onebusaway.transit_data_federation.impl.MockEntryFactory.time;
import static org.onebusaway.transit_data_federation.impl.MockEntryFactory.trip;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.BlockEntryImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.TripEntryImpl;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;

public class StopTimeBlockServiceImplTest {

  private StopTimeBlockServiceImpl _service = new StopTimeBlockServiceImpl();
  
  private BlockEntryImpl _block;

  private TripEntryImpl _tripA;

  private TripEntryImpl _tripB;

  private TripEntryImpl _tripC;

  private StopTimeEntry _stA1;

  private StopTimeEntry _stA2;

  private StopTimeEntry _stB1;

  private StopTimeEntry _stC1;

  private StopTimeEntry _stC2;

  private StopTimeEntry _stC3;

  

  @Before
  public void before() {
    _block = block("block");
    
    _tripA = trip("tripA");
    _tripB = trip("tripB");
    _tripC = trip("tripB");
    
    linkBlockTrips(_block, _tripA,_tripB,_tripC);

    _stA1 = stopTime(1, null, _tripA, time(10, 00), time(10, 00), 100);
    _stA2 = stopTime(2, null, _tripA, time(10, 10), time(10, 20), 200);
    _stB1 = stopTime(3, null, _tripB, time(10, 30), time(10, 40), 300);
    _stC1 = stopTime(4, null, _tripC, time(10, 50), time(10, 50), 400);
    _stC2 = stopTime(5, null, _tripC, time(11, 00), time(11, 00), 500);
    _stC3 = stopTime(6, null, _tripC, time(11, 10), time(11, 10), 600);
    
    _block.setStopTimes(Arrays.asList(_stA1, _stA2,_stB1,_stC1, _stC2, _stC3));
    _tripA.setStopTimeIndices(0, 2);
    _tripB.setStopTimeIndices(2, 3);
    _tripC.setStopTimeIndices(3, 6);
  }

  @Test
  public void test00() {

    List<StopTimeEntry> stopTimes = _service.getSurroundingStopTimes(_tripA,
        time(9, 55));

    assertEquals(0, stopTimes.size());

    stopTimes = _service.getSurroundingStopTimes(_tripB, time(9, 55));

    assertEquals(0, stopTimes.size());

    stopTimes = _service.getSurroundingStopTimes(_tripC, time(9, 55));

    assertEquals(0, stopTimes.size());

    stopTimes = _service.getSurroundingStopTimesFromDistanceAlongBlock(_tripA,
        0);

    assertEquals(0, stopTimes.size());

    stopTimes = _service.getSurroundingStopTimesFromDistanceAlongBlock(_tripB,
        0);

    assertEquals(0, stopTimes.size());

    stopTimes = _service.getSurroundingStopTimesFromDistanceAlongBlock(_tripC,
        0);

    assertEquals(0, stopTimes.size());
  }

  @Test
  public void test01() {

    List<StopTimeEntry> stopTimes = _service.getSurroundingStopTimes(_tripA,
        time(10, 10));

    assertEquals(2, stopTimes.size());
    assertEquals(_stA1, stopTimes.get(0));
    assertEquals(_stA2, stopTimes.get(1));

    stopTimes = _service.getSurroundingStopTimes(_tripB, time(10, 10));

    assertEquals(2, stopTimes.size());
    assertEquals(_stA1, stopTimes.get(0));
    assertEquals(_stA2, stopTimes.get(1));

    stopTimes = _service.getSurroundingStopTimes(_tripC, time(10, 10));

    assertEquals(2, stopTimes.size());
    assertEquals(_stA1, stopTimes.get(0));
    assertEquals(_stA2, stopTimes.get(1));

    stopTimes = _service.getSurroundingStopTimesFromDistanceAlongBlock(_tripA,
        100);

    assertEquals(2, stopTimes.size());
    assertEquals(_stA1, stopTimes.get(0));
    assertEquals(_stA2, stopTimes.get(1));

    stopTimes = _service.getSurroundingStopTimesFromDistanceAlongBlock(_tripB,
        100);

    assertEquals(2, stopTimes.size());
    assertEquals(_stA1, stopTimes.get(0));
    assertEquals(_stA2, stopTimes.get(1));

    stopTimes = _service.getSurroundingStopTimesFromDistanceAlongBlock(_tripC,
        100);

    assertEquals(2, stopTimes.size());
    assertEquals(_stA1, stopTimes.get(0));
    assertEquals(_stA2, stopTimes.get(1));
  }

  @Test
  public void test02() {

    List<StopTimeEntry> stopTimes = _service.getSurroundingStopTimes(_tripA,
        time(10, 05));

    assertEquals(2, stopTimes.size());
    assertEquals(_stA1, stopTimes.get(0));
    assertEquals(_stA2, stopTimes.get(1));

    stopTimes = _service.getSurroundingStopTimes(_tripB, time(10, 05));

    assertEquals(2, stopTimes.size());
    assertEquals(_stA1, stopTimes.get(0));
    assertEquals(_stA2, stopTimes.get(1));

    stopTimes = _service.getSurroundingStopTimes(_tripC, time(10, 05));

    assertEquals(2, stopTimes.size());
    assertEquals(_stA1, stopTimes.get(0));
    assertEquals(_stA2, stopTimes.get(1));

    stopTimes = _service.getSurroundingStopTimesFromDistanceAlongBlock(_tripA,
        150);

    assertEquals(2, stopTimes.size());
    assertEquals(_stA1, stopTimes.get(0));
    assertEquals(_stA2, stopTimes.get(1));

    stopTimes = _service.getSurroundingStopTimesFromDistanceAlongBlock(_tripB,
        150);

    assertEquals(2, stopTimes.size());
    assertEquals(_stA1, stopTimes.get(0));
    assertEquals(_stA2, stopTimes.get(1));

    stopTimes = _service.getSurroundingStopTimesFromDistanceAlongBlock(_tripC,
        150);

    assertEquals(2, stopTimes.size());
    assertEquals(_stA1, stopTimes.get(0));
    assertEquals(_stA2, stopTimes.get(1));
  }

  @Test
  public void test03() {

    List<StopTimeEntry> stopTimes = _service.getSurroundingStopTimes(_tripA,
        time(10, 15));

    assertEquals(2, stopTimes.size());
    assertEquals(_stA1, stopTimes.get(0));
    assertEquals(_stA2, stopTimes.get(1));

    stopTimes = _service.getSurroundingStopTimes(_tripB, time(10, 15));

    assertEquals(2, stopTimes.size());
    assertEquals(_stA1, stopTimes.get(0));
    assertEquals(_stA2, stopTimes.get(1));

    stopTimes = _service.getSurroundingStopTimes(_tripC, time(10, 15));

    assertEquals(2, stopTimes.size());
    assertEquals(_stA1, stopTimes.get(0));
    assertEquals(_stA2, stopTimes.get(1));
  }

  @Test
  public void test04() {

    List<StopTimeEntry> stopTimes = _service.getSurroundingStopTimes(_tripA,
        time(10, 25));

    assertEquals(2, stopTimes.size());
    assertEquals(_stA2, stopTimes.get(0));
    assertEquals(_stB1, stopTimes.get(1));

    stopTimes = _service.getSurroundingStopTimes(_tripB, time(10, 25));

    assertEquals(2, stopTimes.size());
    assertEquals(_stA2, stopTimes.get(0));
    assertEquals(_stB1, stopTimes.get(1));

    stopTimes = _service.getSurroundingStopTimes(_tripC, time(10, 25));

    assertEquals(2, stopTimes.size());
    assertEquals(_stA2, stopTimes.get(0));
    assertEquals(_stB1, stopTimes.get(1));
  }

  @Test
  public void test05() {

    List<StopTimeEntry> stopTimes = _service.getSurroundingStopTimes(_tripA,
        time(10, 30));

    assertEquals(1, stopTimes.size());
    assertEquals(_stB1, stopTimes.get(0));

    stopTimes = _service.getSurroundingStopTimes(_tripB, time(10, 30));

    assertEquals(1, stopTimes.size());
    assertEquals(_stB1, stopTimes.get(0));

    stopTimes = _service.getSurroundingStopTimes(_tripC, time(10, 30));

    assertEquals(1, stopTimes.size());
    assertEquals(_stB1, stopTimes.get(0));
  }

  @Test
  public void test06() {

    List<StopTimeEntry> stopTimes = _service.getSurroundingStopTimes(_tripA,
        time(10, 30));

    assertEquals(1, stopTimes.size());
    assertEquals(_stB1, stopTimes.get(0));

    stopTimes = _service.getSurroundingStopTimes(_tripB, time(10, 30));

    assertEquals(1, stopTimes.size());
    assertEquals(_stB1, stopTimes.get(0));

    stopTimes = _service.getSurroundingStopTimes(_tripC, time(10, 30));

    assertEquals(1, stopTimes.size());
    assertEquals(_stB1, stopTimes.get(0));
  }

  @Test
  public void test07() {

    List<StopTimeEntry> stopTimes = _service.getSurroundingStopTimes(_tripA,
        time(10, 45));

    assertEquals(2, stopTimes.size());
    assertEquals(_stB1, stopTimes.get(0));
    assertEquals(_stC1, stopTimes.get(1));

    stopTimes = _service.getSurroundingStopTimes(_tripB, time(10, 45));

    assertEquals(2, stopTimes.size());
    assertEquals(_stB1, stopTimes.get(0));
    assertEquals(_stC1, stopTimes.get(1));

    stopTimes = _service.getSurroundingStopTimes(_tripC, time(10, 45));

    assertEquals(2, stopTimes.size());
    assertEquals(_stB1, stopTimes.get(0));
    assertEquals(_stC1, stopTimes.get(1));
  }

  @Test
  public void test08() {

    List<StopTimeEntry> stopTimes = _service.getSurroundingStopTimes(_tripA,
        time(10, 55));

    assertEquals(3, stopTimes.size());
    assertEquals(_stC1, stopTimes.get(0));
    assertEquals(_stC2, stopTimes.get(1));
    assertEquals(_stC3, stopTimes.get(2));

    stopTimes = _service.getSurroundingStopTimes(_tripB, time(10, 55));

    assertEquals(3, stopTimes.size());
    assertEquals(_stC1, stopTimes.get(0));
    assertEquals(_stC2, stopTimes.get(1));
    assertEquals(_stC3, stopTimes.get(2));

    stopTimes = _service.getSurroundingStopTimes(_tripC, time(10, 55));

    assertEquals(3, stopTimes.size());
    assertEquals(_stC1, stopTimes.get(0));
    assertEquals(_stC2, stopTimes.get(1));
    assertEquals(_stC3, stopTimes.get(2));

    stopTimes = _service.getSurroundingStopTimesFromDistanceAlongBlock(_tripA,
        450);

    assertEquals(3, stopTimes.size());
    assertEquals(_stC1, stopTimes.get(0));
    assertEquals(_stC2, stopTimes.get(1));
    assertEquals(_stC3, stopTimes.get(2));

    stopTimes = _service.getSurroundingStopTimesFromDistanceAlongBlock(_tripB,
        450);

    assertEquals(3, stopTimes.size());
    assertEquals(_stC1, stopTimes.get(0));
    assertEquals(_stC2, stopTimes.get(1));
    assertEquals(_stC3, stopTimes.get(2));

    stopTimes = _service.getSurroundingStopTimesFromDistanceAlongBlock(_tripC,
        450);

    assertEquals(3, stopTimes.size());
    assertEquals(_stC1, stopTimes.get(0));
    assertEquals(_stC2, stopTimes.get(1));
    assertEquals(_stC3, stopTimes.get(2));
  }

  @Test
  public void test09() {

    List<StopTimeEntry> stopTimes = _service.getSurroundingStopTimes(_tripA,
        time(11, 20));

    assertEquals(0, stopTimes.size());

    stopTimes = _service.getSurroundingStopTimes(_tripB, time(11, 20));

    assertEquals(0, stopTimes.size());

    stopTimes = _service.getSurroundingStopTimes(_tripC, time(11, 20));

    assertEquals(0, stopTimes.size());

    stopTimes = _service.getSurroundingStopTimesFromDistanceAlongBlock(_tripA,
        700);

    assertEquals(0, stopTimes.size());

    stopTimes = _service.getSurroundingStopTimesFromDistanceAlongBlock(_tripB,
        700);

    assertEquals(0, stopTimes.size());

    stopTimes = _service.getSurroundingStopTimesFromDistanceAlongBlock(_tripC,
        700);

    assertEquals(0, stopTimes.size());
  }
}
