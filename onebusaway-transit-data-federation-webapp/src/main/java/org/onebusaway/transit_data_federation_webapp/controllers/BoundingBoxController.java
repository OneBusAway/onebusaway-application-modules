/**
 * Copyright (C) 2013 Kurt Raschke <kurt@kurtraschke.com>
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
package org.onebusaway.transit_data_federation_webapp.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.transit_data_federation.services.AgencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/bounding-box.action")
public class BoundingBoxController {

  @Autowired
  private AgencyService _agencyService;

  @RequestMapping()
  public ModelAndView index() {

    GeometryFactory gf = new GeometryFactory();
    List<Polygon> polygons = new ArrayList<Polygon>();
      
    Map<String, CoordinateBounds> agencies = _agencyService.getAgencyIdsAndCoverageAreas();

    for (CoordinateBounds cb: agencies.values()) {
        Envelope e = new Envelope(cb.getMinLon(), cb.getMaxLon(), cb.getMinLat(), cb.getMaxLat());
        Polygon p = (Polygon) gf.toGeometry(e);
        polygons.add(p);
    }
    
    MultiPolygon mp = gf.createMultiPolygon(polygons.toArray(new Polygon[0]));
    
    Geometry hull = mp.convexHull();
    Envelope env = hull.getEnvelopeInternal();

    ModelAndView mv = new ModelAndView("bounding-box.jspx");
    mv.addObject("minY", env.getMinY());
    mv.addObject("minX", env.getMinX());
    mv.addObject("maxY", env.getMaxY());
    mv.addObject("maxX", env.getMaxX());
    mv.addObject("hullWKT", hull.toText());
    return mv;
  }
}
