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
package org.onebusaway.transit_data_federation.model;

import java.util.List;

import org.locationtech.jts.geom.Point;
import org.onebusaway.container.model.IdentityBean;
import org.onebusaway.transit_data_federation.services.StopSequenceCollectionService;

/**
 * A stop sequence collection contains a list of {@link StopSequence} sequences
 * that are headed in the same direction for a particular
 * {@link RouteCollectionEntry}, along with a general description of the
 * destinations for those stop sequences and general start and stop locations
 * for the sequences. Typically a stop sequence collection will be generated for
 * each direction of travel for a particular route.
 * 
 * @author bdferris
 * @see StopSequence
 * @see StopSequenceCollectionService
 */
public class StopSequenceCollection extends
    IdentityBean<StopSequenceCollectionKey> {

  private static final long serialVersionUID = 1L;

  private StopSequenceCollectionKey id;

  private String publicId;

  private String description;

  private List<StopSequence> stopSequences;

  private double startLat;

  private double startLon;

  private Point startLocation;

  private double endLat;

  private double endLon;

  private Point endLocation;

  public StopSequenceCollectionKey getId() {
    return id;
  }

  public void setId(StopSequenceCollectionKey id) {
    this.id = id;
  }

  public String getPublicId() {
    return publicId;
  }

  public void setPublicId(String publicId) {
    this.publicId = publicId;
  }

  /**
   * Typically, the most frequently mentioned trip headsign for all the trips
   * that correspond to the stop sequences in the block is used as the general
   * destination description.
   * 
   * @return a description of the general destination for the stop sequences in
   *         the block
   */
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * 
   * @return the list of all stop sequences in the block
   */
  public List<StopSequence> getStopSequences() {
    return stopSequences;
  }

  public void setStopSequences(List<StopSequence> stopSequences) {
    this.stopSequences = stopSequences;
  }

  public Point getStartLocation() {
    return startLocation;
  }

  public double getStartLat() {
    return startLat;
  }

  public void setStartLat(double startLat) {
    this.startLat = startLat;
  }

  public double getStartLon() {
    return startLon;
  }

  public void setStartLon(double startLon) {
    this.startLon = startLon;
  }

  public void setStartLocation(Point startLocation) {
    this.startLocation = startLocation;
  }

  public double getEndLat() {
    return endLat;
  }

  public void setEndLat(double endLat) {
    this.endLat = endLat;
  }

  public double getEndLon() {
    return endLon;
  }

  public void setEndLon(double endLon) {
    this.endLon = endLon;
  }

  public Point getEndLocation() {
    return endLocation;
  }

  public void setEndLocation(Point endLocation) {
    this.endLocation = endLocation;
  }

  @Override
  public String toString() {
    return "StopSequenceBlock(id=" + this.id + " desc=" + this.description
        + ")";
  }
}
