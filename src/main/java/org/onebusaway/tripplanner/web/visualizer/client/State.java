/**
 * 
 */
package org.onebusaway.tripplanner.web.visualizer.client;

import com.google.gwt.i18n.client.DateTimeFormat;

import java.util.Date;
import java.util.Map;

public class State {

  private static DateTimeFormat _format = DateTimeFormat.getFormat("hh:mm:ss");

  private long _time;

  public void setTime(long time) {
    _time = time;
  }

  public double x;
  public double y;
  public double lat;
  public double lon;
  public String type;
  public Map<String, String> values;

  public String getLabel() {

    StringBuilder b = new StringBuilder();
    b.append(_format.format(new Date(_time)));
    b.append(" ");
    b.append(type);

    if (type.equals("waitingAtStop") || type.equals("walkFromStop") || type.equals("walkToStop")
        || type.equals("vehicleDeparture") || type.equals("vehicleArrival") || type.equals("vehicleContinuation"))
      b.append(" stop=").append(values.get("stop"));

    if (type.equals("vehicleDeparture") || type.equals("vehicleArrival") || type.equals("vehicleContinuation"))
      b.append(" route=").append(values.get("route"));

    b.append(" score=").append(values.get("score"));

    return b.toString();
  }
}