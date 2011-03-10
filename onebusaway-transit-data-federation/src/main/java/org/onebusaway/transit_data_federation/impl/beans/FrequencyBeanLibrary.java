package org.onebusaway.transit_data_federation.impl.beans;

import org.onebusaway.transit_data.model.schedule.FrequencyBean;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;

public class FrequencyBeanLibrary {
  
  public static FrequencyBean getBeanForFrequency(long serviceDate,
      FrequencyEntry frequency) {

    if (frequency == null)
      return null;

    FrequencyBean bean = new FrequencyBean();
    bean.setStartTime(serviceDate + frequency.getStartTime() * 1000);
    bean.setEndTime(serviceDate + frequency.getEndTime() * 1000);
    bean.setHeadway(frequency.getHeadwaySecs());
    return bean;
  }
}
