package org.onebusaway.presentation.impl;

import org.onebusaway.presentation.services.LocationNameSplitStrategy;
import org.onebusaway.presentation.services.SelectionNameTypes;
import org.onebusaway.transit_data.model.NameBean;

import java.util.ArrayList;
import java.util.List;

public class DefaultLocationNameSplitStrategyImpl implements
    LocationNameSplitStrategy {

  public List<NameBean> splitLocationNameIntoParts(String name) {

    if (name.contains("P&R"))
      name = name.replaceAll("P&R", "ParkAndRide");

    String[] tokens = name.split("\\s+&\\s+");
    List<NameBean> names = new ArrayList<NameBean>();

    if (tokens.length == 2) {
      names.add(new NameBean(SelectionNameTypes.MAIN_STREET, tokens[0]));
      names.add(new NameBean(SelectionNameTypes.CROSS_STREET, tokens[1]));

    } else {
      names.add(new NameBean(SelectionNameTypes.STOP_DESCRIPTION, name));
    }

    return names;
  }
}
