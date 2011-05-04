package org.onebusaway.transit_data_federation.services.realtime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.onebusaway.collections.Range;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;

public class VehicleLocationCacheElements {

  private final BlockInstance _blockInstance;

  private final List<VehicleLocationCacheElement> _elements;

  public VehicleLocationCacheElements(BlockInstance blockInstance) {
    _blockInstance = blockInstance;
    _elements = Collections.emptyList();
  }

  public VehicleLocationCacheElements(BlockInstance blockInstance,
      VehicleLocationCacheElement element) {
    _blockInstance = blockInstance;
    _elements = Arrays.asList(element);
  }

  private VehicleLocationCacheElements(BlockInstance blockInstance,
      List<VehicleLocationCacheElement> elements) {
    _blockInstance = blockInstance;
    _elements = elements;
  }

  public VehicleLocationCacheElements extend(VehicleLocationCacheElement element) {
    List<VehicleLocationCacheElement> elements = new ArrayList<VehicleLocationCacheElement>(
        _elements.size() + 1);
    elements.addAll(_elements);
    elements.add(element);
    return new VehicleLocationCacheElements(_blockInstance, elements);
  }

  public VehicleLocationCacheElements pruneOlderThanTime(long time) {

    /**
     * If we don't need to prune, don't do anything
     */
    if (isEmpty() || _elements.get(0).getMeasuredLastUpdateTime() >= time)
      return this;

    List<VehicleLocationCacheElement> elements = new ArrayList<VehicleLocationCacheElement>();
    for (VehicleLocationCacheElement element : _elements) {
      if (element.getMeasuredLastUpdateTime() >= time)
        elements.add(element);
    }

    return new VehicleLocationCacheElements(_blockInstance, elements);
  }

  public BlockInstance getBlockInstance() {
    return _blockInstance;
  }

  public boolean isEmpty() {
    return _elements.isEmpty();
  }

  public Range getTimeRange() {
    if (_elements.isEmpty())
      throw new IllegalStateException("empty");
    VehicleLocationCacheElement first = _elements.get(0);
    VehicleLocationCacheElement last = _elements.get(_elements.size() - 1);
    return new Range(first.getRecord().getTimeOfRecord(),
        last.getRecord().getTimeOfRecord());
  }
  
  public List<VehicleLocationCacheElement> getElements() {
    return Collections.unmodifiableList(_elements);
  }

  public VehicleLocationCacheElement getElementForTimestamp(long targetTime) {

    if (_elements.isEmpty())
      return null;

    for (int i = _elements.size() - 1; i > 0; i++) {
      VehicleLocationCacheElement element = _elements.get(i);
      VehicleLocationRecord record = element.getRecord();
      if (record.getTimeOfRecord() <= targetTime)
        return element;
    }

    return _elements.get(0);
  }
}
