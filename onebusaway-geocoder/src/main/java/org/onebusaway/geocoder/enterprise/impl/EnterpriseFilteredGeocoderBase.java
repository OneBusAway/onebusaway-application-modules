/**
 * Copyright (C) 2011 Metropolitan Transportation Authority
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
package org.onebusaway.geocoder.enterprise.impl;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.onebusaway.geocoder.enterprise.services.EnterpriseGeocoderResult;
import org.onebusaway.geocoder.enterprise.services.EnterpriseGeocoderService;
import org.onebusaway.geocoder.model.GeocoderResults;
import org.onebusaway.geocoder.services.GeocoderService;


import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of OBA's geocoder class to plug into front-end apps that require it.
 * @author jmaki
 *
 */
public abstract class EnterpriseFilteredGeocoderBase implements EnterpriseGeocoderService, GeocoderService {

  private static GeometryFactory _geometryFactory = JTSFactoryFinder.getGeometryFactory(null);

  private Polygon _wktFilterPolygon = null;
  
  public void setWktFilterPolygon(String wkt) throws ParseException {
    WKTReader reader = new WKTReader();
    _wktFilterPolygon = (Polygon)reader.read(wkt);
  }

  /**
   * This method does nothing--it's only required to implement GeocoderService which 
   * classic OBA components require to exist in the Spring container to work.
   */
  public GeocoderResults geocode(String location) {
    return null;
  }
  
  public abstract List<EnterpriseGeocoderResult> enterpriseGeocode(String location);

  protected List<EnterpriseGeocoderResult> filterResultsByWktPolygon(List<EnterpriseGeocoderResult> input) {
    if(_wktFilterPolygon == null) {
      return input;
    }
    
    List<EnterpriseGeocoderResult> output = new ArrayList<EnterpriseGeocoderResult>();
    for(EnterpriseGeocoderResult result : input) {
      Coordinate coordinate = new Coordinate(result.getLongitude(), result.getLatitude());
      Geometry point = _geometryFactory.createPoint(coordinate);
      
      if(_wktFilterPolygon.intersects(point)) {
        output.add(result);
      }
    }

    return output;
  }

}
