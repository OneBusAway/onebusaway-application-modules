package org.onebusaway.kcmetro2gtfs;

import org.onebusaway.gtfs.csv.CsvEntityContext;
import org.onebusaway.gtfs.csv.CsvEntityWriter;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.kcmetro2gtfs.calendar.CalendarManager;
import org.onebusaway.kcmetro2gtfs.calendar.RouteModificationsStrategy;
import org.onebusaway.kcmetro2gtfs.impl.LocationNamingStrategy;
import org.onebusaway.kcmetro2gtfs.impl.MetroDao;
import org.onebusaway.kcmetro2gtfs.impl.TranslationContextListener;
import org.onebusaway.kcmetro2gtfs.services.ProjectionService;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TranslationContext {

  private String _agencyId;

  private List<TranslationContextListener> _listeners = new ArrayList<TranslationContextListener>();

  private MetroDao _dao;

  private ProjectionService _projection;

  private CalendarManager _calendarManager;

  private CsvEntityWriter _writer;

  private CsvEntityContext _readerContext;

  private Map<Class<?>, Object> _handlers = new HashMap<Class<?>, Object>();

  private LocationNamingStrategy _locationNamingStrategy;

  private List<String> _warnings = new ArrayList<String>();

  private RouteModificationsStrategy _routeModifications;

  private Map<String, Agency> _agencies = new HashMap<String, Agency>();

  private File _warningOutputFile;

  public void addContextListener(TranslationContextListener listener) {
    _listeners.add(listener);
  }

  public String getAgencyId() {
    return _agencyId;
  }

  public void setAgencyId(String agencyId) {
    _agencyId = agencyId;
  }

  public MetroDao getDao() {
    return _dao;
  }

  public void setDao(MetroDao dao) {
    _dao = dao;
  }

  public ProjectionService getProjectionService() {
    return _projection;
  }

  public void setProjection(ProjectionService projection) {
    _projection = projection;
  }

  public CalendarManager getCalendarManager() {
    return _calendarManager;
  }

  public void setCalendarManager(CalendarManager calendarManager) {
    _calendarManager = calendarManager;
  }

  public CsvEntityContext getReaderContext() {
    return _readerContext;
  }

  public void setReaderContext(CsvEntityContext readerContext) {
    _readerContext = readerContext;
  }

  public CsvEntityWriter getWriter() {
    return _writer;
  }

  public void setWriter(CsvEntityWriter writer) {
    _writer = writer;
  }

  public void putHandler(Object handler) {
    _handlers.put(handler.getClass(), handler);
    for (TranslationContextListener listener : _listeners)
      listener.onHandlerRegistered(handler.getClass(), handler);
  }

  @SuppressWarnings("unchecked")
  public <T> T getHandler(Class<T> handlerType) {
    return (T) _handlers.get(handlerType);
  }

  public void setLocationNamingStrategy(LocationNamingStrategy strategy) {
    _locationNamingStrategy = strategy;
  }

  public LocationNamingStrategy getLocationNamingStrategy() {
    return _locationNamingStrategy;
  }

  public void addAgency(Agency a) {
    _agencies.put(a.getId(), a);
  }

  public void addAgencies(List<Agency> agencies) {
    for (Agency agency : agencies)
      addAgency(agency);
  }

  public Agency getAgencyForId(String agencyId) {
    return _agencies.get(agencyId);
  }

  public Collection<Agency> getAgencies() {
    return _agencies.values();
  }

  public void addWarning(String message) {
    _warnings.add(message);
  }

  public List<String> getWarnings() {
    return _warnings;
  }

  public void setRouteModifications(RouteModificationsStrategy modifications) {
    _routeModifications = modifications;
  }

  public RouteModificationsStrategy getRouteModifications() {
    return _routeModifications;
  }

  public void setWarningOutputFile(File output) {
    _warningOutputFile = output;
  }

  public File getWarningOutputFile() {
    return _warningOutputFile;
  }
}
