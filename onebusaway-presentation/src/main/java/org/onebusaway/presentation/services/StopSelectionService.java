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
package org.onebusaway.presentation.services;

import org.onebusaway.exceptions.InvalidSelectionServiceException;
import org.onebusaway.presentation.model.StopSelectionBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;

import java.util.List;

public interface StopSelectionService {

  public StopSelectionBean getSelectedStops(StopsForRouteBean stopsForRoute,
      List<Integer> selectionIndices) throws InvalidSelectionServiceException;
}
