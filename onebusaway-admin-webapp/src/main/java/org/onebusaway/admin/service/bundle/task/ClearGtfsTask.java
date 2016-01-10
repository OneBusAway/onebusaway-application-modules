/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
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
package org.onebusaway.admin.service.bundle.task;

import org.onebusaway.gtfs.impl.GenericDaoImpl;
import org.onebusaway.gtfs.services.GenericMutableDao;
import org.springframework.beans.factory.annotation.Autowired;

public class ClearGtfsTask implements Runnable {

  private GenericMutableDao _dao;
  
  @Autowired
  public void setDao(GenericMutableDao dao) {
    _dao = dao;
  }
  
  @Override
  public void run() {
    try {
      ((GenericDaoImpl)_dao).clear();
    } catch (Throwable ex) {
      throw new IllegalStateException("error loading gtfs", ex);
    }
  }
}
