package org.onebusaway.gtfs.serialization;


import org.onebusaway.common.model.IdentityBean;
import org.onebusaway.common.services.ProjectionService;
import org.onebusaway.csv.CsvEntityContext;
import org.onebusaway.csv.CsvEntityReader;
import org.onebusaway.csv.EntityHandler;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.CalendarDate;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GtfsReader extends CsvEntityReader implements Runnable {

  public static final String KEY_CONTEXT = GtfsReader.class.getName()
      + ".context";

  private List<Class<?>> _entityClasses = new ArrayList<Class<?>>();

  private GtfsReaderContextImpl _context = new GtfsReaderContextImpl();

  private GtfsEntityStore _entityStore;

  private ProjectionService _projection;

  private List<Agency> _agencies = new ArrayList<Agency>();

  private String _feedId;

  public GtfsReader() {
    _entityClasses.add(Agency.class);
    _entityClasses.add(Route.class);
    _entityClasses.add(ShapePoint.class);
    _entityClasses.add(Stop.class);
    _entityClasses.add(Trip.class);
    _entityClasses.add(StopTime.class);
    _entityClasses.add(ServiceCalendar.class);
    _entityClasses.add(CalendarDate.class);
  }

  public void setFeedId(String feedId) {
    _feedId = feedId;
  }

  public void setEntityStore(GtfsEntityStore entityStore) {
    _entityStore = entityStore;
  }

  public void setProjectionService(ProjectionService projection) {
    _projection = projection;
  }

  public List<Class<?>> getEntityClasses() {
    return _entityClasses;
  }

  public void run() {

    if (_entityStore == null)
      _entityStore = new MemoryEntityStore();

    CsvEntityContext ctx = getContext();
    ctx.put(KEY_CONTEXT, _context);
    ctx.put(LocationFieldMappingFactory.PROJECTION_KEY, _projection);

    addEntityHandler(new EntityHandlerImpl());

    List<Class<?>> classes = getEntityClasses();

    for (Class<?> entityClass : classes) {
      System.out.println("====> " + entityClass);
      readEntities(entityClass);
    }

    _entityStore.flush();
  }

  private class EntityHandlerImpl implements EntityHandler {

    public void handleEntity(Object entity) {

      if (entity instanceof IdentityBean) {
        _entityStore.save(entity);
      }

      if (entity instanceof Agency)
        _agencies.add((Agency) entity);
    }
  }

  private class GtfsReaderContextImpl implements GtfsReaderContext {

    public Object getEntity(Class<?> entityClass, Serializable id) {
      if (entityClass == null)
        throw new IllegalArgumentException("entity class must not be null");
      if (id == null)
        throw new IllegalArgumentException("entity id must not be null");
      return _entityStore.load(entityClass, id);
    }

    public String getFeedId() {
      return _feedId;
    }

    public List<Agency> getAgencies() {
      return _agencies;
    }
  }
}
