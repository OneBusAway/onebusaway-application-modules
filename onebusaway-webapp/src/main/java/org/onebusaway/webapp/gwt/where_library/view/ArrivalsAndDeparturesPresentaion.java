package org.onebusaway.webapp.gwt.where_library.view;

import org.onebusaway.presentation.client.RoutePresenter;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.webapp.gwt.where_library.WhereLibrary;
import org.onebusaway.webapp.gwt.where_library.WhereMessages;
import org.onebusaway.webapp.gwt.where_library.resources.WhereLibraryCssResource;

import com.google.gwt.core.client.GWT;

public class ArrivalsAndDeparturesPresentaion {

  private static final String CANCELLED = "cancelled";

  private WhereLibraryCssResource _css;

  private WhereMessages _messages;

  private boolean _showArrivals = false;

  public ArrivalsAndDeparturesPresentaion() {

  }

  public ArrivalsAndDeparturesPresentaion(boolean useDefaultResources) {
    if (useDefaultResources) {
      _css = WhereLibrary.INSTANCE.getCss();
      _messages = GWT.create(WhereMessages.class);
    }
  }

  public void setMessages(WhereMessages messages) {
    _messages = messages;
  }

  public void setCss(WhereLibraryCssResource css) {
    _css = css;
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

    long now = System.currentTimeMillis();
    long predicted = getPredictedTime(pab);
    long scheduled = getScheduledTime(pab);

    if (predicted > 0) {

      double diff = ((predicted - scheduled) / (1000.0 * 60));
      int minutes = (int) Math.abs(Math.round(diff));

      boolean pastTense = predicted < now;

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
      return _css.arrivalStatusCancelled();

    long now = System.currentTimeMillis();
    long predicted = getPredictedTime(pab);
    long scheduled = getScheduledTime(pab);

    if (predicted > 0) {

      double diff = ((predicted - scheduled) / (1000.0 * 60));

      if (predicted < now) {

        if (diff < -1.5) {
          return _css.arrivalStatusDepartedEarly();
        } else if (diff < 1.5) {
          return _css.arrivalStatusDepartedOnTime();
        } else {
          return _css.arrivalStatusDepartedDelayed();
        }
      } else {
        if (diff < -1.5) {
          return _css.arrivalStatusEarly();
        } else if (diff < 1.5) {
          return _css.arrivalStatusOnTime();
        } else {
          return _css.arrivalStatusDelayed();
        }
      }

    } else {
      if (scheduled < now)
        return _css.arrivalStatusDepartedNoInfo();
      else
        return _css.arrivalStatusNoInfo();
    }
  }

  public long getBestTime(ArrivalAndDepartureBean pab) {
    long t = getScheduledTime(pab);
    if (hasPredictedTime(pab))
      t = getPredictedTime(pab);
    return t;
  }

  public String getMinutesLabel(ArrivalAndDepartureBean pab) {

    long now = System.currentTimeMillis();

    if (CANCELLED.equals(pab.getStatus()))
      return "-";

    boolean isNow = isNow(pab);
    long t = getBestTime(pab);
    int minutes = (int) Math.round((t - now) / (1000.0 * 60.0));
    return isNow ? "NOW" : Integer.toString(minutes);
  }

  public boolean isNow(ArrivalAndDepartureBean pab) {
    if (CANCELLED.equals(pab.getStatus()))
      return true;
    long now = System.currentTimeMillis();
    long t = getBestTime(pab);
    int minutes = (int) Math.round((t - now) / (1000.0 * 60.0));
    return Math.abs(minutes) <= 1;
  }

  public boolean isLongRouteName(RouteBean route) {
    String name = RoutePresenter.getNameForRoute(route);
    return RoutePresenter.isRouteNameLong(name);
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

}
