package org.onebusaway.presentation.services;

import org.onebusaway.transit_data.model.NameBean;

import java.util.List;

public interface LocationNameSplitStrategy {
  public List<NameBean> splitLocationNameIntoParts(String name);
}
