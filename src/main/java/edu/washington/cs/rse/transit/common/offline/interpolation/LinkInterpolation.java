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
package edu.washington.cs.rse.transit.common.offline.interpolation;

import edu.washington.cs.rse.geospatial.Geometry;
import edu.washington.cs.rse.geospatial.IGeoPoint;
import edu.washington.cs.rse.transit.common.MetroKCDAO;
import edu.washington.cs.rse.transit.common.model.ServicePattern;
import edu.washington.cs.rse.transit.common.model.StopLocation;
import edu.washington.cs.rse.transit.common.model.Timepoint;
import edu.washington.cs.rse.transit.common.model.TransLink;
import edu.washington.cs.rse.transit.common.model.aggregate.StopTimepointInterpolation;
import edu.washington.cs.rse.transit.common.offline.Indexed;

/**
 * 
 */
class LinkInterpolation {

  private MetroKCDAO _dao;

  private Timepoint _from;

  private Timepoint _to;

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

  private TransLink _link;

  public LinkInterpolation(MetroKCDAO dao, TransLink link, Timepoint from,
      Timepoint to, int fromTimepointIndex, int direction) {
    _dao = dao;
    _link = link;
    _from = from;
    _to = to;
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

  public double getScore(StopLocation stop) {
    return getTotalOffsetLength(stop);
  }

  public double getTotalOffsetLength(StopLocation stop) {
    return _patternOffset + _linkOffset + getStopOffset(stop);
  }

  public StopTimepointInterpolation interpolate(ServicePattern pattern,
      Indexed<StopLocation> indexedStop) {

    int index = indexedStop.getIndex();
    StopLocation stop = indexedStop.getValue();
    
    StopTimepointInterpolation sti = new StopTimepointInterpolation();
    sti.setServicePattern(pattern);
    sti.setFromTimepoint(_from);
    sti.setFromTimepointSequence(_fromTimepointIndex);
    sti.setToTimepoint(_to);
    sti.setToTimepointSequence(_fromTimepointIndex + 1);
    sti.setStop(stop);
    sti.setStopIndex(index);

    double offset = getStopOffset(stop);
    double ratio = (_linkOffset + offset) / _patternLength;
    sti.setRatio(ratio);
    return sti;
  }

  @Override
  public String toString() {
    return "link=" + _link.getId() + " tp=[" + _from.getId() + "(" + _fromTimepointIndex + ")-" + _to.getId()
        + "(" + (_fromTimepointIndex + 1) + ")] patternOffset=" + _patternOffset + " patternLength=" + _patternLength + " linkOffset=" + _linkOffset;
  }

  /*****************************************************************************
   * Private Methods
   ****************************************************************************/

  private double getStopOffset(StopLocation stop) {

    double ratio = stop.getMappedPercentFrom() / 100;

    if (true) {
      IGeoPoint a = _dao.getPointAsGeoPoint(_link.getTransNodeFrom().getLocation());
      IGeoPoint b = _dao.getPointAsGeoPoint(_link.getTransNodeTo().getLocation());
      IGeoPoint c = _dao.getPointAsGeoPoint(stop.getLocation());
      IGeoPoint p = Geometry.projectPointToSegment(c, a, b);

      double d = a.getDistance(b);

      if (d != 0)
        ratio = a.getDistance(p) / d;
    }

    double offset = _link.getLinkLen() * ratio;

    if (_direction > 0)
      offset = _link.getLinkLen() - offset;

    return offset;

  }
}