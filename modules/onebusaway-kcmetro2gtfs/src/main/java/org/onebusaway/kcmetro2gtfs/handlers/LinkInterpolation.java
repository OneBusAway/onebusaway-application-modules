/*
 * Copyright 2008 Brian Ferris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.kcmetro2gtfs.handlers;

import org.onebusaway.kcmetro2gtfs.model.Indexed;
import org.onebusaway.kcmetro2gtfs.model.MetroKCStop;
import org.onebusaway.kcmetro2gtfs.model.MetroKCTransLink;
import org.onebusaway.kcmetro2gtfs.model.MetroKCTransNode;
import org.onebusaway.kcmetro2gtfs.model.ServicePatternKey;
import org.onebusaway.kcmetro2gtfs.model.StopTimepointInterpolation;
import org.onebusaway.kcmetro2gtfs.model.TimepointAndIndex;

import edu.washington.cs.rse.geospatial.DPoint;
import edu.washington.cs.rse.geospatial.Geometry;

/**
 * 
 */
class LinkInterpolation {

  private int _timepointFrom;

  private int _timepointTo;

  private int _fromTimepointIndex;

  private int _direction;

  /**
   * Distance from start of ServicePattern path to the beginning of the pattern
   * of which this link is a member
   */
  private double _patternOffset;

  /**
   * Length of the pattern to which this link is a member
   */
  private double _patternLength;

  /**
   * Distance from start of pattern to the start of this link
   */
  private double _linkOffset;

  private MetroKCTransLink _link;

  private MetroKCTransNode _nodeFrom;

  private MetroKCTransNode _nodeTo;

  public LinkInterpolation(MetroKCTransLink link, MetroKCTransNode nodeFrom,
      MetroKCTransNode nodeTo, int timepointFrom, int timepointTo,
      int fromTimepointIndex, int direction) {

    _link = link;
    _nodeFrom = nodeFrom;
    _nodeTo = nodeTo;

    _timepointFrom = timepointFrom;
    _timepointTo = timepointTo;
    _fromTimepointIndex = fromTimepointIndex;
    _direction = direction;
  }

  public void setPatternOffset(double patternOffset) {
    _patternOffset = patternOffset;
  }

  public void setPatternLength(double patternLength) {
    _patternLength = patternLength;
  }

  public void setLinkOffset(double linkOffset) {
    _linkOffset = linkOffset;
  }

  public double getScore(MetroKCStop stop) {
    return getTotalOffsetLength(stop);
  }

  public double getTotalOffsetLength(MetroKCStop stop) {
    return _patternOffset + _linkOffset + getStopOffset(stop);
  }

  public StopTimepointInterpolation interpolate(ServicePatternKey pattern,
      Indexed<MetroKCStop> indexedStop) {

    int index = indexedStop.getIndex();
    MetroKCStop stop = indexedStop.getValue();

    StopTimepointInterpolation sti = new StopTimepointInterpolation();
    sti.setServicePattern(pattern);
    sti.setTimepointFrom(new TimepointAndIndex(_timepointFrom,
        _fromTimepointIndex));
    sti.setTimepointTo(new TimepointAndIndex(_timepointTo,
        _fromTimepointIndex + 1));
    sti.setStop(stop.getId());
    sti.setStopIndex(index);

    double offset = getStopOffset(stop);
    double ratio = (_linkOffset + offset) / _patternLength;
    sti.setRatio(ratio);
    sti.setTotalDistanceTraveled(_patternOffset + _linkOffset
        + getStopOffset(stop));

    return sti;
  }

  @Override
  public String toString() {
    return "link=" + _link.getId() + " tp=[" + _timepointFrom + "("
        + _fromTimepointIndex + ")-" + _timepointTo + "("
        + (_fromTimepointIndex + 1) + ")] patternOffset=" + _patternOffset
        + " patternLength=" + _patternLength + " linkOffset=" + _linkOffset;
  }

  /*****************************************************************************
   * Private Methods
   ****************************************************************************/

  private double getStopOffset(MetroKCStop stop) {

    DPoint a = new DPoint(_nodeFrom.getX(), _nodeFrom.getY());
    DPoint b = new DPoint(_nodeTo.getX(), _nodeTo.getY());
    DPoint c = new DPoint(stop.getX(), stop.getY());
    DPoint p = Geometry.projectPointToSegment(c, a, b);

    double d = a.getDistance(b);
    double ratio = 0;

    if (d != 0)
      ratio = a.getDistance(p) / d;

    double offset = _link.getLinkLen() * ratio;

    if (_direction > 0)
      offset = _link.getLinkLen() - offset;

    return offset;
  }
}