package org.onebusaway.where.services;

import com.vividsolutions.jts.geom.Point;

import org.onebusaway.common.model.Layer;
import org.onebusaway.common.model.Region;
import org.onebusaway.gtdf.model.Route;
import org.onebusaway.gtdf.model.Stop;
import org.onebusaway.where.model.LocationBookmarks;
import org.onebusaway.where.model.StopSequence;
import org.onebusaway.where.model.StopSequenceBlock;
import org.onebusaway.where.model.Timepoint;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

public interface WhereDao {
  public List<Timepoint> getTimepointsByTripIds(Set<String> tripIds);

  public LocationBookmarks getBookmarksByUserId(String userId);

  public void save(Object object);

  public void update(Object object);

  public void saveOrUpdate(Object object);

  public <T> void saveOrUpdateAllEntities(List<T> updates);

  public List<StopSequence> getStopSequencesByRoute(Route route);

  public List<StopSequence> getStopSequencesByRouteAndDirectionId(Route route,
      String directionId);

  public List<StopSequenceBlock> getStopSequenceBlocksByRoute(Route route);
  
  public List<StopSequenceBlock> getStopSequenceBlocksByStop(Stop stop);

  public List<Region> getRegionsByLocation(Point p);

  public SortedMap<Layer, Region> getRegionsByStop(Stop stop);

  public Map<Stop, SortedMap<Layer, Region>> getRegionsByStops(
      List<Stop> ordered);
}
