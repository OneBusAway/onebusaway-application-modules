package org.onebusaway.layers.model;


import java.util.List;

public class LayerAndRegions {

  private Layer layer;

  private List<Region> regions;

  public Layer getLayer() {
    return layer;
  }

  public void setLayer(Layer layer) {
    this.layer = layer;
  }

  public List<Region> getRegions() {
    return regions;
  }

  public void setRegions(List<Region> regions) {
    this.regions = regions;
  }

}
