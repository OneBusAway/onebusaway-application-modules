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
package org.onebusaway.admin.service.bundle.hastus;

import java.io.File;

import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.factory.Hints;
import org.geotools.feature.FeatureCollection;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShapefileLibrary {

  private static Logger _log = LoggerFactory.getLogger(ShapefileLibrary.class);
  
  public static FeatureCollection<SimpleFeatureType, SimpleFeature> loadShapeFile(
      File path) throws Exception {
  
    _log.info("loading shapefile " + path.toURI());
    ShapefileDataStore dataStore = new ShapefileDataStore(path.toURI().toURL());
    _log.info("loaded!");
    
    String typeNames[] = dataStore.getTypeNames();
    String typeName = typeNames[0];
  
    FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = dataStore.getFeatureSource(typeName);
    CoordinateReferenceSystem sourceCRS = featureSource.getInfo().getCRS();
    _log.info("using sourceCRS=" + sourceCRS + " for typeName=" + typeName);
  
    Hints hints = new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER,
        Boolean.TRUE);
    CRSAuthorityFactory factory = ReferencingFactoryFinder.getCRSAuthorityFactory(
        "EPSG", hints);
    CoordinateReferenceSystem worldCRS = factory.createCoordinateReferenceSystem("EPSG:4326");
  
    Query query = new Query(typeName);
    query.setCoordinateSystem(sourceCRS);
    query.setCoordinateSystemReproject(worldCRS);
    query.setHints(hints);
  
    return featureSource.getFeatures(query);
  }

}
