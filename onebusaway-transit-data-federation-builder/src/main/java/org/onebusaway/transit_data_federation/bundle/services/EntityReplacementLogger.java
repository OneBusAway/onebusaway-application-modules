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
package org.onebusaway.transit_data_federation.bundle.services;

import java.io.Serializable;

import org.onebusaway.gtfs.services.GenericMutableDao;
import org.onebusaway.transit_data_federation.bundle.tasks.MultiCSVLogger;
import org.onebusaway.transit_data_federation.bundle.tasks.MultiCSVLoggerSummarizeListener;

/**
 * A GTFS Entity has been replaced, log some information about that.
 * 
 * Example include logging stop consolidation results for further analysis.
 */
public interface EntityReplacementLogger {
  void setMultiCSVLogger(MultiCSVLogger logger);
  void setStore(GenericMutableDao store);
  void setRejectionStore(GenericMutableDao store);
  MultiCSVLoggerSummarizeListener getListener();
  <T> T  log(Class<T> type, Serializable id, Serializable replacementId, T originalEntity, T replacementEntity);
}
