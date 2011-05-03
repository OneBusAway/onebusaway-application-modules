package org.onebusaway.transit_data_federation.impl.blocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.onebusaway.collections.CollectionsLibrary;
import org.onebusaway.collections.FactoryMap;
import org.onebusaway.collections.MappingLibrary;
import org.onebusaway.collections.Min;
import org.onebusaway.collections.tuple.T2;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.model.XYPoint;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.bundle.tasks.block_indices.BlockSequence;
import org.onebusaway.transit_data_federation.impl.ProjectedPointFactory;
import org.onebusaway.transit_data_federation.impl.shapes.PointAndIndex;
import org.onebusaway.transit_data_federation.impl.shapes.ShapePointsLibrary;
import org.onebusaway.transit_data_federation.model.ProjectedPoint;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.services.blocks.BlockCalendarService;
import org.onebusaway.transit_data_federation.services.blocks.BlockGeospatialService;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndexService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.BlockLayoverIndex;
import org.onebusaway.transit_data_federation.services.blocks.BlockSequenceIndex;
import org.onebusaway.transit_data_federation.services.blocks.BlockTripIndex;
import org.onebusaway.transit_data_federation.services.blocks.FrequencyBlockTripIndex;
import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocation;
import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocationService;
import org.onebusaway.transit_data_federation.services.shapes.ProjectedShapePointService;
import org.onebusaway.transit_data_federation.services.shapes.ShapePointService;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.strtree.STRtree;

@Component
class BlockGeospatialServiceImpl implements BlockGeospatialService {

  private static Logger _log = LoggerFactory.getLogger(BlockGeospatialServiceImpl.class);

  private TransitGraphDao _transitGraphDao;

  private BlockCalendarService _blockCalendarService;

  private BlockIndexService _blockIndexService;

  private Map<AgencyAndId, List<BlockSequenceIndex>> _blockSequenceIndicesByShapeId = new HashMap<AgencyAndId, List<BlockSequenceIndex>>();

  private STRtree _tree = new STRtree();

  private ShapePointService _shapePointService;

  private double _gridSize = 500;

  private ProjectedShapePointService _projectedShapePointService;

  private ShapePointsLibrary _shapePointsLibrary;

