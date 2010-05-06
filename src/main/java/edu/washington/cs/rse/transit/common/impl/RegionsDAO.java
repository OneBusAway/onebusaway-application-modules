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
package edu.washington.cs.rse.transit.common.impl;

import edu.washington.cs.rse.collections.FactoryMap;
import edu.washington.cs.rse.collections.same.TreeUnionFind;
import edu.washington.cs.rse.geospatial.GeoPoint;
import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;
import edu.washington.cs.rse.text.xml.EnhancedContentHandler;
import edu.washington.cs.rse.transit.common.MetroKCDAO;
import edu.washington.cs.rse.transit.common.model.aggregate.Layer;
import edu.washington.cs.rse.transit.common.model.aggregate.Region;
import edu.washington.cs.rse.transit.common.spatial.KMLRuleSet;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import org.apache.commons.digester.Digester;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

@Component
public class RegionsDAO {

  private static final String MULTI_POLYGON_PROPERTY = "/ogr:geometryProperty/gml:MultiPolygon/gml:polygonMember/gml:Polygon/gml:outerBoundaryIs/gml:LinearRing/gml:coordinates";

  private static final String POLYGON_PROPERTY = "/ogr:geometryProperty/gml:Polygon/gml:outerBoundaryIs/gml:LinearRing/gml:coordinates";

  private static final double MIN_HOLE_SIZE = 10000;

  private static GeometryFactory _geoFactory = new GeometryFactory();

  private MetroKCDAO _dao;

  private Map<Layer, Map<String, Region>> _layers = new HashMap<Layer, Map<String, Region>>();

  @Autowired
  public void setMetroKCDAO(MetroKCDAO dao) {
    _dao = dao;
  }

  /***************************************************************************
   * Public Methods
   **************************************************************************/

  public void addLayer(Layer layer) {
    _dao.saveOrUpdate(layer);
  }

  public void readRegionsFromKML(File kmlFile, List<Region> regions)
      throws IOException, SAXException {

    Digester d = new Digester();
    String base = "kml/Document/Placemark";

    d.push(regions);

    d.addObjectCreate(base, Region.class);
    d.addCallMethod(base + "/name", "setName", 0);
    d.addRuleSet(new KMLRuleSet(MetroKCDAO.PROJECTION));
    d.addSetNext(base, "add");

    d.parse(kmlFile);
  }

  public <T extends Region> void readRegionsFromGML(File gmlFile,
      List<T> elements, Class<T> type, String key, Map<String, String> params)
      throws IOException, SAXException {

    Digester d = new Digester();
    String base = "ogr:FeatureCollection/gml:featureMember/" + key;

    d.push(elements);

    d.addObjectCreate(base, type);
    for (Map.Entry<String, String> entry : params.entrySet())
      d.addCallMethod(base + "/" + entry.getKey(), entry.getValue(), 0);
    d.addCallMethod(base + POLYGON_PROPERTY, "addBoundaryFromLocalCoordinates",
        0);
    d.addCallMethod(base + MULTI_POLYGON_PROPERTY,
        "addBoundaryFromLocalCoordinates", 0);
    d.addSetNext(base, "add");

    d.parse(gmlFile);
  }

  public void writeRegionsAsKML(Iterable<Region> regions, File outputFile)
      throws Exception {

    Writer w = new BufferedWriter(new FileWriter(outputFile));
    EnhancedContentHandler h = EnhancedContentHandler.create(w);

    h.startElement("http://earth.google.com/kml/2.2", "kml", "kml");
    h.startElement("Document");

    for (Region region : regions) {

      h.startElement("Placemark");

      h.setElement("name", region.getName());
      h.startElement("MultiGeometry");

      MultiPolygon mp = region.getBoundary();

      for (int i = 0; i < mp.getNumGeometries(); i++) {

        h.startElement("Polygon");

        // Outer Boundary
        Polygon p = (Polygon) mp.getGeometryN(i);
        LineString outterRing = p.getExteriorRing();
        h.startElements("outerBoundaryIs/LinearRing");
        addLinStringAsCoordinates(h, outterRing);
        h.endElements("outerBoundaryIs/LinearRing");

        // Inner Boundaries
        for (int j = 0; j < p.getNumInteriorRing(); j++) {
          LineString innerRing = p.getInteriorRingN(j);
          h.startElements("innerBoundaryIs/LinearRing");
          addLinStringAsCoordinates(h, innerRing);
          h.endElements("innerBoundaryIs/LinearRing");
        }

        h.endElement();
      }

      h.endElement();
      h.endElement();
    }

    h.closeDocument();
    w.close();
  }

