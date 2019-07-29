/**
 * Copyright (C) 2016 University of South Florida
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
package org.onebusaway.transit_data_federation.impl;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.block;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.blockConfiguration;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.dateAsLong;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.lsids;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.serviceIds;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stop;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stopTime;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.time;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.trip;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.TimepointPredictionRecord;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data.model.TransitDataConstants;
import org.onebusaway.transit_data_federation.impl.blocks.BlockStatusServiceImpl;
import org.onebusaway.transit_data_federation.impl.blocks.ScheduledBlockLocationServiceImpl;
import org.onebusaway.transit_data_federation.impl.realtime.BlockLocationServiceImpl;
import org.onebusaway.transit_data_federation.impl.realtime.VehicleLocationRecordCacheImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.BlockEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TripEntryImpl;
import org.onebusaway.transit_data_federation.model.StopTimeInstance;
import org.onebusaway.transit_data_federation.model.TargetTime;
import org.onebusaway.transit_data_federation.services.StopTimeService;
import org.onebusaway.transit_data_federation.services.StopTimeService.EFrequencyStopTimeBehavior;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.BlockStatusService;
import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocation;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocation;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Tests to see if the per stop time point predictions provided by a real-time
 * feed are correctly applied to the scheduled time, so the correct predicted
 * arrival times are produced. Behavior for propagating times is consistent with
 * the GTFS-realtime spec
 * (https://developers.google.com/transit/gtfs-realtime/).
 * 
 * @author cetin
 * @author barbeau
 */
public class ArrivalAndDepartureServiceImplTest {

  private ArrivalAndDepartureServiceImpl _service;

  private BlockStatusService _blockStatusService;

  private StopTimeService _stopTimeService;

  private BlockLocationServiceImpl _blockLocationService;

  // Setup current time
  private long mCurrentTime = dateAsLong("2015-07-23 13:00");

  private long mServiceDate = dateAsLong("2015-07-23 00:00");

  // Stops
  private StopEntryImpl mStopA = stop("stopA", 47.0, -122.0);

  private StopEntryImpl mStopB = stop("stopB", 47.0, -128.0);
  
  private StopEntryImpl mStopC = stop("stopC", 47.0, -130.0);
  
  private StopEntryImpl mStopD = stop("stopD", 47.0, -134.0);
  
  private TripEntryImpl mTrip1 = trip("tripA", "sA", 3000);

  private TripEntryImpl mTrip2 = trip("tripB", "sB", 3000);
  
  private TripEntryImpl mTrip3 = trip("tripC", "sC", 3000);

  @Before
  public void setup() {
    _service = new ArrivalAndDepartureServiceImpl();

    _blockStatusService = new BlockStatusServiceImpl();
    _service.setBlockStatusService(_blockStatusService);

    _stopTimeService = Mockito.mock(StopTimeServiceImpl.class);
    _service.setStopTimeService(_stopTimeService);

    _blockLocationService = new BlockLocationServiceImpl();
    _blockLocationService.setLocationInterpolation(false);
    _service.setBlockLocationService(_blockLocationService);
  }

  /**
   * This method tests time point predictions upstream of a stop for *arrival*
   * times.
   * 
   * Test configuration: Time point predictions are upstream of the stop and
   * include the given stop_ids, which means that the bus hasn't passed these
   * bus stops yet. There are 2 bus stops which have the real time arrival times
   * (time point predictions). In this case
   * getArrivalsAndDeparturesForStopInTimeRange() should return the absolute
   * time point prediction for particular stop that was provided by the feed,
   * which replaces the scheduled time from GTFS for these stops.
   * 
   * Current time = 13:00 
   *        Schedule time    Real-time from feed 
   * Stop A 13:30            13:30
   * Stop B 13:40            13:50
   * 
   * When requesting arrival estimate for Stop B, result should be 13:50 (same
   * as exact prediction from real-time feed).
   */
  @Test
  public void testGetArrivalsAndDeparturesForStopInTimeRange01() {

    // Set time point predictions for stop A
    TimepointPredictionRecord tprA = new TimepointPredictionRecord();
    tprA.setTimepointId(mStopA.getId());
    long tprATime = createPredictedTime(time(13, 30));
    tprA.setTimepointPredictedArrivalTime(tprATime);
    tprA.setTripId(mTrip1.getId());

    // Set time point predictions for stop B
    TimepointPredictionRecord tprB = new TimepointPredictionRecord();
    tprB.setTimepointId(mStopB.getId());
    long tprBTime = createPredictedTime(time(13, 50));
    tprB.setTimepointPredictedArrivalTime(tprBTime);
    tprB.setTripId(mTrip1.getId());

    // Call ArrivalsAndDeparturesForStopInTimeRange method in
    // ArrivalAndDepartureServiceImpl
    List<ArrivalAndDepartureInstance> arrivalsAndDepartures = getArrivalsAndDeparturesForStopInTimeRangeByTimepointPredictionRecord(Arrays.asList(
        tprA, tprB));

    long predictedArrivalTime = getPredictedArrivalTimeByStopId(
        arrivalsAndDepartures, mStopB.getId());
    /**
     * Check if the predictedArrivalTime is exactly the same as the
     * TimepointPrediction.
     */
    assertEquals(tprB.getTimepointPredictedArrivalTime(), predictedArrivalTime);
  }

  /**
   * This method tests upstream time point predictions for scheduled *arrival*
   * times.
   * 
   * Test configuration: Time point predictions are upstream of the current
   * stop_id, which means that the bus hasn't passed the bus stop yet. A real
   * time arrival time (time point prediction) is provided for only one bus stop
   * (Stop A). In this case getArrivalsAndDeparturesForStopInTimeRange() should
   * calculate a new arrival time for Stop B (based on the upstream prediction
   * for Stop A), which is the scheduled arrival time + the upstream deviation.
   * 
   * Current time = 13:00 
   *          Schedule time    Real-time from feed
   * Stop A   13:30            13:35
   * Stop B   13:40            ---
   * 
   * We are requesting arrival time for Stop B, which should be propagated
   * downstream from Stop A's prediction, which should be 13:45 (13:40 + 5 min
   * deviation from Stop A). Stop A's predicted arrival and departure should
   * also be the respective scheduled arrival and departure plus the 5 min
   * deviation.
   * 
   */
  @Test
  public void testGetArrivalsAndDeparturesForStopInTimeRange02() {

    // Set time point predictions for stop A
    TimepointPredictionRecord tprA = new TimepointPredictionRecord();
    tprA.setTimepointId(mStopA.getId());
    long tprATime = createPredictedTime(time(13, 35));
    tprA.setTimepointPredictedArrivalTime(tprATime);
    tprA.setTripId(mTrip1.getId());

    // Call ArrivalsAndDeparturesForStopInTimeRange method in
    // ArrivalAndDepartureServiceImpl
    List<ArrivalAndDepartureInstance> arrivalsAndDepartures = getArrivalsAndDeparturesForStopInTimeRangeByTimepointPredictionRecord(Arrays.asList(tprA));

    long predictedArrivalTimeStopA = getPredictedArrivalTimeByStopId(
        arrivalsAndDepartures, mStopA.getId());
    long predictedDepartureTimeStopA = getPredictedDepartureTimeByStopId(
        arrivalsAndDepartures, mStopA.getId());
    long predictedArrivalTimeStopB = getPredictedArrivalTimeByStopId(
        arrivalsAndDepartures, mStopB.getId());
    long predictedDepartureTimeStopB = getPredictedDepartureTimeByStopId(
        arrivalsAndDepartures, mStopB.getId());

    long scheduledArrivalTimeStopA = getScheduledArrivalTimeByStopId(mTrip1,
        mStopA.getId());
    long scheduledDepartureTimeStopA = getScheduledDepartureTimeByStopId(
        mTrip1, mStopA.getId());
    long scheduledArrivalTimeStopB = getScheduledArrivalTimeByStopId(mTrip1,
        mStopB.getId());
    long scheduledDepartureTimeStopB = getScheduledDepartureTimeByStopId(
        mTrip1, mStopB.getId());

    // The time point prediction for Stop A was 5 min late, so this should be
    // applied to Stop B scheduled arrival
    long delta = TimeUnit.MILLISECONDS.toSeconds(predictedArrivalTimeStopA)
        - scheduledArrivalTimeStopA;
    assertEquals(TimeUnit.MINUTES.toSeconds(5), delta);

    // Check if the predictedArrivalTimes and predictedDepartureTimes is the
    // same as the scheduledArrivalTime plus the delta
    assertEquals(TimeUnit.MILLISECONDS.toSeconds(predictedArrivalTimeStopA),
        scheduledArrivalTimeStopA + delta);
    assertEquals(TimeUnit.MILLISECONDS.toSeconds(predictedDepartureTimeStopA),
        scheduledDepartureTimeStopA + delta);
    assertEquals(TimeUnit.MILLISECONDS.toSeconds(predictedArrivalTimeStopB),
        scheduledArrivalTimeStopB + delta);
    assertEquals(TimeUnit.MILLISECONDS.toSeconds(predictedDepartureTimeStopB),
        scheduledDepartureTimeStopB + delta);
  }

  /**
   * This method tests upstream time point predictions with only a predicted
   * *departure* time.
   * 
   * Test configuration: There is only one bus stop (Stop A) which has the real
   * time departure time (time point prediction). In this case
   * getArrivalsAndDeparturesForStopInTimeRange() should return the time point
   * prediction for Stop A's departure time, which replaces the scheduled time
   * from GTFS for this stop. For Stop B, the upstream departure prediction for
   * Stop A should be propagated down to Stop B, and this deviation should be
   * used to calculate Stop B's arrival and departure times.
   * 
   * Current time = 13:00 
   *          Schedule Arrival time    Schedule Departure time    Real-time departure time
   * Stop A   13:30                    13:35                      13:30
   * Stop B   13:45                    13:50                      ----
   * 
   * When requesting arrival estimate for Stop A, result should be 0 (note this
   * isn't currently supported - see FIXME in method body).
   * 
   * When requesting departure estimate for Stop A, result should be exactly
   * same with the real-time feed's departure time for Stop A.
   * 
   * When requesting arrival and departure estimate for Stop B, the result
   * should be 5 min less then the scheduled arrival and departure times.
   * Because the upstream stop departs 5 min early, OBA should subtract this 5
   * min deviation from the downstream scheduled values.
   */
  @Test
  public void testGetArrivalsAndDeparturesForStopInTimeRange03() {

    // Set time point predictions for stop A
    TimepointPredictionRecord tprA = new TimepointPredictionRecord();
    tprA.setTimepointId(mStopA.getId());
    long tprATime = createPredictedTime(time(13, 30));
    tprA.setTimepointPredictedDepartureTime(tprATime);
    tprA.setTripId(mTrip1.getId());

    // Call ArrivalsAndDeparturesForStopInTimeRange method in
    // ArrivalAndDepartureServiceImpl
    List<ArrivalAndDepartureInstance> arrivalsAndDepartures = getArrivalsAndDeparturesForStopInTimeRangeByTimepointPredictionRecord(Arrays.asList(tprA));

    long predictedArrivalTimeStopA = getPredictedArrivalTimeByStopId(
        arrivalsAndDepartures, mStopA.getId());

    long predictedDepartureTimeStopA = getPredictedDepartureTimeByStopId(
        arrivalsAndDepartures, mStopA.getId());
    /**
     * Check if the predictedDepartureTime is exactly the same with
     * TimepointPrediction.
     */
    assertEquals(tprA.getTimepointPredictedDepartureTime(),
        predictedDepartureTimeStopA);

    /**
     * FIXME - Fully support both real-time arrival and departure times for each
     * stop in OBA
     * 
     * We're currently limited by OBA's internal data model which contains only
     * one deviation per stop. By GTFS-rt spec, if no real-time arrival
     * information is given for a stop, then the scheduled arrival should be
     * used. In our case here, we're getting a real-time departure for Stop A
     * (and no real-time arrival time for Stop A), but then we're showing the
     * real-time departure info for Stop A as the real-time arrival time for
     * Stop A. So, we're effectively propagating the real-time value backwards
     * within the same stop. The correct value for predictedArrivalTimeStopA is
     * actually 0, because we don't have any real-time arrival information for
     * Stop A (or upstream of Stop A).
     * 
     * So, the below assertion is currently commented out, as it fails. Future
     * work should overhaul OBA's data model to support more than one real-time
     * deviation per stop. When this is correctly implemented, the below
     * assertion should be uncommented and it should pass.
     */
    /*
     * TODO: for backward compatiability we did not implement this.  Discuss!
     */
    // assertEquals(0, predictedArrivalTimeStopA);

    /**
     * Test for Stop B
     */

    long predictedArrivalTimeStopB = getPredictedArrivalTimeByStopId(
        arrivalsAndDepartures, mStopB.getId());

    long predictedDepartureTimeStopB = getPredictedDepartureTimeByStopId(
        arrivalsAndDepartures, mStopB.getId());

    long scheduledDepartureTimeForStopA = getScheduledDepartureTimeByStopId(
        mTrip1, mStopA.getId());
    long scheduledArrivalTimeForStopB = getScheduledArrivalTimeByStopId(mTrip1,
        mStopB.getId());
    long scheduledDepartureTimeForStopB = getScheduledDepartureTimeByStopId(
        mTrip1, mStopB.getId());

    // Calculate the departure time difference from the upstream stop
    long deltaB = (scheduledDepartureTimeForStopA - TimeUnit.MILLISECONDS.toSeconds(predictedDepartureTimeStopA));

    /**
     * Check if the predictedArrivalTime is 5 min less then the scheduled
     * arrival time for stop B.
     */
    assertEquals(TimeUnit.MINUTES.toSeconds(5), deltaB);
    assertEquals(scheduledArrivalTimeForStopB - deltaB,
        TimeUnit.MILLISECONDS.toSeconds(predictedArrivalTimeStopB));

    /**
     * Check if the predictedDepartureTime is 5 min less then the scheduled
     * departure time for stop B.
     */
    assertEquals(scheduledDepartureTimeForStopB - deltaB,
        TimeUnit.MILLISECONDS.toSeconds(predictedDepartureTimeStopB));
  }

