package org.onebusaway.transit_data_federation.impl.tripplanner;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.container.refresh.Refreshable;
import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.CompactedTransferPatternFactory;
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

    CompactedTransferPatternFactory factory = new CompactedTransferPatternFactory(
        _transitGraphDao);

    factory.readPatternsFromFile(filePath);

    Map<StopEntry, TransferPattern> patterns = factory.getPatternsByOriginStop();
    _transferPatternsByStop.putAll(patterns);

  }

  @Override
  public boolean isEnabled() {
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

}
