/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
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
import java.util.List;

import org.onebusaway.collections.CollectionsLibrary;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.model.service_alerts.TimeRangeBean;

public class RecentSituationComparator implements Comparator<ServiceAlertBean> {

  @Override
  public int compare(ServiceAlertBean o1, ServiceAlertBean o2) {
    long t1 = getTimeForSituation(o1);
    long t2 = getTimeForSituation(o2);
    return Double.compare(t1, t2);
  }

  private long getTimeForSituation(ServiceAlertBean bean) {

    long t = getFirstTime(bean.getPublicationWindows());
    if (t != Long.MAX_VALUE)
      return t;

    t = getFirstTime(bean.getActiveWindows());
    if (t != Long.MAX_VALUE)
      return t;

    return bean.getCreationTime();
  }

  private long getFirstTime(List<TimeRangeBean> windows) {
    long min = Long.MAX_VALUE;
    if (!CollectionsLibrary.isEmpty(windows)) {
      for (TimeRangeBean window : windows) {
        if (window.getFrom() != 0)
          min = Math.min(min, window.getFrom());
      }
    }
    return min;
  }
}
