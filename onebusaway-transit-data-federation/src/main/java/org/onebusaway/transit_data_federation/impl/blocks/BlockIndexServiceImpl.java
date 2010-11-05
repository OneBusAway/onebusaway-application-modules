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
import org.onebusaway.transit_data_federation.bundle.tasks.block_indices.BlockIndexData;
import org.onebusaway.transit_data_federation.bundle.tasks.block_indices.BlockIndicesFactory;
import org.onebusaway.transit_data_federation.bundle.tasks.block_indices.FrequencyBlockIndexData;
import org.onebusaway.transit_data_federation.impl.RefreshableResources;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopEntryImpl;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndex;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndexService;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.blocks.FrequencyBlockIndex;
import org.onebusaway.transit_data_federation.services.blocks.FrequencyBlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.blocks.HasBlocks;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
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

  private List<BlockIndex> _blockIndices;

  private Map<String, List<BlockIndex>> _blockIndicesByAgencyId;

  private Map<AgencyAndId, List<BlockIndex>> _blockIndicesByRouteId;

  private Map<AgencyAndId, List<BlockIndex>> _blockIndicesByBlockId;

  private List<FrequencyBlockIndex> _frequencyBlockIndices;

  private Map<String, List<FrequencyBlockIndex>> _frequencyBlockIndicesByAgencyId;

  private Map<AgencyAndId, List<FrequencyBlockIndex>> _frequencyBlockIndicesByBlockId;

  private Map<AgencyAndId, List<FrequencyBlockIndex>> _frequencyBlockIndicesByRouteId;

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

    loadBlockIndices();
    loadFrequencyBlockIndices();
  }

  @Override
  public List<BlockIndex> getBlockIndices() {
    return _blockIndices;
  }

  @Override
  public List<BlockIndex> getBlockIndicesForAgencyId(String agencyId) {
    return list(_blockIndicesByAgencyId.get(agencyId));
  }

  @Override
  public List<BlockIndex> getBlockIndicesForRouteCollectionId(
      AgencyAndId routeCollectionId) {
    return list(_blockIndicesByRouteId.get(routeCollectionId));
  }

  @Override
  public List<BlockIndex> getBlockIndicesForBlock(AgencyAndId blockId) {
    return list(_blockIndicesByBlockId.get(blockId));
  }

  @Override
  public List<BlockStopTimeIndex> getStopTimeIndicesForStop(StopEntry stopEntry) {
    return ((StopEntryImpl) stopEntry).getStopTimeIndices();
  }

  @Override
  public List<FrequencyBlockIndex> getFrequencyBlockIndices() {
    return _frequencyBlockIndices;
  }

  @Override
  public List<FrequencyBlockIndex> getFrequencyBlockIndicesForAgencyId(
      String agencyId) {
    return list(_frequencyBlockIndicesByAgencyId.get(agencyId));
  }

  @Override
  public List<FrequencyBlockIndex> getFrequencyBlockIndicesForRouteCollectionId(
      AgencyAndId routeCollectionId) {
    return list(_frequencyBlockIndicesByRouteId.get(routeCollectionId));
  }

  @Override
  public List<FrequencyBlockIndex> getFrequencyBlockIndicesForBlock(
      AgencyAndId blockId) {
    return list(_frequencyBlockIndicesByBlockId.get(blockId));
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

  private void loadBlockIndices() throws IOException, ClassNotFoundException {

    File path = _bundle.getBlockIndicesPath();

    if (path.exists()) {

      _log.info("loading block indices data");

      List<BlockIndexData> datas = ObjectSerializationLibrary.readObject(path);

      _blockIndices = new ArrayList<BlockIndex>(datas.size());
      for (BlockIndexData data : datas)
        _blockIndices.add(data.createIndex(_graphDao));

      _blockIndicesByAgencyId = getBlockIndicesByAgencyId(_blockIndices);
      _blockIndicesByRouteId = getBlocksByRouteId(_blockIndices);

      _log.info("block indices data loaded");

      clearExistingStopTimeIndices();
      setupBlockStopTimeIndices();

    } else {

      _blockIndices = Collections.emptyList();
      _blockIndicesByAgencyId = Collections.emptyMap();
      _blockIndicesByRouteId = Collections.emptyMap();

      clearExistingStopTimeIndices();
    }

    _log.info("calculating block indices by blockId...");
    long t1 = System.currentTimeMillis();

    _blockIndicesByBlockId = new HashMap<AgencyAndId, List<BlockIndex>>();
    _frequencyBlockIndicesByBlockId = new HashMap<AgencyAndId, List<FrequencyBlockIndex>>();

    for (BlockEntry block : _graphDao.getAllBlocks()) {
      BlockIndicesFactory factory = new BlockIndicesFactory();
      List<BlockEntry> list = Arrays.asList(block);
      List<BlockIndex> indices = factory.createIndices(list);
      List<FrequencyBlockIndex> frequencyIndices = factory.createFrequencyIndices(list);

      if (!indices.isEmpty())
        _blockIndicesByBlockId.put(block.getId(), indices);
      if (!frequencyIndices.isEmpty())
        _frequencyBlockIndicesByBlockId.put(block.getId(), frequencyIndices);
    }

    long t2 = System.currentTimeMillis();
    _log.info("completed calculating block indices by blockId: t=" + (t2 - t1));

  }

  private void loadFrequencyBlockIndices() throws IOException,
      ClassNotFoundException {

    File path = _bundle.getFrequencyBlockIndicesPath();

    if (path.exists()) {

      _log.info("loading frequency block indices data");

      List<FrequencyBlockIndexData> datas = ObjectSerializationLibrary.readObject(path);

      _frequencyBlockIndices = new ArrayList<FrequencyBlockIndex>(datas.size());
      for (FrequencyBlockIndexData data : datas)
        _frequencyBlockIndices.add(data.createIndex(_graphDao));

      _frequencyBlockIndicesByAgencyId = getBlockIndicesByAgencyId(_frequencyBlockIndices);
      _frequencyBlockIndicesByRouteId = getBlocksByRouteId(_frequencyBlockIndices);

      _log.info("block frequency indices data loaded");

      clearExistingFrequencyStopTimeIndices();
      setupFrequencyBlockStopTimeIndices();

    } else {

      _frequencyBlockIndices = Collections.emptyList();
      _frequencyBlockIndicesByAgencyId = Collections.emptyMap();
      _frequencyBlockIndicesByRouteId = Collections.emptyMap();

      clearExistingFrequencyStopTimeIndices();
    }
  }

  private <T extends HasBlocks> Map<String, List<T>> getBlockIndicesByAgencyId(
      List<T> indices) {

    Map<String, List<T>> blocksByAgencyId = new FactoryMap<String, List<T>>(
        new ArrayList<T>());

    for (T blockIndex : indices) {
      Set<String> agencyIds = new HashSet<String>();
      for (BlockConfigurationEntry configuration : blockIndex.getBlocks()) {
        for (BlockTripEntry blockTrip : configuration.getTrips())
          agencyIds.add(blockTrip.getTrip().getId().getAgencyId());
      }
      for (String agencyId : agencyIds)
        blocksByAgencyId.get(agencyId).add(blockIndex);
    }
    return blocksByAgencyId;
  }

  private <T extends HasBlocks> Map<AgencyAndId, List<T>> getBlocksByRouteId(
      List<T> indices) {

    Map<AgencyAndId, List<T>> blocksByRouteId = new FactoryMap<AgencyAndId, List<T>>(
        new ArrayList<T>());

    for (T index : indices) {
      Set<AgencyAndId> routeIds = new HashSet<AgencyAndId>();
      for (BlockConfigurationEntry configuration : index.getBlocks()) {
        for (BlockTripEntry blockTrip : configuration.getTrips())
          routeIds.add(blockTrip.getTrip().getRouteCollectionId());
      }
      for (AgencyAndId routeId : routeIds)
        blocksByRouteId.get(routeId).add(index);
    }
    return blocksByRouteId;
  }

  private void setupBlockStopTimeIndices() {

    for (BlockIndex blockIndex : _blockIndices) {

      List<BlockConfigurationEntry> blocks = blockIndex.getBlocks();
      
      System.out.println("blocks=" + blocks.size());

      BlockConfigurationEntry firstBlock = blocks.get(0);
      BlockConfigurationEntry lastBlock = blocks.get(blocks.size() - 1);

      List<BlockStopTimeEntry> firstStopTimes = firstBlock.getStopTimes();
      List<BlockStopTimeEntry> lastStopTimes = lastBlock.getStopTimes();

      int n = firstStopTimes.size();

      for (int i = 0; i < n; i++) {

        BlockStopTimeEntry firstStopTime = firstStopTimes.get(i);
        BlockStopTimeEntry lastStopTime = lastStopTimes.get(i);

        ServiceInterval serviceInterval = getStopTimesAsServiceInterval(
            firstStopTime, lastStopTime);

        BlockStopTimeIndex blockStopTimeIndex = new BlockStopTimeIndex(
            blockIndex, i, serviceInterval);

        StopEntryImpl stop = (StopEntryImpl) firstStopTime.getStopTime().getStop();
        stop.addStopTimeIndex(blockStopTimeIndex);
      }
    }

  }

  public void clearExistingStopTimeIndices() {
    // Clear any existing indices
    for (StopEntry stop : _graphDao.getAllStops()) {
      StopEntryImpl stopImpl = (StopEntryImpl) stop;
      stopImpl.getStopTimeIndices().clear();
    }
  }

  private void setupFrequencyBlockStopTimeIndices() {

    for (FrequencyBlockIndex blockIndex : _frequencyBlockIndices) {

      List<BlockConfigurationEntry> blocks = blockIndex.getBlocks();

      BlockConfigurationEntry firstBlock = blocks.get(0);

      List<BlockStopTimeEntry> firstStopTimes = firstBlock.getStopTimes();

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

  private ServiceInterval getStopTimesAsServiceInterval(
      BlockStopTimeEntry firstStopTime, BlockStopTimeEntry lastStopTime) {

    StopTimeEntry st0 = firstStopTime.getStopTime();
    StopTimeEntry st1 = lastStopTime.getStopTime();

    return new ServiceInterval(st0.getArrivalTime(), st0.getDepartureTime(),
        st1.getArrivalTime(), st1.getDepartureTime());
  }
}
