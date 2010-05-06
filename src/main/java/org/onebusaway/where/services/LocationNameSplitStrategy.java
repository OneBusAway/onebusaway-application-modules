package org.onebusaway.where.services;

import org.onebusaway.where.model.SelectionName;

import java.util.List;

public interface LocationNameSplitStrategy {
  public List<SelectionName> splitLocationNameIntoParts(String name);
}
