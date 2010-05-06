package org.onebusaway.geocoder.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GeocoderResults implements Serializable {

  private static final long serialVersionUID = 1L;

  private List<GeocoderResult> results = new ArrayList<GeocoderResult>();

  public List<GeocoderResult> getResults() {
    return results;
  }

  public void setResults(List<GeocoderResult> results) {
    this.results = results;
  }

  public void addResult(GeocoderResult result) {
    results.add(result);
  }
}
