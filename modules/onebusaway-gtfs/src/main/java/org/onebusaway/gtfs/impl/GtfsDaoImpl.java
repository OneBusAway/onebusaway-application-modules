package org.onebusaway.gtfs.impl;

import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.FareAttribute;
import org.onebusaway.gtfs.model.FareRule;
import org.onebusaway.gtfs.model.Frequency;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Transfer;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.serialization.GtfsEntityStore;
import org.onebusaway.gtfs.serialization.GtfsReaderContext;
import org.onebusaway.gtfs.services.GtfsDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GtfsDaoImpl implements GtfsDao, GtfsEntityStore {

  private final Logger _log = LoggerFactory.getLogger(GtfsDaoImpl.class);

  private Map<Class<?>, Map<Object, Object>> _entitiesByClassAndId = new HashMap<Class<?>, Map<Object, Object>>();

  private Map<Class<?>, EntityHandler<Serializable>> _handlers = new HashMap<Class<?>, EntityHandler<Serializable>>();

  private boolean _generateIds = true;

  public void setGenerateIds(boolean generateIds) {
    _generateIds = generateIds;
  }

  public Set<Class<?>> getEntityClasses() {
    return _entitiesByClassAndId.keySet();
  }

  @SuppressWarnings("unchecked")
  public <K, V> Map<K, V> getEntitiesByIdForEntityType(Class<K> keyType,
      Class<V> entityType) {
    return (Map<K, V>) _entitiesByClassAndId.get(entityType);
  }

  /***
   * {@link GtfsDao} Interface
   ****/

  public Agency getAgencyForId(String id) {
    return get(Agency.class,id);
  }

  public Collection<Agency> getAllAgencies() {
    return getAll(Agency.class);
  }

  public Collection<ServiceCalendarDate> getAllCalendarDates() {
    return getAll(ServiceCalendarDate.class);
  }

  public Collection<ServiceCalendar> getAllCalendars() {
    return getAll(ServiceCalendar.class);
  }
  
  public Collection<FareAttribute> getAllFareAttributes() {
    return getAll(FareAttribute.class);
  }
  
  public Collection<FareRule> getAllFareRules() {
    return getAll(FareRule.class);
  }
  
  public Collection<Frequency> getAllFrequencies() {
    return getAll(Frequency.class);
  }

  public Collection<Route> getAllRoutes() {
    return getAll(Route.class);
  }

  public Collection<ShapePoint> getAllShapePoints() {
    return getAll(ShapePoint.class);
  }

  public Collection<StopTime> getAllStopTimes() {
    return getAll(StopTime.class);
  }

  public Collection<Stop> getAllStops() {
    return getAll(Stop.class);
  }
  
  public Collection<Transfer> getAllTransfers() {
    return getAll(Transfer.class);
  }

  public Collection<Trip> getAllTrips() {
    return getAll(Trip.class);
  }

  public ServiceCalendarDate getCalendarDateForId(int id) {
    return get(ServiceCalendarDate.class,id);
  }

  public ServiceCalendar getCalendarForId(int id) {
    return get(ServiceCalendar.class,id);
  }
  
  public FareAttribute getFareAttributeForId(AgencyAndId id) {
    return get(FareAttribute.class,id);
  }
  
  public FareRule getFareRuleForId(int id) {
    return get(FareRule.class,id);
  }
  
  public Frequency getFrequencyForId(int id) {
    return get(Frequency.class,id);
  }

  public Route getRouteForId(AgencyAndId id) {
    return get(Route.class,id);
  }

  public ShapePoint getShapePointForId(int id) {
    return get(ShapePoint.class,id);
  }

  public Stop getStopForId(AgencyAndId id) {
    return get(Stop.class,id);
  }

  public StopTime getStopTimeForId(int id) {
    return get(StopTime.class,id);
  }
  
  public Transfer getTransferForId(int id) {
    return get(Transfer.class,id);
  }

  public Trip getTripForId(AgencyAndId id) {
    return get(Trip.class,id);
  }

  /****
   * {@link GtfsEntityStore} Interface
   ****/

  @SuppressWarnings("unchecked")
  public void save(GtfsReaderContext context, Object entity) {
    Class<?> c = entity.getClass();

    EntityHandler<Serializable> handler = _handlers.get(c);
    if (handler == null) {
      handler = (EntityHandler<Serializable>) createEntityHandler(c);
      _handlers.put(c, handler);
    }

    IdentityBean<Serializable> bean = ((IdentityBean<Serializable>) entity);
    handler.handle(bean);

    Map<Object, Object> byId = _entitiesByClassAndId.get(c);
    if (byId == null) {
      byId = new HashMap<Object, Object>();
      _entitiesByClassAndId.put(c, byId);
    }
    Object id = bean.getId();
    Object prev = byId.put(id, entity);
    if (prev != null)
      _log.warn("entity with id already exists: class=" + c + " id=" + id
          + " prev=" + prev + " new=" + entity);
  }

  public Object load(Class<?> entityClass, Serializable id) {

    Map<Object, Object> byId = _entitiesByClassAndId.get(entityClass);

    if (byId == null) {
      _log.warn("no stored entities type " + entityClass);
      return null;
    }

    Object entity = byId.get(id);

    if (entity == null)
      _log.warn("no stored entity with type " + entityClass + " and id "
          + id);

    return entity;
  }

  public void flush() {

  }

  /****
   * Private Methods
   ****/
  
  @SuppressWarnings("unchecked")
  private <S extends Serializable, T extends IdentityBean<S>> T get(Class<T> type, S id) {
    return (T) load(type,id);
  }
  
  @SuppressWarnings("unchecked")
  private <T> Collection<T> getAll(Class<T> type) {
    return (Collection<T>) _entitiesByClassAndId.get(type).values();
  }

  private EntityHandler<?> createEntityHandler(Class<?> entityType) {

    if (_generateIds) {
      try {
        Field field = entityType.getDeclaredField("id");
        if (field != null) {
          Class<?> type = field.getType();
          if (type.equals(Integer.class) || type.equals(Integer.TYPE))
            return new GeneratedIdHandler();
        }
      } catch (Exception ex) {

      }
    }

    return new EntityHandler<Serializable>() {
      public void handle(IdentityBean<Serializable> entity) {
      }
    };
  }

  private interface EntityHandler<T extends Serializable> {
    public void handle(IdentityBean<T> entity);
  }

  private static class GeneratedIdHandler implements EntityHandler<Integer> {

    private int _maxId = 0;

    public void handle(IdentityBean<Integer> entity) {
      Integer value = (Integer) entity.getId();
      if (value == null || value.intValue() == 0) {
        value = _maxId + 1;
        entity.setId(value);
      }
      _maxId = Math.max(_maxId, value.intValue());
    }
  }
}
