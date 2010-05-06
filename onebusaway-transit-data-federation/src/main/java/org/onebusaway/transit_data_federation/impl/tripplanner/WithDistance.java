package org.onebusaway.transit_data_federation.impl.tripplanner;

import java.io.Serializable;

public class WithDistance<T> implements Serializable {

  private static final long serialVersionUID = 1L;

  private T _value;

  private double _distance;
  
  public static <T> WithDistance<T> create(T value, double distance) {
    return new WithDistance<T>(value,distance);
  }

  public WithDistance(T value, double distance) {
    _value = value;
    _distance = distance;
  }
  
  public T getValue() {
    return _value;
  }
  
  public double getDistance() {
    return _distance;
  }
}
