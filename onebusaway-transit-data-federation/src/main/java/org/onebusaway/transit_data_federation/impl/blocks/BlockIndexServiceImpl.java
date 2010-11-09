package org.onebusaway.transit_data_federation.impl.blocks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.container.refresh.Refreshable;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceInterval;
import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.bundle.tasks.block_indices.BlockIndicesFactory;
import org.onebusaway.transit_data_federation.bundle.tasks.block_indices.BlockTripIndexData;
import org.onebusaway.transit_data_federation.bundle.tasks.block_indices.FrequencyBlockTripIndexData;
import org.onebusaway.transit_data_federation.impl.RefreshableResources;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopEntryImpl;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndexService;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.blocks.BlockTripIndex;
import org.onebusaway.transit_data_federation.services.blocks.FrequencyBlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.blocks.FrequencyBlockTripIndex;
import org.onebusaway.transit_data_federation.services.blocks.HasBlockTrips;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.utility.ObjectSerializationLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BlockIndexServiceImpl implements BlockIndexService {

  private Logger _log = LoggerFactory.getLogger(BlockIndexServiceImpl.class);

  private FederatedTransitDataBundle _bundle;

  private TransitGraphDao _graphDao;

  private List<BlockTripIndex> _blockTripIndices;

  private Map<String, List<BlockTripIndex>> _blockTripIndicesByAgencyId;

  private Map<AgencyAndId, List<BlockTripIndex>> _blockTripIndicesByRouteId;

  private Map<AgencyAndId, List<BlockTripIndex>> _blockTripIndicesByBlockId;

  private List<FrequencyBlockTripIndex> _frequencyBlockTripIndices;

  private Map<String, List<FrequencyBlockTripIndex>> _frequencyBlockTripIndicesByAgencyId;

  private Map<AgencyAndId, List<FrequencyBlockTripIndex>> _frequencyBlockTripIndicesByRouteId;

  private Map<AgencyAndId, List<FrequencyBlockTripIndex>> _frequencyBlockTripIndicesByBlockId;

  @Autowired
  public void setBundle(FederatedTransitDataBundle bundle) {
    _bundle = bundle;
  }

  @Autowired
  public void setGraphDao(TransitGraphDao graphDao) {
    _graphDao = graphDao;
  }

  @PostConstruct
  @Refreshable(dependsOn = RefreshableResources.BLOCK_INDEX_DATA)
  public void setup() throws Exception {

    loadBlockTripIndices();
    loadFrequencyBlockTripIndices();
    loadBlockTripIndicesByBlockId();
  }

  @Override
  public List<BlockTripIndex> getBlockTripIndices() {
    return _blockTripIndices;
  }

  @Override
  public List<BlockTripIndex> getBlockTripIndicesForAgencyId(String agencyId) {
    return list(_blockTripIndicesByAgencyId.get(agencyId));
  }

  @Override
  public List<BlockTripIndex> getBlockTripIndicesForRouteCollectionId(
      AgencyAndId routeCollectionId) {
    return list(_blockTripIndicesByRouteId.get(routeCollectionId));
  }

  @Override
  public List<BlockTripIndex> getBlockTripIndicesForBlock(AgencyAndId blockId) {
    return list(_blockTripIndicesByBlockId.get(blockId));
  }

  @Override
  public List<BlockStopTimeIndex> getStopTimeIndicesForStop(StopEntry stopEntry) {
    return ((StopEntryImpl) stopEntry).getStopTimeIndices();
  }

  @Override
  public List<FrequencyBlockTripIndex> getFrequencyBlockTripIndices() {
    return _frequencyBlockTripIndices;
  }

  @Override
  public List<FrequencyBlockTripIndex> getFrequencyBlockTripIndicesForAgencyId(
      String agencyId) {
    return list(_frequencyBlockTripIndicesByAgencyId.get(agencyId));
  }

  @Override
  public List<FrequencyBlockTripIndex> getFrequencyBlockTripIndicesForRouteCollectionId(
      AgencyAndId routeCollectionId) {
    return list(_frequencyBlockTripIndicesByRouteId.get(routeCollectionId));
  }

  @Override
  public List<FrequencyBlockTripIndex> getFrequencyBlockTripIndicesForBlock(
      AgencyAndId blockId) {
    return list(_frequencyBlockTripIndicesByBlockId.get(blockId));
  }

  @Override
  public List<FrequencyBlockStopTimeIndex> getFrequencyStopTimeIndicesForStop(
      StopEntry stopEntry) {
    return ((StopEntryImpl) stopEntry).getFrequencyStopTimeIndices();
  }

  /****
   * Private Methods
   ****/

  private <T> List<T> list(List<T> list) {
    if (list == null)
      return Collections.emptyList();
    return list;
  }

  /****
   * 
   ****/

  private void loadBlockTripIndices() throws IOException,
      ClassNotFoundException {

    File path = _bundle.getBlockTripIndicesPath();

    if (path.exists()) {

      _log.info("loading block trip indices data");

      List<BlockTripIndexData> datas = ObjectSerializationLibrary.readObject(path);

      _blockTripIndices = new ArrayList<BlockTripIndex>(datas.size());
      for (BlockTripIndexData data : datas)
        _blockTripIndices.add(data.createIndex(_graphDao));

      _blockTripIndicesByAgencyId = getBlockTripIndicesByAgencyId(_blockTripIndices);
      _blockTripIndicesByRouteId = getBlockTripsByRouteId(_blockTripIndices);

      _log.info("block indices data loaded");

      clearExistingStopTimeIndices();
      setupBlockStopTimeIndices();

    } else {

      _blockTripIndices = Collections.emptyList();
      _blockTripIndicesByAgencyId = Collections.emptyMap();
      _blockTripIndicesByRouteId = Collections.emptyMap();

      clearExistingStopTimeIndices();
    }
  }

  private void loadFrequencyBlockTripIndices() throws IOException,
      ClassNotFoundException {

    File path = _bundle.getFrequencyBlockTripIndicesPath();

    if (path.exists()) {

      _log.info("loading frequency block trip indices data");

      List<FrequencyBlockTripIndexData> datas = ObjectSerializationLibrary.readObject(path);

      _frequencyBlockTripIndices = new ArrayList<FrequencyBlockTripIndex>(
          datas.size());
      for (FrequencyBlockTripIndexData data : datas)
        _frequencyBlockTripIndices.add(data.createIndex(_graphDao));

      _frequencyBlockTripIndicesByAgencyId = getBlockTripIndicesByAgencyId(_frequencyBlockTripIndices);
      _frequencyBlockTripIndicesByRouteId = getBlockTripsByRouteId(_frequencyBlockTripIndices);

      _log.info("block frequency trip indices data loaded");

      clearExistingFrequencyStopTimeIndices();
      setupFrequencyBlockStopTimeIndices();

    } else {

      _frequencyBlockTripIndices = Collections.emptyList();
      _frequencyBlockTripIndicesByAgencyId = Collections.emptyMap();
      _frequencyBlockTripIndicesByRouteId = Collections.emptyMap();

      clearExistingFrequencyStopTimeIndices();
    }
  }

  /****
   * 
   ****/

  private <T extends HasBlockTrips> Map<String, List<T>> getBlockTripIndicesByAgencyId(
      List<T> indices) {

    Map<String, List<T>> blocksByAgencyId = new FactoryMap<String, List<T>>(
        new ArrayList<T>());

    for (T blockIndex : indices) {
      Set<String> agencyIds = new HashSet<String>();
      for (BlockTripEntry blockTrip : blockIndex.getTrips())
        agencyIds.add(blockTrip.getTrip().getId().getAgencyId());
      for (String agencyId : agencyIds)
        blocksByAgencyId.get(agencyId).add(blockIndex);
    }
    return blocksByAgencyId;
  }

  private <T extends HasBlockTrips> Map<AgencyAndId, List<T>> getBlockTripsByRouteId(
      List<T> indices) {

    Map<AgencyAndId, List<T>> blocksByRouteId = new FactoryMap<AgencyAndId, List<T>>(
        new ArrayList<T>());

    for (T index : indices) {
      Set<AgencyAndId> routeIds = new HashSet<AgencyAndId>();
      for (BlockTripEntry blockTrip : index.getTrips())
        routeIds.add(blockTrip.getTrip().getRouteCollectionId());
      for (AgencyAndId routeId : routeIds)
        blocksByRouteId.get(routeId).add(index);
    }
    return blocksByRouteId;
  }

  /****
   * 
   ****/

  private void setupBlockStopTimeIndices() {

    for (BlockTripIndex blockTripIndex : _blockTripIndices) {

      List<BlockTripEntry> trips = blockTripIndex.getTrips();

      System.out.println("trips=" + trips.size());

      BlockTripEntry firstTrip = trips.get(0);
      BlockTripEntry lastTrip = trips.get(trips.size() - 1);

      List<BlockStopTimeEntry> firstStopTimes = firstTrip.getStopTimes();
      List<BlockStopTimeEntry> lastStopTimes = lastTrip.getStopTimes();

      int n = firstStopTimes.size();

      for (int i = 0; i < n; i++) {

        BlockStopTimeEntry firstStopTime = firstStopTimes.get(i);
        BlockStopTimeEntry lastStopTime = lastStopTimes.get(i);

        ServiceInterval serviceInterval = getStopTimesAsServiceInterval(
            firstStopTime, lastStopTime);

        BlockStopTimeIndex blockStopTimeIndex = new BlockStopTimeIndex(
            blockTripIndex, i, serviceInterval);

        StopEntryImpl stop = (StopEntryImpl) firstStopTime.getStopTime().getStop();
        stop.addStopTimeIndex(blockStopTimeIndex);
      }
    }

    int stops = 0;
    int stopIndices = 0;

    for (StopEntry stop : _graphDao.getAllStops()) {
      StopEntryImpl stopImpl = (StopEntryImpl) stop;
      List<BlockStopTimeIndex> stopTimeIndices = stopImpl.getStopTimeIndices();
      stopIndices += stopTimeIndices.size();
      stops++;
    }

    if (stops == 0) {
      _log.info("no stop indices loaded");
    } else {
      _log.info("stops=" + stops + " stop_indices=" + stopIndices
          + " avg (stop_indices/stop)=" + (stopIndices / stops));
    }

  }

  public void clearExistingStopTimeIndices() {
    // Clear any existing indices
    for (StopEntry stop : _graphDao.getAllStops()) {
      StopEntryImpl stopImpl = (StopEntryImpl) stop;
      stopImpl.getStopTimeIndices().clear();
    }
  }

  /****
   * 
   ****/

  private void setupFrequencyBlockStopTimeIndices() {

    for (FrequencyBlockTripIndex blockIndex : _frequencyBlockTripIndices) {

      List<BlockTripEntry> trips = blockIndex.getTrips();

      BlockTripEntry firstTrip = trips.get(0);

      List<BlockStopTimeEntry> firstStopTimes = firstTrip.getStopTimes();

      List<FrequencyEntry> frequencies = blockIndex.getFrequencies();
      FrequencyEntry firstFrequency = frequencies.get(0);
      FrequencyEntry lastFrequency = frequencies.get(frequencies.size() - 1);

      int n = firstStopTimes.size();

      for (int i = 0; i < n; i++) {

        BlockStopTimeEntry firstStopTime = firstStopTimes.get(i);

        ServiceInterval serviceInterval = new ServiceInterval(
            firstFrequency.getStartTime(), lastFrequency.getEndTime());

        FrequencyBlockStopTimeIndex blockStopTimeIndex = new FrequencyBlockStopTimeIndex(
            blockIndex, i, serviceInterval);

        StopEntryImpl stop = (StopEntryImpl) firstStopTime.getStopTime().getStop();
        stop.addFrequencyStopTimeIndex(blockStopTimeIndex);
      }
    }

  }

  public void clearExistingFrequencyStopTimeIndices() {
    // Clear any existing indices
    for (StopEntry stop : _graphDao.getAllStops()) {
      StopEntryImpl stopImpl = (StopEntryImpl) stop;
      stopImpl.getFrequencyStopTimeIndices().clear();
    }
  }

  /****
   * 
   ****/

  private void loadBlockTripIndicesByBlockId() {

    _log.info("calculating block trip indices by blockId...");
    long t1 = System.currentTimeMillis();

    _blockTripIndicesByBlockId = new HashMap<AgencyAndId, List<BlockTripIndex>>();
    _frequencyBlockTripIndicesByBlockId = new HashMap<AgencyAndId, List<FrequencyBlockTripIndex>>();

    for (BlockEntry block : _graphDao.getAllBlocks()) {
      BlockIndicesFactory factory = new BlockIndicesFactory();
      List<BlockEntry> list = Arrays.asList(block);
      List<BlockTripIndex> indices = factory.createTripIndices(list);
      List<FrequencyBlockTripIndex> frequencyIndices = factory.createFrequencyTripIndices(list);

      if (!indices.isEmpty())
        _blockTripIndicesByBlockId.put(block.getId(), indices);
      if (!frequencyIndices.isEmpty())
        _frequencyBlockTripIndicesByBlockId.put(block.getId(), frequencyIndices);
    }

    long t2 = System.currentTimeMillis();
    _log.info("completed calculating block trip indices by blockId: t="
        + (t2 - t1));
  }

  /****
   * 
   ****/

  private ServiceInterval getStopTimesAsServiceInterval(
      BlockStopTimeEntry firstStopTime, BlockStopTimeEntry lastStopTime) {

    StopTimeEntry st0 = firstStopTime.getStopTime();
    StopTimeEntry st1 = lastStopTime.getStopTime();

    return new ServiceInterval(st0.getArrivalTime(), st0.getDepartureTime(),
        st1.getArrivalTime(), st1.getDepartureTime());
  }

}
