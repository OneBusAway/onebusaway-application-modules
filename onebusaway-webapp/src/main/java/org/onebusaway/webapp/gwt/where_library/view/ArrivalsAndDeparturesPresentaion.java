package org.onebusaway.webapp.gwt.where_library.view;

import org.onebusaway.presentation.client.RoutePresenter;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.schedule.FrequencyBean;
import org.onebusaway.webapp.actions.bundles.ArrivalAndDepartureMessages;

import com.google.gwt.core.client.GWT;

public class ArrivalsAndDeparturesPresentaion {

  private static final String CANCELLED = "cancelled";

  private ArrivalAndDepartureMessages _messages;
  
  private long _time = System.currentTimeMillis();

  private boolean _showArrivals = false;

  public ArrivalsAndDeparturesPresentaion() {

  }

  public ArrivalsAndDeparturesPresentaion(boolean useDefaultResources) {
    if (useDefaultResources) {
      _messages = GWT.create(ArrivalAndDepartureMessages.class);
    }
  }

  public void setMessages(ArrivalAndDepartureMessages messages) {
    _messages = messages;
  }
  
  public void setTime(long time){
    _time = time;
  }

  public void setShowArrivals(boolean showArrivals) {
    _showArrivals = showArrivals;
  }

  public boolean isShowArrivals() {
    return _showArrivals;
  }

  /**
   * Returns a text label like "4 minutes late" or "departed 2 minutes early"
   * 
   * @param pab
   * @return
   */
  public String getStatusLabel(ArrivalAndDepartureBean pab) {

    if (CANCELLED.equals(pab.getStatus()))
      return "suspended";

    long predicted = getPredictedTime(pab);
    long scheduled = getScheduledTime(pab);

    if (predicted > 0) {

      double diff = ((predicted - scheduled) / (1000.0 * 60));
      int minutes = (int) Math.abs(Math.round(diff));

      boolean pastTense = predicted < _time;

      if (diff < -1.5) {
        if (pastTense)
          return _showArrivals ? _messages.arrivedEarly(minutes)
              : _messages.departedEarly(minutes);
        else
          return _messages.early(minutes);
      } else if (diff < 1.5) {
        if (pastTense)
          return _showArrivals ? _messages.arrivedOnTime()
              : _messages.departedOnTime();
        else
          return _messages.onTime();
      } else {
        if (pastTense)
          return _showArrivals ? _messages.arrivedLate(minutes)
              : _messages.departedLate(minutes);
        else
          return _messages.delayed(minutes);
      }

    } else {
      if (_showArrivals)
        return _messages.scheduledArrival();
      else
        return _messages.scheduledDeparture();
    }
  }

  public String getStatusLabelStyle(ArrivalAndDepartureBean pab) {

    if (CANCELLED.equals(pab.getStatus()))
      return "arrivalStatusCancelled";

    long predicted = getPredictedTime(pab);
    long scheduled = getScheduledTime(pab);

    if (predicted > 0) {

      double diff = ((predicted - scheduled) / (1000.0 * 60));

      if (predicted < _time) {

        if (diff < -1.5) {
          return "arrivalStatusDepartedEarly";
        } else if (diff < 1.5) {
          return "arrivalStatusDepartedOnTime";
        } else {
          return "arrivalStatusDepartedDelayed";
        }
      } else {
        if (diff < -1.5) {
          return "arrivalStatusEarly";
        } else if (diff < 1.5) {
          return "arrivalStatusOnTime";
        } else {
          return "arrivalStatusDelayed";
        }
      }

    } else {
      if (scheduled < _time)
        return "arrivalStatusDepartedNoInfo";
      else
        return "arrivalStatusNoInfo";
    }
  }

  public long getBestTime(ArrivalAndDepartureBean pab) {
    long t = getScheduledTime(pab);
    if (hasPredictedTime(pab))
      t = getPredictedTime(pab);
    return t;
  }
  
  public String getMinutesElementId(ArrivalAndDepartureBean pab) {
    StringBuilder b = new StringBuilder();
    b.append("stopId_");
    b.append(escapeId(pab.getStop().getId()));
    b.append("-tripId_");
    b.append(escapeId(pab.getTrip().getId()));
    if( pab.getVehicleId() != null) {
      b.append("-vehicleId_");
      b.append(escapeId(pab.getVehicleId()));
    }
    
    return b.toString();
  }

  public String getMinutesLabel(ArrivalAndDepartureBean pab) {

    if (CANCELLED.equals(pab.getStatus()))
      return "-";

    boolean isNow = isNow(pab);
    long t = getBestTime(pab);
    int minutes = (int) Math.round((t - _time) / (1000.0 * 60.0));
    return isNow ? "NOW" : Integer.toString(minutes);
  }

  public boolean isNow(ArrivalAndDepartureBean pab) {
    if (CANCELLED.equals(pab.getStatus()))
      return true;
    long t = getBestTime(pab);
    int minutes = (int) Math.round((t - _time) / (1000.0 * 60.0));
    return Math.abs(minutes) <= 1;
  }

  public boolean isLongRouteName(RouteBean route) {
    String name = RoutePresenter.getNameForRoute(route);
    return RoutePresenter.isRouteNameLong(name);
  }
  
  public boolean isShowFrequencyFrom(FrequencyBean frequency) { 
    return frequency.getStartTime() > _time;
  }

  /****
   * Private Methods
   ****/

  private boolean hasPredictedTime(ArrivalAndDepartureBean pab) {
    return _showArrivals ? pab.hasPredictedArrivalTime()
        : pab.hasPredictedDepartureTime();
  }

  private long getPredictedTime(ArrivalAndDepartureBean pab) {
    return _showArrivals ? pab.getPredictedArrivalTime()
        : pab.getPredictedDepartureTime();
  }

  private long getScheduledTime(ArrivalAndDepartureBean pab) {
    return _showArrivals ? pab.getScheduledArrivalTime()
        : pab.getScheduledDepartureTime();
  }
  
  private String escapeId(String value) {
    value = value.replaceAll(" ", "_");
    return value;
  }

}
