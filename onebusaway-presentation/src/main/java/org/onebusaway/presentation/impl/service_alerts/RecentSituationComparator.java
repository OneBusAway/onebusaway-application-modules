package org.onebusaway.presentation.impl.service_alerts;

import java.util.Comparator;

import org.onebusaway.collections.CollectionsLibrary;
import org.onebusaway.transit_data.model.service_alerts.SituationBean;
import org.onebusaway.transit_data.model.service_alerts.SituationConsequenceBean;
import org.onebusaway.transit_data.model.service_alerts.TimeRangeBean;

public class RecentSituationComparator implements Comparator<SituationBean> {

  @Override
  public int compare(SituationBean o1, SituationBean o2) {
    long t1 = getTimeForSituation(o1);
    long t2 = getTimeForSituation(o2);
    return Double.compare(t1, t2);
  }

  private long getTimeForSituation(SituationBean bean) {

    TimeRangeBean window = bean.getPublicationWindow();
    if (window != null && window.getFrom() != 0)
      return window.getFrom();

    if (CollectionsLibrary.isEmpty(bean.getConsequences())) {
      long t = Long.MAX_VALUE;
      for (SituationConsequenceBean consequence : bean.getConsequences()) {
        TimeRangeBean period = consequence.getPeriod();
        if (period != null && period.getFrom() != 0)
          t = Math.min(t, period.getFrom());
      }

      if (t != Long.MAX_VALUE)
        return t;
    }

    return bean.getCreationTime();
  }
}
