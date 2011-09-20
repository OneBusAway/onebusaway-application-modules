/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.transit_data_federation.services.realtime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import org.onebusaway.collections.Range;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VehicleLocationCacheElements {

  private static Logger _log = LoggerFactory.getLogger(VehicleLocationCacheElements.class);

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
    
    if (!_elements.isEmpty()) {
      VehicleLocationCacheElement lastElement = _elements.get(_elements.size() - 1);
      if (lastElement.getRecord().getTimeOfRecord() > element.getRecord().getTimeOfRecord()) {
        _log.warn("ignoring vehicle location record with decreasing timestamp: "
            + lastElement.getRecord() + " => " + element.getRecord());
        return this;
      }
    }

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
      throw new NoSuchElementException();
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

    for (int i = _elements.size() - 1; i > 0; i--) {
      VehicleLocationCacheElement element = _elements.get(i);
      VehicleLocationRecord record = element.getRecord();
      if (record.getTimeOfRecord() <= targetTime)
        return element;
    }

    return _elements.get(0);
  }
  
  public VehicleLocationCacheElement getLastElement() {
    if( _elements.isEmpty() )
      throw new NoSuchElementException();
    return _elements.get(_elements.size() - 1);
  }
}
