/**
 * 
 */
package org.onebusaway.presentation.impl;

import java.util.Comparator;

import org.onebusaway.presentation.client.RoutePresenter;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.utility.text.NaturalStringOrder;

public class RouteNameComparator implements Comparator<RouteBean> {
  @Override
  public int compare(RouteBean o1, RouteBean o2) {
    String n1 = RoutePresenter.getNameForRoute(o1);
    String n2 = RoutePresenter.getNameForRoute(o2);
    return NaturalStringOrder.compareNatural(n1, n2);
  }
}