package org.onebusaway.common.model;

import edu.washington.cs.rse.geospatial.kml.KMLLibrary;
import edu.washington.cs.rse.geospatial.kml.Placemark;

import org.onebusaway.common.services.ProjectionService;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class LayerAndRegionsFactoryBean implements FactoryBean {

  private static int _id = 0;

  private ApplicationContext _context;

  private ProjectionService _projectionService;

  private String _layerName;

  private int _layerSequence;

  private String _resources;

  private boolean _generateIds = false;

  @Autowired
  public void setApplicationContext(ApplicationContext context) {
    _context = context;
  }

  @Autowired
  public void setProjectionService(ProjectionService projectionService) {
    _projectionService = projectionService;
  }

  public void setLayerName(String layerName) {
    _layerName = layerName;
  }

  public void setLayerSequence(int layerSequence) {
    _layerSequence = layerSequence;
  }

  public void setResources(String resources) {
    _resources = resources;
  }

  public void setGenerateIds(boolean generateIds) {
    _generateIds = generateIds;
  }

  public boolean isSingleton() {
    return false;
  }

  public Class<?> getObjectType() {
    return LayerAndRegions.class;
  }

  public Object getObject() throws Exception {

    Layer layer = new Layer();
    layer.setName(_layerName);
    layer.setSequence(_layerSequence);

    List<Region> regions = new ArrayList<Region>();

    for (String resourceToken : _resources.trim().split("\\s+")) {
      resourceToken = resourceToken.trim();
      System.out.println("token=" + resourceToken);
      Resource[] resources = _context.getResources(resourceToken);

      for (Resource resource : resources) {
        System.out.println("resource=" + resource);
        InputStream is = resource.getInputStream();
        List<Placemark> placemarks = KMLLibrary.readKML(is,
            _projectionService.getProjection());
        is.close();

        for (Placemark mark : placemarks) {
          Region region = new Region(mark.getName(), mark.getGeometry());
          region.setLayer(layer);

          if (_generateIds)
            region.setId(_id++);

          regions.add(region);
        }
      }

    }

    LayerAndRegions lar = new LayerAndRegions();
    lar.setLayer(layer);
    lar.setRegions(regions);

    return lar;
  }
}
