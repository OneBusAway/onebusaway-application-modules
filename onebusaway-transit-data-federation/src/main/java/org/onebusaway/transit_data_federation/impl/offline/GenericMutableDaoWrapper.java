package org.onebusaway.transit_data_federation.impl.offline;

import java.io.Serializable;
import java.util.Collection;

import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.gtfs.services.GenericMutableDao;

/**
 * Support class that provides an implementation of {@link GenericMutableDao}
 * where all methods calls are passed to an underlying wrapped instance of
 * {@link GenericMutableDao}. Useful for when you want to selectively override
 * the behavior of individual {@link GenericMutableDao} methods of an existing
 * instance.
 * 
 * @author bdferris
 * 
 */
public class GenericMutableDaoWrapper implements GenericMutableDao {

  protected GenericMutableDao _source;

  public GenericMutableDaoWrapper(GenericMutableDao source) {
    _source = source;
  }

  @Override
  public <T> void clearAllEntitiesForType(Class<T> type) {
    _source.clearAllEntitiesForType(type);
  }

  @Override
  public void close() {
    _source.close();
  }

  @Override
  public void flush() {
    _source.flush();
  }

  @Override
  public void open() {
    _source.open();
  }

  @Override
  public <K extends Serializable, T extends IdentityBean<K>> void removeEntity(
      T entity) {
    _source.removeEntity(entity);
  }

  @Override
  public void saveEntity(Object entity) {
    _source.saveEntity(entity);
  }

  @Override
  public <T> Collection<T> getAllEntitiesForType(Class<T> type) {
    return _source.getAllEntitiesForType(type);
  }

  @Override
  public <T> T getEntityForId(Class<T> type, Serializable id) {
    return _source.getEntityForId(type, id);
  }
}
