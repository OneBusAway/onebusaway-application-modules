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
package org.onebusaway.transit_data_federation.bundle.tasks;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.onebusaway.transit_data_federation.bundle.services.EntityReplacementLogger;
import org.onebusaway.transit_data_federation.bundle.services.EntityReplacementStrategy;

/**
 * {@link EntityReplacementStrategy} implementation that is used to map entity
 * ids for a particular type.
 * 
 * @author bdferris
 * @see EntityReplacementStrategy
 * @see EntityReplacementStrategyFactory
 */
public class EntityReplacementStrategyImpl implements EntityReplacementStrategy {

  private Map<Class<?>, Map<Serializable, Serializable>> _entityReplacement = new HashMap<Class<?>, Map<Serializable, Serializable>>();

  private EntityReplacementLogger _entityLogger = null;
  
  @Override
  public void setEntityReplacementLogger(EntityReplacementLogger logger) {
    _entityLogger = logger;
    
  }
  
  public void addEntityReplacement(Class<?> entityType, Serializable entityId,
      Serializable replacementEntityId) {
    Map<Serializable, Serializable> idMappings = _entityReplacement.get(entityType);
    if (idMappings == null) {
      idMappings = new HashMap<Serializable, Serializable>();
      _entityReplacement.put(entityType, idMappings);
    }
    idMappings.put(entityId, replacementEntityId);
  }

  @Override
  public boolean hasReplacementEntities(Class<?> entityType) {
    return _entityReplacement.containsKey(entityType);
  }

  @Override
  public boolean hasReplacementEntity(Class<?> entityType, Serializable entityId) {
    Map<Serializable, Serializable> idMappings = _entityReplacement.get(entityType);
    if (idMappings == null)
      return false;
    return idMappings.containsKey(entityId);
  }

  @Override
  public Serializable getReplacementEntityId(Class<?> entityType,
      Serializable entityId) {
    Map<Serializable, Serializable> idMappings = _entityReplacement.get(entityType);
    if (idMappings == null)
      return null;
    return idMappings.get(entityId);
  }

  @Override
  public <T> T logReplacement(Class<T> type, Serializable id, Serializable replacementId, T originalEntity, T replacementEntity) {

    if (_entityLogger != null) {
      _entityLogger.log(type, id, replacementId, originalEntity, replacementEntity);
    }
    
    return replacementEntity;
  }


}
