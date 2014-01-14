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
package org.onebusaway.transit_data_federation.impl.beans;

import java.util.Comparator;

import org.onebusaway.transit_data.model.RouteBean;

/**
 * Comparator to sort {@link RouteBean} beans by their id
 * 
 * @author bdferris
 * @see RouteBean
 */
public class RouteBeanIdComparator implements Comparator<RouteBean> {

  public int compare(RouteBean o1, RouteBean o2) {
    return o1.getId().compareTo(o2.getId());
  }
}
