package org.onebusaway.gtfs_diff;

import org.onebusaway.container.model.IdentityBean;
import org.onebusaway.gtfs.csv.CsvEntityWriter;
import org.onebusaway.gtfs.csv.EntityHandler;
import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.serialization.GtfsEntitySchemaFactory;
import org.onebusaway.gtfs.serialization.GtfsReader;

import edu.washington.cs.rse.collections.CollectionsLibrary;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GtfsPruneMain {

  public static void main(String[] args) throws IOException {

    if (args.length != 2) {
      System.err.println("usage: gtfsInputPath gtfsOutputPath");
      System.exit(-1);
    }

    GtfsPruneMain gtfsPruneMain = new GtfsPruneMain();
    gtfsPruneMain.setInputPath(new File(args[0]));
    gtfsPruneMain.setOutputPath(new File(args[1]));
    gtfsPruneMain.run();
  }

  private File _inputPath;

  private File _outputPath;

  private Set<Object> _includeEntities = new HashSet<Object>();

  private GtfsDaoImpl _entityStore = new GtfsDaoImpl();

  private Map<AgencyAndId, List<ServiceCalendar>> _serviceCalendarsByServiceId;

  private Map<AgencyAndId, List<ServiceCalendarDate>> _calendarDatesByServiceId;

  private Map<AgencyAndId, List<ShapePoint>> _shapePointsByShapeId;

  private int _totalEntityCount = 0;

  public void setInputPath(File inputPath) {
    _inputPath = inputPath;
  }

  public void setOutputPath(File outputPath) {
    _outputPath = outputPath;
  }

  public void run() {

    readGtfs();
    markIncludedEntities();
    writeGtfs();

    System.out.println("includedEntites=" + _includeEntities.size());
    System.out.println("totalEntites=" + _totalEntityCount);
  }

  private void readGtfs() {
    try {
      GtfsReader reader = new GtfsReader();
      reader.setInputLocation(_inputPath);
      reader.setEntityStore(_entityStore);
      reader.addEntityHandler(new SetGeneratedIdEntityHandler());
      reader.run();

      Map<Integer, ServiceCalendar> serviceCalendars = _entityStore.getEntitiesByIdForEntityType(
          Integer.class, ServiceCalendar.class);
      _serviceCalendarsByServiceId = CollectionsLibrary.mapToValueList(
          serviceCalendars.values(), "serviceId", AgencyAndId.class);

      Map<Integer, ServiceCalendarDate> calendarDates = _entityStore.getEntitiesByIdForEntityType(
          Integer.class, ServiceCalendarDate.class);
      _calendarDatesByServiceId = CollectionsLibrary.mapToValueList(
          calendarDates.values(), "serviceId", AgencyAndId.class);

      Map<Integer, ShapePoint> shapePoints = _entityStore.getEntitiesByIdForEntityType(
          Integer.class, ShapePoint.class);
      _shapePointsByShapeId = CollectionsLibrary.mapToValueList(
          shapePoints.values(), "shapeId", AgencyAndId.class);
    } catch (IOException ex) {
      throw new IllegalStateException(ex);
    }
  }

  private void writeGtfs() {

    CsvEntityWriter writer = new CsvEntityWriter();
    writer.setOutputLocation(_outputPath);
    writer.setEntitySchemaFactory(GtfsEntitySchemaFactory.createEntitySchemaFactory());

    Set<Class<?>> entityClasses = _entityStore.getEntityClasses();
    for (Class<?> entityClass : entityClasses) {
      System.out.println("<==== " + entityClass);
      int hits = 0;
      Map<Object, ?> entities = _entityStore.getEntitiesByIdForEntityType(
          Object.class, entityClass);
      for (Object entity : entities.values()) {
        if (_includeEntities.contains(entity)) {
          writer.handleEntity(entity);
          hits++;
        }
        _totalEntityCount++;
      }
      System.out.println("hits=" + hits);
      writer.flush();
    }

    writer.close();
  }

  private void markIncludedEntities() {

    Map<Integer, StopTime> stopTimes = _entityStore.getEntitiesByIdForEntityType(
        Integer.class, StopTime.class);

    System.out.println("stopTimes=" + stopTimes.size());

    for (StopTime stopTime : stopTimes.values()) {
      Trip trip = stopTime.getTrip();
      AgencyAndId serviceId = trip.getServiceId();
      if (serviceId.getId().startsWith("112"))
        includeEntity(stopTime);
    }
  }

  private void includeEntity(Object entity) {
    if (_includeEntities.add(entity)) {
      Set<Object> next = new HashSet<Object>();
      getTransitions(entity, next);
      for (Object o : next)
        includeEntity(o);
    }
  }

  private void getTransitions(Object object, Set<Object> transitions) {

    if (object instanceof StopTime) {

      StopTime stopTime = (StopTime) object;
      transitions.add(stopTime.getStop());
      transitions.add(stopTime.getTrip());

    } else if (object instanceof Trip) {

      Trip trip = (Trip) object;
      transitions.add(trip.getRoute());

      AgencyAndId serviceId = trip.getServiceId();

      List<ServiceCalendar> calendars = getCalendarsForServiceId(serviceId);
      if (calendars != null)
        transitions.addAll(calendars);

      List<ServiceCalendarDate> calendarDates = getCalendarDatesForServiceId(serviceId);
      if (calendarDates != null)
        transitions.addAll(calendarDates);

      AgencyAndId shapeId = trip.getShapeId();

      if (shapeId != null) {
        List<ShapePoint> shapePoints = getShapePointsForShapeId(shapeId);
        if (shapePoints != null)
          transitions.addAll(shapePoints);
      }

    } else if (object instanceof Route) {

      Route route = (Route) object;
      transitions.add(route.getAgency());
    }
  }

  private List<ServiceCalendar> getCalendarsForServiceId(AgencyAndId serviceId) {
    return _serviceCalendarsByServiceId.get(serviceId);
  }

  private List<ServiceCalendarDate> getCalendarDatesForServiceId(AgencyAndId serviceId) {
    return _calendarDatesByServiceId.get(serviceId);
  }

  private List<ShapePoint> getShapePointsForShapeId(AgencyAndId shapeId) {
    return _shapePointsByShapeId.get(shapeId);
  }

  private static class SetGeneratedIdEntityHandler implements EntityHandler {

    private int _index = 1;

    @SuppressWarnings("unchecked")
    public void handleEntity(Object bean) {
      if (bean instanceof StopTime || bean instanceof ServiceCalendarDate
          || bean instanceof ServiceCalendar) {
        IdentityBean<Integer> idBean = (IdentityBean<Integer>) bean;
        idBean.setId(_index++);
      }
    }
  }
}