  /**
   * This method tests upstream time point predictions with both predicted
   * arrival and departure times.
   * 
   * Test configuration: Time point predictions are upstream and include the
   * current stop_id, which means that the bus hasn't passed the bus stop yet.
   * There is only one bus stop (Stop A) which has the real time arrival and
   * departure times (time point prediction). In this case
   * getArrivalsAndDeparturesForStopInTimeRange() should return absolute time
   * point prediction for Stop A's departure time, which replaces the scheduled
   * time from GTFS for this stop. Stop B's predictions should be derived from
   * the upstream predictions provided for Stop A.
   * 
   * Current time = 13:00 
   *          Schedule Arrival time    Schedule Departure time    Real-time arrival time    Real-time departure time
   * Stop A   13:30                    13:35                      13:20                     13:30
   * Stop B   13:45                    13:50                      -----                     -----
   * 
   * When requesting arrival estimate for Stop A, result should be 13:20
   * (predicted real-time arrival time). Note that this currently isn't support
   * - see FIXME statement in method body.
   * 
   * When requesting departure estimate for Stop A, result should be 13:30
   * (predicted real-time departure time).
   * 
   * When requesting arrival and departure estimates for Stop B, results should
   * be 5 min less then the scheduled arrival and departure times. Because the
   * upstream Stop A departs 5 min early, OBA should subtract this 5 min from
   * the downstream estimates.
   */
  @Test
  public void testGetArrivalsAndDeparturesForStopInTimeRange04() {

    // Set time point predictions for stop A
    TimepointPredictionRecord tprA = new TimepointPredictionRecord();
    tprA.setTimepointId(mStopA.getId());
    long tprADepartureTime = createPredictedTime(time(13, 30));
    tprA.setTimepointPredictedDepartureTime(tprADepartureTime);
    long tprAArrivalTime = createPredictedTime(time(13, 20));
    tprA.setTimepointPredictedArrivalTime(tprAArrivalTime);
    tprA.setTripId(mTrip1.getId());

    // Call ArrivalsAndDeparturesForStopInTimeRange method in
    // ArrivalAndDepartureServiceImpl
    List<ArrivalAndDepartureInstance> arrivalsAndDepartures = getArrivalsAndDeparturesForStopInTimeRangeByTimepointPredictionRecord(Arrays.asList(tprA));

    long predictedDepartureTimeStopA = getPredictedDepartureTimeByStopId(
        arrivalsAndDepartures, mStopA.getId());
    /**
     * Check if the predictedDepartureTime is exactly the same with
     * TimepointPrediction.
     */
    assertEquals(tprA.getTimepointPredictedDepartureTime(),
        predictedDepartureTimeStopA);

    /**
     * OBA's data model now supports more than one real-time
     * deviation per stop. 
     */

     long predictedArrivalTimeStopA = getPredictedArrivalTimeByStopId(
     arrivalsAndDepartures, mStopA.getId());
    
     assertEquals(TimeUnit.MILLISECONDS.toSeconds(tprA.getTimepointPredictedArrivalTime()),
     TimeUnit.MILLISECONDS.toSeconds(predictedArrivalTimeStopA));

    /**
     * Test for Stop B
     */

    long scheduledDepartureTimeForStopA = getScheduledDepartureTimeByStopId(
        mTrip1, mStopA.getId());

    long predictedArrivalTimeStopB = getPredictedArrivalTimeByStopId(
        arrivalsAndDepartures, mStopB.getId());

    long predictedDepartureTimeStopB = getPredictedDepartureTimeByStopId(
        arrivalsAndDepartures, mStopB.getId());

    long scheduledArrivalTimeForStopB = getScheduledArrivalTimeByStopId(mTrip1,
        mStopB.getId());
    long scheduledDepartureTimeForStopB = getScheduledDepartureTimeByStopId(
        mTrip1, mStopB.getId());

    // Calculate the departure time difference from the upstream stop
    long deltaB = scheduledDepartureTimeForStopA
        - TimeUnit.MILLISECONDS.toSeconds(predictedDepartureTimeStopA);

    /**
     * Check if the predictedDepartureTime is 5 min less then the scheduled
     * departure time for stop B.
     */
    assertEquals(TimeUnit.MINUTES.toSeconds(5), deltaB);
    assertEquals(scheduledDepartureTimeForStopB - deltaB,
        TimeUnit.MILLISECONDS.toSeconds(predictedDepartureTimeStopB));

    /**
     * Check if the predictedArrivalTime is 5 min less then the scheduled
     * arrival time for stop B.
     */
    assertEquals(scheduledArrivalTimeForStopB - deltaB,
        TimeUnit.MILLISECONDS.toSeconds(predictedArrivalTimeStopB));
  }

  /**
   * This method tests a request for an arrival time for a stop, when the
   * current time is greater than the arrival time prediction for that stop
   * (Stop B). In other words, the bus is predicted to have already passed the
   * stop (Stop B).
   * 
   * Test configuration: There are 2 bus stops which have the real time arrival
   * times (time point predictions) - Stop A and B. In this case
   * getArrivalsAndDeparturesForStopInTimeRange() should return last received
   * time point prediction for particular stop we're requesting information for.
   * 
   * Current time = 14:00 
   *          Schedule time    Real-time from feed
   * Stop A   13:30            13:30
   * Stop B   13:40            13:50
   * 
   */
  @Test
  public void testGetArrivalsAndDeparturesForStopInTimeRange05() {
    // Override the current time with a later time than the time point
    // predictions
    mCurrentTime = dateAsLong("2015-07-23 14:00");

    // Set time point predictions for stop A
    TimepointPredictionRecord tprA = new TimepointPredictionRecord();
    tprA.setTimepointId(mStopA.getId());
    long tprATime = createPredictedTime(time(13, 30));
    tprA.setTimepointPredictedArrivalTime(tprATime);
    tprA.setTripId(mTrip1.getId());

    // Set time point predictions for stop B
    TimepointPredictionRecord tprB = new TimepointPredictionRecord();
    tprB.setTimepointId(mStopB.getId());
    long tprBTime = createPredictedTime(time(13, 50));
    tprB.setTimepointPredictedArrivalTime(tprBTime);
    tprB.setTripId(mTrip1.getId());

    // Call ArrivalsAndDeparturesForStopInTimeRange method in
    // ArrivalAndDepartureServiceImpl
    List<ArrivalAndDepartureInstance> arrivalsAndDepartures = getArrivalsAndDeparturesForStopInTimeRangeByTimepointPredictionRecord(Arrays.asList(
        tprA, tprB));

    long predictedArrivalTimeStopA = getPredictedArrivalTimeByStopId(
        arrivalsAndDepartures, mStopA.getId());
    long predictedArrivalTimeStopB = getPredictedArrivalTimeByStopId(
        arrivalsAndDepartures, mStopB.getId());

    /**
     * Check if the predictedArrivalTime is exactly the same as
     * TimepointPrediction for both stops
     */
    assertEquals(tprA.getTimepointPredictedArrivalTime(),
        predictedArrivalTimeStopA);
    assertEquals(tprB.getTimepointPredictedArrivalTime(),
        predictedArrivalTimeStopB);

    /**
     * Check if the predictedDepartureTimes and scheduledDepartureTimes have the
     * same delta as arrival predictions and scheduled arrival times for both
     * stops
     */
    long predictedDepartureTimeStopA = getPredictedDepartureTimeByStopId(
        arrivalsAndDepartures, mStopA.getId());
    long predictedDepartureTimeStopB = getPredictedDepartureTimeByStopId(
        arrivalsAndDepartures, mStopB.getId());

    long scheduledArrivalTimeForStopA = getScheduledArrivalTimeByStopId(mTrip1,
        mStopA.getId());
    long scheduledArrivalTimeForStopB = getScheduledArrivalTimeByStopId(mTrip1,
        mStopB.getId());
    long scheduledDepartureTimeForStopA = getScheduledDepartureTimeByStopId(
        mTrip1, mStopA.getId());
    long scheduledDepartureTimeForStopB = getScheduledDepartureTimeByStopId(
        mTrip1, mStopB.getId());

    long deltaA = TimeUnit.MILLISECONDS.toSeconds(predictedArrivalTimeStopA)
        - scheduledArrivalTimeForStopA;
    assertEquals(scheduledDepartureTimeForStopA + deltaA,
        TimeUnit.MILLISECONDS.toSeconds(predictedDepartureTimeStopA));

    long deltaB = TimeUnit.MILLISECONDS.toSeconds(predictedArrivalTimeStopB)
        - scheduledArrivalTimeForStopB;
    assertEquals(scheduledDepartureTimeForStopB + deltaB,
        TimeUnit.MILLISECONDS.toSeconds(predictedDepartureTimeStopB));
  }

  /**
   * This method tests to make sure upstream propagation isn't happening.
   * 
   * Test configuration: Time point predictions are downstream of Stop A, which
   * means that the bus is predicted to have already passed the bus stop. There
   * only one bus stop (Stop B) which has a real time arrival time (time point
   * prediction). In this case getArrivalsAndDeparturesForStopInTimeRange() for
   * Stop A should return a predicted arrival time = 0, indicating that no
   * real-time information is available for Stop A.
   * 
   * Current time = 14:00
   *          Schedule time    Real-time from feed
   * Stop A   13:30            -----
   * Stop B   13:45            13:40
   * 
   * Since the bus already passed the bus stop A, and no real-time information
   * is available for Stop A, OBA should NOT propagate arrival estimate for Stop
   * B upstream to Stop A.
   */
  @Test
  public void testGetArrivalsAndDeparturesForStopInTimeRange06() {
    // Override the current time with a later time than the time point
    // predictions
    mCurrentTime = dateAsLong("2015-07-23 14:00");

    // Set time point predictions for stop B
    TimepointPredictionRecord tprB = new TimepointPredictionRecord();
    tprB.setTimepointId(mStopB.getId());
    long tprBTime = createPredictedTime(time(13, 40));
    tprB.setTimepointPredictedArrivalTime(tprBTime);
    tprB.setTripId(mTrip1.getId());

    // Call ArrivalsAndDeparturesForStopInTimeRange method in
    // ArrivalAndDepartureServiceImpl
    List<ArrivalAndDepartureInstance> arrivalsAndDepartures = getArrivalsAndDeparturesForStopInTimeRangeByTimepointPredictionRecord(Arrays.asList(tprB));

    long predictedArrivalTimeStopB = getPredictedArrivalTimeByStopId(
        arrivalsAndDepartures, mStopB.getId());
    /**
     * Check if the predictedArrivalTime for stop B is exactly the same as
     * TimepointPredictionRecord.
     */
    assertEquals(tprB.getTimepointPredictedArrivalTime(),
        predictedArrivalTimeStopB);

    /**
     * Check predicted departure for Stop B too, to make sure its propagated
     * from provided predicted arrival time
     */
    long scheduledArrivalTimeForStopB = getScheduledArrivalTimeByStopId(mTrip1,
        mStopB.getId());
    long scheduledDepartureTimeForStopB = getScheduledDepartureTimeByStopId(
        mTrip1, mStopB.getId());
    long predictedDepartureTimeStopB = getPredictedDepartureTimeByStopId(
        arrivalsAndDepartures, mStopB.getId());
    long deltaB = TimeUnit.MILLISECONDS.toSeconds(predictedArrivalTimeStopB)
        - scheduledArrivalTimeForStopB;
    assertEquals(scheduledDepartureTimeForStopB + deltaB,
        TimeUnit.MILLISECONDS.toSeconds(predictedDepartureTimeStopB));

    /**
     * Make sure the predictedArrivalTime for stop A is equals to 0 - in other
     * words, we should show no real-time information for this stop and use the
     * scheduled time instead.
     */

    long predictedArrivalTimeA = getPredictedArrivalTimeByStopId(
        arrivalsAndDepartures, mStopA.getId());
    assertEquals(0, predictedArrivalTimeA);
  }
  
