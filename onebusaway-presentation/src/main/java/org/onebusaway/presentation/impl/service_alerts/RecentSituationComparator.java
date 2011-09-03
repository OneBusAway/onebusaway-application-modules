/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    if (! CollectionsLibrary.isEmpty(bean.getConsequences())) {
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
