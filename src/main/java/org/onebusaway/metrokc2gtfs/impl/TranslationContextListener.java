package org.onebusaway.metrokc2gtfs.impl;

public interface TranslationContextListener {
  public void onHandlerRegistered(Class<?> type, Object handler);
}
