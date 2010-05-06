package org.onebusaway.presentation.impl;

import java.util.Comparator;

import org.onebusaway.transit_data.model.AgencyBean;

public class AgencyNameComparator implements Comparator<AgencyBean> {

  @Override
  public int compare(AgencyBean o1, AgencyBean o2) {
    return o1.getName().compareTo(o2.getName());
  }

}
