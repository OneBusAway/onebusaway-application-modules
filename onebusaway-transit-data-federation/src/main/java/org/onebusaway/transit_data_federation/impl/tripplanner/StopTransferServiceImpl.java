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
import org.onebusaway.transit_data_federation.services.tripplanner.StopHop;
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

  private static final StopHopList EMPTY_HOPS = new StopHopList(
      new ArrayList<StopHop>());

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

      Map<AgencyAndId, List<StopHopData>> stopHopDataByStopId = allData.getHopData();
      Map<AgencyAndId, List<StopHopData>> reversedStopHopDataByStopId = reverseHops(stopHopDataByStopId);

      Set<AgencyAndId> stopIds = new HashSet<AgencyAndId>();
      stopIds.addAll(stopTransferDataByStopId.keySet());
      stopIds.addAll(reversedStopTransfersByStopId.keySet());

      for (AgencyAndId stopId : stopIds) {

        StopEntry stop = _transitGraphDao.getStopEntryForId(stopId);

        if (stop == null) {
          _log.warn("unknown stop: " + stopId);
          continue;
        }

        List<StopTransferData> transfersFromStopData = stopTransferDataByStopId.get(stopId);
        List<StopTransferData> transfersToStopData = reversedStopTransfersByStopId.get(stopId);

        List<StopHopData> hopsFromStopData = stopHopDataByStopId.get(stopId);
        List<StopHopData> hopsToStopData = reversedStopHopDataByStopId.get(stopId);

        List<StopTransfer> transfersFromStop = getTransferDataAsList(transfersFromStopData);
        List<StopTransfer> transfersToStop = getTransferDataAsList(transfersToStopData);

        List<StopHop> hopsFromStop = getHopDataAsList(hopsFromStopData);
        List<StopHop> hopsToStop = getHopDataAsList(hopsToStopData);

        StopEntryImpl stopEntry = (StopEntryImpl) stop;
        stopEntry.setTransfers(new StopTransfers(transfersFromStop,
            transfersToStop, hopsFromStop, hopsToStop));
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

  @Override
  public List<StopHop> getHopsFromStop(StopEntry stop) {
    StopEntryImpl impl = (StopEntryImpl) stop;
    StopTransfers transfers = impl.getTransfers();
    if (transfers == null || transfers.getHopsFromStop() == null)
      return EMPTY_HOPS;
    return transfers.getHopsFromStop();
  }

  @Override
  public List<StopHop> getHopsToStop(StopEntry stop) {
    StopEntryImpl impl = (StopEntryImpl) stop;
    StopTransfers transfers = impl.getTransfers();
    if (transfers == null || transfers.getHopsToStop() == null)
      return EMPTY_HOPS;
    return transfers.getHopsToStop();
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

  private Map<AgencyAndId, List<StopHopData>> reverseHops(
      Map<AgencyAndId, List<StopHopData>> stopHopDataByStopId) {

    Map<AgencyAndId, List<StopHopData>> reversed = new HashMap<AgencyAndId, List<StopHopData>>();

    for (Map.Entry<AgencyAndId, List<StopHopData>> entry : stopHopDataByStopId.entrySet()) {
      AgencyAndId fromStop = entry.getKey();
      for (StopHopData hop : entry.getValue()) {
        AgencyAndId toStop = hop.getStopId();

        List<StopHopData> hops = reversed.get(toStop);

        if (hops == null) {
          hops = new ArrayList<StopHopData>();
          reversed.put(toStop, hops);
        }

        StopHopData reversedTransfer = new StopHopData(fromStop,
            hop.getMinTravelTime());
        hops.add(reversedTransfer);
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

  private List<StopHop> getHopDataAsList(List<StopHopData> hopData) {

    if (CollectionsLibrary.isEmpty(hopData))
      return null;

    List<StopHop> hops = new ArrayList<StopHop>();

    for (StopHopData data : hopData) {
      AgencyAndId targetStopId = data.getStopId();
      StopEntry targetStop = _transitGraphDao.getStopEntryForId(targetStopId);
      if (targetStop == null) {
        _log.warn("unkown stop: " + targetStopId);
        continue;
      }
      StopHop hop = new StopHop(targetStop, data.getMinTravelTime());

      hops.add(hop);
    }

    return new StopHopList(hops);
  }
}
