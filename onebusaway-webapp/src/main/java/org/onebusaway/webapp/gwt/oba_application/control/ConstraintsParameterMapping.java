package org.onebusaway.webapp.gwt.oba_application.control;

import org.onebusaway.transit_data.model.oba.OneBusAwayConstraintsBean;
import org.onebusaway.webapp.gwt.common.context.Context;

import java.util.Map;

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
      OneBusAwayConstraintsBean constraints, Map<String, String> params) {

    long minDepartureTime = constraints.getMinDepartureTime();
    params.put(PARAM_TIME, Long.toString(minDepartureTime));

    params.put(PARAM_MAX_TRANSFERS,
        Integer.toString(constraints.getMaxTransfers()));
    params.put(PARAM_MAX_TRIP_LENGTH,
        Integer.toString(constraints.getMaxTripDuration()));
    params.put(PARAM_MAX_WALKING_DISTANCE,
        Double.toString(constraints.getMaxWalkingDistance()));

  }

  public static void addParamsToConstraints(Context context,
      OneBusAwayConstraintsBean constraints) {

    String minDepartureTimeValue = context.getParam(PARAM_TIME);
    if (minDepartureTimeValue != null) {
      long minDepartureTime = Long.parseLong(minDepartureTimeValue);
      constraints.setMinDepartureTime(minDepartureTime);
      constraints.setMaxDepartureTime(constraints.getMinDepartureTime() + 60 * 60 * 1000);
    }
    String maxWalkingDistance = context.getParam(PARAM_MAX_WALKING_DISTANCE);
    if (maxWalkingDistance != null)
      constraints.setMaxWalkingDistance(Double.parseDouble(maxWalkingDistance));

    String maxTransfers = context.getParam(PARAM_MAX_TRANSFERS);
    if (maxTransfers != null)
      constraints.setMaxTransfers(Integer.parseInt(maxTransfers));

    String maxTripLength = context.getParam(PARAM_MAX_TRIP_LENGTH);
    if (maxTripLength != null)
      constraints.setMaxTripDuration(Integer.parseInt(maxTripLength));
  }
}
