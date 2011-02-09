package org.onebusaway.webapp.gwt.common;

import java.util.ArrayList;
import java.util.List;

public class ExceptionSupport {

  private static List<ExceptionListener> _listeners = new ArrayList<ExceptionListener>();

  public static void addExceptionListener(ExceptionListener listener) {
    _listeners.add(listener);
  }

  public static void removeExceptionListener(ExceptionListener listener) {
    _listeners.remove(listener);
  }

  public static void handleException(Throwable ex) {
    for (ExceptionListener listener : _listeners)
      listener.onException(ex);
  }

}