  /**
   * This method tests loop routes (the same stop is visited more
   * than once in the same trip) when stop_sequence is missing in the real-time feed.
   * It applies the prediction if there is more than one prediction in the same trip 
   * (i.e., the stop_id can be disambiguated).
   * 
   * Test configuration: There is only one time point prediction for the first stop,
   * and there is only one trip in the block. But time point predictions do not have 
   * stop_sequences. In this case getArrivalsAndDeparturesForStopInTimeRange() for
   * Stop A should return a predicted arrival time = 0, indicating that no
   * real-time information is available for Stop A (i.e., we drop the prediction,
   * because it is ambiguous and could refer to more than one stop instance - Stop A
   * appears twice).
   * 
   * Current time = 14:00
   *          Schedule time    Real-time from feed      GTFS stop_sequence
   * Stop A   13:30            13:35                          0
   * Stop B   13:45            -----                          1
   * Stop A   13:55            -----                          2
   * 
   * Since we
   */
  @Test
  public void testGetArrivalsAndDeparturesForStopInTimeRange07() {
    // Override the current time with a later time than the time point
    // predictions
    mCurrentTime = dateAsLong("2015-07-23 14:00");

    // Set time point predictions for stop A
    TimepointPredictionRecord tprA = new TimepointPredictionRecord();
    tprA.setTimepointId(mStopA.getId());
    long tprBTime = createPredictedTime(time(13, 35));
    tprA.setTimepointPredictedArrivalTime(tprBTime);
    tprA.setTripId(mTrip1.getId());

    // Call ArrivalsAndDeparturesForStopInTimeRange method in
    // ArrivalAndDepartureServiceImpl
    List<ArrivalAndDepartureInstance> arrivalsAndDepartures = 
        getArrivalsAndDeparturesForLoopRouteInTimeRangeByTimepointPredictionRecord(Arrays.asList(tprA), mStopA);


    /**
     * Make sure the predictedArrivalTime for stop A (first and the last stop) and stop B
     * is equals to 0 - in other words, we should show no real-time information for this trip
     * and use the scheduled time instead.
     */

    //
    long predictedArrivalTimeA = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), 0);
    assertEquals(0, predictedArrivalTimeA);
    
    long predictedArrivalTimeB = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopB.getId(), 1);
    assertEquals(0, predictedArrivalTimeB);
    
    predictedArrivalTimeA = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), 2);
    assertEquals(0, predictedArrivalTimeA);
  }

  /**
   * This method tests loop routes (the same stop is visited more
   * than once in the same trip) when stop_sequence is missing in the real-time feed.
   * It applies the prediction if there is more than one prediction in the same trip 
   * (i.e., the stop_id can be disambiguated).
   * 
   * Test configuration: There are two time point predictions, one for the first stop
   * and one for second stop, and there is a single trip in the block. The time point 
   * predictions do not have stop_sequences. In this case, propagation should happen as normal.  
   * getArrivalsAndDeparturesForStopInTimeRange() the first Stop A  and stop B predictions 
   * should match with time point predictions. The last Stop A should return a predicted 
   * arrival based on stop B (6 min delay).
   * 
   * Current time = 14:00
   *          Schedule time    Real-time from feed      GTFS stop_sequence
   * Stop A   13:30            13:35                          0
   * Stop B   13:45            13:51                          1
   * Stop A   13:55            -----                          2
   * 
   */
  @Test
  public void testGetArrivalsAndDeparturesForStopInTimeRange08() {
    // Override the current time with a later time than the time point
    // predictions
    mCurrentTime = dateAsLong("2015-07-23 14:00");

 // Set time point predictions for stop A
    TimepointPredictionRecord tprA = new TimepointPredictionRecord();
    tprA.setTimepointId(mStopA.getId());
    long tprATime = createPredictedTime(time(13, 35));
    tprA.setTimepointPredictedArrivalTime(tprATime);
    tprA.setTripId(mTrip1.getId());
    
    // Set time point predictions for stop B
    TimepointPredictionRecord tprB = new TimepointPredictionRecord();
    tprB.setTimepointId(mStopB.getId());
    long tprBTime = createPredictedTime(time(13, 51));
    tprB.setTimepointPredictedArrivalTime(tprBTime);
    tprB.setTripId(mTrip1.getId());

    // Call ArrivalsAndDeparturesForStopInTimeRange method in
    // ArrivalAndDepartureServiceImpl
    List<ArrivalAndDepartureInstance> arrivalsAndDepartures = 
        getArrivalsAndDeparturesForLoopRouteInTimeRangeByTimepointPredictionRecord(Arrays.asList(tprA, tprB), mStopB);

    long predictedArrivalTimeStopB = getPredictedArrivalTimeByStopId(
        arrivalsAndDepartures, mStopB.getId());
    /**
     * Check if the predictedArrivalTime for stop B is exactly the same as
     * TimepointPredictionRecord.
     */
    assertEquals(tprB.getTimepointPredictedArrivalTime(),
        predictedArrivalTimeStopB);

    /**
     * Make sure the predictedArrivalTime for stop A (the first stop)  is exactly the same as
     * TimepointPredictionRecord.
     */
    long predictedArrivalTimeA = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), 0);
    assertEquals(tprA.getTimepointPredictedArrivalTime(), predictedArrivalTimeA);
    
    /**
     * Make sure the predictedArrivalTime for stop A (the last stop)  is propagated based on
     * TimepointPredictionRecord of the stop B
     */
    
    long scheduledArrivalTimeStopB = getScheduledArrivalTimeByStopId(mTrip1,
        mStopB.getId());
    long scheduledArrivalTimeLastStopA = getScheduledArrivalTimeByStopId(mTrip1,
        mStopA.getId(), 2);

    /**
     * Calculate the delay of the previous stop(stop B)
     */
    long delta = TimeUnit.MILLISECONDS.toSeconds(predictedArrivalTimeStopB)
        - scheduledArrivalTimeStopB;

    predictedArrivalTimeA = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), 2);

    assertEquals(scheduledArrivalTimeLastStopA + delta , TimeUnit.MILLISECONDS.toSeconds(predictedArrivalTimeA));
  }
  
  /**
   * This method tests loop routes (the same stop is visited more
   * than once in the same trip) when stop_sequence is missing in the real-time feed.
   * It applies the prediction if there is more than one prediction in the same trip 
   * (i.e., the stop_id can be disambiguated).
   * 
   * Test configuration: There are two time point predictions for the 2nd and the last stop.
   * But time point predictions do not have stop_sequences. There is a single trip
   * in the block.  In this case getArrivalsAndDeparturesForStopInTimeRange() for the
   * first instance of Stop A should not have any real-time data (i.e., the schedule time 
   * should be shown).  The  Stop B should return a predicted arrival time =  13:50 (the prediction
   * for this stop). The last Stop A should return a predicted arrival time = 13:58 
   * (the estimate for the last Stop A instance).
   * 
   * 
   * Current time = 14:00
   *          Schedule time    Real-time from feed      GTFS stop_sequence
   * Stop A   13:30            -----                          0
   * Stop B   13:45            13:50                          1
   * Stop A   13:55            13:58                          2
   * 
   */
  @Test
  public void testGetArrivalsAndDeparturesForStopInTimeRange09() {
    // Override the current time with a later time than the time point
    // predictions
    mCurrentTime = dateAsLong("2015-07-23 14:00");

    // Set time point predictions for stop A (last instance)
    TimepointPredictionRecord tprA = new TimepointPredictionRecord();
    tprA.setTimepointId(mStopA.getId());
    long tprATime = createPredictedTime(time(13, 58));
    tprA.setTimepointPredictedArrivalTime(tprATime);
    tprA.setTripId(mTrip1.getId());
    
    // Set time point predictions for stop B
    TimepointPredictionRecord tprB = new TimepointPredictionRecord();
    tprB.setTimepointId(mStopB.getId());
    long tprBTime = createPredictedTime(time(13, 50));
    tprB.setTimepointPredictedArrivalTime(tprBTime);
    tprB.setTripId(mTrip1.getId());

    // Call ArrivalsAndDeparturesForStopInTimeRange method in
    // ArrivalAndDepartureServiceImpl
    List<ArrivalAndDepartureInstance> arrivalsAndDepartures = 
        getArrivalsAndDeparturesForLoopRouteInTimeRangeByTimepointPredictionRecord(Arrays.asList(tprB, tprA), mStopB);

    long predictedArrivalTimeStopB = getPredictedArrivalTimeByStopId(
        arrivalsAndDepartures, mStopB.getId());
    
    /**
     * Make sure the predictedArrivalTime for stop A (the first stop)  is equals to 0 - in other
     * words, we should show no real-time information for this stop and use the
     * scheduled time instead.
     */
    long predictedArrivalTimeA = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), 0);
    assertEquals(0, predictedArrivalTimeA);
    
    /**
     * Check if the predictedArrivalTime for stop B is exactly the same as
     * TimepointPredictionRecord.
     */
    assertEquals(tprB.getTimepointPredictedArrivalTime(),
        predictedArrivalTimeStopB);

    /**
     * Make sure the predictedArrivalTime for stop A (the last stop)  is exactly the same as
     * TimepointPredictionRecord.
     */
     predictedArrivalTimeA = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), 2);
    assertEquals(tprA.getTimepointPredictedArrivalTime(), predictedArrivalTimeA);
  }
  
  /**
   * This method tests loop routes (the same stop is visited more
   * than once in the same trip) when stop_sequence is missing in the real-time feed.
   * It applies the prediction if there is more than one prediction in the same trip 
   * (i.e., the stop_id can be disambiguated).
   * 
   * Test configuration: There are three different loop trips and each trip has 3 stops
   * Time point predictions does not have stop sequences.  First 2 stops in middle trip 
   * have predictions.
   * 
   * Current time = 14:00
   *          Schedule time    Real-time from feed      GTFS stop_sequence  trip_id
   * Stop A   13:30            -----                          0           t1
   * Stop B   13:45            -----                          1           t1
   * Stop A   13:55            -----                          2           t1
   * 
   * Stop A   14:05            14:10                          0             t2
   * Stop B   14:15            14:25                          1           t2
   * Stop A   14:25            -----                          2             t2
   * 
   * Stop A   14:30            -----                          0           t3
   * Stop B   14:45            -----                          1           t3
   * Stop A   14:55            -----                          2           t3
   * 
   */
  @Test
  public void testGetArrivalsAndDeparturesForStopInTimeRange10() {
    // Override the current time with a later time than the time point
    // predictions
    mCurrentTime = dateAsLong("2015-07-23 14:00");

 // Set time point predictions for stop A
    TimepointPredictionRecord tprA = new TimepointPredictionRecord();
    tprA.setTimepointId(mStopA.getId());
    long tprATime = createPredictedTime(time(14, 10));
    tprA.setTimepointPredictedArrivalTime(tprATime);
    tprA.setTripId(mTrip2.getId());
    
    // Set time point predictions for stop B
    TimepointPredictionRecord tprB = new TimepointPredictionRecord();
    tprB.setTimepointId(mStopB.getId());
    long tprBTime = createPredictedTime(time(14, 25));
    tprB.setTimepointPredictedArrivalTime(tprBTime);
    tprB.setTripId(mTrip2.getId());

    // Call ArrivalsAndDeparturesForStopInTimeRange method in
    // ArrivalAndDepartureServiceImpl
    List<ArrivalAndDepartureInstance> arrivalsAndDepartures = 
        getArrivalsAndDeparturesForLoopRouteInTimeRangeByTimepointPredictionRecordWithMultipleTrips(Arrays.asList(tprA, tprB), mStopB);

    long predictedArrivalTimeStopAA = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), mTrip1.getId(), 0);
    long predictedArrivalTimeStopAB = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopB.getId(), mTrip1.getId(), 1);
    long predictedArrivalTimeStopAC = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), mTrip1.getId(), 2);
    
    /**
     * Check the upstream stops and make sure no propagation happening.
     */
    assertEquals(predictedArrivalTimeStopAA, 0);
    assertEquals(predictedArrivalTimeStopAB, 0);
    assertEquals(predictedArrivalTimeStopAC, 0);

    /**
     * Make sure the predictedArrivalTime for stop A (the last stop) and the stop B in 
     * the trip B is exactly the same as TimepointPredictionRecord.
     */
    long predictedArrivalTimeBA = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), mTrip2.getId(), 0);
    long predictedArrivalTimeBB = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopB.getId(), mTrip2.getId(), 1);
    
    assertEquals(tprA.getTimepointPredictedArrivalTime(), predictedArrivalTimeBA);
    assertEquals(tprB.getTimepointPredictedArrivalTime(), predictedArrivalTimeBB);
    
    /**
     * Make sure the predictions happening downstream based on the last stop
     * of the trip B
     */
    
    long scheduledArrivalTimeBB = getScheduledArrivalTimeByStopId(mTrip2,
        mStopB.getId(), 1);
    // Calculate the delay of the last stop A in trip B
    long delta = TimeUnit.MILLISECONDS.toSeconds(predictedArrivalTimeBB)
        - scheduledArrivalTimeBB;
    
    long scheduledArrivalTimeStopBC = getScheduledArrivalTimeByStopId(mTrip2,
        mStopA.getId(), 2);
    long predictedArrivalTimeBC = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), mTrip2.getId(), 2);
    
    long scheduledArrivalTimeStopCA = getScheduledArrivalTimeByStopId(mTrip3,
        mStopA.getId(), 0);
    long predictedArrivalTimeCA = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), mTrip3.getId(), 0);
    
    long scheduledArrivalTimeStopCB = getScheduledArrivalTimeByStopId(mTrip3,
        mStopB.getId(), 1);
    long predictedArrivalTimeCB = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopB.getId(), mTrip3.getId(), 1);
    
    long scheduledArrivalTimeStopCC = getScheduledArrivalTimeByStopId(mTrip3,
        mStopA.getId(), 2);
    long predictedArrivalTimeCC = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), mTrip3.getId(), 2);

    assertEquals(scheduledArrivalTimeStopBC + delta , TimeUnit.MILLISECONDS.toSeconds(predictedArrivalTimeBC));
    assertEquals(scheduledArrivalTimeStopCA + delta , TimeUnit.MILLISECONDS.toSeconds(predictedArrivalTimeCA));
    assertEquals(scheduledArrivalTimeStopCB + delta , TimeUnit.MILLISECONDS.toSeconds(predictedArrivalTimeCB));
    assertEquals(scheduledArrivalTimeStopCC + delta , TimeUnit.MILLISECONDS.toSeconds(predictedArrivalTimeCC));
  }
  
  /**
   * This method tests loop routes (the same stop is visited more
   * than once in the same trip) when stop_sequence is missing in the real-time feed.
   * It applies the prediction if there is more than one prediction in the same trip 
   * (i.e., the stop_id can be disambiguated).
   * 
   * Test configuration: There are three different loop trips in the same block
   * and each trip has 3 stops.  Time point predictions do not have stop_sequences.
   * Last 2 stops in middle trip have predictions.
   * 
   * Current time = 14:00
   *          Schedule time    Real-time from feed      GTFS stop_sequence  trip_id
   * Stop A   13:30            -----                          0           t1
   * Stop B   13:45            -----                          1           t1
   * Stop A   13:55            -----                          2           t1
   * 
   * Stop A   14:05            -----                          0             t2
   * Stop B   14:15            14:25                          1           t2
   * Stop A   14:25            14:35                          2             t2
   * 
   * Stop A   14:30            -----                          0           t3
   * Stop B   14:45            -----                          1           t3
   * Stop A   14:55            -----                          2           t3
   * 
   */
  @Test
  public void testGetArrivalsAndDeparturesForStopInTimeRange11() {
    // Override the current time with a later time than the time point
    // predictions
    mCurrentTime = dateAsLong("2015-07-23 14:00");

    // Set time point predictions for trip 2 stop B
    TimepointPredictionRecord tprB = new TimepointPredictionRecord();
    tprB.setTimepointId(mStopB.getId());
    long tprBTime = createPredictedTime(time(14, 25));
    tprB.setTimepointPredictedArrivalTime(tprBTime);
    tprB.setTripId(mTrip2.getId());
    
    // Set time point predictions for trip 3 stop A (last instance)
    TimepointPredictionRecord tprA = new TimepointPredictionRecord();
    tprA.setTimepointId(mStopA.getId());
    long tprATime = createPredictedTime(time(14, 35));
    tprA.setTimepointPredictedArrivalTime(tprATime);
    tprA.setTripId(mTrip2.getId());

    // Call ArrivalsAndDeparturesForStopInTimeRange method in
    // ArrivalAndDepartureServiceImpl
    List<ArrivalAndDepartureInstance> arrivalsAndDepartures = 
        getArrivalsAndDeparturesForLoopRouteInTimeRangeByTimepointPredictionRecordWithMultipleTrips(Arrays.asList(tprB, tprA), mStopB);

    // First trip in block
    long predictedArrivalTimeStop1A = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), mTrip1.getId(), 0);
    long predictedArrivalTimeStop1B = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopB.getId(), mTrip1.getId(), 1);
    long predictedArrivalTimeStop1C = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), mTrip1.getId(), 2);
    
    assertEquals(predictedArrivalTimeStop1A, 0);
    assertEquals(predictedArrivalTimeStop1B, 0);
    assertEquals(predictedArrivalTimeStop1C, 0);
    
    // Second trip in block
    long predictedArrivalTimeStop2A = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), mTrip2.getId(), 0);
    assertEquals(predictedArrivalTimeStop2A, 0);

    /**
     * Make sure the predictedArrivalTime for stop B and stop A (the last stop) in 
     * the trip B is exactly the same as TimepointPredictionRecords.
     */
    long predictedArrivalTime2B = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopB.getId(), mTrip2.getId(), 1);
    long predictedArrivalTime2C = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), mTrip2.getId(), 2);
    
    assertEquals(tprB.getTimepointPredictedArrivalTime(), predictedArrivalTime2B);
    assertEquals(tprA.getTimepointPredictedArrivalTime(), predictedArrivalTime2C);
    
    /**
     * Make sure the predictions happening downstream based on the last stop
     * of the trip B
     */
    
    long scheduledArrivalTime2C = getScheduledArrivalTimeByStopId(mTrip2,
        mStopA.getId(), 2);
    // Calculate the delay of the last stop A in trip B
    long delta = TimeUnit.MILLISECONDS.toSeconds(predictedArrivalTime2C)
        - scheduledArrivalTime2C;
    
    // Third trip in block
    long scheduledArrivalTimeStop3A = getScheduledArrivalTimeByStopId(mTrip3,
        mStopA.getId(), 0);
    long predictedArrivalTime3A = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), mTrip3.getId(), 0);
    
    long scheduledArrivalTimeStop3B = getScheduledArrivalTimeByStopId(mTrip3,
        mStopB.getId(), 1);
    long predictedArrivalTime3B = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopB.getId(), mTrip3.getId(), 1);
    
    long scheduledArrivalTimeStop3C = getScheduledArrivalTimeByStopId(mTrip3,
        mStopA.getId(), 2);
    long predictedArrivalTime3C = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), mTrip3.getId(), 2);

    assertEquals(scheduledArrivalTimeStop3A + delta, TimeUnit.MILLISECONDS.toSeconds(predictedArrivalTime3A));
    assertEquals(scheduledArrivalTimeStop3B + delta, TimeUnit.MILLISECONDS.toSeconds(predictedArrivalTime3B));
    assertEquals(scheduledArrivalTimeStop3C + delta, TimeUnit.MILLISECONDS.toSeconds(predictedArrivalTime3C));
  }
  
  /**
   * This method tests loop routes (the same stop is visited more
   * than once in the same trip) when stop_sequence is missing in the real-time feed.
   * It applies the prediction if there is more than one prediction in the same trip 
   * (i.e., the stop_id can be disambiguated).
   * 
   * Test configuration: There are three different loop trips and each trip has 3 stops
   * Time point predictions does not have stop sequences.  There is only one prediction
   * for the first or the last stop in a loop route, and there it is ambiguous and
   * should be dropped.  As a result, all stops should show scheduled time.
   * 
   * Current time = 14:00
   *          Schedule time    Real-time from feed      GTFS stop_sequence  trip_id
   * Stop A   13:30            -----                          0           t1
   * Stop B   13:45            -----                          1           t1
   * Stop A   13:55            -----                          2           t1
   * 
   * Stop A   14:05            14:10                          0           t2
   * Stop B   14:15            -----                          1           t2
   * Stop A   14:25            -----                          2           t2
   * 
   * Stop A   14:30            -----                          0           t3
   * Stop B   14:45            -----                          1           t3
   * Stop A   14:55            -----                          2           t3
   * 
   */
  @Test
  public void testGetArrivalsAndDeparturesForStopInTimeRange12() {
    // Override the current time with a later time than the time point
    // predictions
    mCurrentTime = dateAsLong("2015-07-23 14:00");

    // Set time point predictions for trip 2 stop A
    TimepointPredictionRecord tprA = new TimepointPredictionRecord();
    tprA.setTimepointId(mStopA.getId());
    long tprATime = createPredictedTime(time(14, 10));
    tprA.setTimepointPredictedArrivalTime(tprATime);
    tprA.setTripId(mTrip2.getId());
    
    // Call ArrivalsAndDeparturesForStopInTimeRange method in
    // ArrivalAndDepartureServiceImpl
    List<ArrivalAndDepartureInstance> arrivalsAndDepartures = 
        getArrivalsAndDeparturesForLoopRouteInTimeRangeByTimepointPredictionRecordWithMultipleTrips(Arrays.asList(tprA), mStopB);

    // First trip in block
    long predictedArrivalTimeStop1A = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), mTrip1.getId(), 0);
    long predictedArrivalTimeStop1B = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopB.getId(), mTrip1.getId(), 1);
    long predictedArrivalTimeStop1C = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), mTrip1.getId(), 2);
    // Second trip in block
    long predictedArrivalTimeStop2A = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), mTrip2.getId(), 0);
    long predictedArrivalTimeStop2B = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopB.getId(), mTrip2.getId(), 1);
    long predictedArrivalTimeStop2C = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), mTrip2.getId(), 2);
    // Third trip in block
    long predictedArrivalTimeStop3A = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), mTrip3.getId(), 0);
    long predictedArrivalTimeStop3B = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopB.getId(), mTrip3.getId(), 1);
    long predictedArrivalTimeStop3C = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), mTrip3.getId(), 2);
    
    /**
     * Check the all stops and make sure no propagation happening.
     */
    assertEquals(predictedArrivalTimeStop1A, 0);
    assertEquals(predictedArrivalTimeStop1B, 0);
    assertEquals(predictedArrivalTimeStop1C, 0);
    assertEquals(predictedArrivalTimeStop2A, 0);
    assertEquals(predictedArrivalTimeStop2B, 0);
    assertEquals(predictedArrivalTimeStop2C, 0);
    assertEquals(predictedArrivalTimeStop3A, 0);
    assertEquals(predictedArrivalTimeStop3B, 0);
    assertEquals(predictedArrivalTimeStop3C, 0);
  }
  
  /**
   * This method tests loop routes (the same stop is visited more
   * than once in the same trip) when stop_sequence is missing in the real-time feed.
   * It applies the prediction if there is more than one prediction in the same trip 
   * (i.e., the stop_id can be disambiguated).
   * 
   * Test configuration: There are three different loop trips and each trip has 3 stops.
   * Time point predictions does not have stop sequences, and therefore the update for
   * each trip Stop A (in trip 1 and trip 2) is ambiguous and cannot be matched.  This 
   * test also ensures the we aren't accidentally recognizing two adjacent updates in 
   * the block as being part of the same trip.  As a result, the updates should be dropped 
   * and schedule time should be provided.
   * 
   * Current time = 14:00
   *          Schedule time    Real-time from feed      GTFS stop_sequence  trip_id
   * Stop A   13:30            -----                          0           t1
   * Stop B   13:45            -----                          1           t1
   * Stop A   13:55            14:00                          2           t1
   * 
   * Stop A   14:05            14:10                          0           t2
   * Stop B   14:15            -----                          1           t2
   * Stop A   14:25            -----                          2           t2
   * 
   * Stop A   14:30            -----                          0           t3
   * Stop B   14:45            -----                          1           t3
   * Stop A   14:55            -----                          2           t3
   * 
   */
  @Test
  public void testGetArrivalsAndDeparturesForStopInTimeRange13() {
    // Override the current time with a later time than the time point
    // predictions
    mCurrentTime = dateAsLong("2015-07-23 14:00");

    // Set time point predictions for trip 1 stop A
    TimepointPredictionRecord tpr1A = new TimepointPredictionRecord();
    tpr1A.setTimepointId(mStopA.getId());
    long tprAATime = createPredictedTime(time(14, 00));
    tpr1A.setTimepointPredictedArrivalTime(tprAATime);
    tpr1A.setTripId(mTrip1.getId());
    
    // Set time point predictions for trip 2 stop A
    TimepointPredictionRecord tpr2A = new TimepointPredictionRecord();
    tpr2A.setTimepointId(mStopA.getId());
    long tprATime = createPredictedTime(time(14, 10));
    tpr2A.setTimepointPredictedArrivalTime(tprATime);
    tpr2A.setTripId(mTrip2.getId());
    
    // Call ArrivalsAndDeparturesForStopInTimeRange method in
    // ArrivalAndDepartureServiceImpl
    List<ArrivalAndDepartureInstance> arrivalsAndDepartures = 
        getArrivalsAndDeparturesForLoopRouteInTimeRangeByTimepointPredictionRecordWithMultipleTrips(Arrays.asList(tpr1A, tpr2A), mStopB);

    // First trip in block
    long predictedArrivalTimeStop1A = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), mTrip1.getId(), 0);
    long predictedArrivalTimeStop1B = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopB.getId(), mTrip1.getId(), 1);
    long predictedArrivalTimeStop1C = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), mTrip1.getId(), 2);
    // Second trip in block
    long predictedArrivalTimeStop2A = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), mTrip2.getId(), 0);
    long predictedArrivalTimeStop2B = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopB.getId(), mTrip2.getId(), 1);
    long predictedArrivalTimeStop2C = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), mTrip2.getId(), 2);
    // Third trip in block
    long predictedArrivalTimeStop3A = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), mTrip3.getId(), 0);
    long predictedArrivalTimeStop3B = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopB.getId(), mTrip3.getId(), 1);
    long predictedArrivalTimeStop3C = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), mTrip3.getId(), 2);
    
    /**
     * Check the all stops and make sure no real-time information has been applied.
     */
    assertEquals(predictedArrivalTimeStop1A, 0);
    assertEquals(predictedArrivalTimeStop1B, 0);
    assertEquals(predictedArrivalTimeStop1C, 0);
    assertEquals(predictedArrivalTimeStop2A, 0);
    assertEquals(predictedArrivalTimeStop2B, 0);
    assertEquals(predictedArrivalTimeStop2C, 0);
    assertEquals(predictedArrivalTimeStop3A, 0);
    assertEquals(predictedArrivalTimeStop3B, 0);
    assertEquals(predictedArrivalTimeStop3C, 0);
  }
  
  /**
   * This method tests loop routes (the same stop is visited more
   * than once in the same trip) when stop_sequence is missing in the real-time feed.
   * It applies the prediction if there is more than one prediction in the same trip 
   * (i.e., the stop_id can be disambiguated).
   * 
   * Test configuration: There are three different loop trips and each trip has 3 stops.
   * Time point predictions does not have stop sequences, and therefore the update for
   * each trip Stop A (in trip 2 and trip 3) is ambiguous and cannot be matched.  This 
   * test also ensures the we aren't accidentally recognizing two adjacent updates in 
   * the block as being part of the same trip.  As a result, the updates should be dropped 
   * and schedule time should be provided.
   * 
   * Current time = 14:00
   *          Schedule time    Real-time from feed   GTFS stop_sequence  trip_id
   * Stop A   13:30            -----                          0           t1
   * Stop B   13:45            -----                          1           t1
   * Stop A   13:55            -----                          2           t1
   * 
   * Stop A   14:05            -----                          0           t2
   * Stop B   14:15            -----                          1           t2
   * Stop A   14:25            14:30                          2           t2
   * 
   * Stop A   14:30            14:40                          0           t3
   * Stop B   14:45            -----                          1           t3
   * Stop A   14:55            -----                          2           t3
   * 
   */
  @Test
  public void testGetArrivalsAndDeparturesForStopInTimeRange14() {
    // Override the current time with a later time than the time point
    // predictions
    mCurrentTime = dateAsLong("2015-07-23 14:00");

    // Set time point predictions for trip 2 stop A
    TimepointPredictionRecord tpr2A = new TimepointPredictionRecord();
    tpr2A.setTimepointId(mStopA.getId());
    long tprAATime = createPredictedTime(time(14, 30));
    tpr2A.setTimepointPredictedArrivalTime(tprAATime);
    tpr2A.setTripId(mTrip2.getId());
    
    // Set time point predictions for trip 3 stop A
    TimepointPredictionRecord tpr3A = new TimepointPredictionRecord();
    tpr3A.setTimepointId(mStopA.getId());
    long tprATime = createPredictedTime(time(14, 40));
    tpr3A.setTimepointPredictedArrivalTime(tprATime);
    tpr3A.setTripId(mTrip3.getId());
    
    // Call ArrivalsAndDeparturesForStopInTimeRange method in
    // ArrivalAndDepartureServiceImpl
    List<ArrivalAndDepartureInstance> arrivalsAndDepartures = 
        getArrivalsAndDeparturesForLoopRouteInTimeRangeByTimepointPredictionRecordWithMultipleTrips(Arrays.asList(tpr2A, tpr3A), mStopB);

    // First trip in block
    long predictedArrivalTimeStop1A = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), mTrip1.getId(), 0);
    long predictedArrivalTimeStop1B = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopB.getId(), mTrip1.getId(), 1);
    long predictedArrivalTimeStop1C = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), mTrip1.getId(), 2);
    // Second trip in block
    long predictedArrivalTimeStop2A = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), mTrip2.getId(), 0);
    long predictedArrivalTimeStop2B = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopB.getId(), mTrip2.getId(), 1);
    long predictedArrivalTimeStop2C = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), mTrip2.getId(), 2);
    // Third trip in block
    long predictedArrivalTimeStop3A = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), mTrip3.getId(), 0);
    long predictedArrivalTimeStop3B = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopB.getId(), mTrip3.getId(), 1);
    long predictedArrivalTimeStop3C = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), mTrip3.getId(), 2);
    
    /**
     * Check the all stops and make sure no real-time information has been applied.
     */
    assertEquals(predictedArrivalTimeStop1A, 0);
    assertEquals(predictedArrivalTimeStop1B, 0);
    assertEquals(predictedArrivalTimeStop1C, 0);
    assertEquals(predictedArrivalTimeStop2A, 0);
    assertEquals(predictedArrivalTimeStop2B, 0);
    assertEquals(predictedArrivalTimeStop2C, 0);
    assertEquals(predictedArrivalTimeStop3A, 0);
    assertEquals(predictedArrivalTimeStop3B, 0);
    assertEquals(predictedArrivalTimeStop3C, 0);
  }
  
  /**
   * This method tests loop routes (the same stop is visited more
   * than once in the same trip) when stop_sequence is missing in the real-time feed.
   * It applies the prediction if there is more than one prediction in the same trip 
   * (i.e., the stop_id can be disambiguated).
   * 
   * Test configuration: There are three different loop trips and each trip has 3 stops.
   * Time point predictions does not have stop sequences, and therefore the update for
   * each trip Stop A (in trip 1 and trip 3) is ambiguous and cannot be matched.  This 
   * test also ensures the we aren't accidentally recognizing two updates in the same block 
   * as being part of the same trip.  As a result, the updates should be dropped 
   * and schedule time should be provided.
   * 
   * Current time = 14:00
   *          Schedule time    Real-time from feed   GTFS stop_sequence  trip_id
   * Stop A   13:30            -----                          0           t1
   * Stop B   13:45            -----                          1           t1
   * Stop A   13:55            14:00                          2           t1
   * 
   * Stop A   14:05            -----                          0           t2
   * Stop B   14:15            -----                          1           t2
   * Stop A   14:25            -----                          2           t2
   * 
   * Stop A   14:30            14:40                          0           t3
   * Stop B   14:45            -----                          1           t3
   * Stop A   14:55            -----                          2           t3
   * 
   */
  @Test
  public void testGetArrivalsAndDeparturesForStopInTimeRange15() {
    // Override the current time with a later time than the time point
    // predictions
    mCurrentTime = dateAsLong("2015-07-23 14:00");

    // Set time point predictions for trip 1 stop A
    TimepointPredictionRecord tpr1A = new TimepointPredictionRecord();
    tpr1A.setTimepointId(mStopA.getId());
    long tprAATime = createPredictedTime(time(14, 00));
    tpr1A.setTimepointPredictedArrivalTime(tprAATime);
    tpr1A.setTripId(mTrip1.getId());
    
    // Set time point predictions for trip 3 stop A
    TimepointPredictionRecord tpr3A = new TimepointPredictionRecord();
    tpr3A.setTimepointId(mStopA.getId());
    long tprATime = createPredictedTime(time(14, 40));
    tpr3A.setTimepointPredictedArrivalTime(tprATime);
    tpr3A.setTripId(mTrip3.getId());
    
    // Call ArrivalsAndDeparturesForStopInTimeRange method in
    // ArrivalAndDepartureServiceImpl
    List<ArrivalAndDepartureInstance> arrivalsAndDepartures = 
        getArrivalsAndDeparturesForLoopRouteInTimeRangeByTimepointPredictionRecordWithMultipleTrips(Arrays.asList(tpr1A, tpr3A), mStopB);

    // First trip in block
    long predictedArrivalTimeStop1A = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), mTrip1.getId(), 0);
    long predictedArrivalTimeStop1B = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopB.getId(), mTrip1.getId(), 1);
    long predictedArrivalTimeStop1C = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), mTrip1.getId(), 2);
    // Second trip in block
    long predictedArrivalTimeStop2A = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), mTrip2.getId(), 0);
    long predictedArrivalTimeStop2B = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopB.getId(), mTrip2.getId(), 1);
    long predictedArrivalTimeStop2C = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), mTrip2.getId(), 2);
    // Third trip in block
    long predictedArrivalTimeStop3A = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), mTrip3.getId(), 0);
    long predictedArrivalTimeStop3B = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopB.getId(), mTrip3.getId(), 1);
    long predictedArrivalTimeStop3C = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), mTrip3.getId(), 2);
    
    /**
     * Check the all stops and make sure no real-time information has been applied.
     */
    assertEquals(predictedArrivalTimeStop1A, 0);
    assertEquals(predictedArrivalTimeStop1B, 0);
    assertEquals(predictedArrivalTimeStop1C, 0);
    assertEquals(predictedArrivalTimeStop2A, 0);
    assertEquals(predictedArrivalTimeStop2B, 0);
    assertEquals(predictedArrivalTimeStop2C, 0);
    assertEquals(predictedArrivalTimeStop3A, 0);
    assertEquals(predictedArrivalTimeStop3B, 0);
    assertEquals(predictedArrivalTimeStop3C, 0);
  }
  
  /**
   * This method tests loop routes (the same stop is visited more
   * than once in the same trip) when stop_sequence is missing in the real-time feed.
   * It applies the prediction if there is more than one prediction in the same trip 
   * (i.e., the stop_id can be disambiguated).
   * 
   * Test configuration: There are three different loop trips and each trip has 3 stops.
   * Time point predictions do not have stop sequences, and therefore the update for
   * trip1 Stop A is ambiguous and cannot be matched.  However, for trip 3, there are at least
   * two updates, so we can infer that the update for Stop A should be applied to the first
   * stop instance.  This test also ensures the we aren't accidentally recognizing two updates 
   * in the same block as being part of the same trip (for trip 1).  As a result, the update
   * for trip 1 should be dropped, but the updates for trip 3 should be applied.
   * 
   * Current time = 14:00
   *          Schedule time    Real-time from feed   GTFS stop_sequence  trip_id
   * Stop A   13:30            -----                          0           t1
   * Stop B   13:45            -----                          1           t1
   * Stop A   13:55            14:00                          2           t1
   * 
   * Stop A   14:05            -----                          0           t2
   * Stop B   14:15            -----                          1           t2
   * Stop A   14:25            -----                          2           t2
   * 
   * Stop A   14:30            14:40                          0           t3
   * Stop B   14:45            14:50                          1           t3
   * Stop A   14:55            -----                          2           t3
   * 
   */
  @Test
  public void testGetArrivalsAndDeparturesForStopInTimeRange16() {
    // Override the current time with a later time than the time point
    // predictions
    mCurrentTime = dateAsLong("2015-07-23 14:00");

    // Set time point prediction for trip 1 stop A
    TimepointPredictionRecord tpr1A = new TimepointPredictionRecord();
    tpr1A.setTimepointId(mStopA.getId());
    long tprAATime = createPredictedTime(time(14, 00));
    tpr1A.setTimepointPredictedArrivalTime(tprAATime);
    tpr1A.setTripId(mTrip1.getId());
    
    // Set time point predictions for trip 3 stop A and stop B
    TimepointPredictionRecord tpr3A = new TimepointPredictionRecord();
    tpr3A.setTimepointId(mStopA.getId());
    long tprATime = createPredictedTime(time(14, 40));
    tpr3A.setTimepointPredictedArrivalTime(tprATime);
    tpr3A.setTripId(mTrip3.getId());
    
    TimepointPredictionRecord tpr3B = new TimepointPredictionRecord();
    tpr3B.setTimepointId(mStopB.getId());
    long tprBTime = createPredictedTime(time(14, 50));
    tpr3B.setTimepointPredictedArrivalTime(tprBTime);
    tpr3B.setTripId(mTrip3.getId());
    
    // Call ArrivalsAndDeparturesForStopInTimeRange method in
    // ArrivalAndDepartureServiceImpl
    List<ArrivalAndDepartureInstance> arrivalsAndDepartures = 
        getArrivalsAndDeparturesForLoopRouteInTimeRangeByTimepointPredictionRecordWithMultipleTrips(Arrays.asList(tpr1A, tpr3A, tpr3B), mStopB);

    // First trip in block
    long predictedArrivalTimeStop1A = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), mTrip1.getId(), 0);
    long predictedArrivalTimeStop1B = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopB.getId(), mTrip1.getId(), 1);
    long predictedArrivalTimeStop1C = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), mTrip1.getId(), 2);
    // Second trip in block
    long predictedArrivalTimeStop2A = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), mTrip2.getId(), 0);
    long predictedArrivalTimeStop2B = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopB.getId(), mTrip2.getId(), 1);
    long predictedArrivalTimeStop2C = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), mTrip2.getId(), 2);
    // Third trip in block
    long predictedArrivalTimeStop3A = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), mTrip3.getId(), 0);
    long predictedArrivalTimeStop3B = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopB.getId(), mTrip3.getId(), 1);
    
    /**
     * Make sure no real-time info is applied to trips 1 and 2
     */
    assertEquals(predictedArrivalTimeStop1A, 0);
    assertEquals(predictedArrivalTimeStop1B, 0);
    assertEquals(predictedArrivalTimeStop1C, 0);
    assertEquals(predictedArrivalTimeStop2A, 0);
    assertEquals(predictedArrivalTimeStop2B, 0);
    assertEquals(predictedArrivalTimeStop2C, 0);
    
    /**
     * Check last three predictions in trip 3.  We should have predictions for these stops. 
     */
    assertEquals(predictedArrivalTimeStop3A, tpr3A.getTimepointPredictedArrivalTime());
    assertEquals(predictedArrivalTimeStop3B, tpr3B.getTimepointPredictedArrivalTime());
    
    long scheduledArrivalTime3B = getScheduledArrivalTimeByStopId(mTrip3,
        mStopB.getId(), 1);
    // Calculate the delay of the last stop A in trip B
    long delta = TimeUnit.MILLISECONDS.toSeconds(predictedArrivalTimeStop3B)
        - scheduledArrivalTime3B;
    
    long scheduledArrivalTimeStop3C = getScheduledArrivalTimeByStopId(mTrip3,
        mStopA.getId(), 2);
    long predictedArrivalTime3C = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), mTrip3.getId(), 2);

    // Check propagation of trip 3 Stop B to the last instance of Stop A 
    assertEquals(scheduledArrivalTimeStop3C + delta , TimeUnit.MILLISECONDS.toSeconds(predictedArrivalTime3C));
  }
  
  /**
   * This method tests loops in the middle of routes (the same stop is visited more
   * than once in the same trip) when stop_sequence is missing in the real-time feed.
   * It attempts to apply the prediction if there is more than one prediction in the 
   * same trip (i.e., the stop_id can be disambiguated).  However, there is a limitation for
   * when the loop is in the middle of the trip (see FIXME below) - in this case, we can't disambiguate
   * the time point prediction without stop_sequence.  In other cases (and tests in this class),
   * we are able to disambiguate the predictions for simple loop trips when the prediction is
   * for the first or last stop in the trip and the first and last stop are the same, and we are able 
   * to infer this from the presence of more than one prediction (see #163).  However, we can't 
   * currently make this same inference for loops that occur in middle of the route, as it would require 
   * us to look at a larger window of stops, which increases the computation time overhead.  We would also
   * need to add some bundle build-time attributes to identify which trips have loops within them,
   * and which stops are the loop stops that appear more than once.
   * 
   * NOTE: The "real" and simplest fix for this issue is for the real-time feed to include stop_sequence
   * to disambiguate which real-time prediction should apply to which stop.  This isn't currently a
   * requirement in GTFS-rt, but we're trying to make it one (see https://github.com/google/transit/pull/20).
   * 
   * Test configuration: There is one trip and this trip has 4 stops. But, in the middle of the route,
   * same stop B visited twice. So, the route look like this: A->B->C->B->D, where the stop B visited twice
   * in the middle of the route. Time point predictions do not have stop sequences,
   * and therefore the update for Stop B is ambiguous. We currently do not have any mechanism to handle 
   * this case, and OBA cannot understand this is a loop route, because loop happens in
   * the middle of the route. OBA will currently (and erroneously) apply the real-time update for both stop Bs.
   * As a result, it will predict the arrival times wrong for stop b (sequence = 3) and stop D.
   * 
   * Current time = 14:00
   *          Schedule time    Real-time from feed   GTFS stop_sequence  trip_id
   * Stop A   13:30            -----                          0           t1
   * Stop B   13:45            13:50                          1           t1
   * Stop C   13:55            -----                          2           t1
   * Stop B   14:05            -----                          3           t1
   * Stop D   14:15            -----                          4           t1
   * 
   */
  @Test
  public void testGetArrivalsAndDeparturesForStopInTimeRange17() {
    // Override the current time with a later time than the time point
    // predictions
    mCurrentTime = dateAsLong("2015-07-23 14:00");

    // Set time point prediction for trip 1 stop B
    TimepointPredictionRecord tpr1A = new TimepointPredictionRecord();
    tpr1A.setTimepointId(mStopB.getId());
    long tpr1ATime = createPredictedTime(time(13, 50));
    tpr1A.setTimepointPredictedArrivalTime(tpr1ATime);
    tpr1A.setTripId(mTrip1.getId());
    
    
    // Call ArrivalsAndDeparturesForStopInTimeRange method in
    // ArrivalAndDepartureServiceImpl
    List<ArrivalAndDepartureInstance> arrivalsAndDepartures = 
        getArrivalsAndDeparturesForLoopInTheMiddleOfRouteInTimeRangeByTimepointPredictionRecord(Arrays.asList(tpr1A));

    // First trip in block
    long predictedArrivalTimeStop1A = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopA.getId(), mTrip1.getId(), 0);
    long predictedArrivalTimeStop1B = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopB.getId(), mTrip1.getId(), 1);
    long predictedArrivalTimeStop1C = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopC.getId(), mTrip1.getId(), 2);
    long predictedArrivalTimeStop1BB = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopB.getId(), mTrip1.getId(),3);
    long predictedArrivalTimeStop1D = getPredictedArrivalTimeByStopIdAndSequence(
        arrivalsAndDepartures, mStopD.getId(), mTrip1.getId(), 4);
    
    
    /**
     * Make sure no real-time info is applied to stop A
     */
    assertEquals(predictedArrivalTimeStop1A, 0);
    
    /**
     * Check predictions for first stop B and stop C.  We should have correct predictions for these stops. 
     */
    long scheduledArrivalTime1B = getScheduledArrivalTimeByStopId(mTrip1,
        mStopB.getId(), 1);
    long scheduledArrivalTime1C = getScheduledArrivalTimeByStopId(mTrip1,
        mStopC.getId(), 2);
    // Calculate the delay of the last stop A in trip B
    long delta = TimeUnit.MILLISECONDS.toSeconds(predictedArrivalTimeStop1B)
        - scheduledArrivalTime1B;

    assertEquals(predictedArrivalTimeStop1B, tpr1A.getTimepointPredictedArrivalTime());
    assertEquals(scheduledArrivalTime1C + delta , TimeUnit.MILLISECONDS.toSeconds(predictedArrivalTimeStop1C));
    
    /**
     * FIXME: Properly apply real-time updates without stop_sequence to loop stops if they are in the middle of the trip
     * 
     * In this test, we have a stop (stop B) and it is visited twice in the middle of the route.
     * We have only one timePointPredicition without stop_sequence for the first stop B. OBA currently
     * cannot determine this is a loop route. Therefore, it will apply the same timePointPrediction to 
     * two different stops (the first stop B and the second stop B). The proper behavior would be for the 
     * second visited stop B (stop_sequence = 3), we should have prediction propagated downstream from the
     *  first Stop B (stop_sequence = 1) (see below assertions for what should be happening).
     * 
     * As a result, the following test cases should fail with the current configuration, and therefore are commented out.
     * A simple fix to solve this problem is for the provider to add stop_squence in the GTFS-realtime feed.
     * 
     */
    
