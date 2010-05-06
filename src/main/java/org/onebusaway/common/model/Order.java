package org.onebusaway.common.model;

public class Order<T> implements Comparable<Order<T>> {

  private T _object;

  private double _value;

  public static <T> Order<T> create(T object, double value) {
    return new Order<T>(object, value);
  }

  public Order(T object, double value) {
    _object = object;
    _value = value;
  }

  public T getObject() {
    return _object;
  }

  public double getValue() {
    return _value;
  }

  public int compareTo(Order<T> o) {
    return _value == o._value ? 0 : (_value < o._value ? -1 : 1);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof Order))
      return false;
    Order<?> o = (Order<?>) obj;
    return _object.equals(o._object) && _value == o._value;
  }

  @Override
  public int hashCode() {
    return _object.hashCode() + new Double(_value).hashCode();
  }
}
