package org.onebusaway.transit_data_federation.services;

import java.util.List;

/**
 * Extension of {@link TransitDataFederationDao} that supports methods for
 * insertion, updates, and deletes of objects. For the most part, resources are
 * immutable in a transit data federation bundle after insert data upload. We
 * break out a separate interface just for mutable objects so that they can be
 * kept in a separate datastore.
 * 
 * See the resource:
 * 
 * <code>org/onebusaway/transit_data_federation/appication-context-hibernate-mutable.xml</code>
 * 
 * for more info
 * 
 * @author bdferris
 */
public interface TransitDataFederationMutableDao extends
    TransitDataFederationDao {

  public void save(Object object);

  public void update(Object object);

  public void saveOrUpdate(Object object);

  public <T> void saveOrUpdateAllEntities(List<T> updates);

  public <T> void deleteAllEntities(Iterable<T> entities);

  public void flush();
}
