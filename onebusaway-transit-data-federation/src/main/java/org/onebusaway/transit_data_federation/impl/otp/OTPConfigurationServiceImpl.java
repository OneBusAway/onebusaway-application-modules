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
package org.onebusaway.transit_data_federation.impl.otp;

import java.util.Set;

import org.onebusaway.transit_data.model.tripplanning.ConstraintsBean;
import org.onebusaway.transit_data.model.tripplanning.Modes;
import org.onebusaway.transit_data_federation.services.ArrivalAndDepartureService;
import org.onebusaway.transit_data_federation.services.StopTimeService;
import org.onebusaway.transit_data_federation.services.otp.OTPConfigurationService;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.tripplanner.ItinerariesService;
import org.onebusaway.transit_data_federation.services.tripplanner.StopHopService;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTransferService;
import org.onebusaway.transit_data_federation.services.tripplanner.TransferPatternService;
import org.onebusaway.utility.time.SystemTime;
import org.opentripplanner.routing.core.TraverseModeSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class OTPConfigurationServiceImpl implements OTPConfigurationService {

  private TransitGraphDao _transitGraphDao;
  private StopHopService _stopHopService;
  private StopTransferService _stopTransferService;
  private ArrivalAndDepartureService _arrivalAndDepartureService;
  private StopTimeService _stopTimeService;
  private ItinerariesService _itinerariesService;
  private TransferPatternService _transferPatternService;

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }

  @Autowired
  public void setStopHopService(StopHopService stopHopService) {
    _stopHopService = stopHopService;
  }

  @Autowired
  public void setStopTranferService(StopTransferService stopTransferService) {
    _stopTransferService = stopTransferService;
  }

  @Autowired
  public void setArrivalAndDepartureService(
      ArrivalAndDepartureService realTimeArrivalAndDepartureService) {
    _arrivalAndDepartureService = realTimeArrivalAndDepartureService;
  }

  @Autowired
  public void setStopTimeService(StopTimeService stopTimeService) {
    _stopTimeService = stopTimeService;
  }

  @Autowired
  public void setItinerariesService(ItinerariesService itinerariesService) {
    _itinerariesService = itinerariesService;
  }

  @Autowired
  public void setTransferPatternService(
      TransferPatternService transferPatternService) {
    _transferPatternService = transferPatternService;
  }

  @Override
  public GraphContext createGraphContext() {

    GraphContext context = new GraphContext();
    context.setArrivalAndDepartureService(_arrivalAndDepartureService);
    context.setItinerariesService(_itinerariesService);

    context.setStopTimeService(_stopTimeService);
    context.setStopHopService(_stopHopService);
    context.setStopTransferService(_stopTransferService);
    context.setTransferPatternService(_transferPatternService);
    context.setTransitGraphDao(_transitGraphDao);

    return context;
  }

  /**
   * From 'Transit Capacity and Quality of Service Manual' - Part 3 - Exhibit
   * 3.9
   * 
   * http://onlinepubs.trb.org/Onlinepubs/tcrp/tcrp100/part%203.pdf
   * 
   * Table of passenger perceptions of time. Given that actual in-vehicle time
   * seems to occur in real-time (penalty ratio of 1.0), how do passengers
   * perceived walking, waiting for the first vehicle, and waiting for a
   * transfer. In addition, is there an additive penalty for making a transfer
   * of any kind.
   */
  @Override
  public OBATraverseOptions createTraverseOptions() {

    OBATraverseOptions options = new OBATraverseOptions();

    options.walkReluctance = 2.2;
    options.waitAtBeginningFactor = 0.1;
    options.waitReluctance = 2.5;

    options.boardCost = 5 * 60;
    options.maxTransfers = 2;
    options.minTransferTime = 120;

    options.maxWalkDistance = 1500;

    /**
     * Ten seconds max
     */
    options.maxComputationTime = 10000;

    options.useServiceDays = false;

    options.currentTime = SystemTime.currentTimeMillis();

    return options;
  }

  @Override
  public void applyConstraintsToTraverseOptions(ConstraintsBean constraints,
      OBATraverseOptions options) {

    options.setArriveBy(constraints.isArriveBy());

    /**
     * Modes
     */
    Set<String> modes = constraints.getModes();
    if (modes != null) {
      TraverseModeSet ms = new TraverseModeSet();
      if (modes.contains(Modes.WALK))
        ms.setWalk(true);
      if (modes.contains(Modes.TRANSIT))
        ms.setTransit(true);
      options.setModes(ms);
    }

    /**
     * Preset optimization types
     */
    String optimizeFor = constraints.getOptimizeFor();

    if (optimizeFor != null) {
      optimizeFor = optimizeFor.toLowerCase();
      if (optimizeFor.equals("min_time")) {
        options.boardCost = 0;
        options.waitReluctance = 1.0;
        options.waitReluctance = 1.0;
      } else if (optimizeFor.equals("min_transfers")) {
        options.boardCost = 20 * 60;
      } else if (optimizeFor.equals("min_walking")) {
        options.walkReluctance = 5.0;
      }
    }

    /**
     * Walking
     */
    if (constraints.getWalkSpeed() != -1)
      options.speed = constraints.getWalkSpeed();
    if (constraints.getMaxWalkingDistance() != -1)
      options.maxWalkDistance = constraints.getMaxWalkingDistance();
    if (constraints.getWalkReluctance() != -1)
      options.walkReluctance = constraints.getWalkReluctance();

    /**
     * Waiting
     */
    if (constraints.getInitialWaitReluctance() != -1)
      options.waitAtBeginningFactor = constraints.getInitialWaitReluctance();
    if (constraints.getInitialWaitReluctance() != -1)
      options.waitReluctance = constraints.getWaitReluctance();

    /**
     * Transferring
     */
    if (constraints.getTransferCost() != -1)
      options.boardCost = constraints.getTransferCost();
    if (constraints.getMinTransferTime() != -1)
      options.minTransferTime = constraints.getMinTransferTime();
    if (constraints.getMaxTransfers() != -1)
      options.maxTransfers = constraints.getMaxTransfers();
    if (constraints.getMaxComputationTime() != -1
        && constraints.getMaxComputationTime() < 15000)
      options.maxComputationTime = constraints.getMaxComputationTime();

    options.numItineraries = constraints.getResultCount();

    options.useRealtime = constraints.isUseRealTime();

    if (constraints.getMaxTripDuration() != -1)
      options.maxTripDuration = constraints.getMaxTripDuration() * 1000;

    if (constraints.getCurrentTime() != -1)
      options.currentTime = constraints.getCurrentTime();
    
    options.lookaheadTime = constraints.getLookaheadTime();
  }
}
