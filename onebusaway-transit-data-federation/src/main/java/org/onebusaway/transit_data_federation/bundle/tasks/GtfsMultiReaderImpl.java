package org.onebusaway.transit_data_federation.bundle.tasks;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.collections.Counter;
import org.onebusaway.gtfs.csv.EntityHandler;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.services.GenericMutableDao;
import org.onebusaway.transit_data_federation.bundle.services.EntityReplacementStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Supports reading from multiple {@link GtfsReader} instances sequentially with
 * respect to GTFS entity classes. That is to say, given three readers A, B, and
 * C, all {@link Agency} entities will be read from A, B, and C in turn, and
 * then all {@link ShapePoint} entities will be read from A, B, and C in turn,
 * and so forth. This sequential reading scheme allows for cases where two
 * separate feeds may have cross-feed references (ex. StopTime => Stop) as
 * facilitated by the use of an {@link EntityReplacementStrategy}.
 * 
 * @author bdferris
 * @see EntityReplacementStrategy
 * @see GtfsReader
 * @see GtfsReadingSupport
 */
public class GtfsMultiReaderImpl implements Runnable {

  private final Logger _log = LoggerFactory.getLogger(GtfsMultiReaderImpl.class);

  private List<GtfsReader> _readers = new ArrayList<GtfsReader>();

  private GenericMutableDao _store;

  private EntityReplacementStrategy _entityReplacementStrategy = new EntityReplacementStrategyImpl();

  public void setStore(GenericMutableDao store) {
    _store = store;
  }

  public void setEntityReplacementStrategy(EntityReplacementStrategy strategy) {
    _entityReplacementStrategy = strategy;
  }

  public void addGtfsReader(GtfsReader reader) {
    _readers.add(reader);
  }

  public void addGtfsReaderFromInputLocation(File inputLocation)
      throws IOException {
    GtfsReader reader = new GtfsReader();
    reader.setInputLocation(inputLocation);
    addGtfsReader(reader);
  }

  @Override
  public void run() {

    if (_readers.isEmpty())
      return;

    try {

      StoreImpl store = new StoreImpl(_store);

      for (GtfsReader reader : _readers) {
        reader.setEntityStore(store);
        reader.addEntityHandler(new EntityCounter());
      }

      store.open();

      List<Agency> agencies = new ArrayList<Agency>();
      List<Class<?>> entityClasses = _readers.get(0).getEntityClasses();

      for (Class<?> entityClass : entityClasses) {
        _log.info("reading entities: " + entityClass.getName());

        for (GtfsReader reader : _readers) {

          // Pre-load the agencies, since one agency can be mentioned across
          // multiple feeds
          if (entityClass.equals(Agency.class))
            reader.setAgencies(agencies);

          reader.readEntities(entityClass);

          if (entityClass.equals(Agency.class))
            agencies.addAll(reader.getAgencies());

          store.flush();
        }
      }

      store.close();

    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  /****
   * Private Methods
   ****/

  /**
   * Custom {@link GenericMutableDao} instance that intercepts methods so that
   * the entity replacement strategy can be applied.
   */
  private class StoreImpl extends GenericMutableDaoWrapper {

    public StoreImpl(GenericMutableDao source) {
      super(source);
    }

    @Override
    public <T> T getEntityForId(Class<T> type, Serializable id) {

      Serializable replacementId = _entityReplacementStrategy.getReplacementEntityId(
          type, id);

      if (replacementId != null) {

        T entity = super.getEntityForId(type, replacementId);

        if (entity != null)
          return entity;

        _log.warn("error replacing entity: type=" + type.getName() + " fromId="
            + id + " toId=" + replacementId + " - replacement not found");
      }
      return super.getEntityForId(type, id);
    }

    @Override
    public void saveEntity(Object entity) {

      Class<? extends Object> entityType = entity.getClass();
      if (entity instanceof IdentityBean<?>
          && _entityReplacementStrategy.hasReplacementEntities(entityType)) {
        IdentityBean<?> bean = (IdentityBean<?>) entity;
        Serializable id = bean.getId();
        if (_entityReplacementStrategy.hasReplacementEntity(entityType, id))
          return;
      }

      super.saveEntity(entity);
    }
  }

  private static class EntityCounter implements EntityHandler {

    private Counter<String> _counter = new Counter<String>();

    private Map<String, Long> _startTime = new HashMap<String, Long>();

    public void handleEntity(Object bean) {
      String name = bean.getClass().getName();
      int index = name.lastIndexOf('.');
      if (index != -1)
        name = name.substring(index + 1);
      increment(name);
    }

    private void increment(String key) {
      _counter.increment(key);
      int c = _counter.getCount(key);
      if (c % 1000 == 0) {
        double ellapsedTime = (System.currentTimeMillis() - getStartTimeForKey(key)) / 1000.0;
        System.out.println(key + " = " + c + " rate="
            + ((long) (c / ellapsedTime)));
      }
    }

    private long getStartTimeForKey(String key) {
      Long value = _startTime.get(key);
      if (value == null) {
        value = System.currentTimeMillis();
        _startTime.put(key, value);
      }
      return value;
    }
  }
}
