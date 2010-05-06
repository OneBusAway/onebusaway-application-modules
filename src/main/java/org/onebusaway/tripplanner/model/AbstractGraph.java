package org.onebusaway.tripplanner.model;

import edu.washington.cs.rse.collections.stats.MutableDoubleInterval;

import com.infomatiq.jsi.Rectangle;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;

public class AbstractGraph {

  public static Rectangle getLocationAsRectangle(Geometry boundary) {
  
    if (!(boundary instanceof LinearRing))
      throw new IllegalStateException(
          "only support linear rings at the moment: class="
              + boundary.getClass());
  
    LineString ring = (LinearRing) boundary;
  
    MutableDoubleInterval x = new MutableDoubleInterval();
    MutableDoubleInterval y = new MutableDoubleInterval();
  
    for (int i = 0; i < ring.getNumPoints(); i++) {
      Point point = ring.getPointN(i);
      x.union(point.getX());
      y.union(point.getY());
    }
  
    return new Rectangle((float) x.getMin(), (float) y.getMin(),
        (float) x.getMax(), (float) y.getMax());
  }

}
