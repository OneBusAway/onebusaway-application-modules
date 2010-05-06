package org.onebusaway.gtdf.serialization;


import org.onebusaway.common.services.ProjectionService;
import org.onebusaway.csv.CsvEntityContext;
import org.onebusaway.csv.CsvEntityReader;
import org.onebusaway.csv.EntityHandler;
import org.onebusaway.gtdf.model.Agency;
import org.onebusaway.gtdf.model.CalendarDate;
import org.onebusaway.gtdf.model.IdentityBean;
import org.onebusaway.gtdf.model.Route;
import org.onebusaway.gtdf.model.ServiceCalendar;
import org.onebusaway.gtdf.model.ShapePoint;
import org.onebusaway.gtdf.model.Stop;
import org.onebusaway.gtdf.model.StopTime;
import org.onebusaway.gtdf.model.Trip;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GTDFReader extends CsvEntityReader implements Runnable {

  public static final String KEY_CONTEXT = GTDFReader.class.getName()
      + ".context";

  private List<Class<?>> _entityClasses = new ArrayList<Class<?>>();

  private GTDFReaderContextImpl _context = new GTDFReaderContextImpl();

  private GTDFEntityStore _entityStore;

  private ProjectionService _projection;

  private List<Agency> _agencies = new ArrayList<Agency>();

  private String _feedId;

  public GTDFReader() {
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

  public void setEntityStore(GTDFEntityStore entityStore) {
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

  private class GTDFReaderContextImpl implements GTDFReaderContext {

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
