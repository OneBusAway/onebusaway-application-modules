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
package org.onebusaway.webapp.gwt.oba_application.control;

import java.util.Map;

import org.onebusaway.transit_data.model.tripplanning.ConstraintsBean;
import org.onebusaway.transit_data.model.tripplanning.TransitShedConstraintsBean;
import org.onebusaway.webapp.gwt.common.context.Context;

public class ConstraintsParameterMapping {

  public static final String PARAM_QUERY = "q";
  public static final String PARAM_LOCATION = "loc";
  public static final String PARAM_LAT = "lat";
  public static final String PARAM_LON = "lon";
  public static final String PARAM_TIME = "t";
  public static final String PARAM_MAX_TRANSFERS = "transfers";
  public static final String PARAM_MAX_TRIP_LENGTH = "duration";
  public static final String PARAM_MAX_WALKING_DISTANCE = "walk";

  public static void addConstraintsToParams(
      TransitShedConstraintsBean constraints, Map<String, String> params) {

    ConstraintsBean c = constraints.getConstraints();

    params.put(PARAM_MAX_TRANSFERS, Integer.toString(c.getMaxTransfers()));
    params.put(PARAM_MAX_TRIP_LENGTH,
        Integer.toString(c.getMaxTripDuration() / 60));
    params.put(PARAM_MAX_WALKING_DISTANCE,
        Double.toString(c.getMaxWalkingDistance()));
  }

  public static void addTimeToParams(long time, Map<String, String> params) {
    params.put(PARAM_TIME, Long.toString(time));
  }

  public static void addParamsToConstraints(Context context,
      TransitShedConstraintsBean constraints) {

    ConstraintsBean c = constraints.getConstraints();

    String maxWalkingDistance = context.getParam(PARAM_MAX_WALKING_DISTANCE);
    if (maxWalkingDistance != null)
      c.setMaxWalkingDistance(Double.parseDouble(maxWalkingDistance));

    String maxTransfers = context.getParam(PARAM_MAX_TRANSFERS);
    if (maxTransfers != null)
      c.setMaxTransfers(Integer.parseInt(maxTransfers));

    String maxTripLength = context.getParam(PARAM_MAX_TRIP_LENGTH);
    if (maxTripLength != null)
      c.setMaxTripDuration(Integer.parseInt(maxTripLength) * 60);
  }

  public static long getParamsAsTime(Context context) {
    String minDepartureTimeValue = context.getParam(PARAM_TIME);
    if (minDepartureTimeValue != null) {
      return Long.parseLong(minDepartureTimeValue);
    }
    return 0;
  }
}
