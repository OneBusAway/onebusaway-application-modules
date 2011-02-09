package org.onebusaway.transit_data_federation.impl.tripplanner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.onebusaway.container.refresh.Refreshable;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.impl.RefreshableResources;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopEntryImpl;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTransfer;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTransferService;
import org.onebusaway.utility.ObjectSerializationLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StopTransferServiceImpl implements StopTransferService {

  private static Logger _log = LoggerFactory.getLogger(StopTransferServiceImpl.class);

  private static final StopTransferList EMPTY = new StopTransferList(
      new ArrayList<StopTransfer>());

  private FederatedTransitDataBundle _bundle;

  private TransitGraphDao _transitGraphDao;

  @Autowired
  public void setBundle(FederatedTransitDataBundle bundle) {
    _bundle = bundle;
  }

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }

  @PostConstruct
  @Refreshable(dependsOn = RefreshableResources.STOP_TRANSFER_DATA)
  public void setup() throws IOException, ClassNotFoundException {

    File path = _bundle.getStopTransfersPath();

    if (path.exists()) {

      Map<AgencyAndId, List<StopTransferData>> stopTransferDataByStopId = ObjectSerializationLibrary.readObject(path);

      for (Map.Entry<AgencyAndId, List<StopTransferData>> entry : stopTransferDataByStopId.entrySet()) {

        AgencyAndId stopId = entry.getKey();
        List<StopTransferData> transferData = entry.getValue();

        StopEntry stop = _transitGraphDao.getStopEntryForId(stopId);

        if (stop == null) {
          _log.warn("unknown stop: " + stopId);
          continue;
        }

        List<StopTransfer> transfers = new ArrayList<StopTransfer>();

        for (StopTransferData data : transferData) {
          AgencyAndId targetStopId = data.getStopId();
          StopEntry targetStop = _transitGraphDao.getStopEntryForId(targetStopId);
          if (targetStop == null) {
            _log.warn("unkown stop: " + targetStopId);
            continue;
          }
          StopTransfer transfer = new StopTransfer(targetStop,
              data.getMinTransferTime(), data.getDistance());

          /*
          System.out.println(stop.getStopLat() + " " + stop.getStopLon() + " "
              + targetStop.getStopLat() + " " + targetStop.getStopLon());
          */

          transfers.add(transfer);
        }

        StopEntryImpl stopEntry = (StopEntryImpl) stop;
        stopEntry.setTransfers(new StopTransferList(transfers));
      }
    }
  }

  @Override
  public List<StopTransfer> getTransfersForStop(StopEntry stop) {
    StopEntryImpl impl = (StopEntryImpl) stop;
    StopTransferList transfers = impl.getTransfers();
    if (transfers == null)
      return EMPTY;
    return transfers;
  }
}
