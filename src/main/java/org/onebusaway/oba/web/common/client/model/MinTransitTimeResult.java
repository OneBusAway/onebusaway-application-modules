package org.onebusaway.oba.web.common.client.model;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MinTransitTimeResult implements Serializable, IsSerializable {

  private static final long serialVersionUID = 1L;

  private String _resultId;

  private List<LocationBounds> _timeGrid = new ArrayList<LocationBounds>();

  private List<Integer> _times = new ArrayList<Integer>();

  private List<LocationBounds> _searchGrid = new ArrayList<LocationBounds>();

  public String getResultId() {
    return _resultId;
  }

  public void setResultId(String resultId) {
    _resultId = resultId;
  }

  public List<LocationBounds> getTimeGrid() {
    return _timeGrid;
  }

  public void setTimeGrid(List<LocationBounds> timeGrid) {
    _timeGrid = timeGrid;
  }

  public List<Integer> getTimes() {
    return _times;
  }

  public void setTimes(List<Integer> times) {
    _times = times;
  }

  public List<LocationBounds> getSearchGrid() {
    return _searchGrid;
  }

  public void setSearchGrid(List<LocationBounds> searchGrid) {
    _searchGrid = searchGrid;
  }
}
