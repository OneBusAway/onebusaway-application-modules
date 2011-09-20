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
package org.onebusaway.presentation.impl;

import org.onebusaway.transit_data.model.AgencyWithCoverageBean;

import java.util.Comparator;

public class AgencyWithCoverageBeanComparator implements
    Comparator<AgencyWithCoverageBean> {

  public int compare(AgencyWithCoverageBean o1, AgencyWithCoverageBean o2) {
    return o1.getAgency().getName().compareTo(o2.getAgency().getName());
  }
}
