package org.onebusaway.kcmetro2gtfs.calendar;

import org.onebusaway.gtfs.model.Route;
import org.onebusaway.kcmetro2gtfs.TranslationContext;

public interface RouteModificationsStrategy {
  public void modifyRoute(TranslationContext context, Route route);
}
