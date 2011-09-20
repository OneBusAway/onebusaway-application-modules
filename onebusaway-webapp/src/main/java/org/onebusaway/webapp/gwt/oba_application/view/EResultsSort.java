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
package org.onebusaway.webapp.gwt.oba_application.view;

import org.onebusaway.transit_data.model.oba.LocalSearchResult;
import org.onebusaway.transit_data.model.oba.TimedPlaceBean;
import org.onebusaway.webapp.gwt.oba_application.model.TimedLocalSearchResult;

import java.util.Comparator;

public enum EResultsSort {

  NAME(new SortByName()), RATING(new SortByRating()), DISTANCE(
      new SortByDistance());

  private Comparator<TimedLocalSearchResult> _comparator;

  private EResultsSort(Comparator<TimedLocalSearchResult> comparator) {
    _comparator = comparator;
  }

  public Comparator<TimedLocalSearchResult> getComparator() {
    return _comparator;
  }

  private static class SortByName implements Comparator<TimedLocalSearchResult> {

    public int compare(TimedLocalSearchResult o1, TimedLocalSearchResult o2) {
      LocalSearchResult lsr1 = o1.getLocalSearchResult();
      LocalSearchResult lsr2 = o2.getLocalSearchResult();
      return lsr1.getName().compareTo(lsr2.getName());
    }
  }

  private static class SortByRating implements
      Comparator<TimedLocalSearchResult> {

    public int compare(TimedLocalSearchResult o1, TimedLocalSearchResult o2) {
      LocalSearchResult lsr1 = o1.getLocalSearchResult();
      LocalSearchResult lsr2 = o2.getLocalSearchResult();
      double r1 = lsr1.getRating();
      double r2 = lsr2.getRating();
      return r1 == r2 ? 0 : (r1 < r2 ? 1 : -1);
    }
  }

  private static class SortByDistance implements
      Comparator<TimedLocalSearchResult> {

    public int compare(TimedLocalSearchResult o1, TimedLocalSearchResult o2) {
      TimedPlaceBean tp1 = o1.getTimedPlace();
      TimedPlaceBean tp2 = o2.getTimedPlace();
      int t1 = tp1.getTime();
      int t2 = tp2.getTime();
      return t1 == t2 ? 0 : (t1 < t2 ? -1 : 1);
    }
  }
}
