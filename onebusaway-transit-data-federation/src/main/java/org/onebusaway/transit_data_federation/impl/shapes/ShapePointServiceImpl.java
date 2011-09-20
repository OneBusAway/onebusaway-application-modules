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
package org.onebusaway.transit_data_federation.impl.shapes;

import java.util.List;

import org.onebusaway.container.cache.Cacheable;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.model.ShapePointsFactory;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;
import org.onebusaway.transit_data_federation.services.shapes.ShapePointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ShapePointServiceImpl implements ShapePointService {

  private NarrativeService _narrativeService;

  @Autowired
  public void setNarrativeService(NarrativeService narrativeService) {
    _narrativeService = narrativeService;
  }

  @Override
  public ShapePoints getShapePointsForShapeId(AgencyAndId shapeId) {
    return _narrativeService.getShapePointsForId(shapeId);
  }

  @Cacheable
  @Override
  public ShapePoints getShapePointsForShapeIds(List<AgencyAndId> shapeIds) {
    ShapePointsFactory factory = new ShapePointsFactory();
    for (AgencyAndId shapeId : shapeIds) {
      ShapePoints shapePoints = getShapePointsForShapeId(shapeId);
      factory.addPoints(shapePoints);
    }
    return factory.create();
  }
}
