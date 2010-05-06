package org.onebusaway.gtdf.serialization;

import org.onebusaway.gtdf.model.IdentityBean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class MemoryEntityStore implements GTDFEntityStore {

  private static Logger _log = Logger.getLogger(MemoryEntityStore.class.getName());

  private Map<Class<?>, Map<Object, Object>> _entitiesByClassAndId = new HashMap<Class<?>, Map<Object, Object>>();

  public void save(Object entity) {
    Class<?> c = entity.getClass();
    Map<Object, Object> byId = _entitiesByClassAndId.get(c);
    if (byId == null) {
      byId = new HashMap<Object, Object>();
      _entitiesByClassAndId.put(c, byId);
    }
    Object id = ((IdentityBean<?>) entity).getId();
    Object prev = byId.put(id, entity);
    if (prev != null)
      _log.warning("entity with id already exists: class=" + c + " id=" + id
          + " prev=" + prev + " new=" + entity);
  }

  public Object load(Class<?> entityClass, Serializable id) {

    Map<Object, Object> byId = _entitiesByClassAndId.get(entityClass);

    if (byId == null) {
      _log.warning("no stored entities type " + entityClass);
      return null;
    }

    Object entity = byId.get(id);

    if (entity == null)
      _log.warning("no stored entity with type " + entityClass + " and id "
          + id);

    return entity;
  }

  public void flush() {

  }
}
