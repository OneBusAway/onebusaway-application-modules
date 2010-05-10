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
  
  public ArrivalsAndDeparturesPresentaion() {
    
  }
  
  public ArrivalsAndDeparturesPresentaion(boolean useDefaultResources) {
    if( useDefaultResources ) {
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

  /**
   * Returns a text label like "4 minutes late" or "departed 2 minutes early"
   * 
   * @param pab
   * @return
   */
  public String getArrivalLabel(ArrivalAndDepartureBean pab) {

    if (CANCELLED.equals(pab.getStatus()))
      return "suspended";

    long now = System.currentTimeMillis();
    long predicted = pab.getPredictedArrivalTime();
    long scheduled = pab.getScheduledArrivalTime();

    if (predicted > 0) {

      double diff = ((pab.getPredictedArrivalTime() - pab.getScheduledArrivalTime()) / (1000.0 * 60));
      int minutes = (int) Math.abs(Math.round(diff));

      boolean departed = predicted < now;

      if (diff < -1.5) {
        return departed ? _messages.departedEarly(minutes)
            : _messages.early(minutes);
      } else if (diff < 1.5) {
        return departed ? _messages.departedOnTime() : _messages.onTime();
      } else {
        return departed ? _messages.departedLate(minutes)
            : _messages.delayed(minutes);
      }

    } else {
      if (scheduled < now)
        return _messages.scheduledDeparture();
      else
        return _messages.scheduledArrival();
    }
  }

  public String getArrivalStatusLabelStyle(ArrivalAndDepartureBean pab) {

    if (CANCELLED.equals(pab.getStatus()))
      return _css.arrivalStatusCancelled();

    long now = System.currentTimeMillis();
    long predicted = pab.getPredictedArrivalTime();
    long scheduled = pab.getScheduledArrivalTime();

    if (predicted > 0) {

      double diff = ((pab.getPredictedArrivalTime() - pab.getScheduledArrivalTime()) / (1000.0 * 60));

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

  public String getMinutesLabel(ArrivalAndDepartureBean pab) {
    
    long now = System.currentTimeMillis();

    if (CANCELLED.equals(pab.getStatus()))
      return "-";

    boolean isNow = isArrivalNow(pab);
    long t = getBestArrivalTime(pab);
    int minutes = (int) Math.round((t - now) / (1000.0 * 60.0));
    return isNow ? "NOW" : Integer.toString(minutes);
  }

  public boolean isArrivalNow(ArrivalAndDepartureBean pab) {
    if (CANCELLED.equals(pab.getStatus()))
      return true;
    long now = System.currentTimeMillis();
    long t = getBestArrivalTime(pab);
    int minutes = (int) Math.round((t - now) / (1000.0 * 60.0));
    return Math.abs(minutes) <= 1;
  }

  private long getBestArrivalTime(ArrivalAndDepartureBean pab) {
    long t = pab.getScheduledArrivalTime();
    if (pab.hasPredictedArrivalTime())
      t = pab.getPredictedArrivalTime();
    return t;
  }
  
  public boolean isLongRouteName(RouteBean route) {
    String name = RoutePresenter.getNameForRoute(route);
    return RoutePresenter.isRouteNameLong(name);
  }
}
