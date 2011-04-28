package org.onebusaway.transit_data_federation.impl.tripplanner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.onebusaway.container.refresh.Refreshable;
import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.CompactedTransferPatternFactory;
import org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.HubNode;
import org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.TransferNode;
import org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.TransferParent;
import org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.TransferPattern;
import org.onebusaway.transit_data_federation.impl.RefreshableResources;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.tripplanner.TransferPatternService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class TransferPatternServiceImpl implements TransferPatternService {

  private static Logger _log = LoggerFactory.getLogger(TransferPatternServiceImpl.class);

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

    _log.info("loading transfer patterns");

    factory.readPatternsFromFile(filePath);

    _log.info("transfer patterns: loaded segments=" + factory.getLines());

    Map<StopEntry, TransferPattern> patterns = factory.getPatternsByOriginStop();
    _transferPatternsByStop.putAll(patterns);
  }

  @Override
  public boolean isEnabled() {
    return !_transferPatternsByStop.isEmpty();
  }

  @Override
  public Collection<TransferNode> getTransferPatternForStops(
      StopEntry stopFrom, List<StopEntry> stopsTo) {

    TransferParent root = new TransferParent();

    TransferPattern pattern = _transferPatternsByStop.get(stopFrom);
    if (pattern == null)
      return Collections.emptyList();

    pattern.getTransfersForStops(root, stopsTo);

    Map<StopEntry, List<TransferParent>> hubParentsByStop = pattern.getTransfersForHubStops(root);

    for (Map.Entry<StopEntry, List<TransferParent>> entry : hubParentsByStop.entrySet()) {

      StopEntry hubStop = entry.getKey();
      List<TransferParent> parents = entry.getValue();

      Collection<TransferNode> nodes = getTransferPatternForStops(hubStop,
          stopsTo);

      for (TransferParent parent : parents) {
        for (TransferNode node : nodes) {
          parent.extendTransferNode(node);
        }
      }

    }

    return root.getTransfers();
  }

  @Override
  public Collection<TransferNode> getReverseTransferPatternForStops(
      Iterable<StopEntry> stopsFrom, StopEntry stopTo) {

    List<StopEntry> stopToAsList = Arrays.asList(stopTo);

    TransferParent root = new TransferParent();

    for (StopEntry stopFrom : stopsFrom) {

      Collection<TransferNode> trees = getTransferPatternForStops(stopFrom,
          stopToAsList);
      if (!trees.isEmpty()) {
        for (TransferNode tree : trees)
          reverseTree(tree, root, true);
      }
    }

    return root.getTransfers();
  }

  @Override
  public Collection<TransferNode> expandNode(HubNode node) {
    throw new IllegalStateException();
    //return getTransferPatternForStops(node.getHubStop(), node.getStopsTo());
  }

  /****
   * Private Methods
   ****/

  private List<TransferParent> reverseTree(TransferNode tree,
      TransferParent root, boolean exitAllowed) {

    if (tree == null)
      return Arrays.asList(root);

    if (tree.isExitAllowed() && !tree.getTransfers().isEmpty())
      throw new IllegalStateException();

    List<TransferParent> results = new ArrayList<TransferParent>();

    if (tree.isExitAllowed()) {
      List<TransferParent> parents = reverseTree(null, root, false);
      for (TransferParent parent : parents) {
        TransferNode extended = parent.extendTree(tree.getToStop(),
            tree.getFromStop(), exitAllowed);
        results.add(extended);
      }
    }

    for (TransferNode subTree : tree.getTransfers()) {
      List<TransferParent> parents = reverseTree(subTree, root, false);
      for (TransferParent parent : parents) {
        TransferNode extended = parent.extendTree(tree.getToStop(),
            tree.getFromStop(), exitAllowed);
        results.add(extended);
      }
    }

    return results;
  }

}
