/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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
package org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime;

import org.onebusaway.transit_data_federation.impl.transit_graph.StopTimeEntriesFactory;
import org.onebusaway.transit_data_federation.services.AgencyService;
import org.onebusaway.transit_data_federation.services.ExtendedCalendarService;
import org.onebusaway.transit_data_federation.services.StopSwapService;
import org.onebusaway.transit_data_federation.services.blocks.BlockCalendarService;
import org.onebusaway.transit_data_federation.services.blocks.BlockGeospatialService;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndexService;
import org.onebusaway.transit_data_federation.services.blocks.DynamicBlockIndexService;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocationService;
import org.onebusaway.transit_data_federation.services.shapes.ShapePointService;

/**
 * Service equivalent of entity source; arrange real-time dependencies
 * of services into a convenient model.
 */
public class GtfsRealtimeServiceSource {


  private BlockIndexService _blockIndexService;

  private ExtendedCalendarService _calendarService;

  private BlockCalendarService _blockCalendarService;

  private BlockGeospatialService _blockGeospatialService;

  private AddedTripService _addedTripService = new AddedTripServiceImpl();

  private DuplicatedTripService _duplicatedTripService;

  private DynamicTripBuilder _dynamicTripBuilder;

  private AgencyService _agencyService;

  private BlockLocationService _blockLocationService;

  private DynamicBlockIndexService _dynamicBlockIndexService;

  private NarrativeService _narrativeService;

  private StopTimeEntriesFactory _stopTimeEntriesFactory;

  private ShapePointService _shapePointService;

  private StopSwapService _stopSwapServce;

  private BlockFinder _blockFinder;

  public BlockIndexService getBlockIndexService() {
    return _blockIndexService;
  }

  public void setBlockIndexService(BlockIndexService blockIndexService) {
    this._blockIndexService = blockIndexService;
  }

  public void setBlockCalendarService(BlockCalendarService blockCalendarService) {
    _blockCalendarService = blockCalendarService;
    _blockFinder = new BlockFinder(this);
  }
  public void setBlockGeospatialService(BlockGeospatialService blockGeospatialService) {
    _blockGeospatialService = blockGeospatialService;
  }

  public void setAddedTripService(AddedTripService addedTripService) {
    _addedTripService = addedTripService;
  }

  public void setDuplicatedTripService(DuplicatedTripService duplicatedTripService) {
    _duplicatedTripService = duplicatedTripService;
  }

  public void setDynamicTripBuilder(DynamicTripBuilder builder) {
    _dynamicTripBuilder = builder;
  }

  public BlockCalendarService getBlockCalendarService() {
    return _blockCalendarService;
  }

  public BlockGeospatialService getBlockGeospatialService() {
    return _blockGeospatialService;
  }

  public AddedTripService getAddedTripService() {
    return _addedTripService;
  }

  public DuplicatedTripService getDuplicatedTripService() {
    return _duplicatedTripService;
  }

  public DynamicTripBuilder getDynamicTripBuilder() {
    return _dynamicTripBuilder;
  }


  public AgencyService getAgencyService() {
    return _agencyService;
  }

  public void setAgencyService(AgencyService _agencyService) {
    this._agencyService = _agencyService;
  }

  public BlockLocationService getBlockLocationService() {
    return _blockLocationService;
  }

  public void setBlockLocationService(BlockLocationService _blockLocationService) {
    this._blockLocationService = _blockLocationService;
  }

  public DynamicBlockIndexService getDynamicBlockIndexService() {
    return _dynamicBlockIndexService;
  }

  public void setDynamicBlockIndexService(DynamicBlockIndexService _dynamicBlockIndexService) {
    this._dynamicBlockIndexService = _dynamicBlockIndexService;
  }

  public NarrativeService getNarrativeService() {
    return _narrativeService;
  }

  public void setNarrativeService(NarrativeService _narrativeService) {
    this._narrativeService = _narrativeService;
  }

  public StopTimeEntriesFactory getStopTimeEntriesFactory() {
    return _stopTimeEntriesFactory;
  }

  public void setStopTimeEntriesFactory(StopTimeEntriesFactory stopTimeEntriesFactory) {
    this._stopTimeEntriesFactory = stopTimeEntriesFactory;
  }

  public ShapePointService getShapePointService() {
    return _shapePointService;
  }

  public void setShapePointService(ShapePointService shapePointService) {
    this._shapePointService = shapePointService;
  }

  public StopSwapService getStopSwapService() {
    return _stopSwapServce;
  }

  public void setStopSwapServce(StopSwapService service) {
    _stopSwapServce = service;
  }

  public BlockFinder getBlockFinder() {
    return _blockFinder;
  }

  public void setCalendarService(ExtendedCalendarService service) {
    this._calendarService = service;
  }

  public ExtendedCalendarService getCalendarService() {
    return _calendarService;
  }

}
