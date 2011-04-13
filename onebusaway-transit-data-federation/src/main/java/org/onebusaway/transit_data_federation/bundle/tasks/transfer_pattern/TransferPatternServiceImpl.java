package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.ArrivalAndDepartureService;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TransferPatternServiceImpl {

  private TransitGraphDao _transitGraphDao;

  private StopTransferService _stopTransferService;

  private ArrivalAndDepartureService _arrivalAndDepartureService;

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }

  @Autowired
  public void setStopTransferService(StopTransferService stopTransferService) {
    _stopTransferService = stopTransferService;
  }

  @Autowired
  public void setArrivalAndDepartureService(
      ArrivalAndDepartureService arrivalAndDepartureService) {
    _arrivalAndDepartureService = arrivalAndDepartureService;
  }

  public void generateTransferPatternForStop(AgencyAndId stopId) {

  }
}
