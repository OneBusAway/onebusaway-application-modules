package org.onebusaway.transit_data_federation.impl.tripplanner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.collections.tuple.Tuples;
import org.onebusaway.container.refresh.Refreshable;
import org.onebusaway.transit_data_federation.impl.RefreshableResources;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopEntryImpl;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.tripplanner.StopHop;
import org.onebusaway.transit_data_federation.services.tripplanner.StopHopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StopHopServiceImpl implements StopHopService {

  private static final StopHopList EMPTY_HOPS = new StopHopList(
      new ArrayList<StopHop>());

  private TransitGraphDao _transitGraphDao;

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }

  @PostConstruct
  @Refreshable(dependsOn = RefreshableResources.TRANSIT_GRAPH)
  public void setup() throws IOException, ClassNotFoundException {

    Map<Pair<StopEntry>, Integer> minTravelTimes = computeStopHopPairs();

    Map<StopEntry, List<StopHop>> allHopsFromStop = new HashMap<StopEntry, List<StopHop>>();
    Map<StopEntry, List<StopHop>> allHopsToStop = new HashMap<StopEntry, List<StopHop>>();

    computeHops(minTravelTimes, allHopsFromStop, allHopsToStop);

    for (StopEntry stop : _transitGraphDao.getAllStops()) {

      List<StopHop> hopsFromStop = allHopsFromStop.get(stop);
      List<StopHop> hopsToStop = allHopsToStop.get(stop);

      StopEntryImpl stopEntry = (StopEntryImpl) stop;
      stopEntry.setHops(new StopHops(hopsFromStop, hopsToStop));
    }
  }

  private void computeHops(Map<Pair<StopEntry>, Integer> minTravelTimes,
      Map<StopEntry, List<StopHop>> allHopsFromStop,
      Map<StopEntry, List<StopHop>> allHopsToStop) {

    for (Map.Entry<Pair<StopEntry>, Integer> entry : minTravelTimes.entrySet()) {

      Pair<StopEntry> pair = entry.getKey();
      StopEntry fromStop = pair.getFirst();
      StopEntry toStop = pair.getSecond();

      int minTravelTime = entry.getValue();

      /**
       * From Stop
       */
      List<StopHop> hopsFromStop = allHopsFromStop.get(fromStop);

      if (hopsFromStop == null) {
        hopsFromStop = new ArrayList<StopHop>();
        allHopsFromStop.put(fromStop, hopsFromStop);
      }

      hopsFromStop.add(new StopHop(toStop, minTravelTime));

      /**
       * To Stop
       */
      List<StopHop> hopsToStop = allHopsToStop.get(toStop);

      if (hopsToStop == null) {
        hopsToStop = new ArrayList<StopHop>();
        allHopsToStop.put(fromStop, hopsToStop);
      }

      hopsToStop.add(new StopHop(fromStop, minTravelTime));
    }
    
    for( Map.Entry<StopEntry, List<StopHop>> entry : allHopsFromStop.entrySet()) {
      List<StopHop> value = entry.getValue();
      value = new StopHopList(value);
      entry.setValue(value);
    }
    
    for( Map.Entry<StopEntry, List<StopHop>> entry : allHopsToStop.entrySet()) {
      List<StopHop> value = entry.getValue();
      value = new StopHopList(value);
      entry.setValue(value);
    }
  }

  private Map<Pair<StopEntry>, Integer> computeStopHopPairs() {
    Map<Pair<StopEntry>, Integer> minTravelTimes = new HashMap<Pair<StopEntry>, Integer>();

    for (BlockEntry block : _transitGraphDao.getAllBlocks()) {
      for (BlockConfigurationEntry blockConfig : block.getConfigurations()) {

        List<BlockStopTimeEntry> stopTimes = blockConfig.getStopTimes();
        BlockStopTimeEntry prevBlockStopTime = null;

        for (BlockStopTimeEntry blockStopTime : stopTimes) {

          if (prevBlockStopTime != null) {

            StopTimeEntry from = prevBlockStopTime.getStopTime();
            StopTimeEntry to = blockStopTime.getStopTime();
            int time = to.getArrivalTime() - from.getDepartureTime();

            StopEntry stopFrom = from.getStop();
            StopEntry stopTo = to.getStop();

            Pair<StopEntry> stopPair = Tuples.pair(stopFrom, stopTo);

            Integer prevTime = minTravelTimes.get(stopPair);
            if (prevTime == null || time < prevTime)
              minTravelTimes.put(stopPair, time);
          }

          prevBlockStopTime = blockStopTime;
        }
      }
    }
    return minTravelTimes;
  }

  @Override
  public List<StopHop> getHopsFromStop(StopEntry stop) {
    StopEntryImpl impl = (StopEntryImpl) stop;
    StopHops hops = impl.getHops();
    if (hops == null || hops.getHopsFromStop() == null)
      return EMPTY_HOPS;
    return hops.getHopsFromStop();
  }

  @Override
  public List<StopHop> getHopsToStop(StopEntry stop) {
    StopEntryImpl impl = (StopEntryImpl) stop;
    StopHops hops = impl.getHops();
    if (hops == null || hops.getHopsToStop() == null)
      return EMPTY_HOPS;
    return hops.getHopsToStop();
  }
}
