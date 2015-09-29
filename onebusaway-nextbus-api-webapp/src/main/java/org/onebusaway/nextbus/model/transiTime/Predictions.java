package org.onebusaway.nextbus.model.transiTime;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.nextbus.model.nextbus.ScheduleTableRow;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("predictions")
public class Predictions {
  
  @XStreamAsAttribute
  @XStreamAlias("routeTag")
  private String routeId;
  
  @XStreamAsAttribute
  @XStreamAlias("routeCode")
  private String routeShortName;
  
  @XStreamAsAttribute
  @XStreamAlias("routeTitle")
  private String routeName;
  
  @XStreamAsAttribute
  @XStreamAlias("stopTitle")
  private String stopName;
  
  @XStreamImplicit
  private List<PredictionsDirection> dest;
  
  public Predictions(){}
  
  public String getRouteId() {
    return routeId;
  }

  public void setRouteId(String routeId) {
    this.routeId = routeId;
  }

  public String getRouteName() {
    return routeName;
  }

  public void setRouteName(String routeName) {
    this.routeName = routeName;
  }

  public String getStopName() {
    return stopName;
  }

  public void setStopName(String stopName) {
    this.stopName = stopName;
  }

  public String getRouteShortName(){
    return routeShortName;
  }

  public void setRouteShortName(String routeShortName) {
    this.routeShortName = routeShortName;
  }

  public List<PredictionsDirection> getDest() {
    return dest;
  }

  public void setDest(List<PredictionsDirection> dest) {
    this.dest = dest;
  }


}