  private ScheduledBlockLocationService _scheduledBlockLocationService;

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }

  @Autowired
  public void setBlockCalendarService(BlockCalendarService blockCalendarService) {
    _blockCalendarService = blockCalendarService;
  }

  @Autowired
  public void setBlockIndexService(BlockIndexService blockIndexService) {
    _blockIndexService = blockIndexService;
  }

  @Autowired
  public void set(ShapePointService shapePointService) {
    _shapePointService = shapePointService;
  }

  @Autowired
  public void setProjected(ProjectedShapePointService projectedShapePointService) {
    _projectedShapePointService = projectedShapePointService;
  }

  @Autowired
  public void setShapePointsLibrary(ShapePointsLibrary shapePointsLibrary) {
    _shapePointsLibrary = shapePointsLibrary;
  }

  @Autowired
  public void setScheduledBlockLocationService(
      ScheduledBlockLocationService scheduledBlockLocationService) {
    _scheduledBlockLocationService = scheduledBlockLocationService;
  }

  @PostConstruct
  public void setup() {
    groupBlockSequenceIndicesByShapeIds();
    buildShapeSpatialIndex();
  }

  @Override
  public List<BlockInstance> getActiveScheduledBlocksPassingThroughBounds(
      CoordinateBounds bounds, long timeFrom, long timeTo) {

    List<StopEntry> stops = _transitGraphDao.getStopsByLocation(bounds);

    Set<BlockTripIndex> blockIndices = new HashSet<BlockTripIndex>();

    for (StopEntry stop : stops) {
      // TODO : we need to fix this
      /*
       * List<BlockStopTimeIndex> stopTimeIndices =
       * _blockIndexService.getStopTimeIndicesForStop(stop); for
       * (BlockStopTimeIndex stopTimeIndex : stopTimeIndices)
       * blockIndices.add(stopTimeIndex.getBlockIndex());
       */
    }

    List<BlockLayoverIndex> layoverIndices = Collections.emptyList();
    List<FrequencyBlockTripIndex> frequencyIndices = Collections.emptyList();

    return _blockCalendarService.getActiveBlocksInTimeRange(blockIndices,
        layoverIndices, frequencyIndices, timeFrom, timeTo);
  }

  @Override
  public Set<BlockSequenceIndex> getBlockSequenceIndexPassingThroughBounds(
      CoordinateBounds bounds) {

    Envelope env = new Envelope(bounds.getMinLon(), bounds.getMaxLon(),
        bounds.getMinLat(), bounds.getMaxLat());

    @SuppressWarnings("unchecked")
    List<List<AgencyAndId>> results = _tree.query(env);

    Set<AgencyAndId> visitedShapeIds = new HashSet<AgencyAndId>();
    Set<BlockSequenceIndex> allIndices = new HashSet<BlockSequenceIndex>();

    for (List<AgencyAndId> shapeIds : results) {
      for (AgencyAndId shapeId : shapeIds) {
        if (visitedShapeIds.add(shapeId)) {
          List<BlockSequenceIndex> indices = _blockSequenceIndicesByShapeId.get(shapeId);
          if (!CollectionsLibrary.isEmpty(indices)) {
            allIndices.addAll(indices);
          }
        }
      }
    }

    return allIndices;
  }

  public ScheduledBlockLocation getBestScheduledBlockLocationForLocation(
      BlockInstance blockInstance, CoordinatePoint location, long timestamp,
      double blockDistanceFrom, double blockDistanceTo) {

    BlockConfigurationEntry block = blockInstance.getBlock();

    ProjectedPoint targetPoint = ProjectedPointFactory.forward(location);

    List<AgencyAndId> shapePointIds = MappingLibrary.map(block.getTrips(),
        "trip.shapeId");

    T2<List<XYPoint>, double[]> tuple = _projectedShapePointService.getProjectedShapePoints(
        shapePointIds, targetPoint.getSrid());

    if (tuple == null) {
      throw new IllegalStateException("block had no shape points: "
          + block.getBlock().getId());
    }

    List<XYPoint> projectedShapePoints = tuple.getFirst();
    double[] distances = tuple.getSecond();

    int fromIndex = 0;
    int toIndex = distances.length;

    if (blockDistanceFrom > 0) {
      fromIndex = Arrays.binarySearch(distances, blockDistanceFrom);
      if (fromIndex < 0) {
        fromIndex = -(fromIndex + 1);
        // Include the previous point if we didn't get an exact match
        if (fromIndex > 0)
          fromIndex--;
      }
    }

    if (blockDistanceTo < distances[distances.length - 1]) {
      toIndex = Arrays.binarySearch(distances, blockDistanceTo);
      if (toIndex < 0) {
        toIndex = -(toIndex + 1);
        // Include the previous point if we didn't get an exact match
        if (toIndex < distances.length)
          toIndex++;
      }
    }

    XYPoint xyPoint = new XYPoint(targetPoint.getX(), targetPoint.getY());

    List<PointAndIndex> assignments = _shapePointsLibrary.computePotentialAssignments(
        projectedShapePoints, distances, xyPoint, fromIndex, toIndex);

    Min<ScheduledBlockLocation> best = new Min<ScheduledBlockLocation>();

    for (PointAndIndex index : assignments) {

      double distanceAlongBlock = index.distanceAlongShape;

      if (distanceAlongBlock > block.getTotalBlockDistance())
        distanceAlongBlock = block.getTotalBlockDistance();

      ScheduledBlockLocation blockLocation = _scheduledBlockLocationService.getScheduledBlockLocationFromDistanceAlongBlock(
          block, distanceAlongBlock);

      if (blockLocation != null) {
        int scheduledTime = blockLocation.getScheduledTime();
        long scheduleTimestamp = blockInstance.getServiceDate() + scheduledTime
            * 1000;

        double delta = Math.abs(scheduleTimestamp - timestamp);
        best.add(delta, blockLocation);
      }
    }

    return best.getMinElement();
  }

  /****
   * Private Methods
   ****/

  private void groupBlockSequenceIndicesByShapeIds() {
    List<BlockSequenceIndex> indices = _blockIndexService.getAllBlockSequenceIndices();

    for (BlockSequenceIndex index : indices) {

      Set<AgencyAndId> shapeIdsForIndex = new HashSet<AgencyAndId>();

      for (BlockSequence sequence : index.getSequences()) {
        for (BlockStopTimeEntry bst : sequence.getStopTimes()) {
          BlockTripEntry blockTrip = bst.getTrip();
          TripEntry trip = blockTrip.getTrip();
          AgencyAndId shapeId = trip.getShapeId();
          if (shapeId != null)
            shapeIdsForIndex.add(shapeId);
        }
      }

      for (AgencyAndId shapeId : shapeIdsForIndex) {
        List<BlockSequenceIndex> list = _blockSequenceIndicesByShapeId.get(shapeId);
        if (list == null) {
          list = new ArrayList<BlockSequenceIndex>();
          _blockSequenceIndicesByShapeId.put(shapeId, list);
        }
        list.add(index);
      }
    }
  }

  private void buildShapeSpatialIndex() {

    CoordinateBounds fullBounds = new CoordinateBounds();
    for (StopEntry stop : _transitGraphDao.getAllStops())
      fullBounds.addPoint(stop.getStopLat(), stop.getStopLon());

    if (fullBounds.isEmpty()) {
      _tree = null;
      return;
    }

    double centerLat = (fullBounds.getMinLat() + fullBounds.getMaxLat()) / 2;
    double centerLon = (fullBounds.getMinLon() + fullBounds.getMaxLon()) / 2;
    CoordinateBounds gridCellExample = SphericalGeometryLibrary.bounds(
        centerLat, centerLon, _gridSize / 2);

    double latStep = gridCellExample.getMaxLat() - gridCellExample.getMinLat();
    double lonStep = gridCellExample.getMaxLon() - gridCellExample.getMinLon();

    Map<CoordinatePoint, Set<AgencyAndId>> shapeIdsByGridCellCorner = new FactoryMap<CoordinatePoint, Set<AgencyAndId>>(
        new HashSet<AgencyAndId>());

    _log.info("generating shape point geospatial index...");

    for (AgencyAndId shapeId : _blockSequenceIndicesByShapeId.keySet()) {

      ShapePoints shapePoints = _shapePointService.getShapePointsForShapeId(shapeId);

      for (int i = 0; i < shapePoints.getSize(); i++) {

        double lat = shapePoints.getLatForIndex(i);
        double lon = shapePoints.getLonForIndex(i);

        CoordinatePoint gridCellCorner = getGridCellCornerForPoint(lat, lon,
            latStep, lonStep);

        shapeIdsByGridCellCorner.get(gridCellCorner).add(shapeId);
      }
    }

    _log.info("block shape geospatial nodes: "
        + shapeIdsByGridCellCorner.size());

    _tree = new STRtree(shapeIdsByGridCellCorner.size());

    for (Map.Entry<CoordinatePoint, Set<AgencyAndId>> entry : shapeIdsByGridCellCorner.entrySet()) {
      CoordinatePoint p = entry.getKey();
      Envelope env = new Envelope(p.getLon(), p.getLon() + lonStep, p.getLat(),
          p.getLat() + latStep);
      List<AgencyAndId> shapeIds = new ArrayList<AgencyAndId>(entry.getValue());
      _tree.insert(env, shapeIds);
    }

    _tree.build();

  }

  private CoordinatePoint getGridCellCornerForPoint(double lat, double lon,
      double latStep, double lonStep) {

    double latCorner = Math.floor(lat / latStep) * latStep;
    double lonCorner = Math.floor(lon / lonStep) * lonStep;
    return new CoordinatePoint(latCorner, lonCorner);
  }
}