//      long scheduledArrivalTime1BB = getScheduledArrivalTimeByStopId(mTrip1,
//        mStopB.getId(), 3);
//      long scheduledArrivalTime1D = getScheduledArrivalTimeByStopId(mTrip1,
//        mStopD.getId(), 4);
//    
//      assertEquals(scheduledArrivalTime1BB + delta , TimeUnit.MILLISECONDS.toSeconds(predictedArrivalTimeStop1BB));
//      assertEquals(scheduledArrivalTime1D + delta , TimeUnit.MILLISECONDS.toSeconds(predictedArrivalTimeStop1D));
    
  }
  
  
  /**
   * This method tests trip update CANCELED support
   * 
   * Test configuration: Trip for stopA and stopB is cancelled. There should be no arrivals and departures returned 
   * from the API!
   */
  @Test
  public void testGetArrivalsAndDeparturesForStopInTimeRange18() {

    // Call ArrivalsAndDeparturesForStopInTimeRange method in
    // ArrivalAndDepartureServiceImpl
    List<ArrivalAndDepartureInstance> arrivalsAndDepartures = getArrivalsAndDeparturesForStopInTimeRangeForCancelledTrip();

    assertNotNull(arrivalsAndDepartures);
    assertEquals(0, arrivalsAndDepartures.size());
    
  }

  /**
   * Testing Historical Occupancy Functionality
   */
  @Test
  public void testGetArrivalsAndDeparturesForStopInTimeRange19() {

    TimepointPredictionRecord tprA = new TimepointPredictionRecord();
    tprA.setTimepointId(mStopA.getId());
    long tprATime = createPredictedTime(time(13, 30));
    tprA.setTimepointPredictedDepartureTime(tprATime);
    tprA.setTripId(mTrip1.getId());

    List<ArrivalAndDepartureInstance> arrivalsAndDepartures = getArrivalsAndDeparturesForStopInTimeRangeByTimepointPredictionRecord(Arrays.asList(tprA));
    assertNotNull(arrivalsAndDepartures.get(0).getHistoricalOccupancy());
    assertNotNull(arrivalsAndDepartures.get(0).getBlockStopTime().getStopTime().getHistoricalOccupancy());
    assertNotNull(arrivalsAndDepartures.get(0).getStopTimeInstance().getStopTime().getStopTime().getHistoricalOccupancy());

  }

  /**
   * Testing StopTime update SKIPPED stops support
   */
  @Test
  public void testGetArrivalsAndDeparturesForStopInTimeRange(){

    List<TimepointPredictionRecord> tprs = getTimepointPredictionsWithSkippedStop();
    List<ArrivalAndDepartureInstance> arrivalsAndDepartures = getArrivalsAndDeparturesForStopInTimeRangeByTimepointPredictionRecord(tprs);

    assertEquals(2, arrivalsAndDepartures.size());
    assertEquals(2, arrivalsAndDepartures.get(0).getBlockLocation().getTimepointPredictions().size());
    assertNotNull(arrivalsAndDepartures.get(0).getBlockLocation().getTimepointPredictions().get(0).getScheduleRelationship());
    assertEquals(1, arrivalsAndDepartures.get(0).getBlockLocation().getTimepointPredictions().get(0).getScheduleRelationship().getValue());


    // Instance doesn't have arrival time from first TPR because its skipped
    assertEquals(arrivalsAndDepartures.get(0).getBlockLocation().getTimepointPredictions().get(1).getTimepointPredictedArrivalTime(), arrivalsAndDepartures.get(0).getPredictedArrivalTime());

  }

  private List<TimepointPredictionRecord> getTimepointPredictionsWithSkippedStop() {

    TimepointPredictionRecord tprA = new TimepointPredictionRecord();
    tprA.setTimepointId(mStopA.getId());
    tprA.setTripId(mTrip1.getId());
    tprA.setScheduleRealtionship(1);

    TimepointPredictionRecord tprB = new TimepointPredictionRecord();
    tprB.setTimepointId(mStopB.getId());
    long tprBTime = createPredictedTime(time(13, 46));
    tprB.setTimepointPredictedArrivalTime(tprBTime);
    tprB.setTripId(mTrip1.getId());


    return Arrays.asList(tprA, tprB);
  }



  /**
   * Set up the BlockLocationServiceImpl for the test, using the given
   * timepointPredictions
   * 
   * This method creates a normal route with a single trip and two stops in a block
   * 
   * stop_id     trip_id    stop_sequence
   *    A           1             0
   *    B           1             1
   * 
   * @param timepointPredictions real-time predictions to apply to the
   *          BlockLocationServiceImpl
   * @return a list of ArrivalAndDepartureInstances which is used to access
   *         predicted arrival/departure times for a stop, for comparison
   *         against the expected values
   */
  private List<ArrivalAndDepartureInstance> getArrivalsAndDeparturesForStopInTimeRangeByTimepointPredictionRecord(
      List<TimepointPredictionRecord> timepointPredictions) {
    TargetTime target = new TargetTime(mCurrentTime, mCurrentTime);

    // Setup block
    BlockEntryImpl block = block("blockA");

    stopTime(0, mStopA, mTrip1, time(13, 30), time(13, 35), 1000,  40.0);
    stopTime(1, mStopB, mTrip1, time(13, 45), time(13, 50), 2000,  20.0);

    BlockConfigurationEntry blockConfig = blockConfiguration(block,
        serviceIds(lsids("sA"), lsids()), mTrip1);
    BlockStopTimeEntry bstAA = blockConfig.getStopTimes().get(0);
    BlockStopTimeEntry bstAB = blockConfig.getStopTimes().get(1);
    BlockStopTimeEntry bstBA = blockConfig.getStopTimes().get(0);

    // Setup block location instance for trip B
    BlockInstance blockInstance = new BlockInstance(blockConfig, mServiceDate);
    BlockLocation blockLocationB = new BlockLocation();
    blockLocationB.setActiveTrip(bstBA.getTrip());
    blockLocationB.setBlockInstance(blockInstance);
    blockLocationB.setClosestStop(bstBA);
    blockLocationB.setDistanceAlongBlock(400);
    blockLocationB.setInService(true);
    blockLocationB.setNextStop(bstAA);
    blockLocationB.setPredicted(false);
    blockLocationB.setScheduledDistanceAlongBlock(400);

    blockLocationB.setTimepointPredictions(timepointPredictions);

    // Mock StopTimeInstance with time frame
    long stopTimeFrom = dateAsLong("2015-07-23 00:00");
    long stopTimeTo = dateAsLong("2015-07-24 00:00");

    StopTimeInstance sti1 = new StopTimeInstance(bstAB,
        blockInstance.getState());
    ArrivalAndDepartureInstance in1 = new ArrivalAndDepartureInstance(sti1);
    in1.setBlockLocation(blockLocationB);
    in1.setPredictedArrivalTime((long) (in1.getScheduledArrivalTime()));
    in1.setPredictedDepartureTime((long) (in1.getScheduledDepartureTime()));

    StopTimeInstance sti2 = new StopTimeInstance(bstBA,
        blockInstance.getState());
    ArrivalAndDepartureInstance in2 = new ArrivalAndDepartureInstance(sti2);
    in2.setBlockLocation(blockLocationB);

    Date fromTimeBuffered = new Date(stopTimeFrom
        - _blockStatusService.getRunningLateWindow() * 1000);
    Date toTimeBuffered = new Date(stopTimeTo
        + _blockStatusService.getRunningEarlyWindow() * 1000);

    Mockito.when(
        _stopTimeService.getStopTimeInstancesInTimeRange(mStopB,
            fromTimeBuffered, toTimeBuffered,
            EFrequencyStopTimeBehavior.INCLUDE_UNSPECIFIED)).thenReturn(
        Arrays.asList(sti1, sti2));

    // Create and add vehicle location record cache
    VehicleLocationRecordCacheImpl _cache = new VehicleLocationRecordCacheImpl();
    VehicleLocationRecord vlr = new VehicleLocationRecord();
    vlr.setBlockId(blockLocationB.getBlockInstance().getBlock().getBlock().getId());
    vlr.setTripId(mTrip1.getId());
    vlr.setTimepointPredictions(blockLocationB.getTimepointPredictions());
    vlr.setTimeOfRecord(mCurrentTime);
    vlr.setVehicleId(new AgencyAndId("1", "123"));

    // Create ScheduledBlockLocation for cache
    ScheduledBlockLocation sbl = new ScheduledBlockLocation();
    sbl.setActiveTrip(blockLocationB.getActiveTrip());

    // Add data to cache
    _cache.addRecord(blockInstance, vlr, sbl, null);
    _blockLocationService.setVehicleLocationRecordCache(_cache);
    ScheduledBlockLocationServiceImpl scheduledBlockLocationServiceImpl = new ScheduledBlockLocationServiceImpl();
    _blockLocationService.setScheduledBlockLocationService(scheduledBlockLocationServiceImpl);

    // Call ArrivalAndDepartureService
    return _service.getArrivalsAndDeparturesForStopInTimeRange(mStopB, target,
        stopTimeFrom, stopTimeTo);
  }
  /**
   * Set up the BlockLocationServiceImpl for the test, using the given
   * timepointPredictions
   * 
   * This method creates a normal route with a single trip and two stops in a block
   * 
   * stop_id     trip_id    stop_sequence
   *    A           1             0
   *    B           1             1
   * 
   * @param timepointPredictions real-time predictions to apply to the
   *          BlockLocationServiceImpl
   * @return a list of ArrivalAndDepartureInstances which is used to access
   *         predicted arrival/departure times for a stop, for comparison
   *         against the expected values
   */
  private List<ArrivalAndDepartureInstance> getArrivalsAndDeparturesForStopInTimeRangeForCancelledTrip() {
    TargetTime target = new TargetTime(mCurrentTime, mCurrentTime);

    // Setup block
    BlockEntryImpl block = block("blockA");

    stopTime(0, mStopA, mTrip1, time(13, 30), time(13, 35), 1000);
    stopTime(1, mStopB, mTrip1, time(13, 45), time(13, 50), 2000);

    BlockConfigurationEntry blockConfig = blockConfiguration(block,
        serviceIds(lsids("sA"), lsids()), mTrip1);
    BlockStopTimeEntry bstAA = blockConfig.getStopTimes().get(0);
    BlockStopTimeEntry bstAB = blockConfig.getStopTimes().get(1);
    BlockStopTimeEntry bstBA = blockConfig.getStopTimes().get(0);

    // Setup block location instance for trip B
    BlockInstance blockInstance = new BlockInstance(blockConfig, mServiceDate);
    BlockLocation blockLocationB = new BlockLocation();
    blockLocationB.setActiveTrip(bstBA.getTrip());
    blockLocationB.setBlockInstance(blockInstance);
    blockLocationB.setClosestStop(bstBA);
    blockLocationB.setDistanceAlongBlock(400);
    blockLocationB.setInService(true);
    blockLocationB.setNextStop(bstAA);
    blockLocationB.setPredicted(false);
    blockLocationB.setScheduledDistanceAlongBlock(400);


    // Mock StopTimeInstance with time frame
    long stopTimeFrom = dateAsLong("2015-07-23 00:00");
    long stopTimeTo = dateAsLong("2015-07-24 00:00");

    StopTimeInstance sti1 = new StopTimeInstance(bstAB,
        blockInstance.getState());
    ArrivalAndDepartureInstance in1 = new ArrivalAndDepartureInstance(sti1);
    in1.setBlockLocation(blockLocationB);
    in1.setPredictedArrivalTime((long) (in1.getScheduledArrivalTime()));
    in1.setPredictedDepartureTime((long) (in1.getScheduledDepartureTime()));

    StopTimeInstance sti2 = new StopTimeInstance(bstBA,
        blockInstance.getState());
    ArrivalAndDepartureInstance in2 = new ArrivalAndDepartureInstance(sti2);
    in2.setBlockLocation(blockLocationB);

    Date fromTimeBuffered = new Date(stopTimeFrom
        - _blockStatusService.getRunningLateWindow() * 1000);
    Date toTimeBuffered = new Date(stopTimeTo
        + _blockStatusService.getRunningEarlyWindow() * 1000);

    Mockito.when(
        _stopTimeService.getStopTimeInstancesInTimeRange(mStopB,
            fromTimeBuffered, toTimeBuffered,
            EFrequencyStopTimeBehavior.INCLUDE_UNSPECIFIED)).thenReturn(
        Arrays.asList(sti1, sti2));

    // Create and add vehicle location record cache
    VehicleLocationRecordCacheImpl _cache = new VehicleLocationRecordCacheImpl();
    VehicleLocationRecord vlr = new VehicleLocationRecord();
    vlr.setBlockId(blockLocationB.getBlockInstance().getBlock().getBlock().getId());
    vlr.setTripId(mTrip1.getId());
    vlr.setTimepointPredictions(blockLocationB.getTimepointPredictions());
    vlr.setTimeOfRecord(mCurrentTime);
    vlr.setVehicleId(new AgencyAndId("1", "123"));
    vlr.setStatus(TransitDataConstants.STATUS_CANCELED);

    // Create ScheduledBlockLocation for cache
    ScheduledBlockLocation sbl = new ScheduledBlockLocation();
    sbl.setActiveTrip(blockLocationB.getActiveTrip());

    // Add data to cache
    _cache.addRecord(blockInstance, vlr, sbl, null);
    _blockLocationService.setVehicleLocationRecordCache(_cache);
    ScheduledBlockLocationServiceImpl scheduledBlockLocationServiceImpl = new ScheduledBlockLocationServiceImpl();
    _blockLocationService.setScheduledBlockLocationService(scheduledBlockLocationServiceImpl);

    // Call ArrivalAndDepartureService
    return _service.getArrivalsAndDeparturesForStopInTimeRange(mStopB, target,
        stopTimeFrom, stopTimeTo);
  }


  /**
   * Set up the BlockLocationServiceImpl for the test, using the given
   * timepointPredictions
   * 
   * This method creates a loop route with a single trip and four stops in a block
   * Stop B is visited twice in the middle of the route
   * 
   * stop_id     trip_id    stop_sequence
   *    A           1             0
   *    B           1             1
   *    C           1             2
   *    B           1             3
   *    D           1             4
   * 
   * @param timepointPredictions real-time predictions to apply to the
   *          BlockLocationServiceImpl
   * @return a list of ArrivalAndDepartureInstances which is used to access
   *         predicted arrival/departure times for a stop, for comparison
   *         against the expected values
   */
  private List<ArrivalAndDepartureInstance>
      getArrivalsAndDeparturesForLoopInTheMiddleOfRouteInTimeRangeByTimepointPredictionRecord(
      List<TimepointPredictionRecord> timepointPredictions) {
    TargetTime target = new TargetTime(mCurrentTime, mCurrentTime);

    // Setup block
    BlockEntryImpl block = block("blockA");

    stopTime(0, mStopA, mTrip1, time(13, 30), time(13, 35), 1000);
    stopTime(1, mStopB, mTrip1, time(13, 45), time(13, 50), 2000);
    stopTime(2, mStopC, mTrip1, time(13, 55), time(14, 00), 2000);
    stopTime(3, mStopB, mTrip1, time(14, 05), time(14, 10), 2000);
    stopTime(4, mStopD, mTrip1, time(14, 15), time(14, 20), 2000);

    BlockConfigurationEntry blockConfig = blockConfiguration(block,
        serviceIds(lsids("sA"), lsids()), mTrip1);
    BlockStopTimeEntry bstAA = blockConfig.getStopTimes().get(0);
    BlockStopTimeEntry bstAB = blockConfig.getStopTimes().get(1);
    BlockStopTimeEntry bstAC = blockConfig.getStopTimes().get(2);
    BlockStopTimeEntry bstABB = blockConfig.getStopTimes().get(3);
    BlockStopTimeEntry bstAD = blockConfig.getStopTimes().get(4);

    // Setup block location instance for trip B
    BlockInstance blockInstance = new BlockInstance(blockConfig, mServiceDate);
    BlockLocation blockLocationB = new BlockLocation();
    blockLocationB.setActiveTrip(bstAA.getTrip());
    blockLocationB.setBlockInstance(blockInstance);
    blockLocationB.setClosestStop(bstAB);
    blockLocationB.setDistanceAlongBlock(400);
    blockLocationB.setInService(true);
    blockLocationB.setNextStop(bstAA);
    blockLocationB.setPredicted(false);
    blockLocationB.setScheduledDistanceAlongBlock(400);

    blockLocationB.setTimepointPredictions(timepointPredictions);

    // Mock StopTimeInstance with time frame
    long stopTimeFrom = dateAsLong("2015-07-23 00:00");
    long stopTimeTo = dateAsLong("2015-07-24 00:00");

    StopTimeInstance sti1 = new StopTimeInstance(bstAB,
        blockInstance.getState());
    ArrivalAndDepartureInstance in1 = new ArrivalAndDepartureInstance(sti1);
    in1.setBlockLocation(blockLocationB);
    in1.setPredictedArrivalTime((long) (in1.getScheduledArrivalTime()));
    in1.setPredictedDepartureTime((long) (in1.getScheduledDepartureTime()));

    StopTimeInstance sti2 = new StopTimeInstance(bstAA,
        blockInstance.getState());
    ArrivalAndDepartureInstance in2 = new ArrivalAndDepartureInstance(sti2);
    in2.setBlockLocation(blockLocationB);
    
    StopTimeInstance sti3 = new StopTimeInstance(bstAC,
        blockInstance.getState());
    ArrivalAndDepartureInstance in3 = new ArrivalAndDepartureInstance(sti3);
    in3.setBlockLocation(blockLocationB);
    
    StopTimeInstance sti4 = new StopTimeInstance(bstABB,
        blockInstance.getState());
    ArrivalAndDepartureInstance in4 = new ArrivalAndDepartureInstance(sti4);
    in4.setBlockLocation(blockLocationB);
    
    StopTimeInstance sti5 = new StopTimeInstance(bstAD,
        blockInstance.getState());
    ArrivalAndDepartureInstance in5 = new ArrivalAndDepartureInstance(sti5);
    in5.setBlockLocation(blockLocationB);

    Date fromTimeBuffered = new Date(stopTimeFrom
        - _blockStatusService.getRunningLateWindow() * 1000);
    Date toTimeBuffered = new Date(stopTimeTo
        + _blockStatusService.getRunningEarlyWindow() * 1000);

    Mockito.when(
        _stopTimeService.getStopTimeInstancesInTimeRange(mStopB,
            fromTimeBuffered, toTimeBuffered,
            EFrequencyStopTimeBehavior.INCLUDE_UNSPECIFIED)).thenReturn(
        Arrays.asList(sti1, sti2, sti3, sti4, sti5));

    // Create and add vehicle location record cache
    VehicleLocationRecordCacheImpl _cache = new VehicleLocationRecordCacheImpl();
    VehicleLocationRecord vlr = new VehicleLocationRecord();
    vlr.setBlockId(blockLocationB.getBlockInstance().getBlock().getBlock().getId());
    vlr.setTripId(mTrip1.getId());
    vlr.setTimepointPredictions(blockLocationB.getTimepointPredictions());
    vlr.setTimeOfRecord(mCurrentTime);
    vlr.setVehicleId(new AgencyAndId("1", "123"));

    // Create ScheduledBlockLocation for cache
    ScheduledBlockLocation sbl = new ScheduledBlockLocation();
    sbl.setActiveTrip(blockLocationB.getActiveTrip());

    // Add data to cache
    _cache.addRecord(blockInstance, vlr, sbl, null);
    _blockLocationService.setVehicleLocationRecordCache(_cache);
    ScheduledBlockLocationServiceImpl scheduledBlockLocationServiceImpl = new ScheduledBlockLocationServiceImpl();
    _blockLocationService.setScheduledBlockLocationService(scheduledBlockLocationServiceImpl);

    // Call ArrivalAndDepartureService
    return _service.getArrivalsAndDeparturesForStopInTimeRange(mStopB, target,
        stopTimeFrom, stopTimeTo);
  }
  
  /**
   * Set up the BlockLocationServiceImpl for the test, using the given
   * timepointPredictions
   *    
   * This method creates a loop route with a single trip and two stops in a block
   * Stop A is visited twice in the route
   * 
   * stop_id     trip_id    stop_sequence
   *    A           1             0
   *    B           1             1
   *    A           1             2
   * 
   * @param timepointPredictions real-time predictions to apply to the
   *          BlockLocationServiceImpl
   * @param stop stop_id for this stop is used to call getArrivalsAndDeparturesForStopInTimeRange()
   * @return a list of ArrivalAndDepartureInstances which is used to access
   *         predicted arrival/departure times for a stop, for comparison
   *         against the expected values
   */
  private List<ArrivalAndDepartureInstance> getArrivalsAndDeparturesForLoopRouteInTimeRangeByTimepointPredictionRecord(
      List<TimepointPredictionRecord> timepointPredictions, StopEntryImpl stop) {
    TargetTime target = new TargetTime(mCurrentTime, mCurrentTime);

    // Setup block
    BlockEntryImpl block = block("blockA");

    stopTime(0, mStopA, mTrip1, time(13, 30), time(13, 35), 1000);
    stopTime(1, mStopB, mTrip1, time(13, 45), time(13, 50), 2000);
    stopTime(2, mStopA, mTrip1, time(13, 55), time(13, 55), 2000);

    BlockConfigurationEntry blockConfig = blockConfiguration(block,
        serviceIds(lsids("sA"), lsids()), mTrip1);
    BlockStopTimeEntry bstAA = blockConfig.getStopTimes().get(0);
    BlockStopTimeEntry bstAB = blockConfig.getStopTimes().get(1);
    BlockStopTimeEntry bstBA = blockConfig.getStopTimes().get(2);

    // Setup block location instance for trip B
    BlockInstance blockInstance = new BlockInstance(blockConfig, mServiceDate);
    BlockLocation blockLocationB = new BlockLocation();
    blockLocationB.setActiveTrip(bstBA.getTrip());
    blockLocationB.setBlockInstance(blockInstance);
    blockLocationB.setClosestStop(bstBA);
    blockLocationB.setDistanceAlongBlock(400);
    blockLocationB.setInService(true);
    blockLocationB.setNextStop(bstAA);
    blockLocationB.setPredicted(false);
    blockLocationB.setScheduledDistanceAlongBlock(400);

    blockLocationB.setTimepointPredictions(timepointPredictions);

    // Mock StopTimeInstance with time frame
    long stopTimeFrom = dateAsLong("2015-07-23 00:00");
    long stopTimeTo = dateAsLong("2015-07-24 00:00");

    StopTimeInstance sti1 = new StopTimeInstance(bstAA,
        blockInstance.getState());
    ArrivalAndDepartureInstance in1 = new ArrivalAndDepartureInstance(sti1);
    in1.setBlockLocation(blockLocationB);
    in1.setPredictedArrivalTime((long) (in1.getScheduledArrivalTime()));
    in1.setPredictedDepartureTime((long) (in1.getScheduledDepartureTime()));

    StopTimeInstance sti2 = new StopTimeInstance(bstAB,
        blockInstance.getState());
    ArrivalAndDepartureInstance in2 = new ArrivalAndDepartureInstance(sti2);
    in2.setBlockLocation(blockLocationB);
    
    StopTimeInstance sti3 = new StopTimeInstance(bstBA,
        blockInstance.getState());
    ArrivalAndDepartureInstance in3 = new ArrivalAndDepartureInstance(sti3);
    in3.setBlockLocation(blockLocationB);
    in3.setPredictedArrivalTime((long) (in3.getScheduledArrivalTime()));
    in3.setPredictedDepartureTime((long) (in3.getScheduledDepartureTime()));

    Date fromTimeBuffered = new Date(stopTimeFrom
        - _blockStatusService.getRunningLateWindow() * 1000);
    Date toTimeBuffered = new Date(stopTimeTo
        + _blockStatusService.getRunningEarlyWindow() * 1000);

    Mockito.when(
        _stopTimeService.getStopTimeInstancesInTimeRange(stop,
            fromTimeBuffered, toTimeBuffered,
            EFrequencyStopTimeBehavior.INCLUDE_UNSPECIFIED)).thenReturn(
        Arrays.asList(sti1, sti2, sti3));

    // Create and add vehicle location record cache
    VehicleLocationRecordCacheImpl _cache = new VehicleLocationRecordCacheImpl();
    VehicleLocationRecord vlr = new VehicleLocationRecord();
    vlr.setBlockId(blockLocationB.getBlockInstance().getBlock().getBlock().getId());
    vlr.setTripId(mTrip1.getId());
    vlr.setTimepointPredictions(blockLocationB.getTimepointPredictions());
    vlr.setTimeOfRecord(mCurrentTime);
    vlr.setVehicleId(new AgencyAndId("1", "123"));

    // Create ScheduledBlockLocation for cache
    ScheduledBlockLocation sbl = new ScheduledBlockLocation();
    sbl.setActiveTrip(blockLocationB.getActiveTrip());

    // Add data to cache
    _cache.addRecord(blockInstance, vlr, sbl, null);
    _blockLocationService.setVehicleLocationRecordCache(_cache);
    ScheduledBlockLocationServiceImpl scheduledBlockLocationServiceImpl = new ScheduledBlockLocationServiceImpl();
    _blockLocationService.setScheduledBlockLocationService(scheduledBlockLocationServiceImpl);

    // Call ArrivalAndDepartureService
    return _service.getArrivalsAndDeparturesForStopInTimeRange(stop, target,
        stopTimeFrom, stopTimeTo);
  }
  
  /**
   * Set up the BlockLocationServiceImpl for the test, using the given
   * timepointPredictions
   * 
   * This method creates a loop route with a three trips and two stops in a block
   * Stop A is visited twice in a trip
   * 
   * stop_id     trip_id    stop_sequence
   *    A           1             0
   *    B           1             1
   *    A           1             3
   *    
   *    A           2             0
   *    B           2             1
   *    A           2             3
   *    
   *    A           3             0
   *    B           3             1
   *    A           3             3
   * 
   * @param timepointPredictions real-time predictions to apply to the
   *          BlockLocationServiceImpl
   * @param stop stop_id for this stop is used to call getArrivalsAndDeparturesForStopInTimeRange()
   * @return a list of ArrivalAndDepartureInstances which is used to access
   *         predicted arrival/departure times for a stop, for comparison
   *         against the expected values
   */
  private List<ArrivalAndDepartureInstance> getArrivalsAndDeparturesForLoopRouteInTimeRangeByTimepointPredictionRecordWithMultipleTrips(
      List<TimepointPredictionRecord> timepointPredictions, StopEntryImpl stop) {
    TargetTime target = new TargetTime(mCurrentTime, mCurrentTime);

    // Setup block
    BlockEntryImpl block = block("blockA");

    stopTime(0, mStopA, mTrip1, time(13, 30), time(13, 35), 1000);
    stopTime(1, mStopB, mTrip1, time(13, 45), time(13, 50), 2000);
    stopTime(2, mStopA, mTrip1, time(13, 55), time(13, 55), 2000);
    
    stopTime(0, mStopA, mTrip2, time(14, 05), time(14, 10), 1000);
    stopTime(1, mStopB, mTrip2, time(14, 15), time(14, 20), 2000);
    stopTime(2, mStopA, mTrip2, time(14, 25), time(14, 25), 2000);
    
    stopTime(0, mStopA, mTrip3, time(14, 30), time(14, 35), 1000);
    stopTime(1, mStopB, mTrip3, time(14, 45), time(14, 50), 2000);
    stopTime(2, mStopA, mTrip3, time(14, 55), time(14, 55), 2000);

    BlockConfigurationEntry blockConfig = blockConfiguration(block,
        serviceIds(lsids("sA", "sB", "sC"), lsids()), mTrip1, mTrip2, mTrip3);
    BlockStopTimeEntry bstAA = blockConfig.getStopTimes().get(0);
    BlockStopTimeEntry bstAB = blockConfig.getStopTimes().get(1);
    BlockStopTimeEntry bstAC = blockConfig.getStopTimes().get(2);
    BlockStopTimeEntry bstBA = blockConfig.getStopTimes().get(3);
    BlockStopTimeEntry bstBB = blockConfig.getStopTimes().get(4);
    BlockStopTimeEntry bstBC = blockConfig.getStopTimes().get(5);
    BlockStopTimeEntry bstCA = blockConfig.getStopTimes().get(6);
    BlockStopTimeEntry bstCB = blockConfig.getStopTimes().get(7);
    BlockStopTimeEntry bstCC = blockConfig.getStopTimes().get(8);

    // Setup block location instance for trip B
    BlockInstance blockInstance = new BlockInstance(blockConfig, mServiceDate);
    BlockLocation blockLocationB = new BlockLocation();
    blockLocationB.setActiveTrip(bstBB.getTrip());
    blockLocationB.setBlockInstance(blockInstance);
    blockLocationB.setClosestStop(bstBC);
    blockLocationB.setDistanceAlongBlock(400);
    blockLocationB.setInService(true);
    blockLocationB.setNextStop(bstBC);
    blockLocationB.setPredicted(false);
    blockLocationB.setScheduledDistanceAlongBlock(400);

    blockLocationB.setTimepointPredictions(timepointPredictions);

    // Mock StopTimeInstance with time frame
    long stopTimeFrom = dateAsLong("2015-07-23 00:00");
    long stopTimeTo = dateAsLong("2015-07-24 00:00");

    StopTimeInstance sti1 = new StopTimeInstance(bstAA,
        blockInstance.getState());
    ArrivalAndDepartureInstance in1 = new ArrivalAndDepartureInstance(sti1);
    in1.setBlockLocation(blockLocationB);
    in1.setPredictedArrivalTime((long) (in1.getScheduledArrivalTime()));
    in1.setPredictedDepartureTime((long) (in1.getScheduledDepartureTime()));

    StopTimeInstance sti2 = new StopTimeInstance(bstAB,
        blockInstance.getState());
    ArrivalAndDepartureInstance in2 = new ArrivalAndDepartureInstance(sti2);
    in2.setBlockLocation(blockLocationB);
    
    StopTimeInstance sti3 = new StopTimeInstance(bstAC,
        blockInstance.getState());
    ArrivalAndDepartureInstance in3 = new ArrivalAndDepartureInstance(sti3);
    in3.setBlockLocation(blockLocationB);
    in3.setPredictedArrivalTime((long) (in3.getScheduledArrivalTime()));
    in3.setPredictedDepartureTime((long) (in3.getScheduledDepartureTime()));
    
    StopTimeInstance sti4 = new StopTimeInstance(bstBA,
        blockInstance.getState());
    ArrivalAndDepartureInstance in4 = new ArrivalAndDepartureInstance(sti4);
    in4.setBlockLocation(blockLocationB);

    StopTimeInstance sti5 = new StopTimeInstance(bstBB,
        blockInstance.getState());
    ArrivalAndDepartureInstance in5 = new ArrivalAndDepartureInstance(sti5);
    in5.setBlockLocation(blockLocationB);
    
    StopTimeInstance sti6 = new StopTimeInstance(bstBC,
        blockInstance.getState());
    ArrivalAndDepartureInstance in6 = new ArrivalAndDepartureInstance(sti6);
    in6.setBlockLocation(blockLocationB);

    StopTimeInstance sti7 = new StopTimeInstance(bstCA,
        blockInstance.getState());
    ArrivalAndDepartureInstance in7 = new ArrivalAndDepartureInstance(sti7);
    in7.setBlockLocation(blockLocationB);

    StopTimeInstance sti8 = new StopTimeInstance(bstCB,
        blockInstance.getState());
    ArrivalAndDepartureInstance in8 = new ArrivalAndDepartureInstance(sti8);
    in8.setBlockLocation(blockLocationB);
    
    StopTimeInstance sti9 = new StopTimeInstance(bstCC,
        blockInstance.getState());
    ArrivalAndDepartureInstance in9 = new ArrivalAndDepartureInstance(sti9);
    in9.setBlockLocation(blockLocationB);

    Date fromTimeBuffered = new Date(stopTimeFrom
        - _blockStatusService.getRunningLateWindow() * 1000);
    Date toTimeBuffered = new Date(stopTimeTo
        + _blockStatusService.getRunningEarlyWindow() * 1000);

    Mockito.when(
        _stopTimeService.getStopTimeInstancesInTimeRange(stop,
            fromTimeBuffered, toTimeBuffered,
            EFrequencyStopTimeBehavior.INCLUDE_UNSPECIFIED)).thenReturn(
        Arrays.asList(sti1, sti2, sti3, sti4, sti5, sti6, sti7, sti8, sti9));

    // Create and add vehicle location record cache
    VehicleLocationRecordCacheImpl _cache = new VehicleLocationRecordCacheImpl();
    VehicleLocationRecord vlr = new VehicleLocationRecord();
    vlr.setBlockId(blockLocationB.getBlockInstance().getBlock().getBlock().getId());
    vlr.setTripId(mTrip2.getId());
    vlr.setTimepointPredictions(blockLocationB.getTimepointPredictions());
    vlr.setTimeOfRecord(mCurrentTime);
    vlr.setVehicleId(new AgencyAndId("1", "123"));

    // Create ScheduledBlockLocation for cache
    ScheduledBlockLocation sbl = new ScheduledBlockLocation();
    sbl.setActiveTrip(blockLocationB.getActiveTrip());

    // Add data to cache
    _cache.addRecord(blockInstance, vlr, sbl, null);
    _blockLocationService.setVehicleLocationRecordCache(_cache);
    ScheduledBlockLocationServiceImpl scheduledBlockLocationServiceImpl = new ScheduledBlockLocationServiceImpl();
    _blockLocationService.setScheduledBlockLocationService(scheduledBlockLocationServiceImpl);

    // Call ArrivalAndDepartureService
    return _service.getArrivalsAndDeparturesForStopInTimeRange(stop, target,
        stopTimeFrom, stopTimeTo);
  }

  //
  // Helper methods
  //

  private long getPredictedArrivalTimeByStopId(
      List<ArrivalAndDepartureInstance> arrivalsAndDepartures,
      AgencyAndId stopId) {
    for (ArrivalAndDepartureInstance adi : arrivalsAndDepartures) {
      if (adi.getStop().getId().equals(stopId)) {
        return adi.getPredictedArrivalTime();
      }
    }
    return 0;
  }
  
  private long getPredictedArrivalTimeByStopIdAndSequence(
      List<ArrivalAndDepartureInstance> arrivalsAndDepartures,
      AgencyAndId stopId, int sequence) {
    for (ArrivalAndDepartureInstance adi : arrivalsAndDepartures) {
      if (adi.getStop().getId().equals(stopId) 
          && adi.getStopTimeInstance().getSequence() == sequence) {
        return adi.getPredictedArrivalTime();
      }
    }
    return 0;
  }
  
  private long getPredictedArrivalTimeByStopIdAndSequence(
      List<ArrivalAndDepartureInstance> arrivalsAndDepartures,
      AgencyAndId stopId, AgencyAndId tripId, int sequence) {
    for (ArrivalAndDepartureInstance adi : arrivalsAndDepartures) {
      if (adi.getStop().getId().equals(stopId) 
          && adi.getStopTimeInstance().getStopTime().getStopTime().getSequence() == sequence
          && tripId.equals(adi.getStopTimeInstance().getTrip().getTrip().getId())) {
        return adi.getPredictedArrivalTime();
      }
    }
    return 0;
  }

  private long getPredictedDepartureTimeByStopId(
      List<ArrivalAndDepartureInstance> arrivalsAndDepartures,
      AgencyAndId stopId) {
    for (ArrivalAndDepartureInstance adi : arrivalsAndDepartures) {
      if (adi.getStop().getId().equals(stopId)) {
        return adi.getPredictedDepartureTime();
      }
    }
    return 0;
  }

  private long getScheduledArrivalTimeByStopId(TripEntryImpl trip,
      AgencyAndId id) {
    for (StopTimeEntry ste : trip.getStopTimes()) {
      if (ste.getStop().getId().equals(id)) {
        return ste.getArrivalTime() + mServiceDate / 1000;
      }
    }
    return 0;
  }
  
  private long getScheduledArrivalTimeByStopId(TripEntryImpl trip,
      AgencyAndId id, int sequence) {
    for (StopTimeEntry ste : trip.getStopTimes()) {
      if (ste.getStop().getId().equals(id) && sequence == ste.getSequence()) {
        return ste.getArrivalTime() + mServiceDate / 1000;
      }
    }
    return 0;
  }

  private long getScheduledDepartureTimeByStopId(TripEntryImpl trip,
      AgencyAndId id) {
    for (StopTimeEntry ste : trip.getStopTimes()) {
      if (ste.getStop().getId().equals(id)) {
        return ste.getDepartureTime() + mServiceDate / 1000;
      }
    }
    return 0;
  }

  private long createPredictedTime(int time) {
    return (mServiceDate / 1000 + time) * 1000;
  }
}