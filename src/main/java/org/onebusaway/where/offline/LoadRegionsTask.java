/*
 * Copyright 2008 Brian Ferris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.where.offline;

import org.onebusaway.common.model.Layer;
import org.onebusaway.common.model.LayersAndRegions;
import org.onebusaway.common.model.Region;
import org.onebusaway.where.services.WhereDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class LoadRegionsTask implements Runnable {

  private WhereDao _whereDao;

  private LayersAndRegions _layersAndRegions;

  @Autowired
  public void setWhereDao(WhereDao dao) {
    _whereDao = dao;
  }

  public void setLayersAndRegions(LayersAndRegions layersAndRegions) {
    _layersAndRegions = layersAndRegions;
  }

  public void run() {

    System.out.println("loading regions...");

    for (Map.Entry<Layer, List<Region>> entry : _layersAndRegions.entrySet()) {
      Layer layer = entry.getKey();
      List<Region> regions = entry.getValue();
      _whereDao.save(layer);
      _whereDao.saveOrUpdateAllEntities(regions);
    }

    System.out.println("complete");
  }
}
