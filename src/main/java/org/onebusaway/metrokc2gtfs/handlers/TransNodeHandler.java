package org.onebusaway.metrokc2gtfs.handlers;

import org.onebusaway.metrokc2gtfs.model.MetroKCTransNode;

public class TransNodeHandler extends EntityHandler<Integer, MetroKCTransNode> {

  private static final String[] TRANS_NODE_FIELDS = {
      "id", "dbModDate", "status", "x", "y", "ignore=z", "city", "ignore",
      "ignore", "ignore"};

  public TransNodeHandler() {
    super(MetroKCTransNode.class, TRANS_NODE_FIELDS);
  }
}