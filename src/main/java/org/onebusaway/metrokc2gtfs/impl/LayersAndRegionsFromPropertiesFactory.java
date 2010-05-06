package org.onebusaway.metrokc2gtfs.impl;

import org.onebusaway.common.model.Layer;
import org.onebusaway.common.model.LayerAndRegions;
import org.onebusaway.common.model.LayersAndRegions;
import org.onebusaway.common.model.Region;
import org.onebusaway.common.services.ProjectionService;

import edu.washington.cs.rse.geospatial.kml.KMLLibrary;
import edu.washington.cs.rse.geospatial.kml.Placemark;

import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class LayersAndRegionsFromPropertiesFactory {

  private boolean _generateIds = true;

  private int _id = 0;

  private ProjectionService _projectionService;

  public void setGenerateIds(boolean generateIds) {
    _generateIds = generateIds;
  }

  public void setProjectionService(ProjectionService projectionService) {
    _projectionService = projectionService;
  }

  public LayersAndRegions create(Properties properties) throws IOException, SAXException {

    List<LayerAndRegions> layersAndRegions = new ArrayList<LayerAndRegions>();

    String layerKeys = properties.getProperty("layers");

    if (layerKeys == null)
      throw new IllegalArgumentException("You must specify a 'layers' property, defining the present layers");

    for (String layerKey : layerKeys.split(",")) {

      Layer layer = new Layer();

      String layerNameKey = layerKey + ".name";
      String layerName = properties.getProperty(layerNameKey);
      if (layerName == null)
        throw new IllegalArgumentException("You must specify a layer name (key=" + layerNameKey + ")");
      layer.setName(layerName);

      String layerSequenceKey = layerKey + ".sequence";
      int layerSequence = 0;
      String layerSequenceValue = properties.getProperty(layerSequenceKey);
      if (layerSequenceValue == null)
        throw new IllegalArgumentException("You must specify a layer sequence (key=" + layerSequenceKey + ")");
      try {
        layerSequence = Integer.parseInt(layerSequenceValue);
      } catch (NumberFormatException ex) {
        throw new IllegalArgumentException("Invalid layer sequence (key=" + layerSequenceKey + " value="
            + layerSequenceValue + ")");
      }
      layer.setSequence(layerSequence);

      String layerRegionsKey = layerKey + ".regions";
      List<Region> regions = new ArrayList<Region>();

      String regionPaths = properties.getProperty(layerRegionsKey);
      if (regionPaths == null)
        throw new IllegalArgumentException("You must specify layer region paths (key=" + layerRegionsKey + ")");

      for (String resourceToken : regionPaths.trim().split("\\s+")) {
        resourceToken = resourceToken.trim();

        BufferedInputStream is = new BufferedInputStream(new FileInputStream(resourceToken));
        List<Placemark> placemarks = KMLLibrary.readKML(is, _projectionService.getProjection());
        is.close();

        for (Placemark mark : placemarks) {
          Region region = new Region(mark.getName(), mark.getGeometry());
          region.setLayer(layer);

          if (_generateIds)
            region.setId(_id++);

          regions.add(region);
        }

      }

      LayerAndRegions lar = new LayerAndRegions();
      lar.setLayer(layer);
      lar.setRegions(regions);
      layersAndRegions.add(lar);
    }

    LayersAndRegions lars = new LayersAndRegions();
    lars.setLayersAndRegions(layersAndRegions);
    return lars;
  }
}
