/**
 * Copyright (c) 2011 Metropolitan Transportation Authority
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
package org.onebusaway.transit_data_federation.impl.nyc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import javax.annotation.PostConstruct;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.csv.CsvEntityReader;
import org.onebusaway.gtfs.csv.ListEntityHandler;
import org.onebusaway.gtfs.csv.exceptions.CsvEntityIOException;
import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.services.nyc.BaseLocationService;
import org.onebusaway.transit_data_federation.services.nyc.model.BaseLocationRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.index.strtree.STRtree;

@Component
class BaseLocationServiceImpl implements BaseLocationService {

  private GeometryFactory _factory = new GeometryFactory();

  private STRtree _baseLocationTree;

  private STRtree _terminalLocationTree;

  private FederatedTransitDataBundle _bundle;

  @Autowired
  public void setBundle(FederatedTransitDataBundle bundle) {
    _bundle = bundle;
  }

  @PostConstruct
  public void setup() throws CsvEntityIOException, IOException {
    _baseLocationTree = readRecordsIntoTree(_bundle.getBaseLocationsPath());
    _terminalLocationTree = readRecordsIntoTree(_bundle.getTerminalLocationsPath());
  }

  /****
   * {@link BaseLocationService} Interface
   ****/

  @Override
  public String getBaseNameForLocation(CoordinatePoint location) {
    return findNameForLocation(_baseLocationTree, location);
  }

  @Override
  public String getTerminalNameForLocation(CoordinatePoint location) {
    return findNameForLocation(_terminalLocationTree, location);
  }

  /****
   * 
   ****/

  private STRtree readRecordsIntoTree(File path) throws IOException,
      FileNotFoundException {

    CsvEntityReader reader = new CsvEntityReader();

    ListEntityHandler<BaseLocationRecord> records = new ListEntityHandler<BaseLocationRecord>();
    reader.addEntityHandler(records);

    if (!path.exists())
    	return null;    
//      throw new RuntimeException("Your bundle is missing " + path.getName());

    try {
      reader.readEntities(BaseLocationRecord.class, new FileReader(path));
    } catch (CsvEntityIOException e) {
      throw new RuntimeException("Error parsing CSV file " + path, e);
    }
    List<BaseLocationRecord> values = records.getValues();

    STRtree baseLocationTree = new STRtree(values.size());

    for (BaseLocationRecord record : values) {
      Geometry geometry = record.getGeometry();
      Envelope env = geometry.getEnvelopeInternal();
      baseLocationTree.insert(env, record);
    }

    baseLocationTree.build();

    return baseLocationTree;
  }

  private String findNameForLocation(STRtree tree, CoordinatePoint location) {
    Envelope env = new Envelope(new Coordinate(location.getLon(),
        location.getLat()));

    @SuppressWarnings("unchecked")
    List<BaseLocationRecord> values = tree.query(env);

    Point point = _factory.createPoint(new Coordinate(location.getLon(),
        location.getLat()));

    for (BaseLocationRecord record : values) {
      Geometry geometry = record.getGeometry();
      if (geometry.contains(point))
        return record.getBaseName();
    }

    return null;
  }

}