  public List<Region> simplifyRegions(List<Region> regions) {

    List<Region> simplifiedRegions = new ArrayList<Region>();

    // Step 1: Group regions by name
    Map<String, List<Region>> regionsByName = new FactoryMap<String, List<Region>>(
        new ArrayList<Region>());
    for (Region region : regions)
      regionsByName.get(region.getName()).add(region);

    for (Map.Entry<String, List<Region>> entry : regionsByName.entrySet()) {

      String name = entry.getKey();
      TreeUnionFind<Geometry> clusters = new TreeUnionFind<Geometry>();

      for (Region r : entry.getValue()) {
        MultiPolygon mp = r.getBoundary();
        for (int i = 0; i < mp.getNumGeometries(); i++) {
          Geometry g = mp.getGeometryN(i);
          Set<Geometry> elements = new HashSet<Geometry>(clusters.getElements());
          clusters.find(g);
          for (Geometry g2 : elements) {
            boolean overlaps = g.overlaps(g2);
            boolean intersects = g.intersects(g2);
            if (overlaps != intersects)
              System.out.println("  mismatch=" + name);
            if (intersects || overlaps) {
              System.out.println("join=" + name);
              clusters.union(g, g2);
            }
          }
        }
      }

      List<Geometry> gs = new ArrayList<Geometry>(clusters.size());

      for (Set<Geometry> cluster : clusters.getSetMembers()) {

        Geometry u = null;
        for (Geometry g : cluster)
          u = u == null ? g : u.union(g);

        u = simplifyPolygon((Polygon) u);
        gs.add(u);
      }

      GeometryCollection gc = _geoFactory.createGeometryCollection(gs.toArray(new Geometry[gs.size()]));
      Region r = new Region();
      r.setName(name);
      r.setGeometry(gc);
      simplifiedRegions.add(r);
    }

    return simplifiedRegions;
  }

  public Collection<Region> consolidateRegionsByName(Iterable<Region> regions) {

    Map<String, Region> consolidated = new HashMap<String, Region>();

    for (Region r : regions) {
      Region rc = consolidated.get(r.getName());
      if (rc == null) {
        rc = new Region();
        rc.setName(r.getName());
        consolidated.put(rc.getName(), rc);
      }
      /*
       * for (List<IGeoPoint> boundary : r.getBoundaries())
       * rc.addBoundary(boundary);
       */
    }

    return consolidated.values();
  }

  public void addRegions(Layer layer, Collection<Region> regions) {
    for (Region r : regions)
      r.setLayer(layer);
    _dao.saveOrUpdateAllEntities(regions);
  }

  public Set<Layer> getLayers() {
    return _layers.keySet();
  }

  public SortedMap<Layer, Region> getRegionsByLocation(Geometry location) {

    List<Region> regions = _dao.getRegionsByLocation(location);

    SortedMap<Layer, Region> m = new TreeMap<Layer, Region>();

    for (Region r : regions) {
      Geometry boundary = r.getBoundary();
      boundary = boundary.buffer(0);
      if (boundary.contains(location)) {
        Region rCurrent = m.get(r.getLayer());
        if (rCurrent == null
            || location.distance(boundary) < location.distance(rCurrent.getBoundary()))
          m.put(r.getLayer(), r);
      }
    }

    return m;
  }

  /****
   * Private Methods
   ****/

  private void addLinStringAsCoordinates(EnhancedContentHandler h,
      LineString ring) throws SAXException {
    StringBuilder b = new StringBuilder();
    for (int x = 0; x < ring.getNumPoints(); x++) {
      Point point = ring.getPointN(x);
      GeoPoint gp = new GeoPoint(MetroKCDAO.PROJECTION, point.getX(),
          point.getY(), 0.0);
      CoordinatePoint cp = gp.getCoordinates();
      if (x > 0)
        b.append(' ');
      b.append(cp.getLon()).append(',').append(cp.getLat());
    }
    h.setElement("coordinates", b.toString());
  }

  private Polygon simplifyPolygon(Polygon p) {
    GeometryFactory f = p.getFactory();

    List<LinearRing> holes = new ArrayList<LinearRing>();
    for (int i = 0; i < p.getNumInteriorRing(); i++) {
      LineString ring = p.getInteriorRingN(i);
      if (ring.getArea() > MIN_HOLE_SIZE)
        holes.add(f.createLinearRing(ring.getCoordinates()));
    }

    LinearRing exterior = f.createLinearRing(p.getExteriorRing().getCoordinates());
    return f.createPolygon(exterior,
        holes.toArray(new LinearRing[holes.size()]));
  }
}
