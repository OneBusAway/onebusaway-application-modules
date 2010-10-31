/**
 * 
 */
package org.onebusaway.transit_data_federation.impl.beans;

import java.util.Comparator;

import org.onebusaway.transit_data.model.StopBean;

public class StopBeanIdComparator implements Comparator<StopBean> {

  public int compare(StopBean o1, StopBean o2) {
    return o1.getId().compareTo(o2.getId());
  }
}