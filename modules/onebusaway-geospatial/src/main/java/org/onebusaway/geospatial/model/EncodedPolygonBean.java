package org.onebusaway.geospatial.model;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EncodedPolygonBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private EncodedPolylineBean _outerRing;

  private List<EncodedPolylineBean> _innerRings = new ArrayList<EncodedPolylineBean>();

  public EncodedPolylineBean getOuterRing() {
    return _outerRing;
  }

  public void setOuterRing(EncodedPolylineBean outerRing) {
    _outerRing = outerRing;
  }

  public List<EncodedPolylineBean> getInnerRings() {
    return _innerRings;
  }

  public void addInnerRing(EncodedPolylineBean encodedPath) {
    _innerRings.add(encodedPath);
  }
}
