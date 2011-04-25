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
import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.collections.tuple.Tuples;
import org.onebusaway.container.refresh.Refreshable;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.bundle.tasks.block_indices.BlockIndexFactory;
import org.onebusaway.transit_data_federation.bundle.tasks.block_indices.BlockLayoverIndexData;
import org.onebusaway.transit_data_federation.bundle.tasks.block_indices.BlockSequence;
import org.onebusaway.transit_data_federation.bundle.tasks.block_indices.BlockStopTimeIndicesFactory;
import org.onebusaway.transit_data_federation.bundle.tasks.block_indices.BlockTripIndexData;
import org.onebusaway.transit_data_federation.bundle.tasks.block_indices.FrequencyBlockTripIndexData;
import org.onebusaway.transit_data_federation.impl.RefreshableResources;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopEntryImpl;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndexService;
import org.onebusaway.transit_data_federation.services.blocks.BlockLayoverIndex;
import org.onebusaway.transit_data_federation.services.blocks.BlockSequenceIndex;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopSequenceIndex;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.blocks.BlockTripIndex;
import org.onebusaway.transit_data_federation.services.blocks.FrequencyBlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.blocks.FrequencyBlockTripIndex;
import org.onebusaway.transit_data_federation.services.blocks.FrequencyStopTripIndex;
import org.onebusaway.transit_data_federation.services.blocks.HasBlockTrips;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
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

  private BlockIndexFactory _factory = new BlockIndexFactory();

  private List<BlockTripIndex> _blockTripIndices;

  private Map<String, List<BlockTripIndex>> _blockTripIndicesByAgencyId;

  private Map<AgencyAndId, List<BlockTripIndex>> _blockTripIndicesByRouteId;

  private Map<AgencyAndId, List<BlockTripIndex>> _blockTripIndicesByBlockId;

  private List<BlockLayoverIndex> _blockLayoverIndices;

  private Map<String, List<BlockLayoverIndex>> _blockLayoverIndicesByAgencyId;

  private Map<AgencyAndId, List<BlockLayoverIndex>> _blockLayoverIndicesByRouteId;

  private Map<AgencyAndId, List<BlockLayoverIndex>> _blockLayoverIndicesByBlockId;

  private List<FrequencyBlockTripIndex> _frequencyBlockTripIndices;

  private Map<String, List<FrequencyBlockTripIndex>> _frequencyBlockTripIndicesByAgencyId;

  private Map<AgencyAndId, List<FrequencyBlockTripIndex>> _frequencyBlockTripIndicesByRouteId;

  private Map<AgencyAndId, List<FrequencyBlockTripIndex>> _frequencyBlockTripIndicesByBlockId;

  private List<BlockSequenceIndex> _blockSequenceIndices = Collections.emptyList();

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
    loadBlockLayoverIndices();
    loadFrequencyBlockTripIndices();
    loadBlockTripIndicesByBlockId();

    loadBlockSequenceIndices();

    loadStopTimeIndices();
    loadStopTripIndices();
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
  public List<BlockStopSequenceIndex> getStopSequenceIndicesForStop(
      StopEntry stopEntry) {
    return ((StopEntryImpl) stopEntry).getStopTripIndices();
  }

  @Override
  public List<Pair<BlockStopSequenceIndex>> getBlockSequenceIndicesBetweenStops(
      StopEntry fromStop, StopEntry toStop) {

    List<BlockStopSequenceIndex> fromIndices = ((StopEntryImpl) fromStop).getStopTripIndices();
    List<BlockStopSequenceIndex> toIndices = ((StopEntryImpl) toStop).getStopTripIndices();

    Map<BlockSequenceIndex, BlockStopSequenceIndex> fromIndicesBySequence = getBlockStopSequenceIndicesBySequence(fromIndices);
    Map<BlockSequenceIndex, BlockStopSequenceIndex> toIndicesBySequence = getBlockStopSequenceIndicesBySequence(toIndices);

    fromIndicesBySequence.keySet().retainAll(toIndicesBySequence.keySet());

    List<Pair<BlockStopSequenceIndex>> results = new ArrayList<Pair<BlockStopSequenceIndex>>();

    for (Map.Entry<BlockSequenceIndex, BlockStopSequenceIndex> entry : fromIndicesBySequence.entrySet()) {
      BlockSequenceIndex index = entry.getKey();
      BlockStopSequenceIndex fromStopIndex = entry.getValue();
      BlockStopSequenceIndex toStopIndex = toIndicesBySequence.get(index);

      /**
       * If the stops aren't in the requested order, then we don't include the
       * sequence indices in the results
       */
      if (fromStopIndex.getOffset() > toStopIndex.getOffset())
        continue;

      Pair<BlockStopSequenceIndex> pair = Tuples.pair(fromStopIndex,
          toStopIndex);
      results.add(pair);
    }

    return results;
  }

  @Override
  public List<BlockLayoverIndex> getBlockLayoverIndicesForAgencyId(
      String agencyId) {
    return list(_blockLayoverIndicesByAgencyId.get(agencyId));
  }

  @Override
  public List<BlockLayoverIndex> getBlockLayoverIndicesForRouteCollectionId(
      AgencyAndId routeCollectionId) {
    return list(_blockLayoverIndicesByRouteId.get(routeCollectionId));
  }

  @Override
  public List<BlockLayoverIndex> getBlockLayoverIndicesForBlock(
      AgencyAndId blockId) {
    return list(_blockLayoverIndicesByBlockId.get(blockId));
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

  @Override
  public List<FrequencyStopTripIndex> getFrequencyStopTripIndicesForStop(
      StopEntry stop) {
    return ((StopEntryImpl) stop).getFrequencyStopTripIndices();
  }

  @Override
  public List<Pair<FrequencyStopTripIndex>> getFrequencyIndicesBetweenStops(
      StopEntry fromStop, StopEntry toStop) {

    List<FrequencyStopTripIndex> fromIndices = getFrequencyStopTripIndicesForStop(fromStop);
    List<FrequencyStopTripIndex> toIndices = getFrequencyStopTripIndicesForStop(toStop);

    Map<FrequencyBlockTripIndex, FrequencyStopTripIndex> fromIndicesBySequence = getFrequencyStopTripIndicesBySequence(fromIndices);
    Map<FrequencyBlockTripIndex, FrequencyStopTripIndex> toIndicesBySequence = getFrequencyStopTripIndicesBySequence(toIndices);

    fromIndicesBySequence.keySet().retainAll(toIndicesBySequence.keySet());

    List<Pair<FrequencyStopTripIndex>> results = new ArrayList<Pair<FrequencyStopTripIndex>>();

    for (Map.Entry<FrequencyBlockTripIndex, FrequencyStopTripIndex> entry : fromIndicesBySequence.entrySet()) {
      FrequencyBlockTripIndex index = entry.getKey();
      FrequencyStopTripIndex fromStopIndex = entry.getValue();
      FrequencyStopTripIndex toStopIndex = toIndicesBySequence.get(index);

      /**
       * If the stops aren't in the requested order, then we don't include the
       * sequence indices in the results
       */
      if (fromStopIndex.getOffset() > toStopIndex.getOffset())
        continue;

      Pair<FrequencyStopTripIndex> pair = Tuples.pair(fromStopIndex,
          toStopIndex);
      results.add(pair);
    }

    return results;
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

    } else {

      _blockTripIndices = Collections.emptyList();
      _blockTripIndicesByAgencyId = Collections.emptyMap();
      _blockTripIndicesByRouteId = Collections.emptyMap();
    }
  }

  private void loadBlockLayoverIndices() throws IOException,
      ClassNotFoundException {

    File path = _bundle.getBlockLayoverIndicesPath();

    if (path.exists()) {

      _log.info("loading block layover indices data");

      List<BlockLayoverIndexData> datas = ObjectSerializationLibrary.readObject(path);

      _blockLayoverIndices = new ArrayList<BlockLayoverIndex>(datas.size());
      for (BlockLayoverIndexData data : datas)
        _blockLayoverIndices.add(data.createIndex(_graphDao));

      _blockLayoverIndicesByAgencyId = getBlockTripIndicesByAgencyId(_blockLayoverIndices);
      _blockLayoverIndicesByRouteId = getBlockTripsByRouteId(_blockLayoverIndices);

      _log.info("block layover indices data loaded");

    } else {

      _blockLayoverIndices = Collections.emptyList();
      _blockLayoverIndicesByAgencyId = Collections.emptyMap();
      _blockLayoverIndicesByRouteId = Collections.emptyMap();
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

    } else {

      _frequencyBlockTripIndices = Collections.emptyList();
      _frequencyBlockTripIndicesByAgencyId = Collections.emptyMap();
      _frequencyBlockTripIndicesByRouteId = Collections.emptyMap();
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

  private void loadBlockTripIndicesByBlockId() {

    _log.info("calculating block trip indices by blockId...");
    long t1 = System.currentTimeMillis();

    _blockTripIndicesByBlockId = new HashMap<AgencyAndId, List<BlockTripIndex>>();
    _blockLayoverIndicesByBlockId = new HashMap<AgencyAndId, List<BlockLayoverIndex>>();
    _frequencyBlockTripIndicesByBlockId = new HashMap<AgencyAndId, List<FrequencyBlockTripIndex>>();

    for (BlockEntry block : _graphDao.getAllBlocks()) {
      List<BlockEntry> list = Arrays.asList(block);
      List<BlockTripIndex> indices = _factory.createTripIndices(list);
      List<BlockLayoverIndex> layoverIndices = _factory.createLayoverIndices(list);
      List<FrequencyBlockTripIndex> frequencyIndices = _factory.createFrequencyTripIndices(list);

      if (!indices.isEmpty())
        _blockTripIndicesByBlockId.put(block.getId(), indices);
      if (!layoverIndices.isEmpty())
        _blockLayoverIndicesByBlockId.put(block.getId(), layoverIndices);
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

  private void loadBlockSequenceIndices() throws IOException,
      ClassNotFoundException {

    _blockSequenceIndices = _factory.createSequenceIndices(_graphDao.getAllBlocks());
  }

  private void loadStopTimeIndices() {

    // Clear any existing indices
    for (StopEntry stop : _graphDao.getAllStops()) {
      StopEntryImpl stopImpl = (StopEntryImpl) stop;
      stopImpl.getStopTimeIndices().clear();
      stopImpl.getFrequencyStopTimeIndices().clear();
    }

    BlockStopTimeIndicesFactory factory = new BlockStopTimeIndicesFactory();
    factory.setVerbose(true);
    List<BlockStopTimeIndex> indices = factory.createIndices(_graphDao.getAllBlocks());

    for (BlockStopTimeIndex index : indices) {
      StopEntryImpl stop = (StopEntryImpl) index.getStop();
      stop.addStopTimeIndex(index);
    }

    List<FrequencyBlockStopTimeIndex> frequencyIndices = factory.createFrequencyIndices(_graphDao.getAllBlocks());

    for (FrequencyBlockStopTimeIndex index : frequencyIndices) {
      StopEntryImpl stop = (StopEntryImpl) index.getStop();
      stop.addFrequencyStopTimeIndex(index);
    }
  }

  private void loadStopTripIndices() {

    // Clear any existing indices
    for (StopEntry stop : _graphDao.getAllStops()) {
      StopEntryImpl stopImpl = (StopEntryImpl) stop;
      stopImpl.getStopTripIndices().clear();
      stopImpl.getFrequencyStopTripIndices().clear();
    }

    for (BlockSequenceIndex index : _blockSequenceIndices) {

      BlockSequence sequence = index.getSequences().get(0);

      int offset = 0;

      for (BlockStopTimeEntry bst : sequence.getStopTimes()) {

        StopTimeEntry stopTime = bst.getStopTime();
        StopEntryImpl stop = (StopEntryImpl) stopTime.getStop();

        BlockStopSequenceIndex blockStopTripIndex = new BlockStopSequenceIndex(
            index, offset);

        stop.addBlockStopTripIndex(blockStopTripIndex);
        offset++;
      }
    }

    for (FrequencyBlockTripIndex index : _frequencyBlockTripIndices) {

      BlockTripEntry trip = index.getTrips().get(0);

      int offset = 0;

      for (BlockStopTimeEntry bst : trip.getStopTimes()) {

        StopTimeEntry stopTime = bst.getStopTime();
        StopEntryImpl stop = (StopEntryImpl) stopTime.getStop();

        FrequencyStopTripIndex stopTripIndex = new FrequencyStopTripIndex(
            index, offset);
        stop.addFrequencyStopTripIndex(stopTripIndex);
        offset++;
      }
    }
  }

  private Map<BlockSequenceIndex, BlockStopSequenceIndex> getBlockStopSequenceIndicesBySequence(
      List<BlockStopSequenceIndex> indices) {

    Map<BlockSequenceIndex, BlockStopSequenceIndex> m = new HashMap<BlockSequenceIndex, BlockStopSequenceIndex>();

    for (BlockStopSequenceIndex index : indices)
      m.put(index.getIndex(), index);

    return m;
  }

  private Map<FrequencyBlockTripIndex, FrequencyStopTripIndex> getFrequencyStopTripIndicesBySequence(
      List<FrequencyStopTripIndex> indices) {

    Map<FrequencyBlockTripIndex, FrequencyStopTripIndex> m = new HashMap<FrequencyBlockTripIndex, FrequencyStopTripIndex>();

    for (FrequencyStopTripIndex index : indices)
      m.put(index.getIndex(), index);

    return m;
  }
}
