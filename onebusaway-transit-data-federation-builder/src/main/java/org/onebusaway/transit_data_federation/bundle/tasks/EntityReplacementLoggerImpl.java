/**
 * Copyright (C) 2017 Cambridge Systematics, Inc.
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

import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GenericMutableDao;
import org.onebusaway.gtfs_transformer.impl.SphericalGeometryLibrary;
import org.onebusaway.transit_data_federation.bundle.services.EntityReplacementLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Log stop consolidation entity replacements using MutliCSVLogger.
 */
public class EntityReplacementLoggerImpl implements EntityReplacementLogger {

  private static Logger _log = LoggerFactory.getLogger(EntityReplacementLoggerImpl.class);
  
  private MultiCSVLogger _csvLogger = null;
  // this is not currently used
  private GenericMutableDao _dao = null;
  private GenericMutableDao _rejectionDao = null;
  
  public void setMultiCSVLogger(MultiCSVLogger logger) {
    _csvLogger = logger;
  }

  public void setStore(GenericMutableDao dao) {
    _dao = dao;
  }

  public void setRejectionStore(GenericMutableDao dao) {
    _rejectionDao = dao;
  }

  private Map<Serializable, EntityStore> _replacements = new HashMap<Serializable, EntityStore>();

  public MultiCSVLoggerSummarizeListener getListener() {
    return new SummaryListener(this);
  }

  @Override
  /**
   * currently only considers stops.
   */
  public <T> T log(Class<T> type, Serializable id, Serializable replacementId,
      T originalEntity, T replacementEntity) {

    if ("Stop".equals(type.getSimpleName())) {
      Stop original = (Stop) originalEntity;
      Stop replacement = (Stop) replacementEntity;
      Double originLat = null;
      Double originLon = null;
      Double replacementLat = null;
      Double replacementLon = null;
      if (original != null) {
        originLat = original.getLat();
        originLon = original.getLon();
      }
      if (replacement != null) {
        replacementLat = replacement.getLat();
        replacementLon = replacement.getLon();
      }
      updateStore(id, replacementId, originLat, originLon, replacementLat,
          replacementLon);
    }
    return replacementEntity;
  }

  // we've been called (potentially again).  see if there is anything
  // worth updating.
  private void updateStore(Serializable id, Serializable replacementId,
      Double originLat, Double originLon, Double replacementLat,
      Double replacementLon) {
    Serializable key = hash(id, replacementId);
    EntityStore store = _replacements.get(key);

    if (store == null) {
      
      if (originLat == null && _rejectionDao != null) {
        Stop stop = _rejectionDao.getEntityForId(Stop.class, id);
        if (stop != null) {
          originLat = stop.getLat();
          originLon = stop.getLon();
        }
      }
      
      store = new EntityStore(id, replacementId, originLat, originLon,
          replacementLat, replacementLon);
      
      _replacements.put(key, store);
    } else {
      store.update(originLat, originLon, replacementLat, replacementLon);
    }
  }

  private String hash(Serializable a, Serializable b) {
    return a + ":" + b;
  }

  /**
   * generate the report via the MultiCSVLogger
   */
  public void log() {
    // only run this once, even if we are called multiple times
    if (!_csvLogger.hasHeader("gtfs_stop_replacements.csv")) {
      _csvLogger.header("gtfs_stop_replacements.csv",
          "orginal_stop_id,replacement_stop_id,original_lat,original_lon,replacement_lat,replacement_lon,distance_in_feet");
      for (Serializable key : _replacements.keySet()) {
        EntityStore es = _replacements.get(key);
        _csvLogger.log("gtfs_stop_replacements.csv", es.getId(),
            es.getReplacementId(), es.getOriginalLat(), es.getOriginalLon(),
            es.getReplacmentLat(), es.getReplacmentLon(), es.getDistanceInFeet());
      }
    }

    
  }

  /**
   * Store some info about the entities considered for later reporting
   */
  public static class EntityStore {
    private Serializable originalId;
    private Serializable replacementId;
    private Double originalLat;
    private Double originalLon;
    private Double replacementLat;
    private Double replacementLon;

    public EntityStore(Serializable originalId, Serializable replacementId,
        Double originalLat, Double originalLon, Double replacementLat,
        Double replacementLon) {
      this.originalId = originalId;
      this.replacementId = replacementId;
      this.originalLat = originalLat;
      this.originalLon = originalLon;
      this.replacementLat = replacementLat;
      this.replacementLon = replacementLon;
    }

    public void update(Double originalLat, Double originalLon,
        Double replacementLat, Double replacementLon) {
      if (this.originalLat == null && originalLat != null) {
        this.originalLat = originalLat;
      }
      if (this.originalLon == null && originalLon != null) {
        this.originalLon = originalLon;
      }
      if (this.replacementLat == null && replacementLat != null) {
        this.replacementLat = replacementLat;
      }
      if (this.replacementLon == null && replacementLon != null) {
        this.replacementLon = replacementLon;
      }
    }

    public Serializable getId() {
      return originalId;
    }

    public Serializable getReplacementId() {
      return replacementId;
    }

    public Double getOriginalLat() {
      return originalLat;
    }

    public Double getOriginalLon() {
      return originalLon;
    }

    public Double getReplacmentLat() {
      return replacementLat;
    }

    public Double getReplacmentLon() {
      return replacementLon;
    }
    
    public Double getDistanceInFeet() {
      if (originalLat == null || originalLon == null 
          || replacementLat == null || replacementLon == null)
        return null;
      return SphericalGeometryLibrary.distanceFaster(originalLat, originalLon, replacementLat, replacementLon)
          * 3.28084; // feet per meter
    }

  }

  /**
   * Compute the report once the gtfs has stabilized, so listen
   * for the summarize event.
   */
  public static class SummaryListener
      implements MultiCSVLoggerSummarizeListener {
    EntityReplacementLoggerImpl self;

    public SummaryListener(EntityReplacementLoggerImpl self) {
      this.self = self;
    }

    public void summarize() {
      self.log();
    }
  }
}
