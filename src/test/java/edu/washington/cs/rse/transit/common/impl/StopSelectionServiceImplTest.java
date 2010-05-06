package edu.washington.cs.rse.transit.common.impl;

import edu.washington.cs.rse.transit.RootTest;
import edu.washington.cs.rse.transit.common.model.aggregate.SelectionName;
import edu.washington.cs.rse.transit.common.model.aggregate.StopSelectionTree;
import edu.washington.cs.rse.transit.common.services.NoSuchRouteException;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class StopSelectionServiceImplTest extends RootTest {

  @Autowired
  private StopSelectionServiceImpl _stopSelectionService;

  @Test
  public void testGo() throws NoSuchRouteException {
    StopSelectionTree tree = _stopSelectionService.getStopsByRoute(65);
    go(tree, "");
  }

  private void go(StopSelectionTree tree, String prefix) {
    for (SelectionName name : tree.getNames()) {
      System.out.println(prefix + name.getName());
      go(tree.getSubTree(name), prefix + "  ");
    }
  }
}
