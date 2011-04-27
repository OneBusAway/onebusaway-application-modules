package org.onebusaway.transit_data_federation.impl.tripplanner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.onebusaway.collections.CollectionsLibrary;
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

  private static final StopTransferList EMPTY_TRANSFERS = new StopTransferList(
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

      StopTransferAndHopData allData = ObjectSerializationLibrary.readObject(path);

      Map<AgencyAndId, List<StopTransferData>> stopTransferDataByStopId = allData.getTransferData();
      Map<AgencyAndId, List<StopTransferData>> reversedStopTransfersByStopId = reverseTransfers(stopTransferDataByStopId);

      Set<AgencyAndId> stopIds = new HashSet<AgencyAndId>();
      stopIds.addAll(stopTransferDataByStopId.keySet());
      stopIds.addAll(reversedStopTransfersByStopId.keySet());

      _log.info("stop transfers=" + stopTransferDataByStopId.size());
      _log.info("reverse stop transfers="
          + reversedStopTransfersByStopId.size());

      for (AgencyAndId stopId : stopIds) {

        StopEntry stop = _transitGraphDao.getStopEntryForId(stopId);

        if (stop == null) {
          _log.warn("unknown stop: " + stopId);
          continue;
        }

        List<StopTransferData> transfersFromStopData = stopTransferDataByStopId.get(stopId);
        List<StopTransferData> transfersToStopData = reversedStopTransfersByStopId.get(stopId);

        List<StopTransfer> transfersFromStop = getTransferDataAsList(transfersFromStopData);
        List<StopTransfer> transfersToStop = getTransferDataAsList(transfersToStopData);

        StopEntryImpl stopEntry = (StopEntryImpl) stop;
        stopEntry.setTransfers(new StopTransfers(transfersFromStop,
            transfersToStop));
      }
    }
  }

  @Override
  public List<StopTransfer> getTransfersFromStop(StopEntry stop) {
    StopEntryImpl impl = (StopEntryImpl) stop;
    StopTransfers transfers = impl.getTransfers();
    if (transfers == null || transfers.getTransfersFromStop() == null)
      return EMPTY_TRANSFERS;
    return transfers.getTransfersFromStop();
  }

  @Override
  public List<StopTransfer> getTransfersToStop(StopEntry stop) {
    StopEntryImpl impl = (StopEntryImpl) stop;
    StopTransfers transfers = impl.getTransfers();
    if (transfers == null || transfers.getTransfersToStop() == null)
      return EMPTY_TRANSFERS;
    return transfers.getTransfersToStop();
  }

  /****
   * 
   ****/

  private Map<AgencyAndId, List<StopTransferData>> reverseTransfers(
      Map<AgencyAndId, List<StopTransferData>> stopTransferDataByStopId) {

    Map<AgencyAndId, List<StopTransferData>> reversed = new HashMap<AgencyAndId, List<StopTransferData>>();

    for (Map.Entry<AgencyAndId, List<StopTransferData>> entry : stopTransferDataByStopId.entrySet()) {
      AgencyAndId fromStop = entry.getKey();
      for (StopTransferData transfer : entry.getValue()) {
        AgencyAndId toStop = transfer.getStopId();

        List<StopTransferData> transfers = reversed.get(toStop);

        if (transfers == null) {
          transfers = new ArrayList<StopTransferData>();
          reversed.put(toStop, transfers);
        }

        StopTransferData reversedTransfer = new StopTransferData(fromStop,
            transfer.getMinTransferTime(), transfer.getDistance());
        transfers.add(reversedTransfer);
      }
    }

    return reversed;
  }

  private List<StopTransfer> getTransferDataAsList(
      List<StopTransferData> transferData) {

    if (CollectionsLibrary.isEmpty(transferData))
      return null;

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

      transfers.add(transfer);
    }

    return new StopTransferList(transfers);
  }
}
