package org.onebusaway.presentation.impl.struts;

class Prefixed<T> {
  private final String _prefix;
  private final T _value;

  public Prefixed(String prefix, T value) {
    _prefix = prefix;
    _value = value;
  }

  public String getPrefix() {
    return _prefix;
  }

  public T getValue() {
    return _value;
  }
}
