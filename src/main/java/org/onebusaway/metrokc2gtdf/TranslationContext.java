package org.onebusaway.metrokc2gtdf;

import org.onebusaway.common.services.ProjectionService;
import org.onebusaway.csv.CsvEntityContext;
import org.onebusaway.csv.CsvEntityWriter;
import org.onebusaway.metrokc2gtdf.calendar.CalendarManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TranslationContext {

  private MetroDao _dao;

  private ProjectionService _projection;

  private CalendarManager _calendarManager;

  private CsvEntityWriter _writer;

  private CsvEntityContext _readerContext;

  private Map<Class<?>, Object> _handlers = new HashMap<Class<?>, Object>();

  private LocationNamingStrategy _locationNamingStrategy;

  private List<String> _warnings = new ArrayList<String>();

  public MetroDao getDao() {
    return _dao;
  }

  public void setDao(MetroDao dao) {
    _dao = dao;
  }

  public ProjectionService getProjection() {
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

  public void addWarning(String message) {
    _warnings.add(message);
  }

  public List<String> getWarnings() {
    return _warnings;
  }
}
