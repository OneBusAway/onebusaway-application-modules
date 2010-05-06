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
package org.onebusaway.where.web.standard.reroute.client;

import org.onebusaway.common.web.common.client.model.PathBean;
import org.onebusaway.where.web.common.client.model.StopSequenceBlockBean;
import org.onebusaway.where.web.common.client.rpc.WhereServiceAsync;
import org.onebusaway.where.web.standard.reroute.client.resources.WhereRerouteResources;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.libideas.client.StyleInjector;
import com.google.gwt.libideas.resources.client.DataResource;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.LargeMapControl;
import com.google.gwt.maps.client.event.MapClickHandler;
import com.google.gwt.maps.client.event.MarkerClickHandler;
import com.google.gwt.maps.client.event.MarkerDragEndHandler;
import com.google.gwt.maps.client.event.MarkerDragHandler;
import com.google.gwt.maps.client.event.MarkerDragStartHandler;
import com.google.gwt.maps.client.event.PolylineClickHandler;
import com.google.gwt.maps.client.event.PolylineMouseOutHandler;
import com.google.gwt.maps.client.event.PolylineMouseOverHandler;
import com.google.gwt.maps.client.geocode.DirectionQueryOptions;
import com.google.gwt.maps.client.geocode.DirectionResults;
import com.google.gwt.maps.client.geocode.Directions;
import com.google.gwt.maps.client.geocode.DirectionsCallback;
import com.google.gwt.maps.client.geocode.Waypoint;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.geom.Size;
import com.google.gwt.maps.client.overlay.Icon;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.maps.client.overlay.Polyline;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MouseListenerAdapter;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WhereRerouteApplication implements EntryPoint {

  private static LatLng _center = LatLng.newInstance(47.601533, -122.32933);

  private static int _zoom = 11;

  private MapWidget _map;

  private ListBox _stopSequenceBlockList;

  private Button _revertButton;

  private Button _saveButton;

  private Button _addWaypoint;

  private Button _removeSegment;

  private List<StopSequenceBlockBean> _blocks;

  private StopSequenceBlockBean _activeBlock;

  private PathGraph _graph = new PathGraph();

  /****
   * Waypoints
   ****/

  private Map<PathNode, NodeContents> _nodes = new HashMap<PathNode, NodeContents>();

  private Map<PathEdge, EdgeContents> _edges = new HashMap<PathEdge, EdgeContents>();

  /*****************************************************************************
   * Add Waypoint Members
   ****************************************************************************/

  private boolean _addingWaypoint = false;

  private boolean _tracking = false;

  private EdgeContents _trackingEdge = null;

  private Marker _trackingMarker = null;

  private int _trackingSegmentVertexIndex = -1;

  public void onModuleLoad() {

    StyleInjector.injectStylesheet(WhereRerouteResources.INSTANCE.getCSS().getText());

    Dictionary info = Dictionary.getDictionary("RerouteConfig");
    String elementId = info.get("elementId");
    String routeName = info.get("routeName");

    RootPanel root = RootPanel.get(elementId);
    FlowPanel mainPanel = new FlowPanel();
    root.add(mainPanel);

    HorizontalPanel horizontalPanel = new HorizontalPanel();
    mainPanel.add(horizontalPanel);

    _stopSequenceBlockList = new ListBox(false);
    _stopSequenceBlockList.setVisibleItemCount(1);
    _stopSequenceBlockList.addChangeListener(new StopSequenceBlockSelectionListener());
    horizontalPanel.add(_stopSequenceBlockList);

    _revertButton = new Button("Start Over");
    _revertButton.setEnabled(false);
    horizontalPanel.add(_revertButton);

    _saveButton = new Button("Save");
    _saveButton.setEnabled(false);
    horizontalPanel.add(_saveButton);

    MouseMovementHandler mouseHandler = new MouseMovementHandler();

    FocusPanel mapMousePanel = new FocusPanel();
    mapMousePanel.addMouseListener(mouseHandler);
    mainPanel.add(mapMousePanel);

    _map = new MapWidget(_center, _zoom);
    _map.getElement().setId("rerouteMap");
    _map.addControl(new LargeMapControl());
    _map.addMapClickHandler(mouseHandler);
    mapMousePanel.add(_map);

    HorizontalPanel opPanel = new HorizontalPanel();
    mainPanel.add(opPanel);

    _addWaypoint = new Button("Add Waypoint");
    _addWaypoint.addClickListener(new ClickListener() {
      public void onClick(Widget arg0) {
        handleAddWaypointButtonClicked();
      }
    });
    opPanel.add(_addWaypoint);

    _removeSegment = new Button("Remove Segment");
    opPanel.add(_removeSegment);

    _graph.addListener(new GraphHandler());

    WhereServiceAsync.SERVICE.getStopSequenceBlocksByRoute(routeName, new StopSequenceBlocksHandler());
  }

  private void setSelectedStopSequenceBlock(StopSequenceBlockBean block) {

    _graph.clear();
    _map.clearOverlays();

    for (PathBean path : block.getPaths()) {
      Polyline line = getPathBeanAsPolyline(path);
      if (line.getVertexCount() == 0)
        continue;
      _graph.addEdge(line);
    }
  }

  private Polyline getPathBeanAsPolyline(PathBean bean) {
    double[] lat = bean.getLat();
    double[] lon = bean.getLon();

    LatLng[] points = new LatLng[lat.length];

    for (int index = 0; index < lat.length; index++)
      points[index] = LatLng.newInstance(lat[index], lon[index]);
    return new Polyline(points);
  }

  private Polyline getPolylineSubset(Polyline line, int indexFrom, int indexTo) {
    int len = indexTo - indexFrom;
    LatLng[] points = new LatLng[len];
    for (int i = 0; i < len; i++)
      points[i] = line.getVertex(indexFrom + i);
    return new Polyline(points);
  }

  private int getClosestVertext(Polyline line, LatLng target) {

    double closestDistance = 0;
    int closestIndex = -1;

    for (int i = 0; i < line.getVertexCount(); i++) {
      LatLng v = line.getVertex(i);
      double d = v.distanceFrom(target);
      if (closestIndex == -1 || d < closestDistance) {
        closestDistance = d;
        closestIndex = i;
      }
    }

    return closestIndex;
  }

  private NodeContents addNode(PathNode node) {

    NodeContents contents = _nodes.get(node);

    if (contents != null)
      throw new IllegalStateException("duplicate node!");

    MarkerOptions opts = MarkerOptions.newInstance(getDragMarkerIcon());
    opts.setAutoPan(true);
    opts.setBouncy(false);
    opts.setClickable(true);
    opts.setDraggable(true);

    Marker m = new Marker(node.getPoint(), opts);

    contents = new NodeContents(node, m);
    _nodes.put(node, contents);

    _map.addOverlay(m);

    return contents;
  }

  private void removeNode(PathNode node) {
    NodeContents contents = _nodes.get(node);
    if (contents == null)
      throw new IllegalStateException("unknown node!");
    _map.removeOverlay(contents.getMarker());
  }

  private Icon getDragMarkerIcon() {
    DataResource dragTokenResource = WhereRerouteResources.INSTANCE.getDragToken();
    Icon icon = Icon.newInstance();
    icon.setImageURL(dragTokenResource.getUrl());
    icon.setIconSize(Size.newInstance(12, 12));
    icon.setIconAnchor(Point.newInstance(6, 6));
    return icon;
  }

  private EdgeContents addEdge(PathEdge edge) {
    EdgeContents contents = new EdgeContents(edge);
    _edges.put(edge, contents);
    _map.addOverlay(edge.getLine());
    // contents.setMouseHandlerStatus(true);
    return contents;
  }

  private void removeEdge(PathEdge edge) {
    EdgeContents contents = _edges.get(edge);
    // contents.setMouseHandlerStatus(false);
    _map.removeOverlay(edge.getLine());
  }

  private void handleAddWaypointButtonClicked() {
    if (!_addingWaypoint) {
      _addWaypoint.setText("Cancel Waypoint");
      _addingWaypoint = true;
      for (EdgeContents segment : _edges.values())
        segment.setMouseHandlerStatus(true);

    } else {
      _addWaypoint.setText("Add Waypoint");
      _addingWaypoint = false;
      for (EdgeContents segment : _edges.values())
        segment.setMouseHandlerStatus(false);
    }
  }

  private void setEdgeForWaypointAddition(EdgeContents edge, boolean isActive) {

    System.out.println("here we go=" + isActive);

    if (isActive) {
      _tracking = true;
      _trackingEdge = edge;
    } else {
      removeTracking();
    }
  }

  private void splitEdge() {

    Polyline line = _trackingEdge.getLine();
    Polyline lineA = getPolylineSubset(line, 0, _trackingSegmentVertexIndex + 1);
    Polyline lineB = getPolylineSubset(line, _trackingSegmentVertexIndex, line.getVertexCount());

    PathEdge edge = _trackingEdge.getEdge();
    PathNode fromNode = edge.getFrom();
    PathNode toNode = edge.getTo();

    _graph.removeEdge(edge);

    LatLng location = _trackingMarker.getLatLng();
    PathNode node = _graph.addNode(location);

    _graph.addEdge(fromNode, node, lineA);
    _graph.addEdge(node, toNode, lineB);

    _addWaypoint.setText("Add Waypoint");
    _addingWaypoint = false;
    for (EdgeContents segment : _edges.values())
      segment.setMouseHandlerStatus(false);

    removeTracking();
  }

  private void removeTracking() {
    _tracking = false;
    _trackingEdge = null;
    _trackingSegmentVertexIndex = -1;
    if (_trackingMarker != null) {
      _map.removeOverlay(_trackingMarker);
      _trackingMarker = null;
    }
  }

  /*****************************************************************************
   * 
   ****************************************************************************/

  private class StopSequenceBlocksHandler implements AsyncCallback<List<StopSequenceBlockBean>> {

    public void onSuccess(List<StopSequenceBlockBean> beans) {

      _blocks = beans;

      for (StopSequenceBlockBean bean : beans)
        _stopSequenceBlockList.addItem(bean.getDescription());

      if (beans.size() > 0) {
        _stopSequenceBlockList.setSelectedIndex(0);
        setSelectedStopSequenceBlock(_blocks.get(0));
      }
    }

    public void onFailure(Throwable ex) {
      ex.printStackTrace();
    }

  }

  private class StopSequenceBlockSelectionListener implements ChangeListener {

    public void onChange(Widget arg0) {
      int index = _stopSequenceBlockList.getSelectedIndex();
      if (0 <= index && index < _blocks.size())
        setSelectedStopSequenceBlock(_blocks.get(index));
    }
  }

  private class MouseMovementHandler extends MouseListenerAdapter implements MapClickHandler {

    @Override
    public void onMouseMove(Widget sender, int x, int y) {

      if (_tracking) {

        LatLng location = _map.convertContainerPixelToLatLng(Point.newInstance(x, y));
        Polyline line = _trackingEdge.getLine();

        int closestIndex = getClosestVertext(line, location);
        LatLng closestPoint = line.getVertex(closestIndex);

        if (_trackingMarker == null) {
          MarkerOptions opts = MarkerOptions.newInstance(getDragMarkerIcon());
          opts.setAutoPan(false);
          opts.setBouncy(false);
          opts.setClickable(true);
          opts.setDraggable(false);
          _trackingMarker = new Marker(closestPoint, opts);
          _trackingMarker.addMarkerClickHandler(new TrackingMarkerClickHandler());
          _map.addOverlay(_trackingMarker);
        }

        _trackingMarker.setLatLng(closestPoint);
        _trackingSegmentVertexIndex = closestIndex;

      } else {
        super.onMouseMove(sender, x, y);
      }
    }

    public void onClick(MapClickEvent event) {
      if (_tracking && _trackingEdge != null)
        splitEdge();
    }
  }

  private class TrackingMarkerClickHandler implements MarkerClickHandler {
    public void onClick(MarkerClickEvent event) {
      splitEdge();
    }
  }

  private class NodeContents implements MarkerDragStartHandler, MarkerDragHandler, MarkerDragEndHandler,
      DirectionsCallback, Comparator<PathEdge> {

    private PathNode _node;

    private Marker _marker;

    private DirectionQueryOptions _opts;

    private boolean _processingDirections = false;

    private int _pendingDirectionsIndex = 0;

    private LatLng _updatedMarkerLocation = null;

    private List<PathEdge> _orderedEdges;

    private Map<PathEdge, Polyline> _updates = new HashMap<PathEdge, Polyline>();

    private boolean _dragPending = false;

    public NodeContents(PathNode node, Marker marker) {
      _node = node;
      _marker = marker;

      _opts = new DirectionQueryOptions();
      _opts.setRetrievePolyline(true);

      _marker.addMarkerDragStartHandler(this);
      _marker.addMarkerDragHandler(this);
      _marker.addMarkerDragEndHandler(this);
    }

    public Marker getMarker() {
      return _marker;
    }

    public void onDragStart(MarkerDragStartEvent event) {
      System.out.println("== onDragStart ==");
    }

    public void onDrag(MarkerDragEvent event) {

      System.out.println("== on Drag ==");
      if (_processingDirections == false) {
        startDirections();
      } else {
        _dragPending = true;
      }
    }

    private void startDirections() {
      _processingDirections = true;
      System.out.println("  looking for directions");
      _dragPending = false;
      _updatedMarkerLocation = _marker.getLatLng();
      _orderedEdges = new ArrayList<PathEdge>(_graph.getEdgesForNode(_node));
      Collections.sort(_orderedEdges, this);
      submitNextDirectionsRequest();
    }

    private boolean submitNextDirectionsRequest() {

      System.out.println("== submitNext: directionIndex=" + _pendingDirectionsIndex + " vs " + _orderedEdges.size() + " ==");

      int remaining = _orderedEdges.size() - _pendingDirectionsIndex;

      if (remaining == 0) {
        System.out.println("  no more directions to submit");
        return false;
      } else if (remaining == 1) {
        System.out.println("  single direction");
        PathEdge edge = _orderedEdges.get(_pendingDirectionsIndex);
        PathNode nodeFrom = edge.getOppositeEndPoint(_node);
        Waypoint from = new Waypoint(nodeFrom.getPoint());
        Waypoint to = new Waypoint(_updatedMarkerLocation);
        Waypoint[] wps = {from, to};
        Directions.loadFromWaypoints(wps, _opts, this);
      } else {
        System.out.println("  double direction");
        PathEdge edgeA = _orderedEdges.get(_pendingDirectionsIndex);
        PathEdge edgeB = _orderedEdges.get(_pendingDirectionsIndex + 1);
        PathNode nodeFrom = edgeA.getOppositeEndPoint(_node);
        PathNode nodeTo = edgeB.getOppositeEndPoint(_node);
        Waypoint from = new Waypoint(nodeFrom.getPoint());
        Waypoint to = new Waypoint(nodeTo.getPoint());
        Waypoint mid = new Waypoint(_updatedMarkerLocation);
        Waypoint[] wps = {from, mid, to};
        Directions.loadFromWaypoints(wps, _opts, this);
      }

      return true;
    }

    public void onDragEnd(MarkerDragEndEvent event) {
      System.out.println("== drag end ==");
      if (_dragPending) {
        System.out.println("  drag still pending");
        startDirections();
      }
    }

    public void onSuccess(DirectionResults result) {

      int size = _orderedEdges.size() - _pendingDirectionsIndex;

      System.out.println("== onSucess: directions result=" + size + " ==");

      if (size == 1) {
        System.out.println("  directions size=1");
        PathEdge edge = _orderedEdges.get(_pendingDirectionsIndex);
        _updates.put(edge, result.getPolyline());
        _pendingDirectionsIndex++;
      } else {
        System.out.println("  directions size=2");
        PathEdge edgeFrom = _orderedEdges.get(_pendingDirectionsIndex);
        PathEdge edgeTo = _orderedEdges.get(_pendingDirectionsIndex + 1);

        Polyline line = result.getPolyline();
        int closestIndex = getClosestVertext(line, _updatedMarkerLocation);

        Polyline lineA = getPolylineSubset(line, 0, closestIndex + 1);
        Polyline lineB = getPolylineSubset(line, closestIndex, line.getVertexCount());
        _updates.put(edgeFrom, lineA);
        _updates.put(edgeTo, lineB);
        _pendingDirectionsIndex += 2;
      }

      size = _orderedEdges.size() - _pendingDirectionsIndex;

      System.out.println("  remaining=" + size);

      if (size == 0) {
        System.out.println("  all done");
        _node = _graph.moveNode(_node, _updatedMarkerLocation, _updates);
        _pendingDirectionsIndex = 0;
        _processingDirections = false;
        _updates.clear();
      } else {
        System.out.println("  more directions neeeded");
        submitNextDirectionsRequest();
      }
    }

    public void onFailure(int statusCode) {
      System.out.println("failure");
      _pendingDirectionsIndex = 0;
      _processingDirections = false;
      _updates.clear();
    }

    public int compare(PathEdge o1, PathEdge o2) {
      double len1 = o1.getLine().getLength();
      double len2 = o2.getLine().getLength();
      return len1 == len2 ? 0 : (len1 > len2 ? -1 : 1);
    }

    /*
     * private int getDirectionsSize() { return _orderedEdges.size() -
     * _pendingDirectionsIndex; }
     */
  }

  private class EdgeContents implements PolylineClickHandler, PolylineMouseOverHandler, PolylineMouseOutHandler {

    private PathEdge _edge;

    public EdgeContents(PathEdge edge) {
      _edge = edge;
    }

    public PathEdge getEdge() {
      return _edge;
    }

    public Polyline getLine() {
      return _edge.getLine();
    }

    public void setMouseHandlerStatus(boolean active) {
      Polyline line = _edge.getLine();
      if (active) {
        line.addPolylineMouseOverHandler(this);
        line.addPolylineMouseOutHandler(this);
        line.addPolylineClickHandler(this);
      } else {
        line.removePolylineMouseOverHandler(this);
        line.removePolylineMouseOutHandler(this);
        line.removePolylineClickHandler(this);
      }
    }

    public void onMouseOver(PolylineMouseOverEvent event) {
      setEdgeForWaypointAddition(this, true);
    }

    public void onMouseOut(PolylineMouseOutEvent event) {
      setEdgeForWaypointAddition(this, false);
    }

    public void onClick(PolylineClickEvent event) {
      
    }

  }

  private class GraphHandler implements PathGraphListener {

    public void handleEdgeAdded(PathEdge edge) {
      System.out.println("graph: edge added");
      addEdge(edge);
    }

    public void handleEdgeRemoved(PathEdge edge) {
      System.out.println("graph: edge removed");
      removeEdge(edge);
    }

    public void handleNodeAdded(PathNode node) {
      System.out.println("graph: node added");
      addNode(node);
    }

    public void handleNodeMoved(PathNode from, PathNode toNode) {
      System.out.println("graph: node moved");
    }

    public void handleNodeRemoved(PathNode node) {
      System.out.println("graph: node removed");
      removeNode(node);
    }

  }

}
