package org.onebusaway.transit_data_federation.impl.tripplanner;

import java.util.Date;
import java.util.List;

import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.transit_data_federation.services.StopTimeService;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;
import org.onebusaway.transit_data_federation.services.tripplanner.TransferPatternPathService;
import org.onebusaway.transit_data_federation.services.tripplanner.TransferPatternService;
import org.opentripplanner.routing.core.TraverseOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class TransferPatternPathServiceImpl implements TransferPatternPathService {

  private TransferPatternService _transferPatternService;

  private StopTimeService _stopTimeService;

  @Autowired
  public void setTransferPatternService(
      TransferPatternService transferPatternService) {
    _transferPatternService = transferPatternService;
  }

  @Autowired
  public void setStopTimeService(StopTimeService stopTimeService) {
    _stopTimeService = stopTimeService;
  }

  public void go(StopEntry stopFrom, StopEntry stopTo, long targetTime,
      TraverseOptions options) {

    List<List<Pair<StopEntry>>> paths = _transferPatternService.getTransferPatternForStops(
        stopFrom, stopTo);

    for (List<Pair<StopEntry>> path : paths) {
      for (Pair<StopEntry> pair : path) {

      }
    }
  }

  public void leg(StopEntry fromStop, StopEntry toStop, long targetTime) {

    Date fromDepartureTime = new Date(targetTime);
    Date toDepartureTime = new Date(targetTime + 30 * 60 * 1000);

    List<Pair<StopTimeInstance>> pairs = _stopTimeService.getDeparturesBetweenStopPairInTimeRange(
        fromStop, toStop, fromDepartureTime, toDepartureTime);
  }
}
