/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.transit_data_federation.bundle.services;

import java.io.Serializable;

import org.onebusaway.transit_data_federation.bundle.tasks.EntityReplacementStrategyFactory;
import org.onebusaway.transit_data_federation.bundle.tasks.MultiCSVLogger;

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
   * optionally inject a logger for recording replacements
   * @param logger
   */
  public void setEntityReplacementLogger(EntityReplacementLogger logger);
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

  /**
   * Log that replacement has occurred.
   * @param id
   * @param originalEntity
   * @param replacementEntity
   */
  public <T> T logReplacement(Class<T> type, Serializable id, Serializable replacementId, 
      T originalEntity, T replacementEntity);
}
