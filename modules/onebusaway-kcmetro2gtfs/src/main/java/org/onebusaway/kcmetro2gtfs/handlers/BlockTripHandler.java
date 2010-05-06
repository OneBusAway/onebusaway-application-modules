/**
 * 
 */
package org.onebusaway.kcmetro2gtfs.handlers;

import org.onebusaway.kcmetro2gtfs.TranslationContext;
import org.onebusaway.kcmetro2gtfs.model.MetroKCBlockTrip;
import org.onebusaway.kcmetro2gtfs.model.MetroKCTrip;
import org.onebusaway.kcmetro2gtfs.model.ServicePatternKey;

import edu.washington.cs.rse.collections.FactoryMap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.emory.mathcs.backport.java.util.Collections;

public class BlockTripHandler extends InputHandler {

  private static BlockTripComparator _blockTripComparator = new BlockTripComparator();

  private static String[] BLOCK_TRIPS_FIELDS = {
      "change_date", "block_id", "trip_id", "trip_sequence", "dbModDate",
      "tripEndTime", "tripStartTime"};

  private Map<ServicePatternKey, MetroKCBlockTrip> _blocksByChangeDateAndTripId = new HashMap<ServicePatternKey, MetroKCBlockTrip>();

  private Map<Integer, List<MetroKCBlockTrip>> _blockIdToBlocks = new FactoryMap<Integer, List<MetroKCBlockTrip>>(
      new ArrayList<MetroKCBlockTrip>());

  private TranslationContext _context;

  public BlockTripHandler(TranslationContext context) {
    super(MetroKCBlockTrip.class, BLOCK_TRIPS_FIELDS);
    _context = context;
  }

  public void handleEntity(Object bean) {

    MetroKCBlockTrip block = (MetroKCBlockTrip) bean;
    ServicePatternKey fullTripId = new ServicePatternKey(block.getChangeDate(),
        block.getTripId());

    MetroKCBlockTrip existingBlock = _blocksByChangeDateAndTripId.get(fullTripId);

    if (existingBlock != null) {
      if (!(block.getBlockId() == existingBlock.getBlockId() && block.getTripSequence() == existingBlock.getTripSequence())) {
        throw new IllegalStateException(
            "trip appears in multiple blocks: changeDate="
                + block.getChangeDate() + " trip=" + block.getTripId()
                + " block=" + block.getBlockId());
      }
      return;
    }

    _blocksByChangeDateAndTripId.put(fullTripId, block);
    _blockIdToBlocks.get(block.getBlockId()).add(block);
  }

  public MetroKCBlockTrip getBlockForTrip(ServicePatternKey tripId) {
    MetroKCBlockTrip block = _blocksByChangeDateAndTripId.get(tripId);
    if (block == null)
      throw new IllegalStateException("no block for trip: " + tripId);
    return block;
  }

  public List<MetroKCBlockTrip> getTripBlocksForTrips(List<MetroKCTrip> trips) {
    List<MetroKCBlockTrip> blocks = new ArrayList<MetroKCBlockTrip>();
    for (MetroKCTrip trip : trips)
      blocks.add(_blocksByChangeDateAndTripId.get(trip.getId()));
    return blocks;
  }

  public Map<Integer, List<MetroKCBlockTrip>> getBlockTripsByBlockIds(
      Set<Integer> ids) {
    Map<Integer, List<MetroKCBlockTrip>> blocks = new HashMap<Integer, List<MetroKCBlockTrip>>();
    for (Integer blockId : ids)
      blocks.put(blockId, _blockIdToBlocks.get(blockId));
    return blocks;
  }

  public Map<Integer, List<MetroKCBlockTrip>> getAllBlockTripsByBlockId() {
    return _blockIdToBlocks;
  }

  @Override
  public void close() {

    super.close();

    int hits = 0;

    for (Map.Entry<Integer, List<MetroKCBlockTrip>> entry : _blockIdToBlocks.entrySet()) {
      List<MetroKCBlockTrip> trips = entry.getValue();
      Collections.sort(trips, _blockTripComparator);

      for (int i = 0; i < trips.size(); i++) {
        MetroKCBlockTrip trip = trips.get(i);
        if (trip.getTripSequence() != i) {
          hits++;
          trip.setTripSequence(i);
        }
      }
    }

    _context.addWarning("out of order block trip sequene values=" + hits);
  }

  private static class BlockTripComparator implements
      Comparator<MetroKCBlockTrip> {

    public int compare(MetroKCBlockTrip o1, MetroKCBlockTrip o2) {
      return o1.getTripSequence() - o2.getTripSequence();
    }
  }
}