/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
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

import org.onebusaway.gtfs.services.GenericMutableDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public class LoadGtfsTask implements Runnable {

  private ApplicationContext _applicationContext;
  
  private GenericMutableDao _dao;
  
  private boolean _disableStopConsolidation = false;

  @Autowired
  public void setApplicationContext(ApplicationContext applicationContext) {
    _applicationContext = applicationContext;
  }
  
  @Autowired
  public void setDao(GenericMutableDao dao) {
    _dao = dao;
  }
  
  public void setDisableStopConsolidation(boolean disable) {
    _disableStopConsolidation = disable;
  }

  @Override
  public void run() {
    try {
      
      GtfsReadingSupport.readGtfsIntoStore(_applicationContext, _dao, _disableStopConsolidation);
      
    } catch (Throwable ex) {
      throw new IllegalStateException("error loading gtfs", ex);
    }

  }

}
