package org.onebusaway.layers.model;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

public class LayersAndRegions {

  private static final long serialVersionUID = 1L;

  private SortedMap<Layer, List<Region>> _layersAndRegions = new TreeMap<Layer, List<Region>>();

  public void setLayersAndRegions(List<LayerAndRegions> layersAndRegions) {
    for (LayerAndRegions layerAndRegions : layersAndRegions)
      _layersAndRegions.put(layerAndRegions.getLayer(),
          layerAndRegions.getRegions());
  }

  public Set<Layer> getLayers() {
    return _layersAndRegions.keySet();
  }

  public Collection<List<Region>> getRegions() {
    return _layersAndRegions.values();
  }

  public Set<Entry<Layer, List<Region>>> entrySet() {
    return _layersAndRegions.entrySet();
  }
}
