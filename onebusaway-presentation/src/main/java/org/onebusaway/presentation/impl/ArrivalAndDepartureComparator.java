/**
 * 
 */
package org.onebusaway.presentation.impl;

import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;

import java.util.Comparator;

public class ArrivalAndDepartureComparator implements
    Comparator<ArrivalAndDepartureBean> {

  public int compare(ArrivalAndDepartureBean o1, ArrivalAndDepartureBean o2) {
    long t1 = o1.getScheduledArrivalTime();
    if (o1.hasPredictedArrivalTime())
      t1 = o1.getPredictedArrivalTime();
    long t2 = o2.getScheduledArrivalTime();
    if (o2.hasPredictedArrivalTime())
      t2 = o2.getPredictedArrivalTime();
    return (int) (t1 - t2);
  }
}