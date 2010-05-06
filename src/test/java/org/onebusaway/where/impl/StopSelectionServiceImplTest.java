package org.onebusaway.where.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.onebusaway.where.model.SelectionName;
import org.onebusaway.where.model.StopSelectionTree;
import org.onebusaway.where.services.NoSuchRouteException;
import org.onebusaway.where.services.StopSelectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(locations = {
    "/data-sources-common.xml",
    "/org/onebusaway/application-context-common.xml",
    "/org/onebusaway/where/impl/application-context-test.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class StopSelectionServiceImplTest {

  @Autowired
  private StopSelectionService _stopSelectionServiceImpl;

  @Test
  public void test() throws NoSuchRouteException {
    StopSelectionTree tree = _stopSelectionServiceImpl.getStopsByRoute("30");
    go(tree, "");
  }

  private void go(StopSelectionTree tree, String prefix) {
    if (tree.hasStop()) {
      System.out.println(prefix + "stop=" + tree.getStop().getName());
      return;
    }
    for (SelectionName name : tree.getNames()) {
      System.out.println(prefix + name.getName());
      go(tree.getSubTree(name), prefix + "  ");
    }
  }

}
