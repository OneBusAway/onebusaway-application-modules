package org.onebusaway.transit_data_federation.services.offline;

import java.io.Serializable;

import org.onebusaway.transit_data_federation.bundle.tasks.EntityReplacementStrategyFactory;

/**
 * Generic interface to support swapping out one entity with another. Use
 * primarily in GTFS loading to consolidate stops from GTFS feeds from separate
 * agencies that have stops in common.
 * 
 * @author bdferris
 * @see EntityReplacementStrategyFactory
 */
public interface EntityReplacementStrategy {

  /**
   * 
   * @param entityType
   * @return true if there are any entity replacements for classes of the
   *         specified type
   */
  public boolean hasReplacementEntities(Class<?> entityType);

  /**
   * 
   * @param entityType
   * @param id
   * @return true if there is an entity replacement for a entity with the
   *         specified type and id
   */
  public boolean hasReplacementEntity(Class<?> entityType, Serializable id);

  /**
   * 
   * @param entityType
   * @param id
   * @return the replacement entity id for the target entity of specified type
   *         and id, or null if no replacement is needed
   */
  public Serializable getReplacementEntityId(Class<?> entityType,
      Serializable id);
}
