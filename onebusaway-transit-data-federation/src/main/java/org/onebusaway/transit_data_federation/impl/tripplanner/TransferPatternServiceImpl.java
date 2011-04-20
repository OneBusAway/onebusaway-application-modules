package org.onebusaway.transit_data_federation.impl.tripplanner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.collections.tuple.Tuples;
import org.onebusaway.container.refresh.Refreshable;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.TransferPattern;
import org.onebusaway.transit_data_federation.impl.RefreshableResources;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.tripplanner.TransferPatternService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class TransferPatternServiceImpl implements TransferPatternService {

  private Map<StopEntry, TransferPattern> _transferPatternsByStop = new HashMap<StopEntry, TransferPattern>();

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
  @Refreshable(dependsOn = RefreshableResources.TRANSFER_PATTERNS)
  public void setup() throws IOException, ClassNotFoundException {

    _transferPatternsByStop.clear();

    File filePath = _bundle.getTransferPatternsPath();

    if (!filePath.exists())
      return;

    /*
    Map<AgencyAndId, TransferPatternData> dataByStopId = ObjectSerializationLibrary.readObject(filePath);
    for (Map.Entry<AgencyAndId, TransferPatternData> entry : dataByStopId.entrySet()) {

      AgencyAndId originStopId = entry.getKey();
      TransferPatternData data = entry.getValue();

      StopEntry originStop = _transitGraphDao.getStopEntryForId(originStopId,
          true);
      TransferPattern pattern = new TransferPattern(originStop);

      for (AgencyAndId stopId : data.getStops()) {
        List<List<Pair<AgencyAndId>>> paths = data.getPathsForStop(stopId);
        for (List<Pair<AgencyAndId>> idPath : paths) {
          List<Pair<StopEntry>> path = convertPath(idPath);
          pattern.addPath(path);
        }
      }
    }
    */
  }

  @Override
  public boolean isServiceEnabled() {
    return !_transferPatternsByStop.isEmpty();
  }

  @Override
  public List<List<Pair<StopEntry>>> getTransferPatternForStops(
      StopEntry stopFrom, StopEntry stopTo) {

    TransferPattern pattern = _transferPatternsByStop.get(stopFrom);
    if (pattern == null)
      return Collections.emptyList();

    return pattern.getPathsForStop(stopTo);
  }

  /****
   * Private Methods
   ****/

  private List<Pair<StopEntry>> convertPath(List<Pair<AgencyAndId>> idPath) {
    List<Pair<StopEntry>> path = new ArrayList<Pair<StopEntry>>(idPath.size());
    for (Pair<AgencyAndId> pair : idPath) {
      StopEntry fromStop = _transitGraphDao.getStopEntryForId(pair.getFirst(),
          true);
      StopEntry toStop = _transitGraphDao.getStopEntryForId(pair.getSecond(),
          true);
      Pair<StopEntry> stops = Tuples.pair(fromStop, toStop);
      path.add(stops);
    }
    return path;
  }
}
