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
import java.util.Set;

import javax.annotation.PostConstruct;

import org.onebusaway.container.refresh.Refreshable;
import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.CompactedTransferPatternFactory;
import org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.TransferPattern;
import org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.TransferTree;
import org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.TransferTreeNode;
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
  public Collection<TransferTree> getTransferPatternForStops(
      StopEntry stopFrom, Iterable<StopEntry> stopsTo) {

    TransferPattern pattern = _transferPatternsByStop.get(stopFrom);
    if (pattern == null)
      return Collections.emptyList();

    TransferTreeNode root = new TransferTreeNode();

    for (StopEntry stopTo : stopsTo) {

      pattern.getTransfersForStop(stopTo, root);

      Set<StopEntry> hubStops = pattern.getHubStops();

      if (!hubStops.isEmpty()) {

        for (StopEntry hubStop : hubStops) {

          TransferPattern hubPattern = _transferPatternsByStop.get(hubStop);
          if (hubPattern == null)
            continue;

          TransferTreeNode hubRoot = new TransferTreeNode();
          hubPattern.getTransfersForStop(stopTo, hubRoot);

          Collection<TransferTreeNode> hubNodes = pattern.getTransfersForHubStop(
              hubStop, root);

          for (TransferTreeNode hubNode : hubNodes) {
            hubNode.extendTree(hubRoot);
          }
        }
      }
    }

    return root.getTransfers();
  }

  @Override
  public Collection<TransferTree> getReverseTransferPatternForStops(
      Iterable<StopEntry> stopsFrom, StopEntry stopTo) {

    List<StopEntry> stopToAsList = Arrays.asList(stopTo);

    TransferTreeNode root = new TransferTreeNode();

    for (StopEntry stopFrom : stopsFrom) {

      Collection<TransferTree> trees = getTransferPatternForStops(stopFrom,
          stopToAsList);
      if (!trees.isEmpty()) {
        for (TransferTree tree : trees)
          reverseTree(tree, root, true);
      }
    }

    return root.getTransfers();
  }

  /****
   * Private Methods
   ****/

  private List<TransferTreeNode> reverseTree(TransferTree tree,
      TransferTreeNode root, boolean exitAllowed) {

    if (tree == null)
      return Arrays.asList(root);

    if (tree.isExitAllowed() && !tree.getTransfers().isEmpty())
      throw new IllegalStateException();

    List<TransferTreeNode> results = new ArrayList<TransferTreeNode>();

    if (tree.isExitAllowed()) {
      List<TransferTreeNode> parents = reverseTree(null, root, false);
      for (TransferTreeNode parent : parents) {
        TransferTree extended = parent.extendTree(tree.getToStop(),
            tree.getFromStop(), exitAllowed);
        results.add(extended);
      }
    }

    for (TransferTree subTree : tree.getTransfers()) {
      List<TransferTreeNode> parents = reverseTree(subTree, root, false);
      for (TransferTreeNode parent : parents) {
        TransferTree extended = parent.extendTree(tree.getToStop(),
            tree.getFromStop(), exitAllowed);
        results.add(extended);
      }
    }

    return results;
  }
}
