package org.onebusaway.presentation.impl;

import org.onebusaway.transit_data.model.AgencyWithCoverageBean;

import java.util.Comparator;

public class AgencyWithCoverageBeanComparator implements
    Comparator<AgencyWithCoverageBean> {

  public int compare(AgencyWithCoverageBean o1, AgencyWithCoverageBean o2) {
    return o1.getAgency().getName().compareTo(o2.getAgency().getName());
  }
}
