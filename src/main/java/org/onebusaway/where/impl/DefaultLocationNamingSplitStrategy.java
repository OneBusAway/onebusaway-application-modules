package org.onebusaway.where.impl;

import org.onebusaway.where.model.SelectionName;
import org.onebusaway.where.model.SelectionNameTypes;
import org.onebusaway.where.services.LocationNameSplitStrategy;

import java.util.ArrayList;
import java.util.List;

public class DefaultLocationNamingSplitStrategy implements
    LocationNameSplitStrategy {

  public List<SelectionName> splitLocationNameIntoParts(String name) {

    if (name.contains("P&R"))
      name = name.replaceAll("P&R", "ParkAndRide");

    String[] tokens = name.split("\\s+&\\s+");
    List<SelectionName> names = new ArrayList<SelectionName>();

    if (tokens.length == 2) {
      names.add(new SelectionName(SelectionNameTypes.MAIN_STREET, tokens[0]));
      names.add(new SelectionName(SelectionNameTypes.CROSS_STREET, tokens[1]));

    } else {
      names.add(new SelectionName(SelectionNameTypes.STOP_DESCRIPTION, name));
    }

    return names;
  }
}
