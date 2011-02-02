package org.onebusaway.transit_data_federation.impl.otp;

import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;

import org.onebusaway.container.refresh.Refreshable;
import org.onebusaway.gtfs.model.calendar.CalendarServiceData;
import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.impl.RefreshableResources;
import org.onebusaway.utility.ObjectSerializationLibrary;
import org.opentripplanner.routing.contraction.ContractionHierarchy;
import org.opentripplanner.routing.contraction.ContractionHierarchySet;
import org.opentripplanner.routing.contraction.ModeAndOptimize;
import org.opentripplanner.routing.core.Graph;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.Vertex;
import org.opentripplanner.routing.impl.StreetVertexIndexServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;

public class ContractionHierarchySetWrapper extends ContractionHierarchySet {

  private static final long serialVersionUID = 1L;

  private ContractionHierarchySet _source = null;

  private FederatedTransitDataBundle _bundle;

  private StreetVertexIndexServiceImpl _streetVertexIndexServiceImpl;

  private StreetToStopGraphLinkerService _streetToStopGraphLinkerService;

  @Autowired
  public void setBundle(FederatedTransitDataBundle bundle) {
    _bundle = bundle;
  }

  @Autowired
  public void setStreetVertexIndexServiceImpl(
      StreetVertexIndexServiceImpl streetVertexIndexServiceImpl) {
    _streetVertexIndexServiceImpl = streetVertexIndexServiceImpl;
  }

  @Autowired
  public void setStreetToStopGraphLinkerService(
      StreetToStopGraphLinkerService streetToStopGraphLinkerService) {
    _streetToStopGraphLinkerService = streetToStopGraphLinkerService;
  }

  @PostConstruct
  @Refreshable(dependsOn = RefreshableResources.OTP_DATA)
  public void setup() throws IOException, ClassNotFoundException {

    File path = _bundle.getGraphPath();

    Graph graph = new Graph();

    if (path.exists()) {
      _source = ObjectSerializationLibrary.readObject(path);
      graph = _source.getGraph();
      _streetVertexIndexServiceImpl.setGraph(_source.getGraph());
      _streetVertexIndexServiceImpl.setup();
    } else {
      _source = null;

    }

    _streetToStopGraphLinkerService.setOtpGraph(graph);
    _streetToStopGraphLinkerService.setup();

    _streetVertexIndexServiceImpl.setGraph(graph);
    _streetVertexIndexServiceImpl.setup();
  }

  /****
   * {@link ContractionHierarchySet} Interface
   ****/

  @Override
  public ContractionHierarchy getHierarchy(TraverseOptions options) {
    return _source.getHierarchy(options);
  }

  @Override
  public boolean hasService(Class<CalendarServiceData> serviceType) {
    if (_source == null)
      return false;
    return _source.hasService(serviceType);
  }

  @Override
  public CalendarServiceData getService(Class<CalendarServiceData> serviceType) {
    if (_source == null)
      return null;
    return _source.getService(serviceType);
  }

  @Override
  public Vertex getVertex(String label) {
    return _source.getVertex(label);
  }

  @Override
  public Graph getGraph() {
    return _source.getGraph();
  }

  /****
   * 
   ****/

  @Override
  public void setGraph(Graph g) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addModeAndOptimize(ModeAndOptimize mando) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void build() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setContractionFactor(double factor) {
    throw new UnsupportedOperationException();
  }
}
