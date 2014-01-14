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

import org.onebusaway.transit_data_federation.services.ArrivalAndDepartureService;
import org.onebusaway.transit_data_federation.services.StopTimeService;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.tripplanner.ItinerariesService;
import org.onebusaway.transit_data_federation.services.tripplanner.StopHopService;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTransferService;
import org.onebusaway.transit_data_federation.services.tripplanner.TransferPatternService;

public class GraphContext {

  private ArrivalAndDepartureService arrivalAndDepartureService;

  private StopTimeService stopTimeService;

  private TransitGraphDao transitGraphDao;

  private StopHopService stopHopService;

  private StopTransferService stopTransferService;

  private TransferPatternService transferPatternService;

  private ItinerariesService itinerariesService;

  private int stopTimeSearchInterval = 10;

  public GraphContext() {

  }

  public GraphContext(GraphContext context) {
    this.arrivalAndDepartureService = context.arrivalAndDepartureService;
    this.stopTimeService = context.stopTimeService;
    this.transitGraphDao = context.transitGraphDao;
    this.stopHopService = context.stopHopService;
    this.stopTransferService = context.stopTransferService;
    this.stopTimeSearchInterval = context.stopTimeSearchInterval;
  }

  public TransitGraphDao getTransitGraphDao() {
    return transitGraphDao;
  }

  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    this.transitGraphDao = transitGraphDao;
  }

  public StopHopService getStopHopService() {
    return stopHopService;
  }

  public void setStopHopService(StopHopService stopHopService) {
    this.stopHopService = stopHopService;
  }

  public StopTransferService getStopTransferService() {
    return stopTransferService;
  }

  public void setStopTransferService(StopTransferService stopTransferService) {
    this.stopTransferService = stopTransferService;
  }

  public ArrivalAndDepartureService getArrivalAndDepartureService() {
    return arrivalAndDepartureService;
  }

  public void setArrivalAndDepartureService(
      ArrivalAndDepartureService arrivalAndDepartureService) {
    this.arrivalAndDepartureService = arrivalAndDepartureService;
  }

  public StopTimeService getStopTimeService() {
    return stopTimeService;
  }

  public void setStopTimeService(StopTimeService stopTimeService) {
    this.stopTimeService = stopTimeService;
  }

  public TransferPatternService getTransferPatternService() {
    return transferPatternService;
  }

  public void setTransferPatternService(
      TransferPatternService transferPatternService) {
    this.transferPatternService = transferPatternService;
  }

  public ItinerariesService getItinerariesService() {
    return itinerariesService;
  }

  public void setItinerariesService(ItinerariesService itinerariesService) {
    this.itinerariesService = itinerariesService;
  }

  /**
   * 
   * @return time, in minutes
   */
  public int getStopTimeSearchInterval() {
    return stopTimeSearchInterval;
  }

  public void setStopTimeSearchInterval(int stopTimeSearchInterval) {
    this.stopTimeSearchInterval = stopTimeSearchInterval;
  }
}
