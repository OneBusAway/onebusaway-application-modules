package org.onebusaway.transit_data_federation.impl.otp;

import org.onebusaway.transit_data_federation.services.ArrivalAndDepartureService;
import org.onebusaway.transit_data_federation.services.StopTimeService;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.tripplanner.StopHopService;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTransferService;

public class GraphContext {

  private ArrivalAndDepartureService arrivalAndDepartureService;

  private StopTimeService stopTimeService;

  private TransitGraphDao transitGraphDao;

  private StopHopService stopHopService;

  private StopTransferService stopTransferService;

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
